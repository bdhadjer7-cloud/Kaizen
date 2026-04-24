package exception;


public class SecurityViolationException extends KaizenException {

    private final String attackerInfo;

    public SecurityViolationException(ErrorCode code, String devMessage, String attackerInfo) {
        super(code, devMessage, "Security violation detected. Action has been logged.");
        this.attackerInfo = attackerInfo;
    }

    public SecurityViolationException(ErrorCode code, String devMessage,
                                      String attackerInfo, Throwable cause) {
        super(code, devMessage, "Security violation detected. Action has been logged.", cause);
        this.attackerInfo = attackerInfo;
    }

    public String getAttackerInfo() { return attackerInfo; }
}
