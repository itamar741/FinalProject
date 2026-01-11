package model;

import java.net.Socket;

public class Session {
    private String username;
    private String employeeNumber;
    private String branchId;
    private String userType;  // שונה מ-role ל-userType (ADMIN/EMPLOYEE)
    private Socket socket;
    private long loginTime;

    public Session(String username, 
                   String employeeNumber, 
                   String branchId, 
                   String userType,  // שונה
                   Socket socket) {
        this.username = username;
        this.employeeNumber = employeeNumber;
        this.branchId = branchId;
        this.userType = userType;
        this.socket = socket;
        this.loginTime = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getUserType() {  // שונה מ-getRole
        return userType;
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