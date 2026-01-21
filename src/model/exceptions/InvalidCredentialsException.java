package model.exceptions;

/**
 * Exception thrown when user authentication fails (invalid username or password).
 * 
 * @author FinalProject
 */
public class InvalidCredentialsException extends Exception {
    /**
     * Constructs a new InvalidCredentialsException with the specified message.
     * 
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}