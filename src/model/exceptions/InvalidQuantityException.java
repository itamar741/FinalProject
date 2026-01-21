package model.exceptions;

/**
 * Exception thrown when an invalid quantity is provided (e.g., zero or negative).
 * 
 * @author FinalProject
 */
public class InvalidQuantityException extends Exception {

    /**
     * Constructs a new InvalidQuantityException with the specified message.
     * 
     * @param message the detail message
     */
    public InvalidQuantityException(String message) {
        super(message);
    }
}
