package model.managers;

import model.Employee;
import model.exceptions.DuplicateEmployeeException;
import model.exceptions.EmployeeNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class EmployeeManager {
    
    private Map<String, Employee> employees;  // employeeNumber -> Employee
    private Map<String, Employee> employeesByIdNumber;  // idNumber -> Employee (לבדיקת כפילויות)
    
    public EmployeeManager() {
        employees = new HashMap<>();
        employeesByIdNumber = new HashMap<>();
    }
    
    /**
     * הוספת עובד חדש
     */
    public void addEmployee(String fullName,
                           String idNumber,
                           String phone,
                           String bankAccount,
                           String employeeNumber,
                           String role,
                           String branchId)
            throws DuplicateEmployeeException {
        
        // בדיקה אם העובד כבר קיים (לפי מספר עובד או ת.ז)
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
     * קבלת עובד לפי מספר עובד
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
     * קבלת כל העובדים
     */
    public Map<String, Employee> getAllEmployees() {
        return new HashMap<>(employees);
    }
    
    /**
     * עדכון פרטי עובד
     */
    public void updateEmployee(String employeeNumber, 
                              String fullName,
                              String phone,
                              String bankAccount,
                              String role,
                              String branchId)
            throws EmployeeNotFoundException {
        Employee employee = getEmployee(employeeNumber);  // זה יזרוק חריגה אם לא נמצא
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            employee.setFullName(fullName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
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
     * השבתת/הפעלת עובד
     */
    public void setEmployeeActive(String employeeNumber, boolean active)
            throws EmployeeNotFoundException {
        Employee employee = getEmployee(employeeNumber);
        employee.setActive(active);
    }
    
    /**
     * מחיקת עובד (הסרה מהמערכת)
     */
    public void deleteEmployee(String employeeNumber)
            throws EmployeeNotFoundException {
        Employee employee = getEmployee(employeeNumber);
        employees.remove(employeeNumber);
        employeesByIdNumber.remove(employee.getIdNumber());
    }
    
    /**
     * קבלת עובדים לפי סניף
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
     * בדיקה אם עובד קיים
     */
    public boolean employeeExists(String employeeNumber) {
        return employees.containsKey(employeeNumber);
    }
}