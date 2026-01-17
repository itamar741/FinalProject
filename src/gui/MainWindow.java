package gui;

import gui.tabs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * חלון ראשי של האפליקציה עם טאבים
 */
public class MainWindow extends JFrame {
    
    private ClientConnection connection;
    private String currentUsername;
    private String userType;  // ADMIN או EMPLOYEE
    private String branchId;
    private String employeeNumber;
    
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    
    // טאבים
    private CustomersTab customersTab;
    private ProductsTab productsTab;
    private UsersManagementTab usersTab;  // Admin only
    private EmployeesManagementTab employeesTab;  // Admin only
    private ReportsTab reportsTab;
    private ChatTab chatTab;
    private LogsTab logsTab;  // Admin only
    
    public MainWindow(ClientConnection connection, String username, String userType, String branchId, String employeeNumber) {
        this.connection = connection;
        this.currentUsername = username;
        this.userType = userType;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        
        setTitle("מערכת ניהול סניפי בגדים - " + username + " (" + userType + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // יצירת טאבים
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // טאב 1: לקוחות (כל המשתמשים)
        customersTab = new CustomersTab(connection, this);
        tabbedPane.addTab("לקוחות", customersTab);
        
        // טאב 2: מוצרים ומלאי (כל המשתמשים)
        productsTab = new ProductsTab(connection, this, userType, branchId);
        tabbedPane.addTab("מוצרים ומלאי", productsTab);
        
        // טאב 3: ניהול משתמשים (Admin only)
        if (userType.equals("ADMIN")) {
            usersTab = new UsersManagementTab(connection, this);
            tabbedPane.addTab("ניהול משתמשים", usersTab);
        }
        
        // טאב 4: ניהול עובדים (Admin only)
        if (userType.equals("ADMIN")) {
            employeesTab = new EmployeesManagementTab(connection, this);
            tabbedPane.addTab("ניהול עובדים", employeesTab);
        }
        
        // טאב 5: לוגים (Admin only)
        if (userType.equals("ADMIN")) {
            logsTab = new LogsTab(connection, this);
            tabbedPane.addTab("לוגים", logsTab);
        }
        
        // טאב 6: דוחות (כל המשתמשים, אבל עם תוכן שונה)
        reportsTab = new ReportsTab(connection, this, userType);
        tabbedPane.addTab("דוחות", reportsTab);
        
        // טאב 7: צ'אט (כל המשתמשים)
        chatTab = new ChatTab(connection, this);
        tabbedPane.addTab("צ'אט", chatTab);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status Bar + כפתור התנתק
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("מוכן");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        JButton logoutButton = new JButton("התנתק");
        logoutButton.addActionListener(e -> performLogout());
        statusPanel.add(logoutButton, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * עדכון הודעת סטטוס
     */
    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    /**
     * עדכון הודעת סטטוס עם צבע
     */
    public void setStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }
    
    /**
     * רענון כל הטאבים (לאחר פעולה)
     */
    public void refreshAllTabs() {
        if (customersTab != null) {
            customersTab.refresh();
        }
        if (productsTab != null) {
            productsTab.refresh();
        }
        if (usersTab != null) {
            usersTab.refresh();
        }
        if (employeesTab != null) {
            employeesTab.refresh();
        }
    }
    
    /**
     * התנתקות מהמערכת
     */
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך להתנתק?",
                "אישור התנתקות",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                connection.logout();
            } catch (IOException e) {
                // ignore
            }
            connection.disconnect();
            
            // פתיחת חלון התחברות מחדש
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
                this.dispose();
            });
        }
    }
    
    // Getters
    public ClientConnection getConnection() {
        return connection;
    }
    
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public String getBranchId() {
        return branchId;
    }
    
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    
    @Override
    public void dispose() {
        if (connection != null && connection.isConnected()) {
            try {
                connection.logout();
            } catch (IOException e) {
                // ignore
            }
            connection.disconnect();
        }
        super.dispose();
    }
}
