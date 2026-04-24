package security;

import exception.SecurityViolationException;
import exception.KaizenException.ErrorCode;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Kaizen — RateLimiter.java
 * Prevents:
 *  - Brute force login attacks
 *  - Spam post/message flooding
 *  - API abuse
 * Uses sliding window algorithm:
 *  Map<identifier, List<timestamps>> 
 *  Count requests in the last N seconds
 */
public class RateLimiter {

    private static final Logger LOG = Logger.getLogger(RateLimiter.class.getName());

    // ConcurrentHashMap — thread-safe for multi-user environment
    // Map<identifier, List<request timestamps>>
    private final Map<String, List<Instant>> requestLog = new ConcurrentHashMap<>();

    // Rate limit rules — Map<action, RateRule>
    private final Map<String, RateRule> rules = new LinkedHashMap<>();

    public static class RateRule {
        final int    maxRequests;
        final int    windowSeconds;
        final String description;

        public RateRule(int maxRequests, int windowSeconds, String description) {
            this.maxRequests   = maxRequests;
            this.windowSeconds = windowSeconds;
            this.description   = description;
        }
        @Override public String toString() {
            return maxRequests + " requests per " + windowSeconds + "s (" + description + ")";
        }
    }

    public RateLimiter() {
        // Define rules for each action
        rules.put("LOGIN",           new RateRule(5,   300,  "5 login attempts per 5 min"));
        rules.put("REGISTER",        new RateRule(3,   3600, "3 registrations per hour"));
        rules.put("POST",            new RateRule(10,  60,   "10 posts per minute"));
        rules.put("MESSAGE",         new RateRule(30,  60,   "30 messages per minute"));
        rules.put("VERIFY_CODE",     new RateRule(3,   900,  "3 code attempts per 15 min"));
        rules.put("RESET_PASSWORD",  new RateRule(3,   3600, "3 resets per hour"));
        rules.put("FILE_UPLOAD",     new RateRule(5,   60,   "5 uploads per minute"));
        rules.put("SEARCH",          new RateRule(20,  60,   "20 searches per minute"));
    }

    //Check rate limit
    /*
     * @param identifier  e.g. email, IP, userId
     * @param action      e.g. "LOGIN", "POST"
     */
    public void check(String identifier, String action) throws SecurityViolationException {
        RateRule rule = rules.get(action.toUpperCase());
        if (rule == null) return; // no rule = no limit

        String key = action + ":" + identifier;
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(rule.windowSeconds);

        // Get or create timestamp list
        List<Instant> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayList<>());

        // Remove timestamps outside the window (sliding window)
        timestamps.removeIf(t -> t.isBefore(windowStart));

        if (timestamps.size() >= rule.maxRequests) {
            long secondsUntilReset = Duration.between(
                windowStart, timestamps.get(0).plusSeconds(rule.windowSeconds)
            ).getSeconds();

            LOG.warning("[RATE_LIMIT] Exceeded for: " + identifier
                      + " action=" + action
                      + " count=" + timestamps.size() + "/" + rule.maxRequests);

            throw new SecurityViolationException(
                ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                "Rate limit exceeded: " + identifier + " action=" + action,
                "Too many " + action.toLowerCase() + " attempts. "
                + "Please wait " + secondsUntilReset + " seconds."
            );
        }

        timestamps.add(now);
    }

    // Reset (on successful auth)
    public void reset(String identifier, String action) {
        requestLog.remove(action + ":" + identifier);
    }

    // Status check
    public int getRequestCount(String identifier, String action) {
        String key = action + ":" + identifier;
        List<Instant> timestamps = requestLog.get(key);
        if (timestamps == null) return 0;
        RateRule rule = rules.get(action);
        if (rule == null) return timestamps.size();
        Instant windowStart = Instant.now().minusSeconds(rule.windowSeconds);
        return (int) timestamps.stream().filter(t -> t.isAfter(windowStart)).count();
    }

    public Map<String, RateRule> getRules() { return Collections.unmodifiableMap(rules); }
}
