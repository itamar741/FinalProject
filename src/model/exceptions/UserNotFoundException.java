package model.exceptions;

/**
 * Exception thrown when attempting to access a user that does not exist.
 * 
 * @author FinalProject
 */
public class UserNotFoundException extends Exception {
    /**
     * Constructs a new UserNotFoundException with the specified message.
     * 
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
