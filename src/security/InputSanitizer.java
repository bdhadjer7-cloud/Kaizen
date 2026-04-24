package security;

import exception.SecurityViolationException;
import exception.KaizenException.ErrorCode;
import java.util.regex.*;
import java.util.logging.Logger;

/**
 * Kaizen — InputSanitizer.java
 * Defends against:
 *  1. SQL Injection   → blocks ; DROP TABLE, UNION SELECT, etc.
 *  2. XSS             → strips <script>, javascript:, onclick=
 *  3. Path Traversal  → blocks ../../../etc/passwd
 *  4. Null bytes      → blocks %00 injection
 *  5. HTML Injection  → escapes < > & " '
 */
public class InputSanitizer {

    private static final Logger LOG = Logger.getLogger(InputSanitizer.class.getName());

    // SQL Injection patterns
    private static final Pattern SQL_INJECTION = Pattern.compile(
        "(?i)(--|;|/\\*|\\*/|xp_|UNION\\s+SELECT|DROP\\s+TABLE|" +
        "INSERT\\s+INTO|DELETE\\s+FROM|ALTER\\s+TABLE|" +
        "EXEC\\s*\\(|EXECUTE\\s*\\(|CAST\\s*\\(|CONVERT\\s*\\(|" +
        "1\\s*=\\s*1|OR\\s+1|AND\\s+1)",
        Pattern.CASE_INSENSITIVE
    );

    // XSS patterns
    private static final Pattern XSS = Pattern.compile(
        "(?i)(<script|</script|javascript:|vbscript:|" +
        "onload=|onclick=|onerror=|onmouseover=|" +
        "eval\\(|expression\\(|<iframe|<object|<embed)",
        Pattern.CASE_INSENSITIVE
    );

    // Path traversal
    private static final Pattern PATH_TRAVERSAL = Pattern.compile(
        "(\\.\\./|\\.\\.\\\\|%2e%2e|%00)"
    );

    //Check for SQL injection
    public static String sanitizeSQL(String input) throws SecurityViolationException {
        if (input == null) return null;
        if (SQL_INJECTION.matcher(input).find()) {
            LOG.severe("[SECURITY] SQL Injection attempt: " + input.substring(0, Math.min(50, input.length())));
            throw new SecurityViolationException(
                ErrorCode.SECURITY_SQL_INJECTION,
                "SQL injection detected in input: " + input,
                "Input: " + input.substring(0, Math.min(20, input.length()))
            );
        }
        return input.trim();
    }

    //Check for XSS
    public static String sanitizeXSS(String input) throws SecurityViolationException {
        if (input == null) return null;
        if (XSS.matcher(input).find()) {
            LOG.severe("[SECURITY] XSS attempt: " + input.substring(0, Math.min(50, input.length())));
            throw new SecurityViolationException(
                ErrorCode.SECURITY_XSS_ATTEMPT,
                "XSS attempt detected: " + input,
                "Input: " + input.substring(0, Math.min(20, input.length()))
            );
        }
        return input.trim();
    }

    //Check for path traversal
    public static String sanitizePath(String input) throws SecurityViolationException {
        if (input == null) return null;
        if (PATH_TRAVERSAL.matcher(input).find()) {
            LOG.severe("[SECURITY] Path traversal attempt: " + input);
            throw new SecurityViolationException(
                ErrorCode.SECURITY_TAMPERED_INPUT,
                "Path traversal detected: " + input,
                "Invalid file path"
            );
        }
        return input.trim();
    }

    //Sanitize all — run all checks
    public static String sanitizeAll(String input) throws SecurityViolationException {
        if (input == null) return null;
        sanitizeSQL(input);
        sanitizeXSS(input);
        sanitizePath(input);
        return escapeHTML(input.trim());
    }

    //HTML escape — for display in UI
    public static String escapeHTML(String input) {
        if (input == null) return null;
        return input
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&#x27;");
    }

    //Sanitize numeric input
    public static int sanitizeId(int id) throws SecurityViolationException {
        if (id <= 0) {
            throw new SecurityViolationException(
                ErrorCode.SECURITY_TAMPERED_INPUT,
                "Invalid ID received: " + id,
                "Invalid ID: " + id
            );
        }
        return id;
    }
}
