package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * דיאלוג להוספת לקוח חדש
 */
public class AddCustomerDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTextField fullNameField;
    private JTextField idNumberField;
    private JTextField phoneField;
    private JComboBox<String> customerTypeCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public AddCustomerDialog(MainWindow parent, ClientConnection connection) {
        super(parent, "הוספת לקוח חדש", true);
        this.connection = connection;
        this.mainWindow = parent;
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // Panel מרכזי
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
        
        // סוג לקוח
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סוג לקוח:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        customerTypeCombo = new JComboBox<>(new String[]{"NEW", "RETURNING", "VIP"});
        mainPanel.add(customerTypeCombo, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור");
        saveButton.addActionListener(e -> saveCustomer());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // הוספת padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void saveCustomer() {
        String fullName = fullNameField.getText().trim();
        String idNumber = idNumberField.getText().trim();
        String phone = phoneField.getText().trim();
        String customerType = (String) customerTypeCombo.getSelectedItem();
        
        // ולידציה
        if (fullName.isEmpty() || idNumber.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String command = "ADD_CUSTOMER;" + fullName + ";" + idNumber + ";" + phone + ";" + customerType;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("לקוח נוסף בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בהוספת לקוח:\n" + errorMsg,
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
