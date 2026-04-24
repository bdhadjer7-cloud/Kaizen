package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.User;
import repository.UserRepository;
import security.InputSanitizer;
import security.RateLimiter;
import security.SecurityManager;
import util.Validator;

import java.util.logging.Logger;

/**
 * Kaizen — AuthService.java
 * Handles login, registration, logout, password reset.
 * Delegates security to SecurityManager.
 * Delegates teacher verification to TeacherVerificationService.
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final UserRepository              userRepo;
    private final SecurityManager             security;
    private final TeacherVerificationService  verifier;
    private final RateLimiter                 rateLimiter;

    public AuthService(UserRepository userRepo, SecurityManager security,
                       TeacherVerificationService verifier) {
        this.userRepo    = userRepo;
        this.security    = security;
        this.verifier    = verifier;
        this.rateLimiter = security.getRateLimiter();
    }

    // ── Login ──────────────────────────────────────────────────────────────────
    /**
     * Authenticates a user.
     * Role is ALWAYS loaded from DB — never from the login form.
     *
     * @return session token (UUID)
     */
    public String login(String email, String password)
            throws KaizenException {

        // 1. Rate limit — max 5 login attempts per 5 min
        try { rateLimiter.check(email, "LOGIN"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }

        // 2. Validate inputs
        Validator.validEmail(email);
        ValidationException ve = new ValidationException();
        Validator.notBlank(password, "password", ve);
        if (ve.hasErrors()) throw ve;

        // 3. Sanitize inputs
        try {
            InputSanitizer.sanitizeSQL(email);
            InputSanitizer.sanitizeSQL(password);
        } catch (SecurityViolationException e) { throw e; }

        // 4. Check account not locked
        if (security.isAccountLocked(email)) {
            throw new AuthException(ErrorCode.AUTH_ACCOUNT_LOCKED,
                    "Account locked: " + email,
                    "Account locked after too many failed attempts. Wait 30 minutes.");
        }

        // 5. Find user by email (role comes FROM DB)
        User user;
        try {
            user = userRepo.findByEmail(email);
        } catch (DatabaseException e) { throw e; }

        if (user == null) {
            try { security.recordFailedLogin(email); } catch (AuthException ignored) {}
            throw new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS,
                    "No user found with email: " + email,
                    "Invalid email or password.");
        }

        // 6. Verify password
        if (!SecurityManager.verifyPassword(password, user.getPasswordHash())) {
            try { security.recordFailedLogin(email); } catch (AuthException ae) { throw ae; }
            throw new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS,
                    "Wrong password for: " + email,
                    "Invalid email or password.");
        }

        // 7. Teacher verification check
        Boolean emailVerified = user.getMeta("emailVerified", Boolean.class);
        if (user.isTeacher() && !Boolean.TRUE.equals(emailVerified)) {
            throw new AuthException(ErrorCode.AUTH_TEACHER_NOT_VERIFIED,
                    "Teacher not email-verified: " + email,
                    "Please verify your teacher account via the email we sent you.");
        }

        // 8. Create session
        rateLimiter.reset(email, "LOGIN");
        security.clearFailedLogins(email);
        String token = security.createSession(user);
        LOG.info("[AUTH] Login success: " + email + " role=" + user.getRole());
        return token;
    }

    // ── Register ───────────────────────────────────────────────────────────────
    public void register(String name, String email, String password, User.Role role)
            throws KaizenException {

        // Rate limit
        try { rateLimiter.check(email, "REGISTER"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }

        // Validate
        Validator.validateRegistration(name, email, password);

        // Sanitize
        try {
            name  = InputSanitizer.sanitizeAll(name);
            email = InputSanitizer.sanitizeSQL(email);
        } catch (SecurityViolationException e) { throw e; }

        // Check duplicate
        try {
            if (userRepo.existsByEmail(email))
                throw new DatabaseException(ErrorCode.DB_DUPLICATE_ENTRY,
                        "Email already registered: " + email,
                        "An account with this email already exists.");
        } catch (DatabaseException e) { throw e; }

        // Hash password and save
        String hash    = SecurityManager.hashPassword(password);
        User   newUser = new User(name, email, hash, role);

        try { userRepo.save(newUser); }
        catch (DatabaseException e) { throw e; }

        LOG.info("[AUTH] Registered: " + email + " role=" + role);

        // If teacher, initiate email verification
        if (role == User.Role.TEACHER) {
            verifier.initiateVerification(email, name);
        }
    }

    // ── Verify teacher ─────────────────────────────────────────────────────────
    public void verifyTeacher(String email, String code)
            throws KaizenException {

        try { rateLimiter.check(email, "VERIFY_CODE"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }

        TeacherVerificationService.VerificationResult result =
                verifier.verifyCode(email, code);

        if (!result.isSuccess()) {
            throw new AuthException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED,
                    "Verification failed for: " + email + " — " + result.status,
                    result.message);
        }

        // Mark verified in DB
        try {
            User user = userRepo.findByEmail(email);
            if (user != null) {
                user.setMeta("emailVerified", true);
                userRepo.update(user);
            }
        } catch (DatabaseException e) { throw e; }

        LOG.info("[AUTH] Teacher verified: " + email);
    }

    // ── Logout ─────────────────────────────────────────────────────────────────
    public void logout(String token) {
        security.destroySession(token);
        LOG.info("[AUTH] Logout: " + (token != null ? token.substring(0, 8) : "null") + "...");
    }

    // ── Get current user ───────────────────────────────────────────────────────
    public User getCurrentUser(String token) throws AuthException {
        return security.getUserFromToken(token);
    }

    // ── Password reset ─────────────────────────────────────────────────────────
    public void requestPasswordReset(String email) throws KaizenException {
        try { rateLimiter.check(email, "RESET_PASSWORD"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }
        Validator.validEmail(email);
        User user;
        try { user = userRepo.findByEmail(email); }
        catch (DatabaseException e) { throw e; }
        if (user == null) return; // silent — don't reveal if email exists
        verifier.initiateVerification(email, user.getName());
    }

    public void confirmPasswordReset(String email, String code, String newPassword)
            throws KaizenException {
        Validator.strongPassword(newPassword);
        TeacherVerificationService.VerificationResult r = verifier.verifyCode(email, code);
        if (!r.isSuccess())
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID, r.message, r.message);
        try {
            User user = userRepo.findByEmail(email);
            if (user == null)
                throw new AuthException(ErrorCode.BUSINESS_USER_NOT_FOUND,
                        "User not found: " + email, "Account not found.");
            user.setPasswordHash(SecurityManager.hashPassword(newPassword));
            userRepo.update(user);
        } catch (DatabaseException e) { throw e; }
        LOG.info("[AUTH] Password reset for: " + email);
    }
}
