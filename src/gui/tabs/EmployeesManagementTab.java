package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.CreateEmployeeDialog;
import gui.dialogs.UpdateEmployeeDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

/**
 * Tab for managing employees (admin and cashier).
 * Displays employees in a table and provides CRUD operations.
 * Admin can view all branches.
 * Cashier can only view employees from their own branch.
 * 
 * @author FinalProject
 */
public class EmployeesManagementTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String role;
    private String branchId;
    
    private JTable employeesTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    /**
     * Constructs a new EmployeesManagementTab.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     * @param role the user's role (admin or cashier)
     * @param branchId the user's branch ID (null for admin)
     */
    public EmployeesManagementTab(ClientConnection connection, MainWindow mainWindow, String role, String branchId) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.role = role;
        this.branchId = branchId;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול עובדים", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים ופילטרים
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        createButton = new JButton("צור עובד חדש");
        createButton.addActionListener(e -> showCreateEmployeeDialog());
        buttonPanel.add(createButton);
        
        updateButton = new JButton("עדכן עובד");
        updateButton.addActionListener(e -> showUpdateEmployeeDialog());
        buttonPanel.add(updateButton);
        
        deleteButton = new JButton("מחק עובד");
        deleteButton.addActionListener(e -> deleteEmployee());
        buttonPanel.add(deleteButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // הסתר כפתורי ניהול מ-cashier
        if ("cashier".equals(role)) {
            updateButton.setVisible(false);
            deleteButton.setVisible(false);
        }
        
        add(topPanel, BorderLayout.NORTH);
        
        // טבלת עובדים
        String[] columns = {"מספר עובד", "שם מלא", "ת.ז.", "טלפון", "חשבון בנק", "תפקיד", "סניף"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        employeesTable = new JTable(tableModel);
        employeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(employeesTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showCreateEmployeeDialog() {
        CreateEmployeeDialog dialog = new CreateEmployeeDialog(mainWindow, connection, role, branchId);
        dialog.setVisible(true);
        refresh();
    }
    
    private void showUpdateEmployeeDialog() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר עובד לעדכון",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
        UpdateEmployeeDialog dialog = new UpdateEmployeeDialog(mainWindow, connection, employeeNumber);
        dialog.setVisible(true);
        refresh();
    }
    
    private void deleteEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר עובד למחיקה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך למחוק את העובד " + employeeName + " (" + employeeNumber + ")?",
                "אישור מחיקה",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String command = "DELETE_EMPLOYEE;" + employeeNumber;
                String response = connection.sendCommand(command);
                
                if (response.startsWith("OK")) {
                    mainWindow.setStatus("עובד נמחק בהצלחה", Color.GREEN);
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
     * רענון רשימת העובדים
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען עובדים...");
            
            try {
                String response;
                // Cashier רואה רק את הסניף שלו, Admin רואה הכל
                if ("cashier".equals(role) && branchId != null) {
                    response = connection.sendCommand("LIST_EMPLOYEES_BY_BRANCH;" + branchId);
                } else {
                    response = connection.sendCommand("LIST_EMPLOYEES");
                }
                
                if (response.startsWith("OK")) {
                    parseAndUpdateTable(response);
                    mainWindow.setStatus("מוכן", Color.BLACK);
                } else {
                    mainWindow.setStatus("שגיאה בטעינת עובדים", Color.RED);
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בטעינת עובדים: " + response,
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
        // פורמט: OK;employeeNumber:fullName:idNumber:phone:bankAccount:role:branchId|...
        String[] parts = response.split(";");
        if (parts.length < 2) return;
        
        String data = parts[1];
        if (data.isEmpty()) return;
        
        String[] employees = data.split("\\|");
        for (String employee : employees) {
            if (employee.isEmpty()) continue;
            String[] fields = employee.split(":");
            if (fields.length >= 7) {
                tableModel.addRow(new Object[]{
                    fields[0],  // employeeNumber
                    fields[1],  // fullName
                    fields[2],  // idNumber
                    fields[3],  // phone
                    fields[4],  // bankAccount
                    fields[5],  // role
                    fields[6]   // branchId
                });
            }
        }
    }
}
