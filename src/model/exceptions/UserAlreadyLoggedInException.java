package model.exceptions;

/**
 * Exception thrown when attempting to log in a user who is already logged in from another location.
 * Prevents duplicate logins.
 * 
 * @author FinalProject
 */
public class UserAlreadyLoggedInException extends Exception {
    /**
     * Constructs a new UserAlreadyLoggedInException with the specified message.
     * 
     * @param message the detail message
     */
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }
}
