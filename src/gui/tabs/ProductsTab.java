package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.AddToInventoryDialog;
import gui.dialogs.SellProductDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

/**
 * Tab for managing products and inventory.
 * For admin: internal tabs for each branch (B1, B2).
 * For employee: only one tab for their branch.
 * 
 * @author FinalProject
 */
public class ProductsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String role;
    private String branchId;
    
    private JTabbedPane branchTabsPane;  // Internal tabs for each branch
    private JButton addToInventoryButton;
    private JButton sellButton;
    private JButton removeFromInventoryButton;
    private JButton deleteProductButton;  // Admin only
    private JButton refreshButton;
    
    /**
     * Constructs a new ProductsTab.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param branchId the branch ID where the user works
     */
    public ProductsTab(ClientConnection connection, MainWindow mainWindow, String role, String branchId) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.role = role;
        this.branchId = branchId;
        
        setLayout(new BorderLayout());
        createUI();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול מוצרים ומלאי", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // כפתור הוספה למלאי
        addToInventoryButton = new JButton("הוסף למלאי");
        addToInventoryButton.addActionListener(e -> showAddToInventoryDialog());
        buttonPanel.add(addToInventoryButton);
        
        // כפתור מכור
        sellButton = new JButton("מכור");
        sellButton.addActionListener(e -> showSellDialog());
        buttonPanel.add(sellButton);
        
        // כפתור מחק מהמלאי
        removeFromInventoryButton = new JButton("מחק מהמלאי");
        removeFromInventoryButton.addActionListener(e -> showRemoveFromInventoryDialog());
        buttonPanel.add(removeFromInventoryButton);
        
        // כפתור מחק מוצר (Admin only)
        if ("admin".equals(role)) {
            deleteProductButton = new JButton("מחק מוצר");
            deleteProductButton.addActionListener(e -> deleteSelectedProduct());
            buttonPanel.add(deleteProductButton);
        }
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refreshAllTabs());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // יצירת טאבים פנימיים לכל סניף
        branchTabsPane = new JTabbedPane();
        branchTabsPane.setTabPlacement(JTabbedPane.TOP);
        
        if ("admin".equals(role)) {
            // לאדמין: 2 טאבים (B1, B2)
            BranchInventoryPanel b1Panel = new BranchInventoryPanel("B1");
            branchTabsPane.addTab("מלאי סניף B1", b1Panel);
            
            BranchInventoryPanel b2Panel = new BranchInventoryPanel("B2");
            branchTabsPane.addTab("מלאי סניף B2", b2Panel);
        } else {
            // לעובד: רק טאב אחד לסניף שלו
            BranchInventoryPanel branchPanel = new BranchInventoryPanel(branchId);
            branchTabsPane.addTab("מלאי סניף " + branchId, branchPanel);
        }
        
        add(branchTabsPane, BorderLayout.CENTER);
    }
    
    /**
     * פאנל פנימי לטאב של סניף ספציפי
     */
    private class BranchInventoryPanel extends JPanel {
        private String branchId;
        private JTable productsTable;
        private DefaultTableModel tableModel;
        
        public BranchInventoryPanel(String branchId) {
            this.branchId = branchId;
            setLayout(new BorderLayout());
            createTable();
            refresh();
        }
        
        private void createTable() {
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
        
        public void refresh() {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                mainWindow.setStatus("טוען מוצרים...");
                
                try {
                    // שליחת פקודה עם branchId
                    String command = "LIST_PRODUCTS_BY_BRANCH;" + branchId;
                    String response = connection.sendCommand(command);
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
        
        public String getSelectedProductId() {
            int selectedRow = productsTable.getSelectedRow();
            if (selectedRow >= 0) {
                return (String) tableModel.getValueAt(selectedRow, 0);  // קוד מוצר
            }
            return null;
        }
        
        public String getBranchId() {
            return branchId;
        }
    }
    
    private void showAddToInventoryDialog() {
        AddToInventoryDialog dialog = new AddToInventoryDialog(mainWindow, connection, role, branchId);
        dialog.setVisible(true);
        refreshAllTabs();
    }
    
    private void showSellDialog() {
        BranchInventoryPanel currentPanel = getCurrentBranchPanel();
        if (currentPanel == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר סניף",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedBranchId = currentPanel.getBranchId();
        SellProductDialog dialog = new SellProductDialog(mainWindow, connection, role, selectedBranchId);
        dialog.setVisible(true);
        refreshAllTabs();
    }
    
    private void showRemoveFromInventoryDialog() {
        BranchInventoryPanel currentPanel = getCurrentBranchPanel();
        if (currentPanel == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר סניף",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productId = currentPanel.getSelectedProductId();
        if (productId == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר מוצר מהטבלה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String quantityStr = JOptionPane.showInputDialog(this,
                "הזן כמות להסרה:",
                "הסרה מהמלאי",
                JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this,
                        "כמות חייבת להיות גדולה מ-0",
                        "שגיאה",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String selectedBranchId = currentPanel.getBranchId();
            String command = "REMOVE_FROM_INVENTORY;" + productId + ";" + quantity + ";" + selectedBranchId;
            String response = connection.sendCommand(command);
            
            if (response.startsWith("OK")) {
                mainWindow.setStatus("מוצר הוסר מהמלאי בהצלחה", Color.GREEN);
                refreshAllTabs();
            } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                JOptionPane.showMessageDialog(this,
                        "שגיאה בהסרה מהמלאי:\n" + errorMsg,
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
    
    private void deleteSelectedProduct() {
        BranchInventoryPanel currentPanel = getCurrentBranchPanel();
        if (currentPanel == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר סניף",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productId = currentPanel.getSelectedProductId();
        if (productId == null) {
            JOptionPane.showMessageDialog(this,
                    "אנא בחר מוצר מהטבלה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך למחוק את המוצר " + productId + "?\n" +
                "פעולה זו תסיר את המוצר מכל הסניפים ותמחק אותו מהמערכת.",
                "אישור מחיקה",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String command = "DELETE_PRODUCT;" + productId;
                String response = connection.sendCommand(command);
                
                if (response.startsWith("OK")) {
                    mainWindow.setStatus("מוצר נמחק בהצלחה", Color.GREEN);
                    refreshAllTabs();
                } else if (response.startsWith("ERROR") || response.startsWith("AUTH_ERROR")) {
                    String errorMsg = response.contains(";") ? response.split(";", 2)[1] : response;
                    JOptionPane.showMessageDialog(this,
                            "שגיאה במחיקת מוצר:\n" + errorMsg,
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
    
    private BranchInventoryPanel getCurrentBranchPanel() {
        int selectedIndex = branchTabsPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            Component component = branchTabsPane.getComponentAt(selectedIndex);
            if (component instanceof BranchInventoryPanel) {
                return (BranchInventoryPanel) component;
            }
        }
        return null;
    }
    
    /**
     * רענון כל הטאבים הפנימיים
     */
    public void refreshAllTabs() {
        for (int i = 0; i < branchTabsPane.getTabCount(); i++) {
            Component component = branchTabsPane.getComponentAt(i);
            if (component instanceof BranchInventoryPanel) {
                ((BranchInventoryPanel) component).refresh();
            }
        }
    }
    
    /**
     * רענון (alias ל-refreshAllTabs)
     */
    public void refresh() {
        refreshAllTabs();
    }
}
