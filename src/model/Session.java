package model;

import java.net.Socket;

/**
 * Represents an active user session in the system.
 * Tracks the user's connection, login time, and session information.
 * Used to prevent duplicate logins and manage user connections.
 * 
 * @author FinalProject
 */
public class Session {
    private String username;
    private String branchId;
    private String role;  // admin, manager, salesman, cashier
    private Socket socket;
    private long loginTime;

    /**
     * Constructs a new Session for a logged-in user.
     * 
     * @param username the username of the logged-in user
     * @param branchId the branch ID where the user works
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param socket the socket connection for this session
     */
    public Session(String username, 
                   String branchId, 
                   String role,
                   Socket socket) {
        this.username = username;
        this.branchId = branchId;
        this.role = role;
        this.socket = socket;
        this.loginTime = System.currentTimeMillis();
    }

    /**
     * Gets the username for this session.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the branch ID for this session.
     * 
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * Gets the user's role for this session.
     * 
     * @return the role (admin, manager, salesman, cashier)
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the socket connection for this session.
     * 
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Gets the login time as a timestamp (milliseconds since epoch).
     * 
     * @return the login time
     */
    public long getLoginTime() {
        return loginTime;
    }

    /**
     * Checks if this session belongs to the specified username.
     * 
     * @param username the username to check
     * @return true if this session belongs to the username, false otherwise
     */
    public boolean isSameUser(String username) {
        return this.username.equals(username);
    }
}