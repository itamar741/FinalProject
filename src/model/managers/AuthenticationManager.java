package model.managers;

import model.User;
import model.exceptions.InvalidCredentialsException;
import model.exceptions.WeakPasswordException;
import model.exceptions.UserNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationManager {
    
    private Map<String, User> users;  // username -> User
    
    public AuthenticationManager() {
        users = new HashMap<>();
        // יצירת משתמש אדמין ברירת מחדל
        createDefaultAdmin();
    }
    
    private void createDefaultAdmin() {
        // משתמש אדמין ברירת מחדל: admin/admin
        User admin = new User("admin", "admin", "ADMIN001", "ADMIN", "ALL");
        users.put("admin", admin);
        
        // משתמש superadmin: superadmin/123456
        User superadmin = new User("superadmin", "123456", "SUPER001", "ADMIN", "ALL");
        users.put("superadmin", superadmin);
    }
    
    /**
     * אימות משתמש
     */
    public User authenticate(String username, String password) 
            throws InvalidCredentialsException {
        
        User user = users.get(username);
        
        if (user == null || !user.isActive()) {
            throw new InvalidCredentialsException("Invalid username");
        }
        
        if (!user.checkPassword(password)) {
            throw new InvalidCredentialsException("Invalid password");
        }
        
        return user;
    }
    
    /**
     * יצירת משתמש חדש (רק אדמין)
     */
    public void createUser(String username, 
                          String password, 
                          String employeeNumber,
                          String userType,  // שונה מ-role
                          String branchId)
            throws WeakPasswordException {
        
        validatePassword(password);
        
        // בדיקה ש-userType תקין
        if (!userType.equals("ADMIN") && !userType.equals("EMPLOYEE")) {
            throw new IllegalArgumentException("UserType must be ADMIN or EMPLOYEE");
        }
        
        User user = new User(username, password, employeeNumber, userType, branchId);
        users.put(username, user);
    }
    
    /**
     * הוספת משתמש ישיר (לטעינה - בלי בדיקת סיסמה)
     */
    public void addUserDirectly(User user) {
        if (user != null && !users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
        }
    }
    
    /**
     * בדיקת מדיניות סיסמה
     */
    private void validatePassword(String password) throws WeakPasswordException {
        if (password == null || password.length() < 6) {
            throw new WeakPasswordException("Password must be at least 6 characters");
        }
        
        // אפשר להוסיף בדיקות נוספות:
        // - לפחות אות אחת
        // - לפחות ספרה אחת
        // - לפחות תו מיוחד
    }
    
    /**
     * שינוי סיסמה
     */
    public void changePassword(String username, String oldPassword, String newPassword)
            throws InvalidCredentialsException, WeakPasswordException {
        
        User user = users.get(username);
        if (user == null || !user.checkPassword(oldPassword)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        validatePassword(newPassword);
        user.setPassword(newPassword);
    }
    
    /**
     * קבלת משתמש לפי username
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * בדיקה אם משתמש קיים
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    /**
     * קבלת כל המשתמשים (עבור אדמין)
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }
    
    /**
     * השבתת/הפעלת משתמש
     */
    public void setUserActive(String username, boolean active) {
        User user = users.get(username);
        if (user != null) {
            user.setActive(active);
        }
    }
    
    /**
     * מחיקת משתמש מהמערכת
     */
    public void deleteUser(String username) throws UserNotFoundException {
        User user = users.get(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        
        // מניעת מחיקת משתמש אדמין ברירת מחדל
        if (username.equals("admin") && user.getUserType().equals("ADMIN") && user.getEmployeeNumber().equals("ADMIN001")) {
            throw new IllegalArgumentException("Cannot delete default admin user");
        }
        
        // מניעת מחיקת משתמש superadmin
        if (username.equals("superadmin") && user.getUserType().equals("ADMIN") && user.getEmployeeNumber().equals("SUPER001")) {
            throw new IllegalArgumentException("Cannot delete superadmin user");
        }
        
        users.remove(username);
    }
}