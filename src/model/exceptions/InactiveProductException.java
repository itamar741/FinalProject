package model.exceptions;

/**
 * Exception thrown when attempting to sell an inactive product.
 * Only active products can be sold.
 * 
 * @author FinalProject
 */
public class InactiveProductException extends Exception {

    /**
     * Constructs a new InactiveProductException with the specified message.
     * 
     * @param message the detail message
     */
    public InactiveProductException(String message) {
        super(message);
    }
}
