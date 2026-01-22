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
 * Tab for managing customers.
 * Contains two sub-tabs: one for customer management and one for discount settings.
 * 
 * @author FinalProject
 */
public class CustomersTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private JTabbedPane tabbedPane;
    private CustomersManagementTab customersManagementTab;
    private DiscountSettingsTab discountSettingsTab;
    
    /**
     * Constructs a new CustomersTab with sub-tabs.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     */
    public CustomersTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
    }
    
    private void createUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // טאב 1: ניהול לקוחות
        customersManagementTab = new CustomersManagementTab(connection, mainWindow);
        tabbedPane.addTab("ניהול לקוחות", customersManagementTab);
        
        // טאב 2: הגדרות הנחות
        discountSettingsTab = new DiscountSettingsTab(connection, mainWindow);
        tabbedPane.addTab("הגדרות הנחות", discountSettingsTab);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * רענון רשימת הלקוחות מהשרת
     */
    public void refresh() {
        if (customersManagementTab != null) {
            customersManagementTab.refresh();
        }
    }
}

/**
 * Sub-tab for managing customers.
 * Displays customers in a table and provides CRUD operations (Create, Read, Update, Delete).
 * 
 * @author FinalProject
 */
class CustomersManagementTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private Timer refreshTimer;
    
    /**
     * Constructs a new CustomersManagementTab.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     */
    public CustomersManagementTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
        startAutoRefresh();
    }
    
    /**
     * Starts auto-refresh timer to keep UI updated.
     */
    private void startAutoRefresh() {
        // רענון אוטומטי כל 1000ms (שנייה אחת)
        refreshTimer = new Timer(1000, e -> refresh());
        refreshTimer.start();
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
            return;
        }
        
        // ניקוי רווחים
        response = response.trim();
        
        if (!response.startsWith("OK;")) {
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
            }
        }
    }
}

/**
 * Sub-tab for managing customer discount percentages.
 * Allows users to set discount percentages for NEW, RETURNING, and VIP customers.
 * 
 * @author FinalProject
 */
class DiscountSettingsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private JTextField newCustomerDiscountField;
    private JTextField returningCustomerDiscountField;
    private JTextField vipCustomerDiscountField;
    private JButton saveButton;
    private Timer refreshTimer;
    
    /**
     * Constructs a new DiscountSettingsTab.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     */
    public DiscountSettingsTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
        startAutoRefresh();
    }
    
    /**
     * Starts auto-refresh timer to keep UI updated.
     */
    private void startAutoRefresh() {
        // רענון אוטומטי כל 1000ms (שנייה אחת)
        refreshTimer = new Timer(1000, e -> refresh());
        refreshTimer.start();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("הגדרות הנחות ללקוחות", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel מרכזי עם שדות
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        
        // לקוח חדש
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("לקוח חדש (%):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        newCustomerDiscountField = new JTextField(10);
        mainPanel.add(newCustomerDiscountField, gbc);
        
        // לקוח חוזר
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("לקוח חוזר (%):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        returningCustomerDiscountField = new JTextField(10);
        mainPanel.add(returningCustomerDiscountField, gbc);
        
        // לקוח VIP
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("לקוח VIP (%):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        vipCustomerDiscountField = new JTextField(10);
        mainPanel.add(vipCustomerDiscountField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור שינויים");
        saveButton.addActionListener(e -> saveDiscounts());
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void saveDiscounts() {
        try {
            double newDiscount = Double.parseDouble(newCustomerDiscountField.getText().trim());
            double returningDiscount = Double.parseDouble(returningCustomerDiscountField.getText().trim());
            double vipDiscount = Double.parseDouble(vipCustomerDiscountField.getText().trim());
            
            // Validation
            if (newDiscount < 0 || newDiscount > 100 ||
                returningDiscount < 0 || returningDiscount > 100 ||
                vipDiscount < 0 || vipDiscount > 100) {
                JOptionPane.showMessageDialog(this,
                        "אחוז ההנחה חייב להיות בין 0 ל-100",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Send commands to server
            String command1 = "SET_DISCOUNT;NEW;" + newDiscount;
            String command2 = "SET_DISCOUNT;RETURNING;" + returningDiscount;
            String command3 = "SET_DISCOUNT;VIP;" + vipDiscount;
            
            String response1 = connection.sendCommand(command1);
            String response2 = connection.sendCommand(command2);
            String response3 = connection.sendCommand(command3);
            
            if (response1 != null && response1.startsWith("OK") &&
                response2 != null && response2.startsWith("OK") &&
                response3 != null && response3.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "ההנחות עודכנו בהצלחה!",
                        "הצלחה",
                        JOptionPane.INFORMATION_MESSAGE);
                mainWindow.setStatus("ההנחות עודכנו בהצלחה", Color.GREEN);
            } else {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בעדכון ההנחות",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "אנא הזן מספרים תקינים",
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בתקשורת: " + e.getMessage(),
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * רענון אחוזי ההנחה מהשרת
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            mainWindow.setStatus("טוען הגדרות הנחות...");
            
            try {
                String response = connection.sendCommand("GET_DISCOUNTS");
                if (response != null && response.startsWith("OK;")) {
                    parseAndUpdateFields(response);
                    mainWindow.setStatus("מוכן", Color.BLACK);
                } else {
                    mainWindow.setStatus("שגיאה בטעינת הגדרות הנחות", Color.RED);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בטעינת הגדרות הנחות:\n" + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                mainWindow.setStatus("שגיאה בטעינת הגדרות הנחות", Color.RED);
            }
        });
    }
    
    private void parseAndUpdateFields(String response) {
        // פורמט: OK;NEW:0.0;RETURNING:5.0;VIP:15.0
        String data = response.substring(3); // הסרת "OK;"
        String[] parts = data.split(";");
        
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                String customerType = keyValue[0];
                double discount = Double.parseDouble(keyValue[1]);
                
                switch (customerType) {
                    case "NEW":
                        newCustomerDiscountField.setText(String.valueOf(discount));
                        break;
                    case "RETURNING":
                        returningCustomerDiscountField.setText(String.valueOf(discount));
                        break;
                    case "VIP":
                        vipCustomerDiscountField.setText(String.valueOf(discount));
                        break;
                }
            }
        }
    }
}
