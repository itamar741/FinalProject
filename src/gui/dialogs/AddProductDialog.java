package gui.dialogs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Dialog for adding a new product to the catalog (admin only).
 * Creates a new product and optionally adds it to inventory.
 * 
 * @author FinalProject
 */
public class AddProductDialog extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JTextField productIdField;
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField priceField;
    private JButton saveButton;
    private JButton cancelButton;
    
    public AddProductDialog(MainWindow parent, ClientConnection connection) {
        super(parent, "הוספת מוצר לקטלוג", true);
        this.connection = connection;
        this.mainWindow = parent;
        
        setSize(400, 250);
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
        
        // שם
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("שם:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);
        
        // קטגוריה
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("קטגוריה:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        categoryField = new JTextField(20);
        mainPanel.add(categoryField, gbc);
        
        // מחיר
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("מחיר:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        priceField = new JTextField(20);
        mainPanel.add(priceField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("שמור");
        saveButton.addActionListener(e -> saveProduct());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("ביטול");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void saveProduct() {
        String productId = productIdField.getText().trim();
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String priceStr = priceField.getText().trim();
        
        if (productId.isEmpty() || name.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                JOptionPane.showMessageDialog(this,
                        "מחיר לא יכול להיות שלילי",
                        "שגיאה",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String command = "ADD_PRODUCT_TO_CATALOG;" + productId + ";" + name + ";" + category + ";" + price;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("מוצר נוסף לקטלוג בהצלחה", Color.GREEN);
                mainWindow.refreshAllTabs();
                dispose();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בהוספת מוצר:\n" + errorMsg,
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
                    "מחיר חייב להיות מספר",
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
