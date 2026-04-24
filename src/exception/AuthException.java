package exception;


public class AuthException extends KaizenException {

    public AuthException(ErrorCode code, String devMessage, String userMessage) {
        super(code, devMessage, userMessage);
    }

    public AuthException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(code, devMessage, userMessage, cause);
    }
}