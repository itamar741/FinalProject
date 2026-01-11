package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג לעדכון עובד (Admin only)
 */
public class UpdateEmployeeDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String employeeNumber;
    
    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField bankAccountField;
    private JTextField roleField;
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
        roleField = new JTextField(20);
        mainPanel.add(roleField, gbc);
        
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
                        roleField.setText(empData[5]);  // role
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
        String role = roleField.getText().trim();
        String branchId = (String) branchCombo.getSelectedItem();
        
        if (fullName.isEmpty() || phone.isEmpty() || bankAccount.isEmpty() || role.isEmpty()) {
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
