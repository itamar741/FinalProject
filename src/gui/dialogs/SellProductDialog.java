package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Dialog for performing a product sale.
 * Calculates price based on customer type (polymorphism) and allows sale execution.
 * Employee number is automatically determined from the logged-in user.
 * For admin: allows selecting branch.
 * 
 * @author FinalProject
 */
public class SellProductDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String role;
    private String branchId;
    
    private JTextField productIdField;
    private JTextField quantityField;
    private JComboBox<String> branchCombo;  // Admin only
    private JTextField customerIdField;
    private JLabel priceLabel;
    private JButton calculateButton;
    private JButton sellButton;
    private JButton cancelButton;
    
    public SellProductDialog(MainWindow parent, ClientConnection connection, String role, String branchId) {
        super(parent, "ביצוע מכירה", true);
        this.connection = connection;
        this.mainWindow = parent;
        this.role = role;
        this.branchId = branchId;
        
        setSize(450, 350);
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
        
        // סניף (Admin יכול לבחור, Employee - קבוע)
        if ("admin".equals(role)) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("סניף:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            branchCombo = new JComboBox<>(new String[]{"B1", "B2"});
            branchCombo.setSelectedItem(branchId);
            mainPanel.add(branchCombo, gbc);
        } else {
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
        
        // ת.ז. לקוח
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("ת.ז. לקוח:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        customerIdField = new JTextField(20);
        mainPanel.add(customerIdField, gbc);
        
        // מחיר (יוצג אחרי חישוב)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("מחיר סופי:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        priceLabel = new JLabel("0.00 ₪");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(Color.BLUE);
        mainPanel.add(priceLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        calculateButton = new JButton("חשב מחיר");
        calculateButton.addActionListener(e -> calculatePrice());
        buttonPanel.add(calculateButton);
        
        sellButton = new JButton("ביצוע מכירה");
        sellButton.addActionListener(e -> performSale());
        buttonPanel.add(sellButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void calculatePrice() {
        String productId = productIdField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String customerId = customerIdField.getText().trim();
        
        // ולידציה
        if (productId.isEmpty() || quantityStr.isEmpty() || customerId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את קוד המוצר, כמות ות.ז. לקוח לחישוב המחיר",
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
            
            String command = "CALCULATE_PRICE;" + productId + ";" + quantity + ";" + customerId;
            String response = connection.sendCommand(command);
            
            if (response == null) {
                JOptionPane.showMessageDialog(this,
                        "אין תגובה מהשרת",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            response = response.trim();
            
            if (response.startsWith("OK;")) {
                String priceStr = response.substring(3);
                try {
                    double finalPrice = Double.parseDouble(priceStr);
                    priceLabel.setText(String.format("%.2f ₪", finalPrice));
                    priceLabel.setForeground(Color.BLUE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "תגובה לא תקינה מהשרת: " + priceStr,
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בחישוב מחיר:\n" + errorMsg,
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                priceLabel.setText("0.00 ₪");
                priceLabel.setForeground(Color.BLACK);
            } else {
                JOptionPane.showMessageDialog(this,
                        "תגובה לא מוכרת מהשרת: " + response,
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
    
    private void performSale() {
        String productId = productIdField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String selectedBranchId = "admin".equals(role) ? 
                (String) branchCombo.getSelectedItem() : branchId;
        String customerId = customerIdField.getText().trim();
        
        if (productId.isEmpty() || quantityStr.isEmpty() || customerId.isEmpty()) {
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
            
            // Employee number is automatically determined by the server from the logged-in user
            String command = "SELL;" + productId + ";" + quantity + ";" + selectedBranchId + ";" + customerId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("מכירה בוצעה בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בביצוע מכירה:\n" + errorMsg,
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
