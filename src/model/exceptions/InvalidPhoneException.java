package model.exceptions;

/**
 * Exception thrown when a phone number is invalid.
 * Phone number must be exactly 10 digits.
 * 
 * @author FinalProject
 */
public class InvalidPhoneException extends Exception {
    
    /**
     * Constructs a new InvalidPhoneException with the specified message.
     * 
     * @param message the detail message
     */
    public InvalidPhoneException(String message) {
        super(message);
    }
}
