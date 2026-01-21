package model.exceptions;

/**
 * Exception thrown when attempting to sell or remove more products than available in inventory.
 * 
 * @author FinalProject
 */
public class InsufficientStockException extends Exception {

    /**
     * Constructs a new InsufficientStockException with the specified message.
     * 
     * @param message the detail message
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
