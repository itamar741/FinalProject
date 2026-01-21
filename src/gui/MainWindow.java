package gui;

import gui.tabs.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main application window with tabs for different functionalities.
 * Displays different tabs based on user role (admin, manager, salesman, cashier).
 * Admin users see additional tabs for employee management and logs.
 * 
 * @author FinalProject
 */
public class MainWindow extends JFrame {
    
    private ClientConnection connection;
    private String currentUsername;
    private String role;  // admin, manager, salesman, cashier
    private String branchId;
    
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    
    // Tabs
    private CustomersTab customersTab;
    private ProductsTab productsTab;
    private EmployeesManagementTab employeesTab;  // Admin only
    private ReportsTab reportsTab;
    private ChatTab chatTab;
    private LogsTab logsTab;  // Admin, Manager, Salesman
    
    /**
     * Constructs a new MainWindow for a logged-in user.
     * 
     * @param connection the ClientConnection to the server
     * @param username the logged-in username
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param branchId the branch ID where the user works
     */
    public MainWindow(ClientConnection connection, String username, String role, String branchId) {
        this.connection = connection;
        this.currentUsername = username;
        this.role = role;
        this.branchId = branchId;
        
        setTitle("מערכת ניהול סניפי בגדים - " + username + " (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        createUI();
    }
    
    /**
     * Creates the user interface with tabs and status bar.
     * Adds tabs based on user type - admin users get additional management tabs.
     */
    private void createUI() {
        setLayout(new BorderLayout());
        
        // יצירת טאבים
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // טאב 1: לקוחות (לא ל-cashier)
        if (!"cashier".equals(role)) {
            customersTab = new CustomersTab(connection, this);
            tabbedPane.addTab("לקוחות", customersTab);
        }
        
        // טאב 2: מוצרים ומלאי (לא ל-cashier)
        if (!"cashier".equals(role)) {
            productsTab = new ProductsTab(connection, this, role, branchId);
            tabbedPane.addTab("מוצרים ומלאי", productsTab);
        }
        
        // טאב 3: ניהול עובדים (Admin and Cashier)
        if ("admin".equals(role) || "cashier".equals(role)) {
            employeesTab = new EmployeesManagementTab(connection, this, role, branchId);
            tabbedPane.addTab("ניהול עובדים", employeesTab);
        }
        
        // טאב 4: לוגים (Admin, Manager, Salesman)
        if ("admin".equals(role) || "manager".equals(role) || "salesman".equals(role)) {
            logsTab = new LogsTab(connection, this);
            tabbedPane.addTab("לוגים", logsTab);
        }
        
        // טאב 5: דוחות (לא ל-cashier)
        if (!"cashier".equals(role)) {
            reportsTab = new ReportsTab(connection, this, role);
            tabbedPane.addTab("דוחות", reportsTab);
        }
        
        // טאב 6: צ'אט (כל המשתמשים)
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
     * Updates the status message in the status bar.
     * Thread-safe - uses SwingUtilities.invokeLater for EDT safety.
     * 
     * @param message the status message to display
     */
    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    /**
     * Updates the status message with a specific color.
     * Thread-safe - uses SwingUtilities.invokeLater for EDT safety.
     * 
     * @param message the status message to display
     * @param color the color for the status message
     */
    public void setStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }
    
    /**
     * Refreshes all tabs (after an operation).
     * Called to update UI after data changes.
     */
    public void refreshAllTabs() {
        if (customersTab != null) {
            customersTab.refresh();
        }
        if (productsTab != null) {
            productsTab.refresh();
        }
        if (employeesTab != null) {
            employeesTab.refresh();
        }
    }
    
    /**
     * Performs logout from the system.
     * Shows confirmation dialog, sends logout command, and opens LoginWindow.
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
    
    public String getRole() {
        return role;
    }
    
    public String getBranchId() {
        return branchId;
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
