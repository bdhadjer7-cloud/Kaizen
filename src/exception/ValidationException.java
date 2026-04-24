package exception;


import java.util.*;

public class ValidationException extends KaizenException {

    // Map<fieldName, errorMessage> — holds ALL field errors at once
    private final Map<String, String> fieldErrors = new LinkedHashMap<>();

    public ValidationException() {
        super(ErrorCode.VALIDATION_EMPTY_FIELD,
                "Validation failed",
                "Please fix the errors below.");
    }

    public void addError(String field, String error) {
        fieldErrors.put(field, error);
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public boolean hasErrors() { return !fieldErrors.isEmpty(); }

    @Override public String toString() {
        return "ValidationException: " + fieldErrors;
    }
}