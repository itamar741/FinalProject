package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג לעדכון לקוח
 */
public class UpdateCustomerDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String idNumber;
    
    private JTextField fullNameField;
    private JLabel idNumberLabel;
    private JTextField phoneField;
    private JComboBox<String> customerTypeCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public UpdateCustomerDialog(MainWindow parent, ClientConnection connection, String idNumber, String fullName, String phone, String customerType) {
        super(parent, "עדכון לקוח: " + idNumber, true);
        this.connection = connection;
        this.mainWindow = parent;
        this.idNumber = idNumber;
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
        createUI();
        
        // טעינת הנתונים הקיימים
        fullNameField.setText(fullName);
        phoneField.setText(phone);
        customerTypeCombo.setSelectedItem(customerType);
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // Panel מרכזי
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // ת.ז. (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("ת.ז.:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        idNumberLabel = new JLabel(idNumber);
        idNumberLabel.setForeground(Color.GRAY);
        mainPanel.add(idNumberLabel, gbc);
        
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
        saveButton.addActionListener(e -> updateCustomer());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // הוספת padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void updateCustomer() {
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String customerType = (String) customerTypeCombo.getSelectedItem();
        
        // ולידציה
        if (fullName.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String command = "UPDATE_CUSTOMER;" + idNumber + ";" + fullName + ";" + phone + ";" + customerType;
            String response = connection.sendCommand(command);
            
            if (response == null) {
                JOptionPane.showMessageDialog(this,
                        "אין תגובה מהשרת",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // ניקוי רווחים ותווים מיותרים
            response = response.trim();
            
            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "לקוח עודכן בהצלחה!",
                        "הצלחה",
                        JOptionPane.INFORMATION_MESSAGE);
                mainWindow.setStatus("לקוח עודכן בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בעדכון לקוח:\n" + errorMsg,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                // אם התגובה לא מזוהה, נציג הודעה עם התגובה בפועל
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
