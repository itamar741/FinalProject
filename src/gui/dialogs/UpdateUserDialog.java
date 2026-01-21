package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Dialog for updating an existing user (admin only).
 * Allows changing password, branch, and active status.
 * 
 * @author FinalProject
 */
public class UpdateUserDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String username;
    
    private JPasswordField passwordField;
    private JComboBox<String> branchCombo;
    private JCheckBox activeCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    public UpdateUserDialog(MainWindow parent, ClientConnection connection, String username) {
        super(parent, "עדכון משתמש: " + username, true);
        this.connection = connection;
        this.mainWindow = parent;
        this.username = username;
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
        createUI();
        loadUserData();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // שם משתמש (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("שם משתמש:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setForeground(Color.GRAY);
        mainPanel.add(usernameLabel, gbc);
        
        // סיסמה חדשה (אופציונלי)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סיסמה חדשה (אופציונלי):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);
        
        // סניף
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סניף:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        branchCombo = new JComboBox<>(new String[]{"B1", "B2"});
        mainPanel.add(branchCombo, gbc);
        
        // פעיל
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("סטטוס:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        activeCheckBox = new JCheckBox("פעיל");
        mainPanel.add(activeCheckBox, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור");
        saveButton.addActionListener(e -> updateUser());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadUserData() {
        try {
            String command = "GET_USER;" + username;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                if (parts.length >= 2) {
                    String[] userData = parts[1].split(":");
                    if (userData.length >= 5) {
                        String branchId = userData[3];
                        branchCombo.setSelectedItem(branchId);
                        activeCheckBox.setSelected(userData[4].equals("active"));
                    }
                }
            }
        } catch (IOException e) {
            // אם נכשל, נשאיר את ברירות המחדל
        }
    }
    
    private void updateUser() {
        String newPassword = new String(passwordField.getPassword()).trim();
        String newBranchId = (String) branchCombo.getSelectedItem();
        boolean active = activeCheckBox.isSelected();
        
        try {
            // אם הסיסמה ריקה, לא נשלח אותה
            String command;
            if (newPassword.isEmpty()) {
                command = "UPDATE_USER;" + username + ";;" + newBranchId + ";" + active;
            } else {
                command = "UPDATE_USER;" + username + ";" + newPassword + ";" + newBranchId + ";" + active;
            }
            
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("משתמש עודכן בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בעדכון משתמש:\n" + errorMsg,
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
