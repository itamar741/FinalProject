package storage;

import model.User;

/**
 * מחלקת DTO לשמירת User ב-JSON
 */
public class UserData {
    public String username;
    public String password;
    public String employeeNumber;
    public String userType;
    public String branchId;
    public boolean active;
    public boolean mustChangePassword;
    
    // Default constructor for JSON deserialization
    public UserData() {}
    
    public UserData(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.employeeNumber = user.getEmployeeNumber();
        this.userType = user.getUserType();
        this.branchId = user.getBranchId();
        this.active = user.isActive();
        this.mustChangePassword = user.isMustChangePassword();
    }
    
    public User toUser() {
        User user = new User(username, password, employeeNumber, userType, branchId);
        user.setActive(active);
        user.setMustChangePassword(mustChangePassword);
        return user;
    }
}
