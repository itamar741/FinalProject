package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.AddProductDialog;
import gui.dialogs.AddToInventoryDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * טאב לניהול מוצרים ומלאי
 */
public class ProductsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    private String branchId;
    
    private JTable productsTable;
    private DefaultTableModel tableModel;
    private JButton addProductButton;  // Admin only
    private JButton addToInventoryButton;
    private JButton refreshButton;
    
    public ProductsTab(ClientConnection connection, MainWindow mainWindow, String userType, String branchId) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.userType = userType;
        this.branchId = branchId;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול מוצרים ומלאי", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // כפתור הוספת מוצר לקטלוג (Admin only)
        if (userType.equals("ADMIN")) {
            addProductButton = new JButton("הוסף מוצר לקטלוג");
            addProductButton.addActionListener(e -> showAddProductDialog());
            buttonPanel.add(addProductButton);
        }
        
        // כפתור הוספה למלאי
        addToInventoryButton = new JButton("הוסף למלאי");
        addToInventoryButton.addActionListener(e -> showAddToInventoryDialog());
        buttonPanel.add(addToInventoryButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // טבלת מוצרים
        String[] columns = {"קוד מוצר", "שם", "קטגוריה", "מחיר", "סטטוס", "כמות במלאי"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(productsTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showAddProductDialog() {
        AddProductDialog dialog = new AddProductDialog(mainWindow, connection);
        dialog.setVisible(true);
        refresh();
    }
    
    private void showAddToInventoryDialog() {
        AddToInventoryDialog dialog = new AddToInventoryDialog(mainWindow, connection, userType, branchId);
        dialog.setVisible(true);
        refresh();
    }
    
    /**
     * רענון רשימת המוצרים והמלאי
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען מוצרים...");
            
            try {
                String response = connection.sendCommand("LIST_PRODUCTS");
                parseAndUpdateTable(response);
                mainWindow.setStatus("מוכן", Color.BLACK);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בטעינת מוצרים:\n" + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                mainWindow.setStatus("שגיאה בטעינת מוצרים", Color.RED);
            }
        });
    }
    
    /**
     * פרסור תגובה מהשרת ועדכון הטבלה
     */
    private void parseAndUpdateTable(String response) {
        if (response == null || !response.startsWith("OK;")) {
            return;
        }
        
        // הסרת "OK;"
        String data = response.substring(3);
        if (data.isEmpty()) {
            return;
        }
        
        // פיצול לפי "|" (זה המפריד בין מוצרים)
        String[] products = data.split("\\|");
        
        for (String productStr : products) {
            if (productStr.trim().isEmpty()) {
                continue;
            }
            
            // פיצול לפי ":" (productId:name:category:price:active:quantity)
            String[] parts = productStr.split(":");
            if (parts.length >= 6) {
                String productId = parts[0];
                String name = parts[1];
                String category = parts[2];
                String price = parts[3];
                String active = parts[4];
                String quantity = parts[5];
                
                String status = active.equals("active") ? "פעיל" : "לא פעיל";
                tableModel.addRow(new Object[]{productId, name, category, price, status, quantity});
            }
        }
    }
}
