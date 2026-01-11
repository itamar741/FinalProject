package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * דיאלוג לביצוע מכירה
 */
public class SellProductDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    private String branchId;
    private String employeeNumber;
    
    private JTextField productIdField;
    private JTextField quantityField;
    private JComboBox<String> branchCombo;  // Admin only
    private JTextField employeeNumberField;  // Admin only
    private JTextField customerIdField;
    private JLabel priceLabel;
    private JButton calculateButton;
    private JButton sellButton;
    private JButton cancelButton;
    
    public SellProductDialog(MainWindow parent, ClientConnection connection, String userType, String branchId, String employeeNumber) {
        super(parent, "ביצוע מכירה", true);
        this.connection = connection;
        this.mainWindow = parent;
        this.userType = userType;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        
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
        
        // מספר עובד (Admin יכול לבחור, Employee - קבוע)
        if (userType.equals("ADMIN")) {
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("מספר עובד:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            employeeNumberField = new JTextField(20);
            mainPanel.add(employeeNumberField, gbc);
        } else {
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("מספר עובד:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            // Employee number יועבר מהשרת, נציג פה placeholder
            JLabel empLabel = new JLabel("(מספר עובד שלך)");
            empLabel.setForeground(Color.GRAY);
            mainPanel.add(empLabel, gbc);
        }
        
        // ת.ז. לקוח
        gbc.gridx = 0;
        gbc.gridy = 4;
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
        gbc.gridy = 5;
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
        // TODO: חישוב מחיר לפי סוג לקוח
        // נצטרך להוסיף פקודה GET_PRODUCT_PRICE או לחשב באופן מקומי
        // בינתיים, נציג הודעה
        JOptionPane.showMessageDialog(this,
                "חישוב המחיר יוטמע בעתיד.\n" +
                "המחיר הסופי יחושב לפי סוג הלקוח בעת ביצוע המכירה.",
                "מידע",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void performSale() {
        String productId = productIdField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String selectedBranchId = userType.equals("ADMIN") ? 
                (String) branchCombo.getSelectedItem() : branchId;
        String empNum = userType.equals("ADMIN") ? 
                employeeNumberField.getText().trim() : this.employeeNumber;
        String customerId = customerIdField.getText().trim();
        
        if (productId.isEmpty() || quantityStr.isEmpty() || customerId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (empNum == null || empNum.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "מספר עובד לא זמין",
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
            
            String command = "SELL;" + productId + ";" + quantity + ";" + selectedBranchId + ";" + empNum + ";" + customerId;
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
