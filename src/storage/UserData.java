package storage;

import model.User;

/**
 * DTO class for storing User in JSON.
 * Implements DTO Pattern - separates Model from storage format.
 * Note: In production, passwords should be hashed, not stored in plain text.
 * 
 * @author FinalProject
 */
public class UserData {
    public String username;
    public String password;
    public String role;
    public String branchId;
    public boolean active;
    public boolean mustChangePassword;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public UserData() {}
    
    /**
     * Constructs UserData from a User object.
     * 
     * @param user the User object to convert
     */
    public UserData(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.branchId = user.getBranchId();
        this.active = user.isActive();
        this.mustChangePassword = user.isMustChangePassword();
    }
    
    /**
     * Converts this DTO to a User object.
     * 
     * @return a User object with all fields set
     */
    public User toUser() {
        User user = new User(username, password, role, branchId);
        user.setActive(active);
        user.setMustChangePassword(mustChangePassword);
        return user;
    }
}
