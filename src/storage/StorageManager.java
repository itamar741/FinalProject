package storage;

import model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages saving and loading of all system data to/from JSON files.
 * Uses manual JSON serialization without external dependencies.
 * Creates the data/ directory if it doesn't exist.
 * Implements DTO Pattern - converts between Model objects and *Data DTOs for storage.
 * 
 * @author FinalProject
 */
public class StorageManager {
    
    /** Directory where all data files are stored */
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String EMPLOYEES_FILE = DATA_DIR + "/employees.json";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.json";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.json";
    private static final String INVENTORY_FILE = DATA_DIR + "/inventory.json";
    private static final String SALES_FILE = DATA_DIR + "/sales.json";
    private static final String LOGS_FILE = DATA_DIR + "/logs.json";
    private static final String BRANCHES_FILE = DATA_DIR + "/branches.json";
    private static final String DISCOUNTS_FILE = DATA_DIR + "/discounts.json";
    
    private JsonSerializer jsonSerializer;
    
    /**
     * Constructs a new StorageManager.
     * Creates the data/ directory if it doesn't exist.
     */
    public StorageManager() {
        this.jsonSerializer = new JsonSerializer();
        
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
    
    // ========== Users Storage ==========
    
    /**
     * Saves users to JSON file.
     * Converts User objects to UserData DTOs before serialization.
     * 
     * @param users a Map of username to User
     * @throws IOException if file write fails
     */
    public void saveUsers(Map<String, User> users) throws IOException {
        Map<String, UserData> userDataMap = new HashMap<>();
        for (User user : users.values()) {
            userDataMap.put(user.getUsername(), new UserData(user));
        }
        String json = jsonSerializer.toJson(userDataMap);
        Files.write(Paths.get(USERS_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads users from JSON file.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of username to UserData
     * @throws IOException if file read fails
     */
    public Map<String, UserData> loadUsers() throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonUsers(json);
    }
    
    // ========== Employees Storage ==========
    
    /**
     * Saves employees to JSON file.
     * Converts Employee objects to EmployeeData DTOs before serialization.
     * 
     * @param employees a Map of employeeNumber to Employee
     * @throws IOException if file write fails
     */
    public void saveEmployees(Map<String, Employee> employees) throws IOException {
        Map<String, EmployeeData> employeeDataMap = new HashMap<>();
        for (Employee emp : employees.values()) {
            employeeDataMap.put(emp.getEmployeeNumber(), new EmployeeData(emp));
        }
        String json = jsonSerializer.toJson(employeeDataMap);
        Files.write(Paths.get(EMPLOYEES_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads employees from JSON file.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of employeeNumber to EmployeeData
     * @throws IOException if file read fails
     */
    public Map<String, EmployeeData> loadEmployees() throws IOException {
        File file = new File(EMPLOYEES_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(EMPLOYEES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonEmployees(json);
    }
    
    // ========== Customers Storage ==========
    
    /**
     * Saves customers to JSON file.
     * Converts Customer objects to CustomerData DTOs before serialization.
     * 
     * @param customers a Map of idNumber to Customer
     * @throws IOException if file write fails
     */
    public void saveCustomers(Map<String, Customer> customers) throws IOException {
        Map<String, CustomerData> customerDataMap = new HashMap<>();
        for (Map.Entry<String, Customer> entry : customers.entrySet()) {
            customerDataMap.put(entry.getKey(), new CustomerData(entry.getValue()));
        }
        String json = jsonSerializer.toJson(customerDataMap);
        Files.write(Paths.get(CUSTOMERS_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads customers from JSON file.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of idNumber to CustomerData
     * @throws IOException if file read fails
     */
    public Map<String, CustomerData> loadCustomers() throws IOException {
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(CUSTOMERS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonCustomers(json);
    }
    
    // ========== Products Storage ==========
    
    /**
     * Saves products to JSON file.
     * Converts Product objects to ProductData DTOs before serialization.
     * 
     * @param products a Map of productId to Product
     * @throws IOException if file write fails
     */
    public void saveProducts(Map<String, Product> products) throws IOException {
        Map<String, ProductData> productDataMap = new HashMap<>();
        for (Product product : products.values()) {
            productDataMap.put(product.getProductId(), new ProductData(product));
        }
        String json = jsonSerializer.toJson(productDataMap);
        Files.write(Paths.get(PRODUCTS_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads products from JSON file.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of productId to ProductData
     * @throws IOException if file read fails
     */
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
     * Saves inventory from all branches to JSON file.
     * Converts Inventory objects to a Map structure (branchId -> productId -> quantity).
     * 
     * @param branches a Map of branchId to Branch
     * @throws IOException if file write fails
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
     * Loads inventory from JSON file.
     * Returns a Map structure: branchId -> productId -> quantity.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of branchId to Map of productId to quantity
     * @throws IOException if file read fails
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
    
    /**
     * Saves sales to JSON file.
     * Converts Sale objects to SaleData DTOs before serialization.
     * 
     * @param sales a List of Sale objects
     * @throws IOException if file write fails
     */
    public void saveSales(List<Sale> sales) throws IOException {
        List<SaleData> saleDataList = new ArrayList<>();
        for (Sale sale : sales) {
            saleDataList.add(new SaleData(sale));
        }
        String json = jsonSerializer.toJson(saleDataList);
        Files.write(Paths.get(SALES_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads sales from JSON file.
     * Returns empty list if file doesn't exist.
     * 
     * @return a List of SaleData objects
     * @throws IOException if file read fails
     */
    public List<SaleData> loadSales() throws IOException {
        File file = new File(SALES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(SALES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonSales(json);
    }
    
    // ========== Logs Storage ==========
    
    /**
     * Saves logs to JSON file.
     * 
     * @param logs a List of LogEntry objects
     * @throws IOException if file write fails
     */
    public void saveLogs(List<LogEntry> logs) throws IOException {
        String json = jsonSerializer.toJson(logs);
        Files.write(Paths.get(LOGS_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads logs from JSON file.
     * Returns empty list if file doesn't exist.
     * 
     * @return a List of LogEntry objects
     * @throws IOException if file read fails
     */
    public List<LogEntry> loadLogs() throws IOException {
        File file = new File(LOGS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(LOGS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonLogs(json);
    }
    
    // ========== Branches Storage ==========
    
    /**
     * Saves branch IDs to JSON file.
     * 
     * @param branches a List of branch IDs
     * @throws IOException if file write fails
     */
    public void saveBranches(List<String> branches) throws IOException {
        String json = jsonSerializer.toJson(branches);
        Files.write(Paths.get(BRANCHES_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads branch IDs from JSON file.
     * Returns empty list if file doesn't exist.
     * 
     * @return a List of branch IDs
     * @throws IOException if file read fails
     */
    public List<String> loadBranches() throws IOException {
        File file = new File(BRANCHES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(BRANCHES_FILE)), "UTF-8");
        return jsonSerializer.fromJsonBranches(json);
    }
    
    // ========== Discounts Storage ==========
    
    /**
     * Saves discount percentages to JSON file.
     * 
     * @param discounts a Map of customerType to discount percentage
     * @throws IOException if file write fails
     */
    public void saveDiscounts(Map<String, Double> discounts) throws IOException {
        String json = jsonSerializer.toJson(discounts);
        Files.write(Paths.get(DISCOUNTS_FILE), json.getBytes("UTF-8"));
    }
    
    /**
     * Loads discount percentages from JSON file.
     * Returns empty map if file doesn't exist.
     * 
     * @return a Map of customerType to discount percentage
     * @throws IOException if file read fails
     */
    public Map<String, Double> loadDiscounts() throws IOException {
        File file = new File(DISCOUNTS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        String json = new String(Files.readAllBytes(Paths.get(DISCOUNTS_FILE)), "UTF-8");
        return jsonSerializer.fromJsonDiscounts(json);
    }
}
