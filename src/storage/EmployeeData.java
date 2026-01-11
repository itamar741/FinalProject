package storage;

import model.Employee;

/**
 * מחלקת DTO לשמירת Employee ב-JSON
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
    
    // Default constructor for JSON deserialization
    public EmployeeData() {}
    
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
    
    public Employee toEmployee() {
        Employee employee = new Employee(
            fullName, idNumber, phone, bankAccount,
            employeeNumber, role, branchId
        );
        employee.setActive(active);
        return employee;
    }
}
