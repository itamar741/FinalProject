package gui.windows;

import gui.ClientConnection;
import gui.MainWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Window for displaying sales reports.
 * Shows report data in a table and provides option to export to RTF format.
 * Supports different report types: by branch, by product, by category, daily.
 * 
 * @author FinalProject
 */
public class ReportViewWindow extends JDialog {
    
    private ClientConnection connection;
    private MainWindow mainWindow;
    private String reportType;
    private String jsonData;
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JButton exportRtfButton;
    private JButton closeButton;
    
    /**
     * Constructs a new ReportViewWindow.
     * 
     * @param parent the parent MainWindow
     * @param connection the ClientConnection to the server
     * @param reportType the type of report (SALES_BY_BRANCH, SALES_BY_PRODUCT, etc.)
     * @param jsonData the report data in JSON format
     */
    public ReportViewWindow(MainWindow parent, ClientConnection connection, String reportType, String jsonData) {
        super(parent, "דוח: " + getReportTypeName(reportType), true);
        this.connection = connection;
        this.mainWindow = parent;
        this.reportType = reportType;
        this.jsonData = jsonData;
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
        createUI();
        parseAndDisplayReport(jsonData);
    }
    
    private static String getReportTypeName(String reportType) {
        switch (reportType) {
            case "SALES_BY_BRANCH": return "מכירות לפי סניף";
            case "SALES_BY_PRODUCT": return "מכירות לפי מוצר";
            case "SALES_BY_CATEGORY": return "מכירות לפי קטגוריה";
            case "DAILY_SALES": return "מכירות יומי";
            default: return "דוח";
        }
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // כותרת
        JLabel titleLabel = new JLabel("דוח: " + getReportTypeName(reportType), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // טבלה
        String[] columns = getColumnsForReportType(reportType);
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // רק קריאה
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // כפתורים
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        exportRtfButton = new JButton("ייצא ל-Word");
        exportRtfButton.addActionListener(e -> exportToRTF());
        buttonPanel.add(exportRtfButton);
        
        closeButton = new JButton("סגור");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private String[] getColumnsForReportType(String reportType) {
        switch (reportType) {
            case "SALES_BY_BRANCH":
                return new String[]{"סניף", "כמות", "סכום כולל (₪)"};
            case "SALES_BY_PRODUCT":
                return new String[]{"סניף", "קוד מוצר", "שם מוצר", "קטגוריה", "כמות", "סכום (₪)", "תאריך"};
            case "SALES_BY_CATEGORY":
                return new String[]{"קטגוריה", "כמות", "סכום כולל (₪)"};
            case "DAILY_SALES":
                return new String[]{"סניף", "קוד מוצר", "שם מוצר", "קטגוריה", "כמות", "סכום (₪)", "תאריך"};
            default:
                return new String[]{"נתונים"};
        }
    }
    
    private void parseAndDisplayReport(String jsonData) {
        // הסרת "OK;" מהתחלה
        if (jsonData.startsWith("OK;")) {
            jsonData = jsonData.substring(3);
        }
        
        try {
            // פשוט מאוד - נפרס את ה-JSON באופן בסיסי
            // format: {"reportType":"...","entries":[{...},...]}
            
            // בואו נמצא את ה-entries
            int entriesStart = jsonData.indexOf("\"entries\":[");
            if (entriesStart == -1) {
                JOptionPane.showMessageDialog(this,
                        "פורמט דוח לא תקין: entries לא נמצא",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // חילוץ ה-entries array
            int arrayStart = entriesStart + 10; // אחרי "entries":[
            int braceLevel = 0;
            int arrayEnd = -1;
            boolean inString = false;
            
            for (int i = arrayStart; i < jsonData.length(); i++) {
                char c = jsonData.charAt(i);
                if (c == '"' && (i == 0 || jsonData.charAt(i - 1) != '\\')) {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') braceLevel++;
                    else if (c == '}') braceLevel--;
                    else if (c == ']' && braceLevel == 0) {
                        arrayEnd = i;
                        break;
                    }
                }
            }
            
            if (arrayEnd == -1) {
                JOptionPane.showMessageDialog(this,
                        "פורמט דוח לא תקין: סוף array לא נמצא",
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String entriesJson = jsonData.substring(arrayStart + 1, arrayEnd); // +1 לדילוג על [
            parseJsonEntries(entriesJson);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "שגיאה בפרסור דוח: " + e.getMessage(),
                    "שגיאה",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void parseJsonEntries(String entriesJson) {
        // פרסור entries - חיפוש כל אובייקט {...}
        entriesJson = entriesJson.trim();
        if (entriesJson.isEmpty()) {
            return; // אין entries
        }
        
        int braceLevel = 0;
        int entryStart = -1;
        boolean inString = false;
        
        for (int i = 0; i < entriesJson.length(); i++) {
            char c = entriesJson.charAt(i);
            if (c == '"' && (i == 0 || entriesJson.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    if (braceLevel == 0) {
                        entryStart = i + 1; // אחרי ה-{
                    }
                    braceLevel++;
                } else if (c == '}') {
                    braceLevel--;
                    if (braceLevel == 0 && entryStart != -1) {
                        // מצאנו entry מלא
                        String entryJson = entriesJson.substring(entryStart, i);
                        parseSingleEntry(entryJson);
                        entryStart = -1;
                    }
                }
            }
        }
    }
    
    private void parseSingleEntry(String entryJson) {
        // פרסור entry אחד
        // format: "key":"value","key2":value2
        
        Object[] row = new Object[tableModel.getColumnCount()];
        
        // לפי סוג הדוח, נחלץ את הערכים הרלוונטיים
        if (reportType.equals("SALES_BY_BRANCH")) {
            String branchId = extractJsonValue(entryJson, "branchId");
            String quantity = extractJsonValue(entryJson, "quantity");
            String totalRevenue = extractJsonValue(entryJson, "totalRevenue");
            row[0] = branchId;
            row[1] = quantity;
            row[2] = String.format("%.2f", Double.parseDouble(totalRevenue));
        } else if (reportType.equals("SALES_BY_PRODUCT")) {
            String branchId = extractJsonValue(entryJson, "branchId");
            String productId = extractJsonValue(entryJson, "productId");
            String productName = extractJsonValue(entryJson, "productName");
            String category = extractJsonValue(entryJson, "category");
            String quantity = extractJsonValue(entryJson, "quantity");
            String totalRevenue = extractJsonValue(entryJson, "totalRevenue");
            String date = extractJsonValue(entryJson, "date");
            row[0] = branchId;
            row[1] = productId;
            row[2] = productName;
            row[3] = category;
            row[4] = quantity;
            row[5] = String.format("%.2f", Double.parseDouble(totalRevenue));
            row[6] = date;
        } else if (reportType.equals("SALES_BY_CATEGORY")) {
            String category = extractJsonValue(entryJson, "category");
            String quantity = extractJsonValue(entryJson, "quantity");
            String totalRevenue = extractJsonValue(entryJson, "totalRevenue");
            row[0] = category;
            row[1] = quantity;
            row[2] = String.format("%.2f", Double.parseDouble(totalRevenue));
        } else if (reportType.equals("DAILY_SALES")) {
            String branchId = extractJsonValue(entryJson, "branchId");
            String productId = extractJsonValue(entryJson, "productId");
            String productName = extractJsonValue(entryJson, "productName");
            String category = extractJsonValue(entryJson, "category");
            String quantity = extractJsonValue(entryJson, "quantity");
            String totalRevenue = extractJsonValue(entryJson, "totalRevenue");
            String date = extractJsonValue(entryJson, "date");
            row[0] = branchId;
            row[1] = productId;
            row[2] = productName;
            row[3] = category;
            row[4] = quantity;
            row[5] = String.format("%.2f", Double.parseDouble(totalRevenue));
            row[6] = date;
        }
        
        tableModel.addRow(row);
    }
    
    private String extractJsonValue(String json, String key) {
        // חילוץ ערך מ-JSON
        // מחפש "key":"value" או "key":value
        
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return "";
        }
        
        int valueStart = keyIndex + searchKey.length();
        // דילוג על רווחים
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) {
            return "";
        }
        
        // אם זה string (מתחיל ב-")
        if (json.charAt(valueStart) == '"') {
            valueStart++; // דילוג על ה-"
            int valueEnd = valueStart;
            // חיפוש ה-" הבא (אבל לא escaped)
            while (valueEnd < json.length()) {
                if (json.charAt(valueEnd) == '"' && (valueEnd == valueStart || json.charAt(valueEnd - 1) != '\\')) {
                    break;
                }
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd);
        } else {
            // זה מספר או משהו אחר
            int valueEnd = valueStart;
            while (valueEnd < json.length() && 
                   (Character.isDigit(json.charAt(valueEnd)) || 
                    json.charAt(valueEnd) == '.' || 
                    json.charAt(valueEnd) == '-' ||
                    json.charAt(valueEnd) == 'e' ||
                    json.charAt(valueEnd) == 'E')) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd);
        }
    }
    
    private void exportToRTF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("שמור דוח כ-RTF (Word)");
        fileChooser.setSelectedFile(new File("report.rtf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String fileName = fileToSave.getAbsolutePath();
            if (!fileName.toLowerCase().endsWith(".rtf")) {
                fileName += ".rtf";
            }
            
            try {
                createRTFDocument(fileName);
                JOptionPane.showMessageDialog(this,
                        "הדוח נשמר בהצלחה! ניתן לפתוח ב-Word.",
                        "הצלחה",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "שגיאה בשמירת קובץ RTF: " + e.getMessage(),
                        "שגיאה",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void createRTFDocument(String fileName) throws IOException {
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(fileName), "Windows-1255")) {
            // RTF Header עם תמיכה בעברית (Windows-1255 encoding)
            writer.write("{\\rtf1\\ansi\\ansicpg1255\\deff0\\nouicompat\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset177 Arial;}}\n");
            writer.write("{\\*\\generator FinalProject Report}\\viewkind4\\uc1\n");
            writer.write("\\pard\\sa200\\sl276\\slmult1\\f0\\fs22\\lang1037\\b\\fs28 ");
            
            // כותרת - עם Unicode escape לעברית
            writer.write(escapeRTFWithUnicode("דוח: " + getReportTypeName(reportType)));
            writer.write("\\b0\\par\\par\n");
            
            // טבלה
            int rowCount = tableModel.getRowCount();
            int colCount = tableModel.getColumnCount();
            
            // כותרות
            writer.write("\\trowd\\trgaph108\\trleft-108\\trrh\n");
            for (int col = 0; col < colCount; col++) {
                writer.write("\\cellx" + ((col + 1) * 2000) + "\n");
            }
            writer.write("\\pard\\intbl\\itap1\\b ");
            for (int col = 0; col < colCount; col++) {
                writer.write(escapeRTFWithUnicode(tableModel.getColumnName(col)));
                if (col < colCount - 1) {
                    writer.write("\\cell ");
                }
            }
            writer.write("\\b0\\cell\\row\n");
            
            // שורות נתונים
            for (int row = 0; row < rowCount; row++) {
                writer.write("\\trowd\\trgaph108\\trleft-108\\trrh\n");
                for (int col = 0; col < colCount; col++) {
                    writer.write("\\cellx" + ((col + 1) * 2000) + "\n");
                }
                writer.write("\\pard\\intbl\\itap1 ");
                for (int col = 0; col < colCount; col++) {
                    Object value = tableModel.getValueAt(row, col);
                    String text = value != null ? escapeRTFWithUnicode(value.toString()) : "";
                    writer.write(text);
                    if (col < colCount - 1) {
                        writer.write("\\cell ");
                    }
                }
                writer.write("\\cell\\row\n");
            }
            
            writer.write("\\pard\\sa200\\sl276\\slmult1\\par\n");
            writer.write("}\n");
        }
    }
    
    private String escapeRTFWithUnicode(String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c < 128) {
                // ASCII characters - escape special RTF characters
                if (c == '\\') {
                    result.append("\\\\");
                } else if (c == '{') {
                    result.append("\\{");
                } else if (c == '}') {
                    result.append("\\}");
                } else if (c == '\n') {
                    result.append("\\par\n");
                } else {
                    result.append(c);
                }
            } else {

                char backslash = '\\';
                result.append(backslash).append("u").append((int)c).append("?");
            }
        }
        return result.toString();
    }
}
