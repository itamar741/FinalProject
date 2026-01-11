package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג להוספת מוצר למלאי
 */
public class AddToInventoryDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    private String branchId;
    
    private JTextField productIdField;
    private JTextField quantityField;
    private JComboBox<String> branchCombo;
    private JButton saveButton;
    private JButton cancelButton;
    
    public AddToInventoryDialog(MainWindow parent, ClientConnection connection, String userType, String branchId) {
        super(parent, "הוספת מוצר למלאי", true);
        this.connection = connection;
        this.mainWindow = parent;
        this.userType = userType;
        this.branchId = branchId;
        
        setSize(400, 200);
        setLocationRelativeTo(parent);
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // קוד מוצר
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("קוד מוצר:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        productIdField = new JTextField(20);
        mainPanel.add(productIdField, gbc);
        
        // כמות
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("כמות:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        quantityField = new JTextField(20);
        mainPanel.add(quantityField, gbc);
        
        // סניף (רק אם Admin)
        if (userType.equals("ADMIN")) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("סניף:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            branchCombo = new JComboBox<>(new String[]{"B1", "B2"});
            branchCombo.setSelectedItem(branchId); // ברירת מחדל - הסניף של המשתמש
            mainPanel.add(branchCombo, gbc);
        } else {
            // Employee - הסניף קבוע
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("סניף:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JLabel branchLabel = new JLabel(branchId);
            branchLabel.setForeground(Color.GRAY);
            mainPanel.add(branchLabel, gbc);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור");
        saveButton.addActionListener(e -> saveToInventory());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void saveToInventory() {
        String productId = productIdField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String selectedBranchId = userType.equals("ADMIN") ? 
                (String) branchCombo.getSelectedItem() : branchId;
        
        if (productId.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this,
                        "כמות חייבת להיות גדולה מ-0",
                        "שגיאה",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String command = "ADD_PRODUCT_TO_INVENTORY;" + productId + ";" + quantity + ";" + selectedBranchId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("מוצר נוסף למלאי בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בהוספה למלאי:\n" + errorMsg,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "תגובה לא מוכרת: " + response,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "כמות חייבת להיות מספר",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בתקשורת: " + e.getMessage(),
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
