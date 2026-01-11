package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג ליצירת משתמש חדש (Admin only)
 */
public class CreateUserDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField employeeNumberField;
    private JComboBox<String> userTypeCombo;
    private JComboBox<String> branchCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public CreateUserDialog(MainWindow parent, ClientConnection connection) {
        super(parent, "יצירת משתמש חדש", true);
        this.connection = connection;
        this.mainWindow = parent;
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // שם משתמש
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("שם משתמש:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);
        
        // סיסמה
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סיסמה:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);
        
        // מספר עובד
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("מספר עובד:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        employeeNumberField = new JTextField(20);
        mainPanel.add(employeeNumberField, gbc);
        
        // סוג משתמש
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סוג משתמש:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        userTypeCombo = new JComboBox<>(new String[]{"EMPLOYEE", "ADMIN"});
        mainPanel.add(userTypeCombo, gbc);
        
        // סניף
        gbc.gridx = 0;
        gbc.gridy = 4;
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
        saveButton.addActionListener(e -> createUser());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String employeeNumber = employeeNumberField.getText().trim();
        String userType = (String) userTypeCombo.getSelectedItem();
        String branchId = (String) branchCombo.getSelectedItem();
        
        if (username.isEmpty() || password.isEmpty() || employeeNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String command = "CREATE_USER;" + username + ";" + password + ";" + employeeNumber + ";" + userType + ";" + branchId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("משתמש נוצר בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה ביצירת משתמש:\n" + errorMsg,
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
