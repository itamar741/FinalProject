package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג ליצירת עובד חדש (Admin only)
 */
public class CreateEmployeeDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTextField fullNameField;
    private JTextField idNumberField;
    private JTextField phoneField;
    private JTextField bankAccountField;
    private JTextField employeeNumberField;
    private JTextField roleField;
    private JComboBox<String> branchCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public CreateEmployeeDialog(MainWindow parent, ClientConnection connection) {
        super(parent, "יצירת עובד חדש", true);
        this.connection = connection;
        this.mainWindow = parent;
        
        setSize(450, 400);
        setLocationRelativeTo(parent);
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // שם מלא
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("שם מלא:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fullNameField = new JTextField(20);
        mainPanel.add(fullNameField, gbc);
        
        // ת.ז.
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("ת.ז.:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        idNumberField = new JTextField(20);
        mainPanel.add(idNumberField, gbc);
        
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
        
        // מספר עובד
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("מספר עובד:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        employeeNumberField = new JTextField(20);
        mainPanel.add(employeeNumberField, gbc);
        
        // תפקיד
        gbc.gridx = 0;
        gbc.gridy = 5;
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
        gbc.gridy = 6;
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
        saveButton.addActionListener(e -> createEmployee());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createEmployee() {
        String fullName = fullNameField.getText().trim();
        String idNumber = idNumberField.getText().trim();
        String phone = phoneField.getText().trim();
        String bankAccount = bankAccountField.getText().trim();
        String employeeNumber = employeeNumberField.getText().trim();
        String role = roleField.getText().trim();
        String branchId = (String) branchCombo.getSelectedItem();
        
        if (fullName.isEmpty() || idNumber.isEmpty() || phone.isEmpty() || 
            bankAccount.isEmpty() || employeeNumber.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String command = "CREATE_EMPLOYEE;" + fullName + ";" + idNumber + ";" + phone + 
                           ";" + bankAccount + ";" + employeeNumber + ";" + role + ";" + branchId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("עובד נוצר בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה ביצירת עובד:\n" + errorMsg,
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
