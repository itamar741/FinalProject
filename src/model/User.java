package model;

/**
 * Represents a system user account.
 * Users have a role (admin, manager, salesman, cashier) that determines their permissions.
 * Users can be active or inactive, and may be required to change their password on next login.
 * 
 * @author FinalProject
 */
public class User {
    private String username;
    private String password;
    private String role;        // admin, manager, salesman, cashier
    private String branchId;
    private boolean active;
    private boolean mustChangePassword;  // Whether password change is required

    /**
     * Constructs a new User with the specified details.
     * New users are created as active with no password change requirement.
     * 
     * @param username the unique username
     * @param password the user's password
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param branchId the branch ID where the user works
     */
    public User(String username, 
                String password, 
                String role,
                String branchId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.branchId = branchId;
        this.active = true;
        this.mustChangePassword = false;
    }

    /**
     * Gets the username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     * Note: In production, passwords should be hashed, not stored in plain text.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's role.
     * Role determines system access level and permissions.
     * 
     * @return the role (admin, manager, salesman, cashier)
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the branch ID where the user works.
     * 
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * Checks if the user account is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks if the user must change password on next login.
     * 
     * @return true if password change is required, false otherwise
     */
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    /**
     * Sets a new password for the user.
     * Automatically clears the mustChangePassword flag when password is changed.
     * 
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
        this.mustChangePassword = false;
    }

    /**
     * Sets the user's active status.
     * 
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Sets whether the user must change password on next login.
     * 
     * @param mustChangePassword true to require password change, false otherwise
     */
    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    /**
     * Sets the branch ID where the user works.
     * 
     * @param branchId the new branch ID
     */
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    /**
     * Checks if the provided password matches the user's password.
     * 
     * @param password the password to check
     * @return true if passwords match, false otherwise
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}