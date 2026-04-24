package security;

import exception.AuthException;
import exception.KaizenException.ErrorCode;
import model.User;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class SecurityManager {

    private static final Logger LOG = Logger.getLogger(SecurityManager.class.getName());

    // Cryptography Constants
    private static final String HASH_ALGORITHM          = "PBKDF2WithHmacSHA256";
    private static final int    CURRENT_HASH_ITERATIONS = 310000; // OWASP Recommended
    private static final int    HASH_KEY_LENGTH          = 256;
    private static final int    SALT_LENGTH              = 16;

    // Failed login tracking: email -> fail count
    private static final int MAX_FAILED_LOGINS = 5;
    private final Map<String, Integer> failedLogins = new ConcurrentHashMap<>();

    private final Map<String, SessionContext> activeSessions = new ConcurrentHashMap<>();
    private final RateLimiter rateLimiter = new RateLimiter();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RequiresRole {
        User.Role value();
    }

    private static class SessionContext {
        final User user;
        final long createdAt;

        SessionContext(User user) {
            this.user      = user;
            this.createdAt = System.currentTimeMillis();
        }
    }

    /** Kept for API compatibility; use AuthException in service layer. */
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) { super(message); }
    }

    //Rate limiter access
    public RateLimiter getRateLimiter() { return rateLimiter; }

    //Failed login tracking
    public boolean isAccountLocked(String email) {
        return failedLogins.getOrDefault(email, 0) >= MAX_FAILED_LOGINS;
    }

    public void recordFailedLogin(String email) throws AuthException {
        int count = failedLogins.merge(email, 1, Integer::sum);
        if (count >= MAX_FAILED_LOGINS) {
            LOG.warning("[SECURITY] Account locked after " + count + " failures: " + email);
            throw new AuthException(ErrorCode.AUTH_ACCOUNT_LOCKED,
                    "Account locked after too many failed attempts: " + email,
                    "Too many failed login attempts. Please wait 30 minutes.");
        }
    }

    public void clearFailedLogins(String email) {
        failedLogins.remove(email);
    }

    //Session management
    public String createSession(User userFromDatabase) {
        if (userFromDatabase == null) throw new IllegalArgumentException("User cannot be null");

        String token = UUID.randomUUID().toString();
        activeSessions.put(token, new SessionContext(userFromDatabase));

        LOG.info(() -> String.format("Session created | User: %s | Role: %s | Token: %s...",
                userFromDatabase.getEmail(), userFromDatabase.getRole(), token.substring(0, 8)));
        return token;
    }

    /**
     * Returns the user for the given token, or throws AuthException if the session is invalid.
     */
    public User getUserFromToken(String token) throws AuthException {
        if (token == null || token.isBlank()) {
            throw new AuthException(ErrorCode.AUTH_SESSION_NOT_FOUND,
                    "Null or blank token provided",
                    "You are not logged in. Please log in to continue.");
        }
        SessionContext context = activeSessions.get(token);
        if (context == null) {
            throw new AuthException(ErrorCode.AUTH_SESSION_NOT_FOUND,
                    "Session not found for token: " + token.substring(0, Math.min(8, token.length())),
                    "Your session has expired. Please log in again.");
        }
        return context.user;
    }

    public boolean hasRole(String token, User.Role requiredRole) {
        try {
            User user = getUserFromToken(token);
            return user.getRole() == requiredRole;
        } catch (AuthException e) {
            return false;
        }
    }

    public void destroySession(String token) {
        if (token != null) {
            activeSessions.remove(token);
            LOG.info(() -> "Session destroyed: " + token.substring(0, Math.min(8, token.length())) + "...");
        }
    }

    public void checkAccess(String token, Class<?> clazz, String methodName, Class<?>... parameterTypes)
            throws AuthException {

        User user = getUserFromToken(token); // throws AuthException if invalid

        // Try to find the @RequiresRole annotation on the method
        RequiresRole annotation = findRequiresRole(clazz, methodName, parameterTypes);
        if (annotation == null) return; // No role restriction

        User.Role required = annotation.value();
        User.Role actual   = user.getRole();

        if (actual != required) {
            LOG.warning(() -> String.format(
                    "ACCESS DENIED | User: %s | Actual: %s | Required: %s | Method: %s",
                    user.getEmail(), actual, required, methodName));
            throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                    String.format("Access denied. User: %s Actual: %s Required: %s Method: %s",
                            user.getEmail(), actual, required, methodName),
                    String.format("Access denied. This action requires role %s, but you are a %s.",
                            required, actual));
        }

        LOG.finest(() -> "Access granted — " + user.getEmail() + " → " + methodName);
    }

    private RequiresRole findRequiresRole(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        // Try exact signature first
        if (parameterTypes.length > 0) {
            try {
                Method method = clazz.getMethod(methodName, parameterTypes);
                RequiresRole a = method.getAnnotation(RequiresRole.class);
                if (a != null) return a;
            } catch (NoSuchMethodException ignored) {}
        }
        // Scan all public methods by name
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                RequiresRole a = m.getAnnotation(RequiresRole.class);
                if (a != null) return a;
            }
        }
        return null;
    }

    //Password hashing
    public static String hashPassword(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = generatePbkdf2(plainPassword.toCharArray(), salt,
                    CURRENT_HASH_ITERATIONS, HASH_KEY_LENGTH);

            Base64.Encoder encoder = Base64.getEncoder();
            return CURRENT_HASH_ITERATIONS + ":"
                    + encoder.encodeToString(salt) + ":"
                    + encoder.encodeToString(hash);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Password hashing failed", e);
            throw new RuntimeException("Internal security error during hashing.");
        }
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            int    iterations;
            byte[] salt;
            byte[] storedHashBytes;

            if (parts.length == 3) {
                iterations      = Integer.parseInt(parts[0]);
                salt            = Base64.getDecoder().decode(parts[1]);
                storedHashBytes = Base64.getDecoder().decode(parts[2]);
            } else if (parts.length == 2) {
                iterations      = 65536;
                salt            = Base64.getDecoder().decode(parts[0]);
                storedHashBytes = Base64.getDecoder().decode(parts[1]);
            } else {
                return false;
            }

            byte[] computedHashBytes = generatePbkdf2(plainPassword.toCharArray(), salt,
                    iterations, HASH_KEY_LENGTH);
            return MessageDigest.isEqual(storedHashBytes, computedHashBytes);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Password verification exception triggered", e);
            return false;
        }
    }

    private static byte[] generatePbkdf2(char[] password, byte[] salt,
                                          int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
}
