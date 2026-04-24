package exception;


public class BusinessException extends KaizenException {

    public BusinessException(ErrorCode code, String devMessage, String userMessage) {
        super(code, devMessage, userMessage);
    }

    public BusinessException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(code, devMessage, userMessage, cause);
    }
}