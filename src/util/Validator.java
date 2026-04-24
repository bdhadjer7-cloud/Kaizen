package util;

import exception.ValidationException;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*[0-9]).{8,}$");

    public static <T extends CharSequence> void notBlank(T value, String field,
            ValidationException errors) {
        if (value == null || value.toString().isBlank())
            errors.addError(field, field + " cannot be empty");
    }

    public static void validEmail(String email) throws ValidationException {
        ValidationException e = new ValidationException();
        if (email == null || email.isBlank()) {
            e.addError("email", "Email cannot be empty");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            e.addError("email", "'" + email + "' is not a valid email address");
        }
        if (e.hasErrors()) throw e;
    }

    public static void strongPassword(String password) throws ValidationException {
        ValidationException e = new ValidationException();
        if (password == null || password.isBlank()) {
            e.addError("password", "Password cannot be empty");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            e.addError("password",
                "Password must be 8+ chars with at least 1 uppercase and 1 number");
        }
        if (e.hasErrors()) throw e;
    }

    public static void maxLength(String value, String field, int max)
            throws ValidationException {
        if (value != null && value.length() > max) {
            ValidationException e = new ValidationException();
            e.addError(field, field + " max length is " + max + " characters");
            throw e;
        }
    }

    public static void positiveId(int id, String field) throws ValidationException {
        if (id <= 0) {
            ValidationException e = new ValidationException();
            e.addError(field, field + " must be a positive integer, got: " + id);
            throw e;
        }
    }

    public static void validateRegistration(String name, String email, String password)
            throws ValidationException {
        ValidationException errors = new ValidationException();
        notBlank(name, "name", errors);
        if (email == null || email.isBlank()) errors.addError("email", "Email cannot be empty");
        else if (!EMAIL_PATTERN.matcher(email).matches())
            errors.addError("email", "'" + email + "' is not a valid email");
        if (password == null || password.isBlank()) errors.addError("password","Password cannot be empty");
        else if (!PASSWORD_PATTERN.matcher(password).matches())
            errors.addError("password","Password must be 8+ chars, 1 uppercase, 1 number");
        if (errors.hasErrors()) throw errors;
    }
}
