package model.exceptions;

/**
 * Exception thrown when attempting to access an employee that does not exist.
 * 
 * @author FinalProject
 */
public class EmployeeNotFoundException extends Exception {
    /**
     * Constructs a new EmployeeNotFoundException with the specified message.
     * 
     * @param message the detail message
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
