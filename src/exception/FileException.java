package exception;



public class FileException extends KaizenException {

    public FileException(ErrorCode code, String devMessage, String userMessage) {
        super(code, devMessage, userMessage);
    }

    public FileException(ErrorCode code, String devMessage, String userMessage, Throwable cause) {
        super(code, devMessage, userMessage, cause);
    }
}