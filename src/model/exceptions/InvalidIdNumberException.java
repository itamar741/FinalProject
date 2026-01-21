package model.exceptions;

/**
 * Exception thrown when an ID number (תעודת זהות) is invalid.
 * ID number must be exactly 9 digits.
 * 
 * @author FinalProject
 */
public class InvalidIdNumberException extends Exception {
    
    /**
     * Constructs a new InvalidIdNumberException with the specified message.
     * 
     * @param message the detail message
     */
    public InvalidIdNumberException(String message) {
        super(message);
    }
}
