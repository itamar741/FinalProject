package model.exceptions;

/**
 * Exception thrown when a user attempts to perform an operation they are not authorized for.
 * 
 * @author FinalProject
 */
public class UnauthorizedException extends Exception {
    /**
     * Constructs a new UnauthorizedException with the specified message.
     * 
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}