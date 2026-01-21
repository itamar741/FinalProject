package model;

/**
 * Represents an employee in the system.
 * Employees are identified by their employeeNumber and belong to a specific branch.
 * Each employee has a role (e.g., manager, cashier, salesperson) and can be active or inactive.
 * 
 * @author FinalProject
 */
public class Employee {
    private String fullName;
    private String idNumber;
    private String phone;
    private String bankAccount;
    private String employeeNumber;
    private String role;
    private String branchId;
    private boolean active;

    /**
     * Constructs a new Employee with the specified details.
     * New employees are created as active by default.
     * 
     * @param fullName the employee's full name
     * @param idNumber the employee's ID number
     * @param phone the employee's phone number
     * @param bankAccount the employee's bank account number
     * @param employeeNumber the unique employee number
     * @param role the employee's role (e.g., "manager", "cashier", "salesperson")
     * @param branchId the branch ID where the employee works
     */
    public Employee(String fullName,
                    String idNumber,
                    String phone,
                    String bankAccount,
                    String employeeNumber,
                    String role,
                    String branchId) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.bankAccount = bankAccount;
        this.employeeNumber = employeeNumber;
        this.role = role;
        this.branchId = branchId;
        this.active = true;
    }

    /**
     * Gets the employee's full name.
     * 
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the employee's ID number.
     * 
     * @return the ID number
     */
    public String getIdNumber() {
        return idNumber;
    }

    /**
     * Gets the employee's phone number.
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the employee's bank account number.
     * 
     * @return the bank account number
     */
    public String getBankAccount() {
        return bankAccount;
    }

    /**
     * Gets the employee's unique number.
     * 
     * @return the employee number
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * Gets the employee's role.
     * 
     * @return the role (e.g., "manager", "cashier", "salesperson")
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the branch ID where the employee works.
     * 
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * Checks if the employee is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the employee's active status.
     * 
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Sets the employee's full name.
     * 
     * @param fullName the new full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Sets the employee's phone number.
     * 
     * @param phone the new phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the employee's bank account number.
     * 
     * @param bankAccount the new bank account number
     */
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    /**
     * Sets the employee's role.
     * 
     * @param role the new role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the branch ID where the employee works.
     * 
     * @param branchId the new branch ID
     */
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}