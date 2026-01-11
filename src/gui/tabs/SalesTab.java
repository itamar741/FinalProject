package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.SellProductDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * טאב לביצוע מכירות
 */
public class SalesTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    private String branchId;
    private String employeeNumber;
    
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JButton sellButton;
    private JButton refreshButton;
    
    public SalesTab(ClientConnection connection, MainWindow mainWindow, String userType, String branchId, String employeeNumber) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.userType = userType;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול מכירות", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        sellButton = new JButton("ביצוע מכירה");
        sellButton.addActionListener(e -> showSellProductDialog());
        buttonPanel.add(sellButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // טבלת מכירות
        String[] columns = {"תאריך", "קוד מוצר", "כמות", "סניף", "מספר עובד", "ת.ז. לקוח"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        salesTable = new JTable(tableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showSellProductDialog() {
        SellProductDialog dialog = new SellProductDialog(mainWindow, connection, userType, branchId, employeeNumber);
        dialog.setVisible(true);
        refresh();
    }
    
    /**
     * רענון רשימת המכירות
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען מכירות...");
            
            // TODO: שליחת פקודה LIST_SALES לשרת (עדיין לא קיימת)
            // String response = connection.sendCommand("LIST_SALES");
            // parseAndUpdateTable(response);
            
            mainWindow.setStatus("מוכן", Color.BLACK);
        });
    }
}
