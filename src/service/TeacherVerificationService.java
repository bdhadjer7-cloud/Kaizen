package service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TeacherVerificationService {

    private static final Logger LOG =
            Logger.getLogger(TeacherVerificationService.class.getName());

    private static final int  CODE_DIGITS    = 6;
    private static final long EXPIRY_MINUTES = 15;
    private static final int  MAX_ATTEMPTS   = 3;


    public static class VerificationEntry {

        public final String        code;
        public final String        email;
        public final String        name;
        public final LocalDateTime createdAt;
        public final LocalDateTime expiresAt;

        public final AtomicInteger attempts;
        public final AtomicBoolean used;

        public VerificationEntry(String code, String email, String name) {
            this.code      = code;
            this.email     = email;
            this.name      = name;
            this.createdAt = LocalDateTime.now();
            this.expiresAt = this.createdAt.plusMinutes(EXPIRY_MINUTES);
            this.attempts  = new AtomicInteger(0);
            this.used      = new AtomicBoolean(false);
        }

        public boolean isExpired()  { return LocalDateTime.now().isAfter(expiresAt); }
        public boolean isLocked()   { return attempts.get() >= MAX_ATTEMPTS; }
        public boolean isValid()    { return !used.get() && !isExpired() && !isLocked(); }

        @Override
        public String toString() {
            return "VerificationEntry{"
                    + "email=" + email
                    + ", expires=" + expiresAt
                    + ", attempts=" + attempts.get() + "/" + MAX_ATTEMPTS
                    + ", valid=" + isValid() + "}";
        }
    }


    public enum VerificationStatus {
        SUCCESS,
        WRONG_CODE,
        EXPIRED,
        LOCKED,
        NOT_FOUND,
        ALREADY_USED,
        TEMP_LOCKOUT // Added new status for requesting a new code while locked
    }

    public static class VerificationResult {
        public final VerificationStatus status;
        public final String             message;

        public VerificationResult(VerificationStatus status, String message) {
            this.status  = status;
            this.message = message;
        }

        public boolean isSuccess() { return status == VerificationStatus.SUCCESS; }

        @Override
        public String toString() { return "[" + status + "] " + message; }
    }


    private final Map<String, VerificationEntry> pendingVerifications = new ConcurrentHashMap<>();

    //depnd
    private final EmailService emailService;
    private final ScheduledExecutorService cleanupExecutor;

    public TeacherVerificationService(EmailService emailService) {
        this.emailService = emailService;

        // Background thread to clean up memory leaks
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cleanupExecutor.scheduleAtFixedRate(
                this::cleanExpiredEntries,
                EXPIRY_MINUTES,
                EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    //Initiate verification
    public VerificationResult initiateVerification(String email, String name) {

        // Prevent lock bypass: If they are currently locked out, don't let them generate a new code yet
        VerificationEntry existing = pendingVerifications.get(email);
        if (existing != null && existing.isLocked() && !existing.isExpired()) {
            return new VerificationResult(VerificationStatus.TEMP_LOCKOUT,
                    "Account is temporarily locked. Please wait for the lockout period to expire.");
        }

        String code = generateCode();
        VerificationEntry entry = new VerificationEntry(code, email, name);
        pendingVerifications.put(email, entry);

        LOG.log(Level.INFO, "[VERIFY] Code generated for: {0} | Expires at: {1}",
                new Object[]{email, entry.expiresAt});

        try {
            emailService.sendTeacherVerification(email, name, code);
            LOG.log(Level.INFO, "[VERIFY] Email sent successfully to: {0}", email);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "[VERIFY] SMTP unavailable (demo mode). Code for {0} : {1}",
                    new Object[]{email, code});
        }

        return new VerificationResult(VerificationStatus.SUCCESS, "Verification initiated.");
    }

    //STEP 2 : Verify the code
    public VerificationResult verifyCode(String email, String enteredCode) {

        VerificationEntry entry = pendingVerifications.get(email);

        if (entry == null) {
            return new VerificationResult(VerificationStatus.NOT_FOUND,
                    "No pending verification found. Only registered teacher candidates can verify.");
        }

        if (entry.used.get()) {
            return new VerificationResult(VerificationStatus.ALREADY_USED,
                    "This code has already been used. Please log in.");
        }

        if (entry.isExpired()) {
            pendingVerifications.remove(email);
            return new VerificationResult(VerificationStatus.EXPIRED,
                    "Code expired. Please request a new verification email.");
        }

        if (entry.isLocked()) {
            // Intentionally not removing the entry! We keep it in memory so the lockout persists
            // until the expiry time hits.
            return new VerificationResult(VerificationStatus.LOCKED,
                    "Account locked due to too many failed attempts. Please wait "
                            + EXPIRY_MINUTES + " minutes before requesting a new code.");
        }

        // Constant-time comparison
        if (!codesMatch(enteredCode, entry.code)) {

            // Thread-safe increment
            int currentAttempts = entry.attempts.incrementAndGet();
            int remaining = MAX_ATTEMPTS - currentAttempts;

            LOG.log(Level.WARNING, "[VERIFY] Wrong code for: {0} | Attempts: {1}/{2}",
                    new Object[]{email, currentAttempts, MAX_ATTEMPTS});

            if (remaining <= 0) {
                return new VerificationResult(VerificationStatus.LOCKED,
                        "Too many wrong attempts. Account locked for " + EXPIRY_MINUTES + " minutes.");
            }

            return new VerificationResult(VerificationStatus.WRONG_CODE,
                    "Wrong code. " + remaining + " attempt(s) remaining.");
        }

        //Atomically mark as used
        if (entry.used.compareAndSet(false, true)) {
            pendingVerifications.remove(email);
            LOG.log(Level.INFO, "[VERIFY] SUCCESS — {0} verified as TEACHER.", email);

            return new VerificationResult(VerificationStatus.SUCCESS,
                    "Email verified! Your Teacher account is now active, " + entry.name + ".");
        }

        return new VerificationResult(VerificationStatus.ALREADY_USED, "Code was used concurrently.");
    }

    //Resend code
    public VerificationResult resendCode(String email, String name) {
        LOG.log(Level.INFO, "[VERIFY] Resending code to: {0}", email);
        return initiateVerification(email, name);
    }

    //Clean up memory leaks
    /**
     * Prevents the ConcurrentHashMap from growing indefinitely if users request
     * codes but never verify them.
     */
    private void cleanExpiredEntries() {
        int before = pendingVerifications.size();
        pendingVerifications.entrySet().removeIf(e -> e.getValue().isExpired());
        int removed = before - pendingVerifications.size();

        if (removed > 0) {
            LOG.log(Level.INFO, "[VERIFY] Memory Cleanup: Removed {0} expired verification attempts.", removed);
        }
    }

    //Shutdown Hook
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
        }
    }

    //Secure code generation
    private String generateCode() {
        int code = new SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    //Constant-time comparison
    private boolean codesMatch(String entered, String actual) {
        if (entered == null || actual == null) return false;
        if (entered.length() != actual.length()) return false;
        int result = 0;
        for (int i = 0; i < entered.length(); i++) {
            result |= entered.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
