package model;

public class Employee {
    private String fullName;
    private String idNumber;
    private String phone;
    private String bankAccount;
    private String employeeNumber;
    private String role;
    private String branchId;
    private boolean active;


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


    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getRole() {
        return role;
    }

    public String getBranchId() {
        return branchId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Setters נוספים לעדכון פרטי עובד
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

}