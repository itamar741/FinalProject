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
     * Constructs a new Session for a logged-in user
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

  
    public String getUsername() {
        return username;
    }

    public String getBranchId() {
        return branchId;
    }


    public String getRole() {
        return role;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public boolean isSameUser(String username) {
        return this.username.equals(username);
    }
}