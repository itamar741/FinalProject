package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * טאב לדוחות (placeholders)
 */
public class ReportsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String userType;
    
    public ReportsTab(ClientConnection connection, MainWindow mainWindow, String userType) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.userType = userType;
        
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
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי תקופה", 0);
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי סניף", 1);
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי עובד", 2);
            addReportButton(reportsPanel, gbc, "דוח מכירות - לפי לקוח", 3);
            addReportButton(reportsPanel, gbc, "דוח מלאי - לפי סניף", 4);
            addReportButton(reportsPanel, gbc, "דוח מלאי - לפי מוצר", 5);
            addReportButton(reportsPanel, gbc, "דוח לקוחות - לפי סוג", 6);
            addReportButton(reportsPanel, gbc, "דוח עובדים - לפי סניף", 7);
            addReportButton(reportsPanel, gbc, "דוח עובדים - לפי תפקיד", 8);
        } else {
            // דוחות לעובד
            addReportButton(reportsPanel, gbc, "דוח מכירות שלי - לפי תקופה", 0);
            addReportButton(reportsPanel, gbc, "דוח מלאי סניף שלי", 1);
        }
        
        add(reportsPanel, BorderLayout.CENTER);
        
        // הודעה
        JLabel messageLabel = new JLabel(
                "<html><center>פונקציונליות זו טרם הוטמעה.<br>" +
                "הדוחות יוצגו כאן בעתיד.</center></html>",
                SwingConstants.CENTER);
        messageLabel.setForeground(Color.GRAY);
        add(messageLabel, BorderLayout.SOUTH);
    }
    
    private void addReportButton(JPanel panel, GridBagConstraints gbc, String text, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 40));
        button.setEnabled(false); // עדיין לא מיושם
        button.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "פונקציונליות זו טרם הוטמעה.",
                    "מידע",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(button, gbc);
    }
    
    /**
     * רענון (אין צורך כאן, אבל נדרש על ידי MainWindow)
     */
    public void refresh() {
        // אין צורך לעשות כלום כרגע
    }
}
