package model;

public class User {
    private String username;
    private String password;
    private String employeeNumber;  // קישור לעובד
    private String userType;        // ADMIN או EMPLOYEE (סוג המשתמש במערכת)
    private String branchId;
    private boolean active;
    private boolean mustChangePassword;  // אם צריך לשנות סיסמה

    public User(String username, 
                String password, 
                String employeeNumber, 
                String userType,    // שונה מ-role ל-userType
                String branchId) {
        this.username = username;
        this.password = password;
        this.employeeNumber = employeeNumber;
        this.userType = userType;
        this.branchId = branchId;
        this.active = true;
        this.mustChangePassword = false;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getUserType() {  // שונה מ-getRole
        return userType;
    }

    public String getBranchId() {
        return branchId;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    // Setters
    public void setPassword(String password) {
        this.password = password;
        this.mustChangePassword = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}