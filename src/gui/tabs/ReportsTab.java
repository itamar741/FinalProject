package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import gui.windows.ReportViewWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * טאב לדוחות
 */
public class ReportsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    private String branchId;
    
    public ReportsTab(ClientConnection connection, MainWindow mainWindow, String userType) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.userType = userType;
        this.branchId = mainWindow.getBranchId();
        
        setLayout(new BorderLayout());
        createUI();
    }
    
    private void createUI() {
        // כותרת
        JLabel titleLabel = new JLabel("דוחות", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel עם כפתורי דוחות
        JPanel reportsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        
        if (userType.equals("ADMIN")) {
            // דוחות לאדמין
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי סניף", 0, () -> showSalesByBranchReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי מוצר", 1, () -> showSalesByProductReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי קטגוריה", 2, () -> showSalesByCategoryReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות יומי", 3, () -> showDailySalesReport());
        } else {
            // דוחות לעובד
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי סניף", 0, () -> showSalesByBranchReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי מוצר", 1, () -> showSalesByProductReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי קטגוריה", 2, () -> showSalesByCategoryReport());
            addReportButton(reportsPanel, gbc, "דוח מכירות יומי", 3, () -> showDailySalesReport());
        }
        
        add(reportsPanel, BorderLayout.CENTER);
    }
    
    private void addReportButton(JPanel panel, GridBagConstraints gbc, String text, int row, Runnable action) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 40));
        button.addActionListener(e -> action.run());
        panel.add(button, gbc);
    }
    
    private void showSalesByBranchReport() {
        String branchId = null;
        if (userType.equals("ADMIN")) {
            // דיאלוג לבחירת סניף (אופציונלי)
            Object[] options = {"כל הסניפים", "B1", "B2", "ביטול"};
            int choice = JOptionPane.showOptionDialog(this,
                    "בחר סניף:",
                    "דוח מכירות לפי סניף",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            
            if (choice == 3) return; // ביטול
            if (choice == 1) branchId = "B1";
            else if (choice == 2) branchId = "B2";
            // choice == 0 -> branchId נשאר null (כל הסניפים)
        }
        
        loadAndShowReport("REPORT_SALES_BY_BRANCH", "SALES_BY_BRANCH", branchId != null ? branchId : "");
    }
    
    private void showSalesByProductReport() {
        String productId = JOptionPane.showInputDialog(this,
                "הכנס קוד מוצר (או השאר ריק לכל המוצרים):",
                "דוח מכירות לפי מוצר",
                JOptionPane.QUESTION_MESSAGE);
        
        if (productId == null) return; // ביטול
        
        loadAndShowReport("REPORT_SALES_BY_PRODUCT", "SALES_BY_PRODUCT", productId != null ? productId : "");
    }
    
    private void showSalesByCategoryReport() {
        String category = JOptionPane.showInputDialog(this,
                "הכנס קטגוריה (או השאר ריק לכל הקטגוריות):",
                "דוח מכירות לפי קטגוריה",
                JOptionPane.QUESTION_MESSAGE);
        
        if (category == null) return; // ביטול
        
        loadAndShowReport("REPORT_SALES_BY_CATEGORY", "SALES_BY_CATEGORY", category != null ? category : "");
    }
    
    private void showDailySalesReport() {
        // דיאלוג לבחירת תאריך וסניף
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // תאריך
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("תאריך (YYYY-MM-DD, או ריק לכל התאריכים):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField dateField = new JTextField(20);
        panel.add(dateField, gbc);
        
        // סניף (רק לאדמין)
        JComboBox<String> branchCombo = null;
        if (userType.equals("ADMIN")) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel("סניף:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            branchCombo = new JComboBox<>(new String[]{"כל הסניפים", "B1", "B2"});
            panel.add(branchCombo, gbc);
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                panel,
                "דוח מכירות יומי",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String date = dateField.getText().trim();
            String selectedBranchId = "";
            if (userType.equals("ADMIN") && branchCombo != null) {
                String branchChoice = (String) branchCombo.getSelectedItem();
                if ("B1".equals(branchChoice)) selectedBranchId = "B1";
                else if ("B2".equals(branchChoice)) selectedBranchId = "B2";
            }
            
            loadAndShowReport("REPORT_DAILY_SALES", "DAILY_SALES", date + ";" + selectedBranchId);
        }
    }
    
    private void loadAndShowReport(String command, String reportType, String parameters) {
        // הרצת הרשת ב-thread נפרד כדי לא לחסום את ה-GUI
        new Thread(() -> {
            try {
                String fullCommand = command + (parameters != null && !parameters.isEmpty() ? ";" + parameters : "");
                String response = connection.sendCommand(fullCommand);
                
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        ReportViewWindow window = new ReportViewWindow(mainWindow, connection, reportType, response);
                        window.setVisible(true);
                        mainWindow.setStatus("דוח נטען בהצלחה", Color.GREEN);
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בטעינת דוח";
                        JOptionPane.showMessageDialog(this,
                                "שגיאה בטעינת דוח:\n" + errorMsg,
                                "שגיאה",
                                JOptionPane.ERROR_MESSAGE);
                        mainWindow.setStatus("שגיאה בטעינת דוח", Color.RED);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בתקשורת: " + e.getMessage(),
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                    mainWindow.setStatus("שגיאה בתקשורת", Color.RED);
                });
            }
        }).start();
    }
    
    /**
     * רענון (אין צורך כאן, אבל נדרש על ידי MainWindow)
     */
    public void refresh() {
        // אין צורך לעשות כלום כרגע
    }
}
