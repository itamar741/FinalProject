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
 * טאב לניהול עובדים (Admin only)
 */
public class EmployeesManagementTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTable employeesTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton updateButton;
    private JButton activateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String> branchFilterCombo;
    
    public EmployeesManagementTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
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
        
        activateButton = new JButton("הפעל/השבת");
        activateButton.addActionListener(e -> toggleEmployeeActive());
        buttonPanel.add(activateButton);
        
        deleteButton = new JButton("מחק עובד");
        deleteButton.addActionListener(e -> deleteEmployee());
        buttonPanel.add(deleteButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // פילטר לפי סניף
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("סניף:"));
        branchFilterCombo = new JComboBox<>(new String[]{"כל הסניפים", "B1", "B2"});
        branchFilterCombo.addActionListener(e -> filterByBranch());
        filterPanel.add(branchFilterCombo);
        
        topPanel.add(filterPanel, BorderLayout.NORTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // טבלת עובדים
        String[] columns = {"מספר עובד", "שם מלא", "ת.ז.", "טלפון", "תפקיד", "סניף", "סטטוס"};
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
        CreateEmployeeDialog dialog = new CreateEmployeeDialog(mainWindow, connection);
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
    
    private void toggleEmployeeActive() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר עובד",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 6);
        boolean newStatus = !status.equals("active");
        
        try {
            String command = "SET_EMPLOYEE_ACTIVE;" + employeeNumber + ";" + newStatus;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("סטטוס העובד עודכן בהצלחה", Color.GREEN);
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
    
    private void filterByBranch() {
        String selectedBranch = (String) branchFilterCombo.getSelectedItem();
        if (selectedBranch == null || selectedBranch.equals("כל הסניפים")) {
            refresh();
            return;
        }
        
        try {
            String command = "LIST_EMPLOYEES_BY_BRANCH;" + selectedBranch;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                parseAndUpdateTable(response);
                mainWindow.setStatus("מוכן", Color.BLACK);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בתקשורת: " + e.getMessage(),
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
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
                String response = connection.sendCommand("LIST_EMPLOYEES");
                
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
        // פורמט: OK;employeeNumber:fullName:idNumber:phone:role:branchId:status|...
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
                    fields[4],  // role
                    fields[5],  // branchId
                    fields[6]   // status
                });
            }
        }
    }
}
