package model.exceptions;

/**
 * Exception thrown when a password does not meet the password policy requirements.
 * Current policy: minimum 6 characters.
 * 
 * @author FinalProject
 */
public class WeakPasswordException extends Exception {
    /**
     * Constructs a new WeakPasswordException with the specified message.
     * 
     * @param message the detail message
     */
    public WeakPasswordException(String message) {
        super(message);
    }
}