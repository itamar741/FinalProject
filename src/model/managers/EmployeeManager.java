package model.managers;

import model.Employee;
import model.exceptions.DuplicateEmployeeException;
import model.exceptions.EmployeeNotFoundException;
import model.exceptions.InvalidIdNumberException;
import model.exceptions.InvalidPhoneException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages employee data and operations.
 * Maintains two maps for efficient lookup: by employeeNumber and by idNumber (for duplicate checking).
 * 
 * @author FinalProject
 */
public class EmployeeManager {
    
    private Map<String, Employee> employees;  // employeeNumber -> Employee
    private Map<String, Employee> employeesByIdNumber;  // idNumber -> Employee (for duplicate checking)
    private Map<String, String> usernameToEmployeeNumber;  // username -> employeeNumber
    
    /**
     * Constructs a new EmployeeManager with empty employee maps.
     */
    public EmployeeManager() {
        employees = new HashMap<>();
        employeesByIdNumber = new HashMap<>();
        usernameToEmployeeNumber = new HashMap<>();
    }
    
    /**
     * Adds a new employee to the system.
     * Checks for duplicates by both employeeNumber and idNumber.
     * 
     * @param fullName the employee's full name
     * @param idNumber the employee's ID number
     * @param phone the employee's phone number
     * @param bankAccount the employee's bank account number
     * @param employeeNumber the unique employee number
     * @param role the employee's role (e.g., "manager", "cashier", "salesperson")
     * @param branchId the branch ID where the employee works
     * @throws DuplicateEmployeeException if employee with same employeeNumber or idNumber already exists
     */
    public void addEmployee(String fullName,
                           String idNumber,
                           String phone,
                           String bankAccount,
                           String employeeNumber,
                           String role,
                           String branchId)
            throws DuplicateEmployeeException, InvalidIdNumberException, InvalidPhoneException {
        
        // Validate ID number (must be exactly 9 digits)
        validateIdNumber(idNumber);
        
        // Validate phone number (must be exactly 10 digits)
        validatePhoneNumber(phone);
        
        // Check if employee already exists (by employee number or ID number)
        if (employees.containsKey(employeeNumber)) {
            throw new DuplicateEmployeeException("Employee with number " + employeeNumber + " already exists");
        }
        
        if (employeesByIdNumber.containsKey(idNumber)) {
            throw new DuplicateEmployeeException("Employee with ID number " + idNumber + " already exists");
        }
        
        Employee employee = new Employee(
            fullName, idNumber, phone, bankAccount, 
            employeeNumber, role, branchId
        );
        employees.put(employeeNumber, employee);
        employeesByIdNumber.put(idNumber, employee);
    }
    
    /**
     * Validates that an ID number is exactly 9 digits.
     * 
     * @param idNumber the ID number to validate
     * @throws InvalidIdNumberException if ID number is not exactly 9 digits or contains non-digit characters
     */
    private void validateIdNumber(String idNumber) throws InvalidIdNumberException {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new InvalidIdNumberException("ID number cannot be empty");
        }
        
        if (idNumber.length() != 9) {
            throw new InvalidIdNumberException("ID number must be exactly 9 digits");
        }
        
        // Check that all characters are digits
        for (char c : idNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new InvalidIdNumberException("ID number must contain only digits");
            }
        }
    }
    
    /**
     * Validates that a phone number is exactly 10 digits.
     * 
     * @param phone the phone number to validate
     * @throws InvalidPhoneException if phone number is not exactly 10 digits or contains non-digit characters
     */
    private void validatePhoneNumber(String phone) throws InvalidPhoneException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidPhoneException("Phone number cannot be empty");
        }
        
        if (phone.length() != 10) {
            throw new InvalidPhoneException("Phone number must be exactly 10 digits");
        }
        
        // Check that all characters are digits
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new InvalidPhoneException("Phone number must contain only digits");
            }
        }
    }
    
    /**
     * Gets an employee by employee number.
     * 
     * @param employeeNumber the employee number
     * @return the Employee object
     * @throws EmployeeNotFoundException if employee not found
     */
    public Employee getEmployee(String employeeNumber) 
            throws EmployeeNotFoundException {
        Employee employee = employees.get(employeeNumber);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee with number " + employeeNumber + " not found");
        }
        return employee;
    }
    
    /**
     * Gets all employees (for saving to storage).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of employeeNumber to Employee
     */
    public Map<String, Employee> getAllEmployees() {
        return new HashMap<>(employees);
    }
    
    /**
     * Updates employee details.
     * Only updates fields that are provided (not null or empty).
     * Validates phone number if provided.
     * 
     * @param employeeNumber the employee number
     * @param fullName the new full name (null or empty to keep current)
     * @param phone the new phone number (null or empty to keep current)
     * @param bankAccount the new bank account (null or empty to keep current)
     * @param role the new role (null or empty to keep current)
     * @param branchId the new branch ID (null or empty to keep current)
     * @throws EmployeeNotFoundException if employee not found
     * @throws InvalidPhoneException if phone number is invalid
     */
    public void updateEmployee(String employeeNumber, 
                              String fullName,
                              String phone,
                              String bankAccount,
                              String role,
                              String branchId)
            throws EmployeeNotFoundException, InvalidPhoneException {
        Employee employee = getEmployee(employeeNumber);  // Throws exception if not found
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            employee.setFullName(fullName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            validatePhoneNumber(phone);  // Validate before updating
            employee.setPhone(phone);
        }
        if (bankAccount != null && !bankAccount.trim().isEmpty()) {
            employee.setBankAccount(bankAccount);
        }
        if (role != null && !role.trim().isEmpty()) {
            employee.setRole(role);
        }
        if (branchId != null && !branchId.trim().isEmpty()) {
            employee.setBranchId(branchId);
        }
    }
    
    /**
     * Activates or deactivates an employee.
     * 
     * @param employeeNumber the employee number
     * @param active true to activate, false to deactivate
     * @throws EmployeeNotFoundException if employee not found
     */
    public void setEmployeeActive(String employeeNumber, boolean active)
            throws EmployeeNotFoundException {
        Employee employee = getEmployee(employeeNumber);
        employee.setActive(active);
    }
    
    /**
     * Deletes an employee from the system.
     * Removes from both employee maps.
     * 
     * @param employeeNumber the employee number to delete
     * @throws EmployeeNotFoundException if employee not found
     */
    public void deleteEmployee(String employeeNumber)
            throws EmployeeNotFoundException {
        Employee employee = getEmployee(employeeNumber);
        employees.remove(employeeNumber);
        employeesByIdNumber.remove(employee.getIdNumber());
    }
    
    /**
     * Gets all employees for a specific branch.
     * 
     * @param branchId the branch ID
     * @return a Map of employeeNumber to Employee for the specified branch
     */
    public Map<String, Employee> getEmployeesByBranch(String branchId) {
        Map<String, Employee> result = new HashMap<>();
        for (Employee emp : employees.values()) {
            if (emp.getBranchId().equals(branchId)) {
                result.put(emp.getEmployeeNumber(), emp);
            }
        }
        return result;
    }
    
    /**
     * Checks if an employee exists.
     * 
     * @param employeeNumber the employee number to check
     * @return true if employee exists, false otherwise
     */
    public boolean employeeExists(String employeeNumber) {
        return employees.containsKey(employeeNumber);
    }
    
    /**
     * Registers a mapping between username and employee number.
     * Called when an employee is created with a user account.
     * 
     * @param username the username
     * @param employeeNumber the employee number
     */
    public void registerUsernameMapping(String username, String employeeNumber) {
        usernameToEmployeeNumber.put(username, employeeNumber);
    }
    
    /**
     * Gets the employee number for a given username.
     * 
     * @param username the username
     * @return the employee number, or null if not found
     */
    public String getEmployeeNumberByUsername(String username) {
        return usernameToEmployeeNumber.get(username);
    }
}