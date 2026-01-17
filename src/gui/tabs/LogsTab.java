package gui.tabs;

import gui.ClientConnection;
import gui.MainWindow;
import model.LogEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * טאב לניהול לוגים - רק למנהלים (ADMIN)
 */
public class LogsTab extends JPanel {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    
    private JList<String> logsList;
    private DefaultListModel<String> logsModel;
    private JComboBox<String> filterComboBox;
    private JButton refreshButton;
    private JButton saveChatButton;
    
    private List<LogEntry> allLogs;
    private List<LogEntry> filteredLogs;
    private LogEntry selectedLog;
    
    public LogsTab(ClientConnection connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.allLogs = new ArrayList<>();
        this.filteredLogs = new ArrayList<>();
        
        setLayout(new BorderLayout());
        createUI();
        refresh();
    }
    
    private void createUI() {
        // פאנל עליון - סינון וכפתורים
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel filterLabel = new JLabel("סינון:");
        filterComboBox = new JComboBox<>(new String[]{"הכל", "רישום עובדים", "רישום לקוחות", "קניות/מכירות", "פרטי שיחות"});
        filterComboBox.addActionListener(e -> filterLogs());
        
        refreshButton = new JButton("רענן");
        refreshButton.addActionListener(e -> refresh());
        
        saveChatButton = new JButton("שמור שיחה");
        saveChatButton.addActionListener(e -> saveChat());
        saveChatButton.setEnabled(false);
        
        topPanel.add(filterLabel);
        topPanel.add(filterComboBox);
        topPanel.add(refreshButton);
        topPanel.add(saveChatButton);
        
        // רשימת לוגים
        logsModel = new DefaultListModel<>();
        logsList = new JList<>(logsModel);
        logsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = logsList.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < filteredLogs.size()) {
                    selectedLog = filteredLogs.get(selectedIndex);
                    // הפעלת כפתור "שמור שיחה" רק אם יש chatId
                    saveChatButton.setEnabled(selectedLog.getChatId() != null && !selectedLog.getChatId().isEmpty());
                } else {
                    selectedLog = null;
                    saveChatButton.setEnabled(false);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(logsList);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void refresh() {
        new Thread(() -> {
            try {
                String response = connection.sendCommand("GET_LOGS");
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;")) {
                        parseAndDisplayLogs(response.substring(3));
                    } else {
                        mainWindow.setStatus("שגיאה בטעינת לוגים", Color.RED);
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    mainWindow.setStatus("שגיאה בתקשורת: " + e.getMessage(), Color.RED);
                });
            }
        }).start();
    }
    
    private void parseAndDisplayLogs(String json) {
        allLogs.clear();
        logsModel.clear();
        
        try {
            // פרסור JSON array
            if (json == null || json.trim().isEmpty() || json.equals("[]")) {
                return;
            }
            
            // הסרת [ ו-]
            String content = json.trim();
            if (content.startsWith("[")) {
                content = content.substring(1);
            }
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }
            
            if (content.trim().isEmpty()) {
                return;
            }
            
            // פרסור כל אובייקט לוג
            List<String> logObjects = new ArrayList<>();
            int braceLevel = 0;
            int startPos = -1;
            boolean inString = false;
            
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') {
                        if (braceLevel == 0) {
                            startPos = i;
                        }
                        braceLevel++;
                    } else if (c == '}') {
                        braceLevel--;
                        if (braceLevel == 0 && startPos != -1) {
                            logObjects.add(content.substring(startPos, i + 1));
                            startPos = -1;
                        }
                    }
                }
            }
            
            // יצירת LogEntry מכל אובייקט
            for (String logJson : logObjects) {
                LogEntry log = parseLogEntry(logJson);
                if (log != null) {
                    allLogs.add(log);
                }
            }
            
            // מיון לפי תאריך (החדש ביותר ראשון)
            allLogs.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
            
            // הצגה
            filterLogs();
            
        } catch (Exception e) {
            mainWindow.setStatus("שגיאה בפרסור לוגים: " + e.getMessage(), Color.RED);
        }
    }
    
    private LogEntry parseLogEntry(String json) {
        try {
            String actionType = extractJsonValue(json, "actionType");
            String description = extractJsonValue(json, "description");
            String dateTime = extractJsonValue(json, "dateTime");
            String chatId = extractJsonValue(json, "chatId");
            
            if (actionType == null || actionType.isEmpty() || 
                description == null || description.isEmpty() ||
                dateTime == null || dateTime.isEmpty()) {
                return null;
            }
            
            if (chatId == null || chatId.isEmpty()) {
                return new LogEntry(actionType, description, dateTime);
            } else {
                return new LogEntry(actionType, description, dateTime, chatId);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        
        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return null;
        
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
        
        return null;
    }
    
    private void filterLogs() {
        logsModel.clear();
        filteredLogs = new ArrayList<>();
        
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        if (selectedFilter == null) {
            selectedFilter = "הכל";
        }
        
        for (LogEntry log : allLogs) {
            boolean include = false;
            
            switch (selectedFilter) {
                case "הכל":
                    include = true;
                    break;
                case "רישום עובדים":
                    include = "CREATE_EMPLOYEE".equals(log.getActionType());
                    break;
                case "רישום לקוחות":
                    include = "ADD_CUSTOMER".equals(log.getActionType());
                    break;
                case "קניות/מכירות":
                    include = "SALE".equals(log.getActionType());
                    break;
                case "פרטי שיחות":
                    include = log.getActionType().startsWith("CHAT_") || "MANAGER_JOINED".equals(log.getActionType());
                    break;
            }
            
            if (include) {
                filteredLogs.add(log);
                String displayText = formatLogEntry(log);
                logsModel.addElement(displayText);
            }
        }
    }
    
    private String formatLogEntry(LogEntry log) {
        // פורמט: [תאריך] סוג פעולה: תיאור
        String dateStr = formatDateTime(log.getDateTime());
        return "[" + dateStr + "] " + log.getActionType() + ": " + log.getDescription();
    }
    
    private String formatDateTime(String dateTime) {
        try {
            // ניסיון לפרסור תאריך
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (Exception e) {
            return dateTime; // אם לא הצליח, נחזיר את המקורי
        }
    }
    
    private void saveChat() {
        if (selectedLog == null || selectedLog.getChatId() == null) {
            JOptionPane.showMessageDialog(this,
                    "לא נבחר לוג של שיחה",
                    "שגיאה",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String chatId = selectedLog.getChatId();
        
        new Thread(() -> {
            try {
                String response = connection.sendCommand("SAVE_CHAT_TO_RTF;" + chatId);
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK;Saved;")) {
                        String[] parts = response.split(";");
                        if (parts.length > 2) {
                            String fileName = parts[2];
                            JOptionPane.showMessageDialog(this,
                                    "השיחה נשמרה בהצלחה לקובץ:\n" + fileName,
                                    "שמירה הושלמה",
                                    JOptionPane.INFORMATION_MESSAGE);
                            mainWindow.setStatus("שיחה נשמרה: " + fileName, Color.GREEN);
                        } else {
                            mainWindow.setStatus("שיחה נשמרה", Color.GREEN);
                        }
                    } else {
                        String errorMsg = response != null && response.contains(";") ? 
                            response.split(";", 2)[1] : "שגיאה בשמירת שיחה";
                        JOptionPane.showMessageDialog(this, errorMsg, "שגיאה", JOptionPane.ERROR_MESSAGE);
                        mainWindow.setStatus("שגיאה בשמירת שיחה", Color.RED);
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
}
