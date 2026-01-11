package model.managers;

import model.Session;
import model.exceptions.UserAlreadyLoggedInException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    
    // username -> Session (לבדיקה מהירה אם משתמש כבר מחובר)
    private Map<String, Session> activeSessions;
    
    // socket -> Session (לשליפה לפי חיבור)
    private Map<Socket, Session> sessionsBySocket;
    
    public SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionsBySocket = new ConcurrentHashMap<>();
    }
    
    /**
     * יצירת סשן חדש - עם בדיקה שלא מחובר כבר
     */
    public Session createSession(String username, 
                                String employeeNumber, 
                                String branchId, 
                                String userType,  // שונה מ-role
                                Socket socket)
            throws UserAlreadyLoggedInException {
        
        // בדיקה אם המשתמש כבר מחובר
        if (activeSessions.containsKey(username)) {
            throw new UserAlreadyLoggedInException(
                "User " + username + " is already logged in from another location"
            );
        }
        
        Session session = new Session(username, employeeNumber, branchId, userType, socket);
        activeSessions.put(username, session);
        sessionsBySocket.put(socket, session);
        
        return session;
    }
    
    /**
     * התנתקות - הסרת סשן
     */
    public void removeSession(Socket socket) {
        Session session = sessionsBySocket.remove(socket);
        if (session != null) {
            activeSessions.remove(session.getUsername());
        }
    }
    
    /**
     * קבלת סשן לפי socket
     */
    public Session getSession(Socket socket) {
        return sessionsBySocket.get(socket);
    }
    
    /**
     * קבלת סשן לפי username
     */
    public Session getSessionByUsername(String username) {
        return activeSessions.get(username);
    }
    
    /**
     * בדיקה אם משתמש מחובר
     */
    public boolean isUserLoggedIn(String username) {
        return activeSessions.containsKey(username);
    }
    
    /**
     * בדיקה אם socket קשור לסשן תקין
     */
    public boolean isValidSession(Socket socket) {
        return sessionsBySocket.containsKey(socket);
    }
    
    /**
     * קבלת כל הסשנים הפעילים (לצורך ניהול/דיבוג)
     */
    public Map<String, Session> getAllActiveSessions() {
        return new HashMap<>(activeSessions);
    }
}