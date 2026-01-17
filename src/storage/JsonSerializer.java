package storage;

import model.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * מחלקה פשוטה ל-JSON serialization/deserialization ללא תלויות חיצוניות
 * משתמשת בגישה פשוטה של string manipulation
 */
public class JsonSerializer {
    
    public String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof Map) {
            return toJsonMap((Map<?, ?>) obj);
        } else if (obj instanceof List) {
            return toJsonList((List<?>) obj);
        } else if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else {
            // Object - משתמש ב-reflection פשוט דרך toString או DTO
            return toJsonObject(obj);
        }
    }
    
    private String toJsonMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",\n");
            sb.append("  \"").append(escapeJson(entry.getKey().toString())).append("\": ");
            sb.append(toJson(entry.getValue()).replace("\n", "\n  "));
            first = false;
        }
        sb.append("\n}");
        return sb.toString();
    }
    
    private String toJsonList(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",\n");
            String itemJson = toJson(list.get(i));
            sb.append("  ").append(itemJson.replace("\n", "\n  "));
        }
        sb.append("\n]");
        return sb.toString();
    }
    
    private String toJsonObject(Object obj) {
        // משתמש ב-reflection פשוט דרך getters או fields
        // כאן נשתמש בגישה פשוטה יותר - serialize ידני לפי סוג
        if (obj instanceof UserData) {
            return toJsonUserData((UserData) obj);
        } else if (obj instanceof EmployeeData) {
            return toJsonEmployeeData((EmployeeData) obj);
        } else if (obj instanceof CustomerData) {
            return toJsonCustomerData((CustomerData) obj);
        } else if (obj instanceof ProductData) {
            return toJsonProductData((ProductData) obj);
        } else if (obj instanceof SaleData) {
            return toJsonSaleData((SaleData) obj);
        } else if (obj instanceof LogEntry) {
            return toJsonLogEntry((LogEntry) obj);
        } else if (obj instanceof Product) {
            return toJsonProduct((Product) obj);
        }
        return "{}";
    }
    
    private String toJsonUserData(UserData user) {
        return String.format(
            "{\n  \"username\": \"%s\",\n  \"password\": \"%s\",\n  \"employeeNumber\": \"%s\",\n  \"userType\": \"%s\",\n  \"branchId\": \"%s\",\n  \"active\": %s,\n  \"mustChangePassword\": %s\n}",
            escapeJson(user.username), escapeJson(user.password), escapeJson(user.employeeNumber),
            escapeJson(user.userType), escapeJson(user.branchId), user.active, user.mustChangePassword
        );
    }
    
    private String toJsonEmployeeData(EmployeeData emp) {
        return String.format(
            "{\n  \"fullName\": \"%s\",\n  \"idNumber\": \"%s\",\n  \"phone\": \"%s\",\n  \"bankAccount\": \"%s\",\n  \"employeeNumber\": \"%s\",\n  \"role\": \"%s\",\n  \"branchId\": \"%s\",\n  \"active\": %s\n}",
            escapeJson(emp.fullName), escapeJson(emp.idNumber), escapeJson(emp.phone),
            escapeJson(emp.bankAccount), escapeJson(emp.employeeNumber), escapeJson(emp.role),
            escapeJson(emp.branchId), emp.active
        );
    }
    
    private String toJsonCustomerData(CustomerData cust) {
        return String.format(
            "{\n  \"fullName\": \"%s\",\n  \"idNumber\": \"%s\",\n  \"phone\": \"%s\",\n  \"customerType\": \"%s\"\n}",
            escapeJson(cust.fullName), escapeJson(cust.idNumber), escapeJson(cust.phone), escapeJson(cust.customerType)
        );
    }
    
    private String toJsonProductData(ProductData prod) {
        return String.format(
            "{\n  \"productId\": \"%s\",\n  \"name\": \"%s\",\n  \"category\": \"%s\",\n  \"price\": %.2f,\n  \"active\": %s\n}",
            escapeJson(prod.productId), escapeJson(prod.name), escapeJson(prod.category), prod.price, prod.active
        );
    }
    
    private String toJsonProduct(Product prod) {
        return String.format(
            "{\n  \"productId\": \"%s\",\n  \"name\": \"%s\",\n  \"category\": \"%s\",\n  \"price\": %.2f,\n  \"active\": %s\n}",
            escapeJson(prod.getProductId()), escapeJson(prod.getName()), escapeJson(prod.getCategory()),
            prod.getPrice(), prod.isActive()
        );
    }
    
    private String toJsonSaleData(SaleData sale) {
        return String.format(
            "{\n  \"productId\": \"%s\",\n  \"productName\": \"%s\",\n  \"productCategory\": \"%s\",\n  \"productPrice\": %.2f,\n  \"quantity\": %d,\n  \"branchId\": \"%s\",\n  \"employeeNumber\": \"%s\",\n  \"customerId\": \"%s\",\n  \"dateTime\": \"%s\",\n  \"basePrice\": %.2f,\n  \"finalPrice\": %.2f\n}",
            escapeJson(sale.productId), escapeJson(sale.productName), escapeJson(sale.productCategory),
            sale.productPrice, sale.quantity, escapeJson(sale.branchId), escapeJson(sale.employeeNumber),
            escapeJson(sale.customerId), escapeJson(sale.dateTime), sale.basePrice, sale.finalPrice
        );
    }
    
    private String toJsonLogEntry(LogEntry log) {
        String chatIdJson = log.getChatId() != null ? 
            String.format(",\n  \"chatId\": \"%s\"", escapeJson(log.getChatId())) : "";
        return String.format(
            "{\n  \"actionType\": \"%s\",\n  \"description\": \"%s\",\n  \"dateTime\": \"%s\"%s\n}",
            escapeJson(log.getActionType()), escapeJson(log.getDescription()), escapeJson(log.getDateTime()), chatIdJson
        );
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // ========== Deserialization Methods ==========
    
    public Map<String, UserData> fromJsonUsers(String json) {
        return parseMap(json, this::parseUserData);
    }
    
    // זה קוד מורכב, בואו נעשה משהו פשוט יותר - נשתמש בספריית JSON חיצונית או נשנה גישה
    // למעשה, בואו נשתמש בגישה אחרת - נשמור ב-format פשוט יותר או נשתמש ב-Java serialization
    
    // בינתיים, בואו נשתמש בגישה פשוטה יותר - נשמור JSON ידני פשוט
    
    private UserData parseUserData(String json) {
        UserData user = new UserData();
        user.username = extractString(json, "username");
        user.password = extractString(json, "password");
        user.employeeNumber = extractString(json, "employeeNumber");
        user.userType = extractString(json, "userType");
        user.branchId = extractString(json, "branchId");
        user.active = extractBoolean(json, "active");
        user.mustChangePassword = extractBoolean(json, "mustChangePassword");
        return user;
    }
    
    // ... (המשך עם parse methods אחרים)
    
    // זה מתחיל להיות מסובך מדי. בואו נשנה גישה - נשתמש ב-Java Serializable או format פשוט יותר
    // למעשה, בואו ניצור גרסה פשוטה יותר עם JSON parsing בסיסי
    
    private String extractString(String json, String key) {
        // Find "key": "value" pattern, handling escaped quotes
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return "";
    }
    
    private boolean extractBoolean(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return false;
    }
    
    private double extractDouble(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    private int extractInt(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private String unescapeJson(String str) {
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
    
    // Methods נוספים ל-deserialization...
    public Map<String, EmployeeData> fromJsonEmployees(String json) {
        return parseMap(json, this::parseEmployeeData);
    }
    
    private EmployeeData parseEmployeeData(String json) {
        EmployeeData emp = new EmployeeData();
        emp.fullName = extractString(json, "fullName");
        emp.idNumber = extractString(json, "idNumber");
        emp.phone = extractString(json, "phone");
        emp.bankAccount = extractString(json, "bankAccount");
        emp.employeeNumber = extractString(json, "employeeNumber");
        emp.role = extractString(json, "role");
        emp.branchId = extractString(json, "branchId");
        emp.active = extractBoolean(json, "active");
        return emp;
    }
    
    public Map<String, CustomerData> fromJsonCustomers(String json) {
        return parseMap(json, this::parseCustomerData);
    }
    
    public Map<String, ProductData> fromJsonProducts(String json) {
        return parseMap(json, this::parseProductData);
    }
    
    private CustomerData parseCustomerData(String json) {
        CustomerData cust = new CustomerData();
        cust.fullName = extractString(json, "fullName");
        cust.idNumber = extractString(json, "idNumber");
        cust.phone = extractString(json, "phone");
        cust.customerType = extractString(json, "customerType");
        return cust;
    }
    
    // Generic map parser helper
    private <T> Map<String, T> parseMap(String json, java.util.function.Function<String, T> parser) {
        Map<String, T> result = new HashMap<>();
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return result;
        }
        
        // Parse JSON object manually with proper nested object handling
        int braceLevel = 0;
        int startPos = json.indexOf('{');
        if (startPos < 0) return result;
        
        int i = startPos + 1;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;
            
            // Find key
            if (json.charAt(i) != '"') break;
            int keyStart = i + 1;
            int keyEnd = json.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = json.substring(keyStart, keyEnd);
            
            // Skip to ':'
            i = keyEnd + 1;
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++; // Skip ':'
            
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;
            
            // Find value object
            if (json.charAt(i) != '{') break;
            int valueStart = i;
            braceLevel = 1;
            i++;
            
            while (i < json.length() && braceLevel > 0) {
                char c = json.charAt(i);
                if (c == '{' && json.charAt(i-1) != '\\') braceLevel++;
                else if (c == '}' && json.charAt(i-1) != '\\') braceLevel--;
                i++;
            }
            
            if (braceLevel == 0) {
                String valueJson = json.substring(valueStart, i);
                try {
                    T obj = parser.apply(valueJson);
                    result.put(key, obj);
                } catch (Exception e) {
                    // Skip invalid entries
                }
            }
            
            // Skip to next entry or end
            while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }
        
        return result;
    }
    
    private ProductData parseProductData(String json) {
        ProductData prod = new ProductData();
        prod.productId = extractString(json, "productId");
        prod.name = extractString(json, "name");
        prod.category = extractString(json, "category");
        prod.price = extractDouble(json, "price");
        prod.active = extractBoolean(json, "active");
        return prod;
    }
    
    public Map<String, Map<String, Integer>> fromJsonInventory(String json) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return result;
        }
        
        // Parse nested map: {"branchId": {"productId": quantity, ...}}
        int i = json.indexOf('{');
        if (i < 0) return result;
        i++;
        
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length() || json.charAt(i) == '}') break;
            
            // Find branch key
            if (json.charAt(i) != '"') break;
            int keyStart = i + 1;
            int keyEnd = json.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String branchId = json.substring(keyStart, keyEnd);
            
            // Skip to ':'
            i = keyEnd + 1;
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++;
            
            // Find nested object
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length() || json.charAt(i) != '{') break;
            
            int objStart = i;
            int braceLevel = 1;
            i++;
            
            while (i < json.length() && braceLevel > 0) {
                char c = json.charAt(i);
                if (c == '{' && (i == 0 || json.charAt(i-1) != '\\')) braceLevel++;
                else if (c == '}' && (i == 0 || json.charAt(i-1) != '\\')) braceLevel--;
                i++;
            }
            
            if (braceLevel == 0) {
                String nestedJson = json.substring(objStart, i);
                Map<String, Integer> inventory = parseInventoryMap(nestedJson);
                result.put(branchId, inventory);
            }
            
            // Skip to next entry
            while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }
        
        return result;
    }
    
    private Map<String, Integer> parseInventoryMap(String json) {
        Map<String, Integer> result = new HashMap<>();
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String productId = matcher.group(1);
            int quantity = Integer.parseInt(matcher.group(2));
            result.put(productId, quantity);
        }
        return result;
    }
    
    public List<SaleData> fromJsonSales(String json) {
        return parseList(json, this::parseSaleData);
    }
    
    private <T> List<T> parseList(String json, java.util.function.Function<String, T> parser) {
        List<T> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return result;
        }
        
        // Parse JSON array: [{...}, {...}]
        int i = json.indexOf('[');
        if (i < 0) return result;
        i++;
        
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length() || json.charAt(i) == ']') break;
            
            // Find object
            if (json.charAt(i) != '{') break;
            int objStart = i;
            int braceLevel = 1;
            i++;
            
            while (i < json.length() && braceLevel > 0) {
                char c = json.charAt(i);
                if (c == '{' && (i == 0 || json.charAt(i-1) != '\\')) braceLevel++;
                else if (c == '}' && (i == 0 || json.charAt(i-1) != '\\')) braceLevel--;
                i++;
            }
            
            if (braceLevel == 0) {
                String objJson = json.substring(objStart, i);
                try {
                    T obj = parser.apply(objJson);
                    result.add(obj);
                } catch (Exception e) {
                    // Skip invalid entries
                }
            }
            
            // Skip to next entry
            while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != ']') i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }
        
        return result;
    }
    
    private SaleData parseSaleData(String json) {
        SaleData sale = new SaleData();
        sale.productId = extractString(json, "productId");
        sale.productName = extractString(json, "productName");
        sale.productCategory = extractString(json, "productCategory");
        sale.productPrice = extractDouble(json, "productPrice");
        sale.quantity = extractInt(json, "quantity");
        sale.branchId = extractString(json, "branchId");
        sale.employeeNumber = extractString(json, "employeeNumber");
        sale.customerId = extractString(json, "customerId");
        sale.dateTime = extractString(json, "dateTime");
        // טעינת basePrice ו-finalPrice (אם קיימים)
        sale.basePrice = extractDouble(json, "basePrice");
        sale.finalPrice = extractDouble(json, "finalPrice");
        // אם לא נמצאו (0), נחשב אותם (למקרה של מכירות ישנות)
        if (sale.basePrice == 0 && sale.finalPrice == 0) {
            sale.basePrice = sale.productPrice * sale.quantity;
            sale.finalPrice = sale.basePrice; // ללא הנחה (לא נדע מה היה)
        }
        return sale;
    }
    
    public List<LogEntry> fromJsonLogs(String json) {
        return parseList(json, this::parseLogEntry);
    }
    
    private LogEntry parseLogEntry(String json) {
        String actionType = extractString(json, "actionType");
        String description = extractString(json, "description");
        String dateTime = extractString(json, "dateTime");
        String chatId = extractString(json, "chatId");
        // אם chatId ריק או null, נשתמש בקונסטרקטור ללא chatId
        if (chatId == null || chatId.isEmpty()) {
            return new LogEntry(actionType, description, dateTime);
        }
        return new LogEntry(actionType, description, dateTime, chatId);
    }
    
    public List<String> fromJsonBranches(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return result;
        }
        
        // Parse JSON array of strings: ["B1", "B2"]
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
}
