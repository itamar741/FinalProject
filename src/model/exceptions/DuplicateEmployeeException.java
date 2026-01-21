package model.exceptions;

/**
 * Exception thrown when attempting to create an employee that already exists.
 * 
 * @author FinalProject
 */
public class DuplicateEmployeeException extends Exception {
    /**
     * Constructs a new DuplicateEmployeeException with the specified message.
     * 
     * @param message the detail message
     */
    public DuplicateEmployeeException(String message) {
        super(message);
    }
}
