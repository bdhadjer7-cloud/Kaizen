package exception;


public class KaizenException extends Exception {

    public enum ErrorCode {
        // Database
        DB_CONNECTION_FAILED, DB_QUERY_FAILED, DB_INSERT_FAILED,
        DB_UPDATE_FAILED, DB_DELETE_FAILED, DB_NOT_FOUND,
        DB_DUPLICATE_ENTRY, DB_TRANSACTION_FAILED, DB_CONSTRAINT_VIOLATION, DB_TIMEOUT,
        // Auth
        AUTH_INVALID_CREDENTIALS, AUTH_ACCOUNT_LOCKED, AUTH_SESSION_EXPIRED,
        AUTH_SESSION_NOT_FOUND, AUTH_UNAUTHORIZED, AUTH_FORBIDDEN,
        AUTH_EMAIL_NOT_VERIFIED, AUTH_TEACHER_NOT_VERIFIED,
        AUTH_TOKEN_INVALID, AUTH_TOKEN_EXPIRED,
        // Validation
        VALIDATION_EMPTY_FIELD, VALIDATION_INVALID_EMAIL, VALIDATION_WEAK_PASSWORD,
        VALIDATION_FIELD_TOO_LONG, VALIDATION_INVALID_FORMAT,
        VALIDATION_NULL_VALUE, VALIDATION_INVALID_ID,
        // Network
        NETWORK_SMTP_FAILED, NETWORK_TIMEOUT, NETWORK_CONNECTION_REFUSED,
        // File
        FILE_NOT_FOUND, FILE_TOO_LARGE, FILE_INVALID_FORMAT,
        FILE_UPLOAD_FAILED, FILE_READ_FAILED,
        // Business
        BUSINESS_COURSE_FULL, BUSINESS_ROOM_FULL, BUSINESS_QUIZ_ALREADY_ATTEMPTED,
        BUSINESS_ALREADY_ENROLLED, BUSINESS_NOT_ENROLLED,
        BUSINESS_RESOURCE_NOT_FOUND, BUSINESS_USER_NOT_FOUND,
        BUSINESS_COURSE_NOT_FOUND, BUSINESS_INVALID_JOIN_CODE,
        BUSINESS_QUIZ_TIME_EXPIRED, BUSINESS_BADGE_ALREADY_EARNED,
        // Security
        SECURITY_SQL_INJECTION, SECURITY_XSS_ATTEMPT, SECURITY_BRUTE_FORCE,
        SECURITY_TAMPERED_INPUT, SECURITY_RATE_LIMIT_EXCEEDED
    }

    private final ErrorCode errorCode;
    private final String    userMessage;

    public KaizenException(ErrorCode code, String devMessage, String userMessage) {
        super(devMessage);
        this.errorCode   = code;
        this.userMessage = userMessage;
    }

    public KaizenException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(devMessage, cause);
        this.errorCode   = code;
        this.userMessage = userMessage;
    }

    public ErrorCode getErrorCode()  { return errorCode; }
    public String    getUserMessage(){ return userMessage; }

    @Override public String toString() {
        return "KaizenException[" + errorCode + "]: " + getMessage()
                + " | User: " + userMessage;
    }
}