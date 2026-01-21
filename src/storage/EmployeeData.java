package storage;

import model.Employee;

/**
 * DTO class for storing Employee in JSON.
 * Implements DTO Pattern - separates Model from storage format.
 * 
 * @author FinalProject
 */
public class EmployeeData {
    public String fullName;
    public String idNumber;
    public String phone;
    public String bankAccount;
    public String employeeNumber;
    public String role;
    public String branchId;
    public boolean active;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public EmployeeData() {}
    
    /**
     * Constructs EmployeeData from an Employee object.
     * 
     * @param employee the Employee object to convert
     */
    public EmployeeData(Employee employee) {
        this.fullName = employee.getFullName();
        this.idNumber = employee.getIdNumber();
        this.phone = employee.getPhone();
        this.bankAccount = employee.getBankAccount();
        this.employeeNumber = employee.getEmployeeNumber();
        this.role = employee.getRole();
        this.branchId = employee.getBranchId();
        this.active = employee.isActive();
    }
    
    /**
     * Converts this DTO to an Employee object.
     * 
     * @return an Employee object with all fields set
     */
    public Employee toEmployee() {
        Employee employee = new Employee(
            fullName, idNumber, phone, bankAccount,
            employeeNumber, role, branchId
        );
        employee.setActive(active);
        return employee;
    }
}
