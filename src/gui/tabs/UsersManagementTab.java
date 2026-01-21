package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.CreateUserDialog;
import gui.dialogs.UpdateUserDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

/**
 * Tab for managing system users (admin only).
 * Displays users in a table and provides CRUD operations.
 * Allows creating, updating, activating/deactivating, and deleting users.
 * 
 * @author FinalProject
 */
public class UsersManagementTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton updateButton;
    private JButton activateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    /**
     * Constructs a new UsersManagementTab (admin only).
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     */
    public UsersManagementTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול משתמשים", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        createButton = new JButton("צור משתמש חדש");
        createButton.addActionListener(e -> showCreateUserDialog());
        buttonPanel.add(createButton);
        
        updateButton = new JButton("עדכן משתמש");
        updateButton.addActionListener(e -> showUpdateUserDialog());
        buttonPanel.add(updateButton);
        
        activateButton = new JButton("הפעל/השבת");
        activateButton.addActionListener(e -> toggleUserActive());
        buttonPanel.add(activateButton);
        
        deleteButton = new JButton("מחק משתמש");
        deleteButton.addActionListener(e -> deleteUser());
        buttonPanel.add(deleteButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // טבלת משתמשים
        String[] columns = {"שם משתמש", "תפקיד", "סניף", "סטטוס"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showCreateUserDialog() {
        CreateUserDialog dialog = new CreateUserDialog(mainWindow, connection);
        dialog.setVisible(true);
        refresh();
    }
    
    private void showUpdateUserDialog() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר משתמש לעדכון",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        UpdateUserDialog dialog = new UpdateUserDialog(mainWindow, connection, username);
        dialog.setVisible(true);
        refresh();
    }
    
    private void toggleUserActive() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר משתמש",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);
        boolean newStatus = !status.equals("active");
        
        try {
            String command = "SET_USER_ACTIVE;" + username + ";" + newStatus;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("סטטוס המשתמש עודכן בהצלחה", Color.GREEN);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בעדכון סטטוס: " + response,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בתקשורת: " + e.getMessage(),
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר משתמש למחיקה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        
        if (username.equals(mainWindow.getCurrentUsername())) {
            JOptionPane.showMessageDialog(this,
                    "אינך יכול למחוק את המשתמש המחובר",
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך למחוק את המשתמש " + username + "?",
                "אישור מחיקה",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String command = "DELETE_USER;" + username;
                String response = connection.sendCommand(command);
                
                if (response.startsWith("OK")) {
                    mainWindow.setStatus("משתמש נמחק בהצלחה", Color.GREEN);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה במחיקה: " + response,
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בתקשורת: " + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * רענון רשימת המשתמשים
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען משתמשים...");
            
            try {
                String response = connection.sendCommand("LIST_USERS");
                
                if (response.startsWith("OK")) {
                    parseAndUpdateTable(response);
                    mainWindow.setStatus("מוכן", Color.BLACK);
                } else {
                    mainWindow.setStatus("שגיאה בטעינת משתמשים", Color.RED);
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בטעינת משתמשים: " + response,
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                mainWindow.setStatus("שגיאה בתקשורת", Color.RED);
                JOptionPane.showMessageDialog(this,
                        "שגיאה בתקשורת: " + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void parseAndUpdateTable(String response) {
        // פורמט: OK;username:role:branchId:status|username2:role2:...
        String[] parts = response.split(";");
        if (parts.length < 2) return;
        
        String data = parts[1];
        if (data.isEmpty()) return;
        
        String[] users = data.split("\\|");
        for (String user : users) {
            if (user.isEmpty()) continue;
            String[] fields = user.split(":");
            if (fields.length >= 4) {
                tableModel.addRow(new Object[]{
                    fields[0],  // username
                    fields[1],  // role
                    fields[2],  // branchId
                    fields[3]   // status
                });
            }
        }
    }
}
