package storage;

import model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * מנהל שמירה וטעינה של כל המידע במערכת לקבצי JSON
 * משתמש ב-JSON serialization ידנית ללא תלויות חיצוניות
 */
public class StorageManager {
    
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String EMPLOYEES_FILE = DATA_DIR + "/employees.json";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.json";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.json";
    private static final String INVENTORY_FILE = DATA_DIR + "/inventory.json";
    private static final String SALES_FILE = DATA_DIR + "/sales.json";
    private static final String LOGS_FILE = DATA_DIR + "/logs.json";
    private static final String BRANCHES_FILE = DATA_DIR + "/branches.json";
    
    private JsonSerializer jsonSerializer;
    
    public StorageManager() {
        this.jsonSerializer = new JsonSerializer();
        
        // יצירת תיקיית data אם לא קיימת
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
    
    // ========== Users Storage ==========
    
    public void saveUsers(Map<String, User> users) throws IOException {
        Map<String, UserData> userDataMap = new HashMap<>();
        for (User user : users.values()) {
            userDataMap.put(user.getUsername(), new UserData(user));
        }
        String json = jsonSerializer.toJson(userDataMap);
        Files.write(Paths.get(USERS_FILE), json.getBytes("UTF-8"));
    }
    
    public Map<String, UserData> loadUsers() throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonUsers(json);
    }
    
    // ========== Employees Storage ==========
    
    public void saveEmployees(Map<String, Employee> employees) throws IOException {
        Map<String, EmployeeData> employeeDataMap = new HashMap<>();
        for (Employee emp : employees.values()) {
            employeeDataMap.put(emp.getEmployeeNumber(), new EmployeeData(emp));
        }
        String json = jsonSerializer.toJson(employeeDataMap);
        Files.write(Paths.get(EMPLOYEES_FILE), json.getBytes("UTF-8"));
    }
    
    public Map<String, EmployeeData> loadEmployees() throws IOException {
        File file = new File(EMPLOYEES_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(EMPLOYEES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonEmployees(json);
    }
    
    // ========== Customers Storage ==========
    
    public void saveCustomers(Map<String, Customer> customers) throws IOException {
        Map<String, CustomerData> customerDataMap = new HashMap<>();
        for (Map.Entry<String, Customer> entry : customers.entrySet()) {
            customerDataMap.put(entry.getKey(), new CustomerData(entry.getValue()));
        }
        String json = jsonSerializer.toJson(customerDataMap);
        Files.write(Paths.get(CUSTOMERS_FILE), json.getBytes("UTF-8"));
    }
    
    public Map<String, CustomerData> loadCustomers() throws IOException {
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(CUSTOMERS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonCustomers(json);
    }
    
    // ========== Products Storage ==========
    
    public void saveProducts(Map<String, Product> products) throws IOException {
        Map<String, ProductData> productDataMap = new HashMap<>();
        for (Product product : products.values()) {
            productDataMap.put(product.getProductId(), new ProductData(product));
        }
        String json = jsonSerializer.toJson(productDataMap);
        Files.write(Paths.get(PRODUCTS_FILE), json.getBytes("UTF-8"));
    }
    
    public Map<String, ProductData> loadProducts() throws IOException {
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(PRODUCTS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonProducts(json);
    }
    
    // ========== Inventory Storage ==========
    
    /**
     * שמירת מלאי מכל הסניפים
     * @param branches Map של branchId -> Branch
     */
    public void saveInventory(Map<String, Branch> branches) throws IOException {
        Map<String, Map<String, Integer>> inventoryData = new HashMap<>();
        
        for (Map.Entry<String, Branch> entry : branches.entrySet()) {
            String branchId = entry.getKey();
            Branch branch = entry.getValue();
            Inventory inventory = branch.getInventory();
            
            // המרת Inventory ל-Map של productId -> quantity
            Map<String, Integer> branchInventory = new HashMap<>();
            Map<Product, Integer> products = inventory.getAllProducts();
            
            for (Map.Entry<Product, Integer> productEntry : products.entrySet()) {
                branchInventory.put(productEntry.getKey().getProductId(), productEntry.getValue());
            }
            
            inventoryData.put(branchId, branchInventory);
        }
        
        String json = jsonSerializer.toJson(inventoryData);
        Files.write(Paths.get(INVENTORY_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * טעינת מלאי - מחזיר Map של branchId -> Map של productId -> quantity
     */
    public Map<String, Map<String, Integer>> loadInventory() throws IOException {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(INVENTORY_FILE)), "UTF-8");
        return jsonSerializer.fromJsonInventory(json);
    }
    
    // ========== Sales Storage ==========
    
    public void saveSales(List<Sale> sales) throws IOException {
        List<SaleData> saleDataList = new ArrayList<>();
        for (Sale sale : sales) {
            saleDataList.add(new SaleData(sale));
        }
        String json = jsonSerializer.toJson(saleDataList);
        Files.write(Paths.get(SALES_FILE), json.getBytes("UTF-8"));
    }
    
    public List<SaleData> loadSales() throws IOException {
        File file = new File(SALES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(SALES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonSales(json);
    }
    
    // ========== Logs Storage ==========
    
    public void saveLogs(List<LogEntry> logs) throws IOException {
        String json = jsonSerializer.toJson(logs);
        Files.write(Paths.get(LOGS_FILE), json.getBytes("UTF-8"));
    }
    
    public List<LogEntry> loadLogs() throws IOException {
        File file = new File(LOGS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(LOGS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonLogs(json);
    }
    
    // ========== Branches Storage ==========
    
    public void saveBranches(List<String> branches) throws IOException {
        String json = jsonSerializer.toJson(branches);
        Files.write(Paths.get(BRANCHES_FILE), json.getBytes("UTF-8"));
    }
    
    public List<String> loadBranches() throws IOException {
        File file = new File(BRANCHES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(BRANCHES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonBranches(json);
    }
}
