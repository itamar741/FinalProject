package model.exceptions;

/**
 * Exception thrown when attempting to create a customer that already exists.
 * 
 * @author FinalProject
 */
public class DuplicateCustomerException extends Exception {

    /**
     * Constructs a new DuplicateCustomerException with the specified message.
     * 
     * @param message the detail message
     */
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
