package model.exceptions;

/**
 * Exception thrown when attempting to create a user that already exists.
 * 
 * @author FinalProject
 */
public class DuplicateUserException extends Exception {
    /**
     * Constructs a new DuplicateUserException with the specified message.
     * 
     * @param message the detail message
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}
