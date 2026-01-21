package model.managers;

import model.Session;
import model.exceptions.UserAlreadyLoggedInException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages active user sessions.
 * Prevents duplicate logins by tracking active sessions per username.
 * Uses synchronized collections for thread-safety in multi-threaded server environment.
 * Maintains two maps: one by username (for quick duplicate check), one by socket (for retrieval by connection).
 * 
 * @author FinalProject
 */
public class SessionManager {
    
    /** username -> Session (for quick check if user already logged in) */
    private Map<String, Session> activeSessions;
    
    /** socket -> Session (for retrieval by connection) */
    private Map<Socket, Session> sessionsBySocket;
    
    /**
     * Constructs a new SessionManager with empty session maps.
     */
    public SessionManager() {
        this.activeSessions = Collections.synchronizedMap(new HashMap<>());
        this.sessionsBySocket = Collections.synchronizedMap(new HashMap<>());
    }
    
    /**
     * Creates a new session - with check that user is not already logged in.
     * Prevents duplicate logins from multiple locations.
     * 
     * @param username the username
     * @param branchId the branch ID
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param socket the socket connection
     * @return the created Session object
     * @throws UserAlreadyLoggedInException if user is already logged in from another location
     */
    public Session createSession(String username, 
                                String branchId, 
                                String role,
                                Socket socket)
            throws UserAlreadyLoggedInException {
        
        // Normalize username (lowercase + trim) to prevent case-sensitivity issues
        String normalizedUsername = username.trim().toLowerCase();
        
        // Synchronize for atomic check-and-put operation
        synchronized (activeSessions) {
            // Check if user is already logged in
            if (activeSessions.containsKey(normalizedUsername)) {
                throw new UserAlreadyLoggedInException(
                    "User " + username + " is already logged in from another location"
                );
            }
            
            Session session = new Session(normalizedUsername, branchId, role, socket);
            activeSessions.put(normalizedUsername, session);
            sessionsBySocket.put(socket, session);
            
            return session;
        }
    }
    
    /**
     * Logout - removes a session.
     * Removes from both session maps.
     * 
     * @param socket the socket connection to remove
     */
    public void removeSession(Socket socket) {
        synchronized (activeSessions) {
            Session session = sessionsBySocket.remove(socket);
            if (session != null) {
                // Normalize username for removal
                String normalizedUsername = session.getUsername().trim().toLowerCase();
                activeSessions.remove(normalizedUsername);
            }
        }
    }
    
    /**
     * Gets a session by socket.
     * 
     * @param socket the socket connection
     * @return the Session object, or null if not found
     */
    public Session getSession(Socket socket) {
        return sessionsBySocket.get(socket);
    }
    
    /**
     * Gets a session by username.
     * 
     * @param username the username
     * @return the Session object, or null if not found
     */
    public Session getSessionByUsername(String username) {
        String normalizedUsername = username.trim().toLowerCase();
        return activeSessions.get(normalizedUsername);
    }
    
    /**
     * Checks if a user is logged in.
     * 
     * @param username the username to check
     * @return true if user is logged in, false otherwise
     */
    public boolean isUserLoggedIn(String username) {
        String normalizedUsername = username.trim().toLowerCase();
        return activeSessions.containsKey(normalizedUsername);
    }
    
    /**
     * Checks if a socket is associated with a valid session.
     * 
     * @param socket the socket to check
     * @return true if socket has a valid session, false otherwise
     */
    public boolean isValidSession(Socket socket) {
        return sessionsBySocket.containsKey(socket);
    }
    
    /**
     * Gets all active sessions (for management/debugging).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of username to Session
     */
    public Map<String, Session> getAllActiveSessions() {
        synchronized (activeSessions) {
            return new HashMap<>(activeSessions);
        }
    }
}