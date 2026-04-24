package exception;

public class NetworkException extends KaizenException {

    public NetworkException(ErrorCode code, String devMessage, String userMessage) {
        super(code, devMessage, userMessage);
    }

    public NetworkException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(code, devMessage, userMessage, cause);
    }
}