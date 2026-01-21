package model.managers;

import model.User;
import model.exceptions.InvalidCredentialsException;
import model.exceptions.WeakPasswordException;
import model.exceptions.UserNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages user authentication and user account operations.
 * Validates passwords according to password policy (minimum 6 characters).
 * All users must be created manually - no default users are created.
 * 
 * @author FinalProject
 */
public class AuthenticationManager {
    
    private Map<String, User> users;  // username -> User
    
    /**
     * Constructs a new AuthenticationManager with empty user map.
     * No default users are created - all users must be created manually.
     */
    public AuthenticationManager() {
        users = Collections.synchronizedMap(new HashMap<>());
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the password
     * @return the authenticated User object
     * @throws InvalidCredentialsException if username not found, user is inactive, or password is incorrect
     */
    public User authenticate(String username, String password) 
            throws InvalidCredentialsException {
        
        // Normalize username (lowercase + trim) to prevent case-sensitivity issues
        String normalizedUsername = username.trim().toLowerCase();
        
        synchronized (users) {
            // Try to find user with normalized username first
            User user = users.get(normalizedUsername);
            
            // If not found, try original username (for backward compatibility)
            if (user == null) {
                user = users.get(username);
            }
            
            if (user == null || !user.isActive()) {
                throw new InvalidCredentialsException("Invalid username");
            }
            
            if (!user.checkPassword(password)) {
                throw new InvalidCredentialsException("Invalid password");
            }
            
            return user;
        }
    }
    
    /**
     * Creates a new user account.
     * Validates password strength and role.
     * 
     * @param username the unique username
     * @param password the user's password (must be at least 6 characters)
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param branchId the branch ID where the user works
     * @throws WeakPasswordException if password does not meet requirements
     * @throws IllegalArgumentException if role is invalid
     */
    public void createUser(String username, 
                          String password, 
                          String role,
                          String branchId)
            throws WeakPasswordException {
        
        validatePassword(password);
        
        // Validate role
        if (!role.equals("admin") && !role.equals("manager") && 
            !role.equals("salesman") && !role.equals("cashier")) {
            throw new IllegalArgumentException("Role must be one of: admin, manager, salesman, cashier");
        }
        
        User user = new User(username, password, role, branchId);
        
        // Synchronize for atomic check-and-put operation
        synchronized (users) {
            if (users.containsKey(username)) {
                throw new IllegalArgumentException("User " + username + " already exists");
            }
            users.put(username, user);
        }
    }
    
    /**
     * Adds a user directly (for loading from storage - no password validation).
     * Used during data loading.
     * 
     * @param user the user to add
     */
    public void addUserDirectly(User user) {
        if (user != null) {
            synchronized (users) {
                if (!users.containsKey(user.getUsername())) {
                    users.put(user.getUsername(), user);
                }
            }
        }
    }
    
    /**
     * Validates password according to password policy.
     * Current policy: minimum 6 characters.
     * Additional checks can be added (letters, digits, special characters).
     * 
     * @param password the password to validate
     * @throws WeakPasswordException if password does not meet requirements
     */
    private void validatePassword(String password) throws WeakPasswordException {
        if (password == null || password.length() < 6) {
            throw new WeakPasswordException("Password must be at least 6 characters");
        }
        
        // Additional checks can be added:
        // - At least one letter
        // - At least one digit
        // - At least one special character
    }
    
    /**
     * Changes a user's password.
     * Validates old password and new password strength.
     * 
     * @param username the username
     * @param oldPassword the current password
     * @param newPassword the new password (must meet password policy)
     * @throws InvalidCredentialsException if username not found or old password is incorrect
     * @throws WeakPasswordException if new password does not meet requirements
     */
    public void changePassword(String username, String oldPassword, String newPassword)
            throws InvalidCredentialsException, WeakPasswordException {
        
        synchronized (users) {
            User user = users.get(username);
            if (user == null || !user.checkPassword(oldPassword)) {
                throw new InvalidCredentialsException("Invalid credentials");
            }
            
            validatePassword(newPassword);
            user.setPassword(newPassword);
        }
    }
    
    /**
     * Gets a user by username.
     * 
     * @param username the username
     * @return the User object, or null if not found
     */
    public User getUser(String username) {
        synchronized (users) {
            return users.get(username);
        }
    }
    
    /**
     * Checks if a user exists.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        synchronized (users) {
            return users.containsKey(username);
        }
    }
    
    /**
     * Gets all users (for admin operations).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of username to User
     */
    public Map<String, User> getAllUsers() {
        synchronized (users) {
            return new HashMap<>(users);
        }
    }
    
    /**
     * Activates or deactivates a user account.
     * 
     * @param username the username
     * @param active true to activate, false to deactivate
     */
    public void setUserActive(String username, boolean active) {
        synchronized (users) {
            User user = users.get(username);
            if (user != null) {
                user.setActive(active);
            }
        }
    }
    
    /**
     * Deletes a user from the system.
     * 
     * @param username the username to delete
     * @throws UserNotFoundException if user not found
     */
    public void deleteUser(String username) throws UserNotFoundException {
        synchronized (users) {
            User user = users.get(username);
            if (user == null) {
                throw new UserNotFoundException("User " + username + " not found");
            }
            
            users.remove(username);
        }
    }
}