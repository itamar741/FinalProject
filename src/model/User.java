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

   
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

 
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }


    public void setPassword(String password) {
        this.password = password;
        this.mustChangePassword = false;
    }



    /**
     * Sets whether the user must change password on next login.
     * 
     * @param mustChangePassword true to require password change, false otherwise
     */
    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}