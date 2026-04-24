package exception;


public class DatabaseException extends KaizenException {

    public DatabaseException(ErrorCode code, String devMessage, String userMessage) {
        super(code, devMessage, userMessage);
    }

    public DatabaseException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(code, devMessage, userMessage, cause);
    }

    // Convenience — same message for dev and user
    public DatabaseException(ErrorCode code, String message) {
        super(code, message, "A database error occurred. Please try again.");
    }
}