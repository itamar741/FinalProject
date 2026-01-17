package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.AddCustomerDialog;
import gui.dialogs.UpdateCustomerDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * טאב לניהול לקוחות
 */
public class CustomersTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    public CustomersTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול לקוחות", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("הוסף לקוח חדש");
        addButton.addActionListener(e -> showAddCustomerDialog());
        buttonPanel.add(addButton);
        
        updateButton = new JButton("ערוך");
        updateButton.addActionListener(e -> showUpdateCustomerDialog());
        buttonPanel.add(updateButton);
        
        deleteButton = new JButton("מחק");
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        buttonPanel.add(deleteButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // טבלת לקוחות
        String[] columns = {"ת.ז.", "שם מלא", "טלפון", "סוג לקוח"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        customersTable = new JTable(tableModel);
        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(customersTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showAddCustomerDialog() {
        AddCustomerDialog dialog = new AddCustomerDialog(mainWindow, connection);
        dialog.setVisible(true);
        // refresh() יקרא דרך mainWindow.refreshAllTabs() אחרי הוספה מוצלחת
    }
    
    private void showUpdateCustomerDialog() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר לקוח לעריכה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String fullName = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String customerType = (String) tableModel.getValueAt(selectedRow, 3);
        
        UpdateCustomerDialog dialog = new UpdateCustomerDialog(mainWindow, connection, idNumber, fullName, phone, customerType);
        dialog.setVisible(true);
        // refresh() יקרא דרך mainWindow.refreshAllTabs() אחרי עדכון מוצלח
    }
    
    private void deleteSelectedCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר לקוח למחיקה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String fullName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך למחוק את הלקוח " + fullName + " (ת.ז.: " + idNumber + ")?",
                "אישור מחיקה",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String command = "DELETE_CUSTOMER;" + idNumber;
                String response = connection.sendCommand(command);
                
                if (response == null) {
                    JOptionPane.showMessageDialog(this,
                            "אין תגובה מהשרת",
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                response = response.trim();
                
                if (response.startsWith("OK")) {
                    JOptionPane.showMessageDialog(this,
                            "לקוח נמחק בהצלחה!",
                            "הצלחה",
                            JOptionPane.INFORMATION_MESSAGE);
                    mainWindow.setStatus("לקוח נמחק בהצלחה", Color.GREEN);
                    mainWindow.refreshAllTabs();
                } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                    String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                    JOptionPane.showMessageDialog(this,
                            "שגיאה במחיקת לקוח:\n" + errorMsg,
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "תגובה לא מוכרת מהשרת:\n" + response,
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
     * רענון רשימת הלקוחות מהשרת
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען לקוחות...");
            
            try {
                String response = connection.sendCommand("LIST_CUSTOMERS");
                if (response != null) {
                    response = response.trim();
                    parseAndUpdateTable(response);
                }
                mainWindow.setStatus("מוכן", Color.BLACK);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בטעינת לקוחות:\n" + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                mainWindow.setStatus("שגיאה בטעינת לקוחות", Color.RED);
            }
        });
    }
    
    /**
     * פרסור תגובה מהשרת ועדכון הטבלה
     */
    private void parseAndUpdateTable(String response) {
        if (response == null) {
            System.err.println("CustomersTab: response is null");
            return;
        }
        
        // ניקוי רווחים
        response = response.trim();
        
        if (!response.startsWith("OK;")) {
            System.err.println("CustomersTab: response does not start with OK: " + response);
            return;
        }
        
        // הסרת "OK;"
        String data = response.substring(3);
        if (data.isEmpty()) {
            // אין לקוחות במערכת
            return;
        }
        
        // פיצול לפי "|" (זה המפריד בין לקוחות)
        String[] customers = data.split("\\|", -1);  // -1 כדי לשמור גם על ערכים ריקים בסוף
        
        int addedCount = 0;
        for (String customerStr : customers) {
            customerStr = customerStr.trim();
            if (customerStr.isEmpty()) {
                continue;
            }
            
            // פיצול לפי ":" (idNumber:fullName:phone:type)
            String[] parts = customerStr.split(":", -1);  // -1 כדי לשמור גם על ערכים ריקים
            if (parts.length >= 4) {
                String idNumber = parts[0];
                String fullName = parts[1];
                String phone = parts[2];
                String customerType = parts[3];
                
                tableModel.addRow(new Object[]{idNumber, fullName, phone, customerType});
                addedCount++;
            } else {
                System.err.println("CustomersTab: Invalid customer format: " + customerStr + " (parts.length=" + parts.length + ")");
            }
        }
        
        System.out.println("CustomersTab: Added " + addedCount + " customers to table");
    }
}
