package gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * חלון התחברות למערכת
 */
public class LoginWindow extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private ClientConnection connection;
    private boolean loginSuccessful = false;  // האם ההתחברות הצליחה וה-MainWindow נפתח
    
    public LoginWindow() {
        setTitle("התחברות למערכת");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // יצירת connection
        connection = new ClientConnection();
        
        // יצירת UI
        createUI();
        
        // ניסיון התחברות לשרת
        if (!connection.connect()) {
            JOptionPane.showMessageDialog(this,
                    "לא ניתן להתחבר לשרת.\nאנא וודא שהשרת פועל.",
                    "שגיאת חיבור",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Creates the login UI with username, password fields, and login button.
     */
    private void createUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel מרכזי
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // שם משתמש
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("שם משתמש:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);
        
        // סיסמה
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("סיסמה:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        passwordField.addActionListener(e -> performLogin());
        mainPanel.add(passwordField, gbc);
        
        // כפתור התחבר
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("התחבר");
        loginButton.addActionListener(e -> performLogin());
        mainPanel.add(loginButton, gbc);
        
        // הוספת padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Performs login attempt.
     * Validates input, sends login command to server, and handles response.
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "אנא מלא את כל השדות",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // ניסיון התחברות
        loginButton.setEnabled(false);
        loginButton.setText("מתחבר...");
        
        try {
            String response = connection.login(username, password);
            handleLoginResponse(response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בתקשורת עם השרת:\n" + e.getMessage(),
                    "שגיאת תקשורת",
                    JOptionPane.ERROR_MESSAGE);
            loginButton.setEnabled(true);
            loginButton.setText("התחבר");
        }
    }
    
    private void handleLoginResponse(String response) {
        loginButton.setEnabled(true);
        loginButton.setText("התחבר");
        
        if (response == null) {
            JOptionPane.showMessageDialog(this,
                    "לא התקבלה תגובה מהשרת",
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] parts = response.split(";");
        
        if (parts[0].equals("LOGIN_SUCCESS")) {
            // התחברות מוצלחת
            String username = usernameField.getText().trim();
            String role = parts[1];  // admin, manager, salesman, cashier
            String branchId = parts.length > 2 ? parts[2] : null;
            
            // פתיחת חלון ראשי
            String finalUsername = username;
            loginSuccessful = true;  // מסמן שההתחברות הצליחה
            SwingUtilities.invokeLater(() -> {
                MainWindow mainWindow = new MainWindow(connection, finalUsername, role, branchId);
                mainWindow.setVisible(true);
                this.dispose(); // סגירת חלון ההתחברות (בלי לנתק את ה-connection)
            });
        } else if (parts[0].equals("AUTH_ERROR")) {
            String errorMsg = parts.length > 1 ? parts[1] : "שגיאת אימות";
            JOptionPane.showMessageDialog(this,
                    errorMsg,
                    "שגיאת התחברות",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "תגובה לא מוכרת מהשרת: " + response,
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void dispose() {
        // אם ההתחברות הצליחה וה-MainWindow נפתח, לא להתנתק
        // ה-MainWindow אחראי על ניהול ה-connection שלו
        if (!loginSuccessful && connection != null && connection.isConnected()) {
            try {
                connection.logout();
            } catch (IOException e) {
                // ignore
            }
            connection.disconnect();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}
