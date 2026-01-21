package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import model.ChatMessage;
import model.ChatSession;
import model.ChatUserStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Tab for managing inter-branch chat.
 * Displays waiting requests, active chats, and message area.
 * Implements auto-refresh every 500ms (0.5 seconds) to keep UI updated with minimal server load.
 * Managers can join existing chats.
 * 
 * @author FinalProject
 */
public class ChatTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String currentUsername;
    private String branchId;
    private String role;
    
    private JList<String> waitingRequestsList;
    private DefaultListModel<String> waitingRequestsModel;
    private JList<String> activeChatsList;
    private DefaultListModel<String> activeChatsModel;
    private JTextArea messagesArea;
    private JTextField messageField;
    private JButton requestChatButton;
    private JButton sendButton;
    private JButton endChatButton;
    private JButton refreshButton;
    private JButton joinChatButton; // For shift manager
    private JButton acceptRequestButton; // For accepting chat request
    private JButton cancelRequestButton; // For canceling request
    
    private String currentChatId;
    private Timer refreshTimer;
    
    /**
     * Constructs a new ChatTab.
     * Starts auto-refresh timer to keep UI updated.
     * 
     * @param connection the ClientConnection to the server
     * @param mainWindow the parent MainWindow
     */
    public ChatTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.currentUsername = mainWindow.getCurrentUsername();
        this.branchId = mainWindow.getBranchId();
        this.role = mainWindow.getRole();
        
        setLayout(new BorderLayout());
        createUI();
        startAutoRefresh();
    }
    
    private void createUI() {
        // פאנל שמאלי - משתמשים פנויים וצ'אטים פעילים
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        
        // בקשות ממתינות
        JLabel waitingLabel = new JLabel("בקשות ממתינות לצ'אט:");
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        waitingRequestsModel = new DefaultListModel<>();
        waitingRequestsList = new JList<>(waitingRequestsModel);
        waitingRequestsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane waitingScroll = new JScrollPane(waitingRequestsList);
        waitingScroll.setPreferredSize(new Dimension(250, 150));
        
        // צ'אטים פעילים
        JLabel chatsLabel = new JLabel("צ'אטים פעילים:");
        chatsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        activeChatsModel = new DefaultListModel<>();
        activeChatsList = new JList<>(activeChatsModel);
        activeChatsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeChatsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = activeChatsList.getSelectedValue();
                if (selected != null) {
                    // חילוץ chatId מהמחרוזת (format: "chatId - user1, user2")
                    String chatId = selected.split(" - ")[0];
                    loadChat(chatId);
                }
            }
        });
        JScrollPane chatsScroll = new JScrollPane(activeChatsList);
        chatsScroll.setPreferredSize(new Dimension(250, 150));
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        requestChatButton = new JButton("בקש צ'אט");
        requestChatButton.addActionListener(e -> requestChat());
        buttonPanel.add(requestChatButton);
        
        cancelRequestButton = new JButton("בטל בקשה");
        cancelRequestButton.addActionListener(e -> cancelChatRequest());
        cancelRequestButton.setEnabled(false);
        buttonPanel.add(cancelRequestButton);
        
        acceptRequestButton = new JButton("קבל בקשה");
        acceptRequestButton.addActionListener(e -> acceptChatRequest());
        acceptRequestButton.setEnabled(false);
        buttonPanel.add(acceptRequestButton);
        
        joinChatButton = new JButton("הצטרף לצ'אט (מנהל)");
        joinChatButton.addActionListener(e -> joinChatAsManager());
        // רק למנהל משמרת (ADMIN או role=manager)
        if ("admin".equals(role) || "manager".equals(role)) {
            buttonPanel.add(joinChatButton);
        }
        
        endChatButton = new JButton("סיים צ'אט");
        endChatButton.addActionListener(e -> endChat());
        endChatButton.setEnabled(false);
        buttonPanel.add(endChatButton);
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);
        
        leftPanel.add(waitingLabel, BorderLayout.NORTH);
        leftPanel.add(waitingScroll, BorderLayout.CENTER);
        leftPanel.add(chatsLabel, BorderLayout.SOUTH);
        
        JPanel leftBottom = new JPanel(new BorderLayout());
        leftBottom.add(chatsScroll, BorderLayout.CENTER);
        leftBottom.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.add(leftBottom, BorderLayout.SOUTH);
        
        // פאנל מרכזי - הודעות
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        JLabel chatTitleLabel = new JLabel("צ'אט", SwingConstants.CENTER);
        chatTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        centerPanel.add(chatTitleLabel, BorderLayout.NORTH);
        
        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        JScrollPane messagesScroll = new JScrollPane(messagesArea);
        messagesScroll.setPreferredSize(new Dimension(500, 400));
        centerPanel.add(messagesScroll, BorderLayout.CENTER);
        
        // פאנל תחתון - שליחת הודעה
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        sendButton = new JButton("שלח");
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setEnabled(false);
        
        messagePanel.add(new JLabel("הודעה:"), BorderLayout.WEST);
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        
        centerPanel.add(messagePanel, BorderLayout.SOUTH);
        
        // הוספה ל-panel הראשי
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        
        // רענון ראשוני
        refresh();
    }
    
    private void requestChat() {
        // שליחת בקשה לצ'אט
        new Thread(() -> {
            try {
                String response = connection.sendCommand("REQUEST_CHAT");
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        String[] parts = response.split(";");
                        if (parts.length > 1 && parts[1].equals("MATCHED")) {
                            // נמצאה התאמה
                            String chatId = parts[2];
                            String user1 = parts[3];
                            String user2 = parts[4];
                            currentChatId = chatId;
                            loadChat(chatId);
                            mainWindow.setStatus("צ'אט התחיל עם " + (user1.equals(currentUsername) ? user2 : user1), Color.GREEN);
                        } else if (parts.length > 1 && parts[1].equals("QUEUE")) {
                            // נוסף לתור
                            mainWindow.setStatus("נוסף לתור, ממתין לעובד פנוי...", Color.BLUE);
                            cancelRequestButton.setEnabled(true);
                        } else {
                            mainWindow.setStatus("בקשה נשלחה", Color.GREEN);
                        }
                        refresh();
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בבקשת צ'אט";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                        mainWindow.setStatus("שגיאה בבקשת צ'אט", Color.RED);
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
    
    private void sendMessage() {
        if (currentChatId == null || messageField.getText().trim().isEmpty()) {
            return;
        }
        
        String message = messageField.getText().trim();
        messageField.setText("");
        
        new Thread(() -> {
            try {
                String response = connection.sendCommand("SEND_MESSAGE;" + currentChatId + ";" + message);
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        // רענון הודעות
                        loadChat(currentChatId);
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בשליחת הודעה";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בתקשורת: " + e.getMessage(),
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void endChat() {
        if (currentChatId == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "האם אתה בטוח שברצונך לסיים את הצ'אט?",
                "אישור סיום צ'אט",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    String response = connection.sendCommand("END_CHAT;" + currentChatId);
                    SwingUtilities.invokeLater(() -> {
                        if (response != null && response.startsWith("OK;")) {
                            // בדיקה אם יש משתמשים שמחכים
                            String[] parts = response.split(";");
                            if (parts.length > 2 && parts[2].equals("WAITING")) {
                                String waitingRequests = parts.length > 3 ? parts[3] : "";
                                if (!waitingRequests.isEmpty()) {
                                    // רענון רשימת בקשות ממתינות
                                    refreshWaitingRequests();
                                    showNotification("יש בקשות ממתינות לצ'אט! בדוק את הרשימה.");
                                }
                            }
                            
                            currentChatId = null;
                            messagesArea.setText("");
                            sendButton.setEnabled(false);
                            endChatButton.setEnabled(false);
                            messageField.setEnabled(false);
                            mainWindow.setStatus("צ'אט הסתיים", Color.GREEN);
                            refresh();
                        } else {
                            String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בסיום צ'אט";
                            JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "שגיאה בתקשורת: " + e.getMessage(),
                                "שגיאה",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }
    
    private void joinChatAsManager() {
        String selected = activeChatsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "בחר צ'אט פעיל להצטרפות",
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String chatId = selected.split(" - ")[0];
        
        new Thread(() -> {
            try {
                String response = connection.sendCommand("JOIN_CHAT;" + chatId);
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        currentChatId = chatId;
                        loadChat(chatId);
                        mainWindow.setStatus("הצטרפת לצ'אט", Color.GREEN);
                        refresh();
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בהצטרפות לצ'אט";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בתקשורת: " + e.getMessage(),
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void loadChat(String chatId) {
        currentChatId = chatId;
        new Thread(() -> {
            try {
                String response = connection.sendCommand("GET_CHAT_MESSAGES;" + chatId);
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        // פרסור JSON (פשוט)
                        parseAndDisplayMessages(response.substring(3)); // הסרת "OK;"
                        sendButton.setEnabled(true);
                        endChatButton.setEnabled(true);
                        messageField.setEnabled(true);
                    } else {
                        messagesArea.setText("שגיאה בטעינת הודעות");
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    messagesArea.setText("שגיאה בתקשורת: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void parseAndDisplayMessages(String json) {
        messagesArea.setText("");
        // פרסור בסיסי של JSON
        // format: {"chatId":"...","messages":[{...},...]}
        try {
            int messagesStart = json.indexOf("\"messages\":[");
            if (messagesStart == -1) {
                messagesArea.setText("אין הודעות");
                return;
            }
            
            int arrayStart = messagesStart + 12; // אחרי "messages":[
            int arrayEnd = json.lastIndexOf("]");
            if (arrayEnd == -1) {
                messagesArea.setText("אין הודעות");
                return;
            }
            
            String messagesJson = json.substring(arrayStart, arrayEnd);
            if (messagesJson.trim().isEmpty()) {
                messagesArea.setText("אין הודעות");
                return;
            }
            
            // פרסור כל הודעה
            List<String> messages = new ArrayList<>();
            int braceLevel = 0;
            int msgStart = -1;
            boolean inString = false;
            
            for (int i = 0; i < messagesJson.length(); i++) {
                char c = messagesJson.charAt(i);
                if (c == '"' && (i == 0 || messagesJson.charAt(i - 1) != '\\')) {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') {
                        if (braceLevel == 0) {
                            msgStart = i + 1;
                        }
                        braceLevel++;
                    } else if (c == '}') {
                        braceLevel--;
                        if (braceLevel == 0 && msgStart != -1) {
                            String msgJson = messagesJson.substring(msgStart, i);
                            messages.add(msgJson);
                            msgStart = -1;
                        }
                    }
                }
            }
            
            // הצגת הודעות
            for (String msgJson : messages) {
                String sender = extractJsonValue(msgJson, "senderUsername");
                String message = extractJsonValue(msgJson, "message");
                String type = extractJsonValue(msgJson, "messageType");
                
                if ("SYSTEM".equals(type)) {
                    messagesArea.append("[מערכת] " + message + "\n");
                } else {
                    messagesArea.append(sender + ": " + message + "\n");
                }
            }
            
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
        } catch (Exception e) {
            messagesArea.setText("שגיאה בפרסור הודעות: " + e.getMessage());
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "";
        
        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return "";
        
        if (json.charAt(valueStart) == '"') {
            valueStart++;
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                if (json.charAt(valueEnd) == '"' && (valueEnd == valueStart || json.charAt(valueEnd - 1) != '\\')) {
                    break;
                }
                valueEnd++;
            }
            String value = json.substring(valueStart, valueEnd);
            return value.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        }
        
        return "";
    }
    
    public void refresh() {
        // רענון בקשות ממתינות
        refreshWaitingRequests();
        
        // רענון צ'אטים פעילים
        refreshActiveChats();
        
        // בדיקת מצב משתמש (להפעלת/כיבוי כפתור ביטול)
        checkUserStatus();
        
        // רענון הודעות אם יש צ'אט פעיל
        if (currentChatId != null) {
            loadChat(currentChatId);
        }
    }
    
    private void checkUserStatus() {
        new Thread(() -> {
            try {
                String response = connection.sendCommand("GET_USER_CHAT_STATUS");
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        String status = response.substring(3);
                        if ("IN_QUEUE".equals(status)) {
                            cancelRequestButton.setEnabled(true);
                        } else {
                            cancelRequestButton.setEnabled(false);
                        }
                    }
                });
            } catch (IOException e) {
                // ignore
            }
        }).start();
    }
    
    private void refreshWaitingRequests() {
        new Thread(() -> {
            try {
                String response = connection.sendCommand("GET_WAITING_REQUESTS");
                SwingUtilities.invokeLater(() -> {
                    waitingRequestsModel.clear();
                    acceptRequestButton.setEnabled(false);
                    if (response != null && response.startsWith("OK;")) {
                        String requestsStr = response.substring(3);
                        if (!requestsStr.isEmpty()) {
                            String[] requests = requestsStr.split("\\|");
                            for (String request : requests) {
                                if (!request.isEmpty()) {
                                    // פורמט: requestId:requesterUsername
                                    String[] parts = request.split(":");
                                    if (parts.length >= 2) {
                                        String requestId = parts[0];
                                        String requester = parts[1];
                                        waitingRequestsModel.addElement(requester + " (" + requestId + ")");
                                    }
                                }
                            }
                            if (waitingRequestsModel.getSize() > 0) {
                                acceptRequestButton.setEnabled(true);
                            }
                        }
                    }
                });
            } catch (IOException e) {
                // ignore
            }
        }).start();
    }
    
    private void showNotification(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "התראה",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void refreshActiveChats() {
        new Thread(() -> {
            try {
                String response = connection.sendCommand("GET_USER_CHAT");
                SwingUtilities.invokeLater(() -> {
                    activeChatsModel.clear();
                    if (response != null && response.startsWith("OK;")) {
                        String[] parts = response.split(";");
                        if (parts.length > 1 && parts[1].equals("CHAT")) {
                            String chatId = parts[2];
                            String participants = "";
                            if (parts.length > 5) {
                                participants = parts[5].replace(",", ", ");
                            }
                            activeChatsModel.addElement(chatId + " - " + participants);
                        }
                    }
                });
            } catch (IOException e) {
                // ignore
            }
        }).start();
    }
    
    private void startAutoRefresh() {
        // רענון אוטומטי כל 500ms (0.5 שניות) - איזון טוב בין ביצועים לחוויית משתמש
        refreshTimer = new Timer(500, e -> {
            if (currentChatId != null) {
                loadChat(currentChatId);
            }
            refreshWaitingRequests();
            refreshActiveChats();
        });
        refreshTimer.start();
    }
    
    private void cancelChatRequest() {
        new Thread(() -> {
            try {
                String response = connection.sendCommand("CANCEL_CHAT_REQUEST");
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        cancelRequestButton.setEnabled(false);
                        mainWindow.setStatus("בקשה בוטלה", Color.GREEN);
                        refresh();
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה בביטול בקשה";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בתקשורת: " + e.getMessage(),
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void acceptChatRequest() {
        String selected = waitingRequestsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "בחר בקשה מהרשימה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // חילוץ requestId מהמחרוזת (format: "requester (requestId)")
        String requestId = null;
        int startIdx = selected.lastIndexOf("(");
        int endIdx = selected.lastIndexOf(")");
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            requestId = selected.substring(startIdx + 1, endIdx);
        }
        
        if (requestId == null || requestId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בחילוץ מזהה הבקשה",
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // העתקה למשתנה final לשימוש ב-lambda
        final String finalRequestId = requestId;
        
        new Thread(() -> {
            try {
                String response = connection.sendCommand("ACCEPT_CHAT_REQUEST;" + finalRequestId);
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        String[] parts = response.split(";");
                        if (parts.length > 1 && parts[1].equals("MATCHED")) {
                            String chatId = parts[2];
                            String user1 = parts[3];
                            String user2 = parts[4];
                            currentChatId = chatId;
                            loadChat(chatId);
                            mainWindow.setStatus("צ'אט התחיל עם " + (user1.equals(currentUsername) ? user2 : user1), Color.GREEN);
                            refresh();
                        } else {
                            mainWindow.setStatus("בקשה אושרה", Color.GREEN);
                            refresh();
                        }
                    } else {
                        String errorMsg = response != null && response.contains(";") ? response.split(";", 2)[1] : "שגיאה באישור בקשה";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "שגיאה בתקשורת: " + e.getMessage(),
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    @Override
    public void removeNotify() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.removeNotify();
    }
}
