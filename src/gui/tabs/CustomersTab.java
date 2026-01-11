package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.dialogs.AddCustomerDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * טאב לניהול לקוחות
 */
public class CustomersTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton refreshButton;
    
    public CustomersTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("ניהול לקוחות", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("הוסף לקוח חדש");
        addButton.addActionListener(e -> showAddCustomerDialog());
        buttonPanel.add(addButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // טבלת לקוחות
        String[] columns = {"ת.ז.", "שם מלא", "טלפון", "סוג לקוח"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        customersTable = new JTable(tableModel);
        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(customersTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // הודעה אם אין לקוחות
        if (tableModel.getRowCount() == 0) {
            JLabel emptyLabel = new JLabel("אין לקוחות במערכת", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            add(emptyLabel, BorderLayout.CENTER);
        }
    }
    
    private void showAddCustomerDialog() {
        AddCustomerDialog dialog = new AddCustomerDialog(mainWindow, connection);
        dialog.setVisible(true);
        
        // רענון אוטומטי אחרי סגירת הדיאלוג
        refresh();
    }
    
    /**
     * רענון רשימת הלקוחות מהשרת
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            mainWindow.setStatus("טוען לקוחות...");
            
            try {
                String response = connection.sendCommand("LIST_CUSTOMERS");
                parseAndUpdateTable(response);
                mainWindow.setStatus("מוכן", Color.BLACK);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בטעינת לקוחות:\n" + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                mainWindow.setStatus("שגיאה בטעינת לקוחות", Color.RED);
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
        
        // פיצול לפי "|" (זה המפריד בין לקוחות)
        String[] customers = data.split("\\|");
        
        for (String customerStr : customers) {
            if (customerStr.trim().isEmpty()) {
                continue;
            }
            
            // פיצול לפי ":" (idNumber:fullName:phone:type)
            String[] parts = customerStr.split(":");
            if (parts.length >= 4) {
                String idNumber = parts[0];
                String fullName = parts[1];
                String phone = parts[2];
                String customerType = parts[3];
                
                tableModel.addRow(new Object[]{idNumber, fullName, phone, customerType});
            }
        }
    }
}
