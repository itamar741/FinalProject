package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Dialog for updating an existing employee (admin only).
 * Allows changing employee details including name, phone, bank account, role, and branch.
 * 
 * @author FinalProject
 */
public class UpdateEmployeeDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String employeeNumber;
    
    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField bankAccountField;
    private JComboBox<String> roleCombo;
    private JComboBox<String> branchCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public UpdateEmployeeDialog(MainWindow parent, ClientConnection connection, String employeeNumber) {
        super(parent, "עדכון עובד: " + employeeNumber, true);
        this.connection = connection;
        this.mainWindow = parent;
        this.employeeNumber = employeeNumber;
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        createUI();
        loadEmployeeData();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // מספר עובד (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("מספר עובד:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JLabel empNumLabel = new JLabel(employeeNumber);
        empNumLabel.setForeground(Color.GRAY);
        mainPanel.add(empNumLabel, gbc);
        
        // שם מלא
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("שם מלא:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fullNameField = new JTextField(20);
        mainPanel.add(fullNameField, gbc);
        
        // טלפון
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("טלפון:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        phoneField = new JTextField(20);
        mainPanel.add(phoneField, gbc);
        
        // חשבון בנק
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("חשבון בנק:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        bankAccountField = new JTextField(20);
        mainPanel.add(bankAccountField, gbc);
        
        // תפקיד
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("תפקיד:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        roleCombo = new JComboBox<>(new String[]{"manager", "salesman", "cashier"});
        mainPanel.add(roleCombo, gbc);
        
        // סניף
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סניף:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        branchCombo = new JComboBox<>(new String[]{"B1", "B2"});
        mainPanel.add(branchCombo, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור");
        saveButton.addActionListener(e -> updateEmployee());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadEmployeeData() {
        try {
            String command = "GET_EMPLOYEE;" + employeeNumber;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                if (parts.length >= 2) {
                    String[] empData = parts[1].split(":");
                    if (empData.length >= 8) {
                        fullNameField.setText(empData[1]);  // fullName
                        phoneField.setText(empData[3]);  // phone
                        bankAccountField.setText(empData[4]);  // bankAccount
                        // Set role in combo box if it exists in the list
                        String role = empData[5];
                        roleCombo.setSelectedItem(role);
                        // If role is not in the list (e.g., "admin"), select first item
                        if (roleCombo.getSelectedItem() == null || !roleCombo.getSelectedItem().equals(role)) {
                            roleCombo.setSelectedIndex(0);
                        }
                        branchCombo.setSelectedItem(empData[6]);  // branchId
                    }
                }
            }
        } catch (IOException e) {
            // אם נכשל, נשאיר את השדות ריקים
        }
    }
    
    private void updateEmployee() {
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String bankAccount = bankAccountField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        String branchId = (String) branchCombo.getSelectedItem();
        
        if (fullName.isEmpty() || phone.isEmpty() || bankAccount.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String command = "UPDATE_EMPLOYEE;" + employeeNumber + ";" + fullName + ";" + 
                           phone + ";" + bankAccount + ";" + role + ";" + branchId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("עובד עודכן בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בעדכון עובד:\n" + errorMsg,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "תגובה לא מוכרת: " + response,
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
