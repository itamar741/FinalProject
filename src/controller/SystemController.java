package controller;
import model.Product;
import model.managers.*;
import model.managers.PermissionChecker;
import model.exceptions.*;
import model.Customer;
import model.LogEntry;
import model.Sale;
import model.Branch;
import model.Session;
import model.User;
import model.Employee;
import model.Inventory;
import model.ReportEntry;
import model.Product;
import model.ChatMessage;
import model.ChatSession;
import model.ChatUserStatus;
import model.ChatRequest;
import storage.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.io.FileWriter;

/**
 * Central controller for the entire system.
 * Acts as a Facade Pattern - provides a unified API for all system operations.
 * Mediates between GUI/Server and Managers, handles data loading/saving, and ensures logging.
 * All business operations go through this controller, which delegates to appropriate Managers.
 * 
 * @author FinalProject
 */
public class SystemController {

    private CustomerManager customerManager;
    private InventoryManager inventoryManager;
    private SalesManager salesManager;
    private LogManager logManager;
    private ProductManager productManager;
    private BranchManager branchManager;
    private AuthenticationManager authenticationManager;
    private SessionManager sessionManager;
    private EmployeeManager employeeManager;
    private ChatManager chatManager;
    private DiscountManager discountManager;
    private StorageManager storageManager;

    /**
     * Constructs a new SystemController.
     * Initializes all Managers and loads data from storage.
     */
    public SystemController() {
        storageManager = new StorageManager();
        
        customerManager = new CustomerManager();
        inventoryManager = new InventoryManager();
        salesManager = new SalesManager();
        logManager = new LogManager();
        productManager = new ProductManager();
        branchManager = new BranchManager();
        authenticationManager = new AuthenticationManager();
        sessionManager = new SessionManager();
        employeeManager = new EmployeeManager();
        chatManager = new ChatManager(sessionManager);
        discountManager = new DiscountManager();
        
        // Set DiscountManager for Customer classes
        model.Customer.setDiscountManager(discountManager);
        
        // Load data from existing storage
        loadAllData();
    }
    
    /**
     * Loads all data from storage.
     * Called during initialization. Continues with empty data if loading fails.
     */
    private void loadAllData() {
        try {
            loadUsers();
            loadEmployees();
            rebuildUsernameMappings();  // Rebuild username->employeeNumber mappings from existing data
            loadCustomers();
            loadProducts();
            loadBranches();
            loadInventory();
            loadSales();
            loadLogs();
            loadDiscounts();
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            // ממשיכים עם נתונים ריקים
        }
    }
    
    /**
     * Rebuilds username to employee number mappings from existing employees and users.
     * This is needed when loading data from storage, as the mapping isn't persisted separately.
     * Attempts to match users to employees by finding employees with matching role and branch.
     */
    private void rebuildUsernameMappings() {
        Map<String, User> users = authenticationManager.getAllUsers();
        Map<String, Employee> employees = employeeManager.getAllEmployees();
        
        // For each user, try to find a matching employee
        for (User user : users.values()) {
            // Skip admin user (handled specially)
            if ("admin".equals(user.getRole()) && "ALL".equals(user.getBranchId())) {
                continue;
            }
            
            // Try to find employee with matching role and branch
            for (Employee emp : employees.values()) {
                if (emp.getRole().equals(user.getRole()) && 
                    emp.getBranchId().equals(user.getBranchId())) {
                    // Found a match - register the mapping
                    // Note: This is a best-effort approach. If multiple employees match,
                    // we'll use the first one found. In practice, username should match employeeNumber
                    // or be stored in Employee for accurate mapping.
                    employeeManager.registerUsernameMapping(user.getUsername(), emp.getEmployeeNumber());
                    break;  // Use first match found
                }
            }
        }
    }
    
    /**
     * Saves all data to storage.
     * Called periodically or on shutdown to persist all system data.
     */
    public void saveAllData() {
        try {
            saveUsers();
            saveEmployees();
            saveCustomers();
            saveProducts();
            saveInventory();
            saveSales();
            saveLogs();
            saveBranches();
            saveDiscounts();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    // ========== Load Methods ==========
    
    private void loadUsers() throws IOException {
        Map<String, UserData> usersData = storageManager.loadUsers();
        for (UserData userData : usersData.values()) {
            // לא טוען משתמשי אדמין ברירת מחדל (admin, superadmin) אם הם כבר קיימים
            if (!authenticationManager.userExists(userData.username)) {
                User user = userData.toUser();
                authenticationManager.addUserDirectly(user);
            }
        }
    }
    
    private void loadEmployees() throws IOException {
        Map<String, EmployeeData> employeesData = storageManager.loadEmployees();
        for (EmployeeData empData : employeesData.values()) {
            try {
                employeeManager.addEmployee(
                    empData.fullName, empData.idNumber, empData.phone,
                    empData.bankAccount, empData.employeeNumber,
                    empData.role, empData.branchId
                );
            } catch (DuplicateEmployeeException e) {
                // כבר קיים, מדלג
            } catch (InvalidIdNumberException | InvalidPhoneException e) {
                // נתונים לא תקינים מהקובץ - מדלג על העובד הזה
                System.err.println("Skipping employee with invalid data: " + empData.employeeNumber + " - " + e.getMessage());
            }
        }
    }
    
    private void loadCustomers() throws IOException {
        Map<String, CustomerData> customersData = storageManager.loadCustomers();
        for (CustomerData custData : customersData.values()) {
            try {
                customerManager.addCustomer(
                    custData.fullName, custData.idNumber,
                    custData.phone, custData.customerType
                );
            } catch (DuplicateCustomerException e) {
                // כבר קיים, מדלג
            }
        }
    }
    
    private void loadProducts() throws IOException {
        Map<String, ProductData> productsData = storageManager.loadProducts();
        for (ProductData prodData : productsData.values()) {
            Product product = prodData.toProduct();
            productManager.addProductDirectly(product);
        }
    }
    
    private void loadBranches() throws IOException {
        List<String> branchIds = storageManager.loadBranches();
        if (!branchIds.isEmpty()) {
            branchManager.loadBranches(branchIds);
        }
    }
    
    private void loadInventory() throws IOException {
        Map<String, Map<String, Integer>> inventoryData = storageManager.loadInventory();
        Map<String, Product> productsMap = productManager.getAllProducts(); // צריך להוסיף method זה
        
        for (Map.Entry<String, Map<String, Integer>> branchEntry : inventoryData.entrySet()) {
            String branchId = branchEntry.getKey();
            Branch branch = branchManager.getBranch(branchId);
            if (branch == null) {
                branchManager.addBranch(branchId);
                branch = branchManager.getBranch(branchId);
            }
            
            Inventory inventory = branch.getInventory();
            Map<String, Integer> branchInventory = branchEntry.getValue();
            
            for (Map.Entry<String, Integer> productEntry : branchInventory.entrySet()) {
                String productId = productEntry.getKey();
                int quantity = productEntry.getValue();
                Product product = productsMap.get(productId);
                
                if (product != null) {
                    try {
                        inventory.loadProduct(product, quantity);
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
            }
        }
    }
    
    private void loadSales() throws IOException {
        List<SaleData> salesData = storageManager.loadSales();
        for (SaleData saleData : salesData) {
            Sale sale = saleData.toSale();
            salesManager.addSale(sale);
        }
    }
    
    private void loadLogs() throws IOException {
        List<LogEntry> logs = storageManager.loadLogs();
        for (LogEntry log : logs) {
            logManager.addLog(log);
        }
    }
    
    // ========== Save Methods ==========
    
    private void saveUsers() throws IOException {
        Map<String, User> users = authenticationManager.getAllUsers();
        storageManager.saveUsers(users);
    }
    
    private void saveEmployees() throws IOException {
        Map<String, Employee> employees = employeeManager.getAllEmployees();
        storageManager.saveEmployees(employees);
    }
    
    private void saveCustomers() throws IOException {
        Map<String, Customer> customers = customerManager.getAllCustomers();
        storageManager.saveCustomers(customers);
    }
    
    private void saveProducts() throws IOException {
        Map<String, Product> products = productManager.getAllProducts();
        storageManager.saveProducts(products);
    }
    
    private void saveInventory() throws IOException {
        Map<String, Branch> branches = branchManager.getAllBranches();
        storageManager.saveInventory(branches);
    }
    
    private void saveSales() throws IOException {
        List<Sale> sales = salesManager.getSales();
        storageManager.saveSales(sales);
    }
    
    private void saveLogs() throws IOException {
        List<LogEntry> logs = logManager.getLogs();
        storageManager.saveLogs(logs);
    }
    
    private void saveBranches() throws IOException {
        List<String> branchIds = branchManager.getBranchIds();
        storageManager.saveBranches(branchIds);
    }
    
    private void loadDiscounts() throws IOException {
        Map<String, Double> discounts = storageManager.loadDiscounts();
        if (!discounts.isEmpty()) {
            discountManager.setAllDiscounts(discounts);
        }
    }
    
    private void saveDiscounts() throws IOException {
        Map<String, Double> discounts = discountManager.getAllDiscounts();
        storageManager.saveDiscounts(discounts);
    }

    public Session login(String username, String password, Socket socket)
            throws InvalidCredentialsException, UserAlreadyLoggedInException {
        
        User user = authenticationManager.authenticate(username, password);
        
        Session session = sessionManager.createSession(
            username,
            user.getBranchId(),
            user.getRole(),
            socket
        );
        
        LogEntry entry = new LogEntry(
            "LOGIN",
            "User " + username + " (" + user.getRole() + ") logged in from " + socket.getRemoteSocketAddress(),
            LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
        
        return session;
    }
    
    /**
     * Logs out a user and removes their session.
     * 
     * @param socket the socket connection to logout
     */
    public void logout(Socket socket) {
        Session session = sessionManager.getSession(socket);
        if (session != null) {
            LogEntry entry = new LogEntry(
                "LOGOUT",
                "User " + session.getUsername() + " logged out",
                LocalDateTime.now().toString()
            );
            logManager.addLog(entry);
            try {
                saveLogs();
            } catch (IOException e) {
                System.err.println("Error saving logs: " + e.getMessage());
            }
        }
        sessionManager.removeSession(socket);
    }
    
    /**
     * Gets the session for a socket connection.
     * 
     * @param socket the socket connection
     * @return the Session object, or null if not found
     */
    public Session getSession(Socket socket) {
        return sessionManager.getSession(socket);
    }
    
    /**
     * Gets the AuthenticationManager (for internal use).
     * 
     * @return the AuthenticationManager instance
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    
    /**
     * Gets the EmployeeManager (for internal use).
     * 
     * @return the EmployeeManager instance
     */
    public EmployeeManager getEmployeeManager() {
        return employeeManager;
    }
    
    /**
     * Gets the SessionManager (for internal use).
     * 
     * @return the SessionManager instance
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Adds a new customer to the system.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number (unique identifier)
     * @param phone the customer's phone number
     * @param customerType the customer type ("NEW", "RETURNING", or "VIP")
     * @throws DuplicateCustomerException if a customer with the same ID number already exists
     */
    public void addCustomer(String fullName,
                            String idNumber,
                            String phone,
                            String customerType)
            throws DuplicateCustomerException {

        customerManager.addCustomer(
                fullName,
                idNumber,
                phone,
                customerType
        );

        LogEntry entry = new LogEntry(
                "ADD_CUSTOMER",
                "Customer added: " + fullName +
                        ", id=" + idNumber +
                        ", type=" + customerType,
                LocalDateTime.now().toString()
        );

        logManager.addLog(entry);
        try {
            saveCustomers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Updates customer details.
     * 
     * @param idNumber the customer's ID number
     * @param fullName the new full name (null or empty to keep current)
     * @param phone the new phone number (null or empty to keep current)
     * @param customerType the new customer type (null or empty to keep current)
     */
    public void updateCustomer(String idNumber,
                               String fullName,
                               String phone,
                               String customerType) {
        customerManager.updateCustomer(idNumber, fullName, phone, customerType);
        
        LogEntry entry = new LogEntry(
                "UPDATE_CUSTOMER",
                "Customer updated: id=" + idNumber +
                        ", name=" + fullName +
                        ", type=" + customerType,
                LocalDateTime.now().toString()
        );
        
        logManager.addLog(entry);
        try {
            saveCustomers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a customer from the system.
     * 
     * @param idNumber the customer's ID number
     */
    public void deleteCustomer(String idNumber) {
        customerManager.deleteCustomer(idNumber);
        
        LogEntry entry = new LogEntry(
                "DELETE_CUSTOMER",
                "Customer deleted: id=" + idNumber,
                LocalDateTime.now().toString()
        );
        
        logManager.addLog(entry);
        try {
            saveCustomers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Adds a new product to inventory (creates the product if it doesn't exist).
     * 
     * @param productId the product ID
     * @param name the product name
     * @param category the product category
     * @param price the product price
     * @param quantity the quantity to add to inventory
     * @param branchId the branch ID
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     */
    public void addProduct(String productId,
                           String name,
                           String category,
                           double price,
                           int quantity,
                           String branchId)
            throws InvalidQuantityException {
        
        // 1. יצירת/שליפת מוצר
        Product product = productManager.getProduct(productId, name, category, price);
        
        // 2. שליפת הסניף
        Branch branch = branchManager.getBranch(branchId);
        
        // 3. הוספה למלאי
        inventoryManager.addProduct(branch, product, quantity);
        
        // 4. רישום לוג
        LogEntry entry = new LogEntry(
                "ADD_PRODUCT",
                "Product " + productId + " (" + name + ") added to inventory of branch " + branchId +
                        " (qty=" + quantity + ", price=" + price + ")",
                LocalDateTime.now().toString()
        );
        
        logManager.addLog(entry);
        try {
            saveProducts();
            saveInventory();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Adds an existing product to inventory.
     * Product must already exist in the catalog.
     * 
     * @param productId the product ID (must exist)
     * @param quantity the quantity to add
     * @param branchId the branch ID
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     */
    public void addProductToInventory(String productId,
                                      int quantity,
                                      String branchId)
            throws InvalidQuantityException {

        // 1. שליפת מוצר קיים מהקטלוג
        Product product = productManager.getExistingProduct(productId);

        // הנחה לפי הדרישות: המוצר קיים בקטלוג
        // אם לא – תתקבל שגיאת ריצה (או חריגה אם תחליטו להוסיף)

        // 2. שליפת הסניף
        Branch branch = branchManager.getBranch(branchId);

        // 3. הוספה למלאי
        inventoryManager.addProduct(branch, product, quantity);

        // 4. רישום לוג
        LogEntry entry = new LogEntry(
                "ADD_PRODUCT_TO_INVENTORY",
                "Product " + productId +
                        " added to inventory of branch " + branchId +
                        " (qty=" + quantity + ")",
                LocalDateTime.now().toString()
        );

        logManager.addLog(entry);
        try {
            saveInventory();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Removes a quantity of a product from inventory.
     * 
     * @param productId the product ID
     * @param quantity the quantity to remove
     * @param branchId the branch ID
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock to remove
     */
    public void removeFromInventory(String productId,
                                    int quantity,
                                    String branchId)
            throws InvalidQuantityException, InsufficientStockException {
        
        // 1. שליפת מוצר קיים
        Product product = productManager.getExistingProduct(productId);
        
        // 2. שליפת הסניף
        Branch branch = branchManager.getBranch(branchId);
        
        // 3. הסרה מהמלאי
        inventoryManager.removeProduct(branch, product, quantity);
        
        // 4. רישום לוג
        LogEntry entry = new LogEntry(
                "REMOVE_FROM_INVENTORY",
                "Product " + productId +
                        " removed from inventory of branch " + branchId +
                        " (qty=" + quantity + ")",
                LocalDateTime.now().toString()
        );
        
        logManager.addLog(entry);
        try {
            saveInventory();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a product from the system (admin only).
     * Removes the product from all branch inventories before deletion.
     * 
     * @param productId the product ID to delete
     * @throws IllegalArgumentException if product not found
     */
    public void deleteProduct(String productId) {
        // 1. בדיקה שהמוצר קיים
        Product product = productManager.getExistingProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        
        // 2. הסרת המוצר מכל הסניפים (הסרה מהמלאי)
        Map<String, Branch> branches = branchManager.getAllBranches();
        for (Branch branch : branches.values()) {
            Inventory inventory = branch.getInventory();
            int quantity = inventory.getProductQuantity(product);
            if (quantity > 0) {
                try {
                    inventoryManager.removeProduct(branch, product, quantity);
                } catch (Exception e) {
                    // אם יש שגיאה, נמשיך עם הסניפים האחרים
                    System.err.println("Error removing product from branch " + branch.getBranchId() + ": " + e.getMessage());
                }
            }
        }
        
        // 3. הסרת המוצר מהמנהל
        productManager.deleteProduct(productId);
        
        // 4. רישום לוג
        LogEntry entry = new LogEntry(
                "DELETE_PRODUCT",
                "Product " + productId + " deleted from system",
                LocalDateTime.now().toString()
        );
        
        logManager.addLog(entry);
        try {
            saveProducts();
            saveInventory();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }


    /**
     * Sells a product to a customer.
     * Validates stock, calculates price based on customer type (polymorphism), creates sale record.
     * 
     * @param productId the product ID
     * @param quantity the quantity to sell
     * @param branchId the branch ID where the sale occurs
     * @param employeeNumber the employee number making the sale
     * @param customerId the customer ID making the purchase
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock
     */
    public void sellProduct(String productId,
                            int quantity,
                            String branchId,
                            String employeeNumber,
                            String customerId)
            throws InvalidQuantityException,
            InsufficientStockException {

        // 1. שליפת מוצר קיים מהקטלוג
        Product product = productManager.getExistingProduct(productId);

        // 2. שליפת סניף
        Branch branch = branchManager.getBranch(branchId);

        // 3. שליפת לקוח
        Customer customer = customerManager.getCustomerById(customerId);

        // 4. מכירה מהמלאי (בדיקות קורות כאן)
        inventoryManager.sellProduct(branch, product, quantity);

        // 5. חישוב מחיר לפי סוג הלקוח (פולימורפיזם)
        double basePrice = product.getPrice() * quantity;
        double finalPrice = customer.calculatePrice(basePrice);

        // 6. יצירת מכירה
        String dateTime = LocalDateTime.now().toString();
        Sale sale = new Sale(
                product,
                quantity,
                branchId,
                employeeNumber,
                customerId,
                dateTime,
                basePrice,
                finalPrice
        );

        salesManager.addSale(sale);

        // 7. לוג
        String customerType = customer.getCustomerType();
        LogEntry entry = new LogEntry(
                "SALE",
                "Sold product " + productId +
                        " (price=" + product.getPrice() + ")" +
                        ", qty=" + quantity +
                        ", basePrice=" + basePrice +
                        ", finalPrice=" + finalPrice +
                        ", branch=" + branchId +
                        ", customerId=" + customerId +
                        ", customerType=" + customerType +
                        ", employeeNumber=" + employeeNumber,
                dateTime
        );

        logManager.addLog(entry);
        try {
            saveSales();
            saveInventory();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Calculates the final price for a sale (before executing the sale).
     * Uses customer's calculatePrice() method to apply appropriate discount (polymorphism).
     * 
     * @param productId the product ID
     * @param quantity the quantity to calculate price for
     * @param customerId the customer ID
     * @return the final price after customer discount
     * @throws IllegalArgumentException if product or customer not found
     */
    public double calculatePrice(String productId, int quantity, String customerId) {
        Product product = productManager.getExistingProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        
        Customer customer = customerManager.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        
        // חישוב מחיר בסיסי
        double basePrice = product.getPrice() * quantity;
        
        // חישוב מחיר סופי לפי סוג הלקוח
        double finalPrice = customer.calculatePrice(basePrice);
        
        return finalPrice;
    }

    // ========== Admin Methods - User Management ==========
    
    /**
     * Creates a new user account.
     * Permission check should be done by caller using PermissionChecker.canCreateUser().
     * 
     * @param username the unique username
     * @param password the user's password (must meet password policy)
     * @param role the user's role (admin, manager, salesman, cashier)
     * @param branchId the branch ID where the user works
     * @throws WeakPasswordException if password does not meet requirements
     * @throws DuplicateUserException if username already exists
     * @throws IllegalArgumentException if role is invalid
     */
    public void createUser(String username,
                          String password,
                          String role,
                          String branchId)
            throws WeakPasswordException, DuplicateUserException {
        
        // בדיקה אם המשתמש כבר קיים
        if (authenticationManager.userExists(username)) {
            throw new DuplicateUserException("User with username " + username + " already exists");
        }
        
        authenticationManager.createUser(username, password, role, branchId);
        
        LogEntry entry = new LogEntry(
                "CREATE_USER",
                "User created: " + username + ", role=" + role + ", branch=" + branchId,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveUsers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Updates user details (admin only).
     * Only updates fields that are provided (not null).
     * 
     * @param username the username to update
     * @param newPassword the new password (null to keep current)
     * @param newBranchId the new branch ID (null to keep current)
     * @param active the new active status (null to keep current)
     * @throws UserNotFoundException if user not found
     * @throws WeakPasswordException if new password does not meet requirements
     */
    public void updateUser(String username,
                          String newPassword,
                          String newBranchId,
                          Boolean active)
            throws UserNotFoundException, WeakPasswordException {
        
        User user = authenticationManager.getUser(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        
        // עדכון סיסמה אם הועברה
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // שינוי סיסמה - צריך את הסיסמה הישנה, אבל כאן זו פעולת אדמין אז נשנה ישירות
            user.setPassword(newPassword);
        }
        
        // עדכון branchId אם הועבר
        if (newBranchId != null && !newBranchId.trim().isEmpty()) {
            user.setBranchId(newBranchId);
        }
        
        // עדכון active אם הועבר
        if (active != null) {
            user.setActive(active);
        }
        
        LogEntry entry = new LogEntry(
                "UPDATE_USER",
                "User updated: " + username,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveUsers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Activates or deactivates a user account (admin only).
     * If deactivating, disconnects the user if currently logged in.
     * 
     * @param username the username
     * @param active true to activate, false to deactivate
     * @throws UserNotFoundException if user not found
     */
    public void setUserActive(String username, boolean active)
            throws UserNotFoundException {
        
        User user = authenticationManager.getUser(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        
        authenticationManager.setUserActive(username, active);
        
        // אם המשתמש מחובר, יש לנתק אותו
        if (!active) {
            Session session = sessionManager.getSessionByUsername(username);
            if (session != null) {
                sessionManager.removeSession(session.getSocket());
            }
        }
        
        LogEntry entry = new LogEntry(
                "SET_USER_ACTIVE",
                "User " + username + " set to " + (active ? "active" : "inactive"),
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveUsers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Gets all users (admin only).
     * 
     * @return a Map of username to User
     */
    public Map<String, User> getAllUsers() {
        return authenticationManager.getAllUsers();
    }
    
    /**
     * Gets a user by username (admin only).
     * 
     * @param username the username
     * @return the User object
     * @throws UserNotFoundException if user not found
     */
    public User getUser(String username) throws UserNotFoundException {
        User user = authenticationManager.getUser(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        return user;
    }
    
    /**
     * Deletes a user from the system (admin only).
     * Disconnects the user if currently logged in before deletion.
     * 
     * @param username the username to delete
     * @throws UserNotFoundException if user not found
     * @throws IllegalArgumentException if attempting to delete default admin or superadmin
     */
    public void deleteUser(String username) throws UserNotFoundException {
        User user = authenticationManager.getUser(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        
        // אם המשתמש מחובר, יש לנתק אותו קודם
        Session session = sessionManager.getSessionByUsername(username);
        if (session != null) {
            sessionManager.removeSession(session.getSocket());
        }
        
        authenticationManager.deleteUser(username);
        
        LogEntry entry = new LogEntry(
                "DELETE_USER",
                "User deleted: " + username,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveUsers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    // ========== Admin Methods - Employee Management ==========
    
    /**
     * Creates a new employee and automatically creates a user account for them.
     * 
     * @param fullName the employee's full name
     * @param idNumber the employee's ID number
     * @param phone the employee's phone number
     * @param bankAccount the employee's bank account number
     * @param employeeNumber the unique employee number
     * @param username the username for the user account
     * @param password the password for the user account
     * @param role the employee's role (e.g., "manager", "cashier", "salesperson")
     * @param branchId the branch ID where the employee works
     * @throws DuplicateEmployeeException if employee with same employeeNumber or idNumber already exists
     * @throws DuplicateUserException if username already exists
     * @throws WeakPasswordException if password does not meet requirements
     */
    public void createEmployee(String fullName,
                              String idNumber,
                              String phone,
                              String bankAccount,
                              String employeeNumber,
                              String username,
                              String password,
                              String role,
                              String branchId)
            throws DuplicateEmployeeException, DuplicateUserException, WeakPasswordException,
                   InvalidIdNumberException, InvalidPhoneException {
        
        // Create employee first
        employeeManager.addEmployee(fullName, idNumber, phone, bankAccount, employeeNumber, role, branchId);
        
        // Register username to employee number mapping
        employeeManager.registerUsernameMapping(username, employeeNumber);
        
        // Create user account with same role and branchId
        try {
            createUser(username, password, role, branchId);
        } catch (DuplicateUserException | WeakPasswordException e) {
            // If user creation fails, we should rollback employee creation
            // But EmployeeManager doesn't have a delete method, so we'll just throw the exception
            // In a production system, we'd use transactions
            try {
                employeeManager.deleteEmployee(employeeNumber);
            } catch (EmployeeNotFoundException ex) {
                // Employee wasn't created or already deleted
            }
            throw e;
        }
        
        LogEntry entry = new LogEntry(
                "CREATE_EMPLOYEE",
                "Employee created: " + fullName + ", number=" + employeeNumber + ", role=" + role + ", branch=" + branchId + ", username=" + username,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveEmployees();
            saveUsers();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Updates employee details (admin only).
     * Only updates fields that are provided (not null or empty).
     * 
     * @param employeeNumber the employee number
     * @param fullName the new full name (null or empty to keep current)
     * @param phone the new phone number (null or empty to keep current)
     * @param bankAccount the new bank account (null or empty to keep current)
     * @param role the new role (null or empty to keep current)
     * @param branchId the new branch ID (null or empty to keep current)
     * @throws EmployeeNotFoundException if employee not found
     */
    public void updateEmployee(String employeeNumber,
                              String fullName,
                              String phone,
                              String bankAccount,
                              String role,
                              String branchId)
            throws EmployeeNotFoundException, InvalidPhoneException {
        
        employeeManager.updateEmployee(employeeNumber, fullName, phone, bankAccount, role, branchId);
        
        LogEntry entry = new LogEntry(
                "UPDATE_EMPLOYEE",
                "Employee updated: " + employeeNumber,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveEmployees();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    
    /**
     * Deletes an employee from the system (admin only).
     * 
     * @param employeeNumber the employee number to delete
     * @throws EmployeeNotFoundException if employee not found
     */
    public void deleteEmployee(String employeeNumber)
            throws EmployeeNotFoundException {
        
        employeeManager.deleteEmployee(employeeNumber);
        
        LogEntry entry = new LogEntry(
                "DELETE_EMPLOYEE",
                "Employee deleted: " + employeeNumber,
                LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveEmployees();
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Gets all employees (admin only).
     * 
     * @return a Map of employeeNumber to Employee
     */
    public Map<String, Employee> getAllEmployees() {
        return employeeManager.getAllEmployees();
    }
    
    /**
     * Gets an employee by employee number (admin only).
     * 
     * @param employeeNumber the employee number
     * @return the Employee object
     * @throws EmployeeNotFoundException if employee not found
     */
    public Employee getEmployee(String employeeNumber)
            throws EmployeeNotFoundException {
        return employeeManager.getEmployee(employeeNumber);
    }
    
    /**
     * Gets all employees for a specific branch (admin only).
     * 
     * @param branchId the branch ID
     * @return a Map of employeeNumber to Employee for the specified branch
     */
    public Map<String, Employee> getEmployeesByBranch(String branchId) {
        return employeeManager.getEmployeesByBranch(branchId);
    }
    
    // ========== List Methods for GUI ==========
    
    /**
     * Gets all customers (for display in GUI).
     * 
     * @return a Map of idNumber to Customer
     */
    public Map<String, Customer> getAllCustomersForDisplay() {
        return customerManager.getAllCustomers();
    }
    
    /**
     * Gets all products (for display in GUI).
     * 
     * @return a Map of productId to Product
     */
    public Map<String, Product> getAllProductsForDisplay() {
        return productManager.getAllProducts();
    }
    
    /**
     * Gets inventory quantity for a product and branch.
     * If branchId = "ALL", returns the sum from all branches (admin only).
     * 
     * @param productId the product ID
     * @param branchId the branch ID, or "ALL" for sum from all branches
     * @return the quantity available, or 0 if product not found
     */
    public int getInventoryQuantity(String productId, String branchId) {
        Product product = productManager.getExistingProduct(productId);
        if (product == null) {
            return 0;
        }
        
        if (branchId.equals("ALL")) {
            // סכום מכל הסניפים
            int total = 0;
            Map<String, Branch> branches = branchManager.getAllBranches();
            for (Branch branch : branches.values()) {
                Inventory inventory = branch.getInventory();
                total += inventory.getProductQuantity(product);
            }
            return total;
        } else {
            // כמות בסניף ספציפי
            Branch branch = branchManager.getBranch(branchId);
            if (branch == null) {
                return 0;
            }
            Inventory inventory = branch.getInventory();
            return inventory.getProductQuantity(product);
        }
    }
    
    // ========== Report Methods ==========
    
    /**
     * Generates a sales report by branch.
     * Returns a list of ReportEntry objects summarizing sales by branch.
     * 
     * @param branchId the branch ID to filter by, or null/"ALL" for all branches
     * @return a list of ReportEntry objects
     */
    public List<ReportEntry> getSalesReportByBranch(String branchId) {
        List<Sale> allSales = salesManager.getSales();
        Map<String, ReportEntry> summary = new HashMap<>();
        
        for (Sale sale : allSales) {
            // אם branchId לא null, סינון לפי סניף
            if (branchId != null && !branchId.isEmpty() && !branchId.equals("ALL")) {
                if (!sale.getBranchId().equals(branchId)) {
                    continue;
                }
            }
            
            String key = sale.getBranchId();
            Product product = sale.getProduct();
            
            if (summary.containsKey(key)) {
                ReportEntry entry = summary.get(key);
                // עדכון הסיכום - הוספת כמות ו-revenue
                // אבל ReportEntry לא מאפשר עדכון, אז נצטרך ליצור מחדש
                summary.put(key, new ReportEntry(
                    entry.getBranchId(),
                    "",  // לא רלוונטי בדוח לפי סניף
                    "",  // לא רלוונטי
                    "",  // לא רלוונטי
                    entry.getQuantity() + sale.getQuantity(),
                    entry.getTotalRevenue() + sale.getFinalPrice(),
                    entry.getDate()
                ));
            } else {
                summary.put(key, new ReportEntry(
                    sale.getBranchId(),
                    "",
                    "",
                    "",
                    sale.getQuantity(),
                    sale.getFinalPrice(),
                    ""
                ));
            }
        }
        
        return new ArrayList<>(summary.values());
    }
    
    /**
     * Generates a sales report by product.
     * Returns a list of ReportEntry objects for all sales of the specified product.
     * 
     * @param productId the product ID to filter by, or null/empty for all products
     * @return a list of ReportEntry objects
     */
    public List<ReportEntry> getSalesReportByProduct(String productId) {
        List<Sale> allSales = salesManager.getSales();
        List<ReportEntry> result = new ArrayList<>();
        
        for (Sale sale : allSales) {
            if (productId == null || productId.isEmpty() || sale.getProduct().getProductId().equals(productId)) {
                result.add(new ReportEntry(
                    sale.getBranchId(),
                    sale.getProduct().getProductId(),
                    sale.getProduct().getName(),
                    sale.getProduct().getCategory(),
                    sale.getQuantity(),
                    sale.getFinalPrice(),
                    sale.getDateTime().split("T")[0]  // רק תאריך, לא זמן
                ));
            }
        }
        
        return result;
    }
    
    /**
     * Generates a sales report by category.
     * Returns a list of ReportEntry objects summarizing sales by product category.
     * 
     * @param category the category to filter by, or null/empty for all categories
     * @return a list of ReportEntry objects
     */
    public List<ReportEntry> getSalesReportByCategory(String category) {
        List<Sale> allSales = salesManager.getSales();
        Map<String, ReportEntry> summary = new HashMap<>();
        
        for (Sale sale : allSales) {
            String saleCategory = sale.getProduct().getCategory();
            
            // אם category לא null, סינון לפי קטגוריה
            if (category != null && !category.isEmpty()) {
                if (!saleCategory.equals(category)) {
                    continue;
                }
            }
            
            String key = saleCategory;
            
            if (summary.containsKey(key)) {
                ReportEntry entry = summary.get(key);
                summary.put(key, new ReportEntry(
                    entry.getBranchId(),
                    entry.getProductId(),
                    entry.getProductName(),
                    entry.getCategory(),
                    entry.getQuantity() + sale.getQuantity(),
                    entry.getTotalRevenue() + sale.getFinalPrice(),
                    entry.getDate()
                ));
            } else {
                summary.put(key, new ReportEntry(
                    "",
                    "",
                    "",
                    saleCategory,
                    sale.getQuantity(),
                    sale.getFinalPrice(),
                    ""
                ));
            }
        }
        
        return new ArrayList<>(summary.values());
    }
    
    /**
     * Generates a daily sales report (by date).
     * Filters sales by date and optionally by branch.
     * 
     * @param date the date to filter by (format: YYYY-MM-DD), or null/empty for all dates
     * @param branchId the branch ID to filter by, or null/"ALL" for all branches
     * @return a list of ReportEntry objects
     */
    public List<ReportEntry> getDailySalesReport(String date, String branchId) {
        List<Sale> allSales = salesManager.getSales();
        List<ReportEntry> result = new ArrayList<>();
        
        for (Sale sale : allSales) {
            String saleDate = sale.getDateTime().split("T")[0];  // רק תאריך
            
            // סינון לפי תאריך
            if (date != null && !date.isEmpty()) {
                if (!saleDate.equals(date)) {
                    continue;
                }
            }
            
            // סינון לפי סניף
            if (branchId != null && !branchId.isEmpty() && !branchId.equals("ALL")) {
                if (!sale.getBranchId().equals(branchId)) {
                    continue;
                }
            }
            
            result.add(new ReportEntry(
                sale.getBranchId(),
                sale.getProduct().getProductId(),
                sale.getProduct().getName(),
                sale.getProduct().getCategory(),
                sale.getQuantity(),
                sale.getFinalPrice(),
                saleDate
            ));
        }
        
        return result;
    }
    
    // ========== Chat Methods ==========
    
    /**
     * User requests a chat.
     * Attempts immediate matching; if no match found, request is queued.
     * 
     * @param username the username requesting the chat
     * @param branchId the branch ID of the requester
     * @return "OK;MATCHED;chatId;user1;user2" if matched, "OK;QUEUE;requestId" if queued, "ERROR;..." on failure
     */
    public String requestChat(String username, String branchId) {
        String result = chatManager.requestChat(username, branchId);
        
        // לוג
        LogEntry entry = new LogEntry(
            "CHAT_REQUESTED",
            "User " + username + " requested chat from branch " + branchId,
            LocalDateTime.now().toString(),
            null // אין chatId עדיין
        );
        logManager.addLog(entry);
        
        // אם נמצאה התאמה - לוג נוסף
        if (result != null && result.startsWith("OK;MATCHED")) {
            String[] parts = result.split(";");
            if (parts.length > 2) {
                String chatId = parts[2];
                LogEntry startEntry = new LogEntry(
                    "CHAT_STARTED",
                    "Chat " + chatId + " started for user " + username,
                    LocalDateTime.now().toString(),
                    chatId
                );
                logManager.addLog(startEntry);
            }
        }
        
        try {
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Sends a message in a chat.
     * 
     * @param chatId the chat ID
     * @param sender the sender's username
     * @param message the message content
     */
    public void sendChatMessage(String chatId, String sender, String message) {
        chatManager.addMessage(chatId, sender, message);
        
        // לוג
        LogEntry entry = new LogEntry(
            "CHAT_MESSAGE",
            "Chat " + chatId + ": " + sender + " sent: " + message,
            LocalDateTime.now().toString(),
            chatId
        );
        logManager.addLog(entry);
        try {
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }
    
    /**
     * Ends a chat session.
     * 
     * @param chatId the chat ID to end
     */
    public void endChat(String chatId) {
        chatManager.endChat(chatId);
        
        // לוג
        LogEntry entry = new LogEntry(
            "CHAT_ENDED",
            "Chat " + chatId + " ended",
            LocalDateTime.now().toString(),
            chatId
        );
        logManager.addLog(entry);
        try {
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }
    
    /**
     * Manager joins an existing chat.
     * 
     * @param chatId the chat ID to join
     * @param managerUsername the username of the manager joining
     * @throws IllegalArgumentException if user is not a manager or chat not found
     */
    public void joinChatAsManager(String chatId, String managerUsername) {
        chatManager.joinChatAsManager(chatId, managerUsername);
        
        // לוג
        LogEntry entry = new LogEntry(
            "MANAGER_JOINED",
            "Manager " + managerUsername + " joined chat " + chatId,
            LocalDateTime.now().toString(),
            chatId
        );
        logManager.addLog(entry);
        try {
            saveLogs();
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }
    
    /**
     * קבלת רשימת בקשות ממתינות לסניף מסוים
     */
    /**
     * Gets waiting chat requests for a specific branch.
     * Used to notify available employees about pending requests from other branches.
     * 
     * @param branchId the branch ID
     * @return a list of pending ChatRequest objects
     */
    public List<model.ChatRequest> getWaitingRequestsForBranch(String branchId) {
        return chatManager.getWaitingRequestsForBranch(branchId);
    }
    
    /**
     * User accepts a chat request.
     * Creates a chat session between the requester and the accepting user.
     * 
     * @param acceptingUsername the username of the user accepting the request
     * @param requestId the request ID to accept
     * @return "OK;MATCHED;chatId;requester;acceptor" if successful, "ERROR;..." on failure
     */
    public String acceptChatRequest(String acceptingUsername, String requestId) {
        String result = chatManager.acceptChatRequest(acceptingUsername, requestId);
        
        // לוג
        if (result != null && result.startsWith("OK;")) {
            String chatId = null;
            if (result.contains(";") && result.split(";").length > 2) {
                String[] parts = result.split(";");
                if (parts[1].equals("MATCHED") && parts.length > 2) {
                    chatId = parts[2];
                }
            }
            LogEntry entry = new LogEntry(
                "CHAT_ACCEPTED",
                "User " + acceptingUsername + " accepted chat request " + requestId,
                LocalDateTime.now().toString(),
                chatId
            );
            logManager.addLog(entry);
            try {
                saveLogs();
            } catch (IOException e) {
                System.err.println("Error saving logs: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * היסטוריית צ'אט
     */
    public List<model.ChatMessage> getChatHistory(String chatId) {
        return chatManager.getChatHistory(chatId);
    }
    
    /**
     * קבלת צ'אט של משתמש
     */
    public model.ChatSession getUserChat(String username) {
        return chatManager.getUserChat(username);
    }
    
    /**
     * ביטול בקשה לצ'אט
     */
    public boolean cancelChatRequest(String username) {
        return chatManager.cancelChatRequest(username);
    }
    
    /**
     * בדיקת מצב משתמש בצ'אט
     */
    public model.ChatUserStatus getUserChatStatus(String username) {
        return chatManager.getUserStatus(username);
    }
    
    /**
     * Getter ל-ChatManager (לצורך גישה ישירה במידת הצורך)
     */
    public ChatManager getChatManager() {
        return chatManager;
    }
    
    /**
     * Gets all log entries in the system.
     * 
     * @return a list of all LogEntry objects
     */
    public List<LogEntry> getAllLogs() {
        return logManager.getLogs();
    }
    
    /**
     * Gets log entries for a specific chat.
     * Filters logs by chatId.
     * 
     * @param chatId the chat ID
     * @return a list of LogEntry objects related to the chat
     */
    public List<LogEntry> getChatLogs(String chatId) {
        List<LogEntry> allLogs = logManager.getLogs();
        List<LogEntry> chatLogs = new ArrayList<>();
        for (LogEntry log : allLogs) {
            if (chatId.equals(log.getChatId())) {
                chatLogs.add(log);
            }
        }
        return chatLogs;
    }
    
    /**
     * Gets complete chat details (logs + ChatSession if exists).
     * Returns a JSON string with chat information including session details and log entries.
     * 
     * @param chatId the chat ID
     * @return a JSON string with chat details
     */
    public String getChatDetails(String chatId) {
        // איסוף כל הלוגים של השיחה
        List<LogEntry> chatLogs = getChatLogs(chatId);
        
        // ניסיון לקבל ChatSession אם השיחה עדיין פעילה
        model.ChatSession session = null;
        Map<String, model.ChatSession> allChats = chatManager.getAllActiveChats();
        for (model.ChatSession s : allChats.values()) {
            if (s.getChatId().equals(chatId)) {
                session = s;
                break;
            }
        }
        
        // בניית JSON
        StringBuilder json = new StringBuilder("{\"chatId\":\"").append(escapeJson(chatId)).append("\"");
        
        if (session != null) {
            json.append(",\"session\":{");
            json.append("\"startTime\":").append(session.getStartTime());
            json.append(",\"active\":").append(session.isActive());
            json.append(",\"participants\":[");
            boolean first = true;
            for (String participant : session.getParticipants()) {
                if (!first) json.append(",");
                json.append("\"").append(escapeJson(participant)).append("\"");
                first = false;
            }
            json.append("]}");
        }
        
        json.append(",\"logs\":[");
        for (int i = 0; i < chatLogs.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJsonLogEntry(chatLogs.get(i)));
        }
        json.append("]}");
        
        return json.toString();
    }
    
    /**
     * Saves a chat conversation to an RTF file.
     * Collects all chat logs and messages, creates an RTF document with Hebrew support.
     * 
     * @param chatId the chat ID to save
     * @return the filename of the created RTF file
     * @throws IOException if file creation fails
     * @throws IllegalArgumentException if no logs found for the chat
     */
    public String saveChatToRTF(String chatId) throws IOException {
        // קבלת כל הלוגים של השיחה
        List<LogEntry> chatLogs = getChatLogs(chatId);
        if (chatLogs.isEmpty()) {
            throw new IllegalArgumentException("No logs found for chat: " + chatId);
        }
        
        // קבלת הודעות נוספות מ-ChatManager (אם השיחה עדיין פעילה)
        List<model.ChatMessage> messages = chatManager.getChatHistory(chatId);
        
        // קבלת פרטי ChatSession אם קיים
        model.ChatSession session = null;
        Map<String, model.ChatSession> allChats = chatManager.getAllActiveChats();
        for (model.ChatSession s : allChats.values()) {
            if (s.getChatId().equals(chatId)) {
                session = s;
                break;
            }
        }
        
        // יצירת שם קובץ
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = "chat_" + chatId.replace("CHAT_", "") + "_" + dateStr + ".rtf";
        
        // יצירת קובץ RTF
        createChatRTFDocument(fileName, chatId, chatLogs, messages, session);
        
        return fileName;
    }
    
    private void createChatRTFDocument(String fileName, String chatId, List<LogEntry> chatLogs, 
                                      List<model.ChatMessage> messages, model.ChatSession session) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, java.nio.charset.Charset.forName("Windows-1255"))) {
            // RTF Header עם תמיכה בעברית
            writer.write("{\\rtf1\\ansi\\ansicpg1255\\deff0\\nouicompat\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset177 Arial;}}\n");
            writer.write("{\\colortbl ;\\red0\\green0\\blue0;}\n");
            writer.write("\\viewkind4\\uc1\n");
            writer.write("\\pard\\sa200\\sl276\\slmult1\\f0\\fs22\\lang1037\\cf1\n");
            
            // כותרת
            writer.write(escapeRTFWithUnicode("תוכן שיחה - " + chatId) + "\\par\n");
            writer.write("\\par\n");
            
            // פרטי שיחה
            if (session != null) {
                writer.write(escapeRTFWithUnicode("תאריך התחלה: " + session.getStartTime()) + "\\par\n");
                writer.write(escapeRTFWithUnicode("משתתפים: "));
                boolean first = true;
                for (String participant : session.getParticipants()) {
                    if (!first) writer.write(", ");
                    writer.write(escapeRTFWithUnicode(participant));
                    first = false;
                }
                writer.write("\\par\n");
            } else {
                // אם אין session, נשתמש בלוג הראשון
                if (!chatLogs.isEmpty()) {
                    writer.write(escapeRTFWithUnicode("תאריך התחלה: " + chatLogs.get(0).getDateTime()) + "\\par\n");
                }
            }
            writer.write("\\par\n");
            
            // הודעות מהלוגים
            writer.write(escapeRTFWithUnicode("הודעות:") + "\\par\n");
            writer.write("\\par\n");
            
            // מיון הלוגים לפי תאריך
            chatLogs.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime()));
            
            for (LogEntry log : chatLogs) {
                if ("CHAT_MESSAGE".equals(log.getActionType())) {
                    // חילוץ sender ו-message מהתיאור
                    String desc = log.getDescription();
                    // פורמט: "Chat CHAT_123: sender sent: message"
                    int colonIdx = desc.indexOf(": ");
                    if (colonIdx > 0) {
                        String afterChat = desc.substring(colonIdx + 2);
                        int sentIdx = afterChat.indexOf(" sent: ");
                        if (sentIdx > 0) {
                            String sender = afterChat.substring(0, sentIdx);
                            String message = afterChat.substring(sentIdx + 7);
                            writer.write(escapeRTFWithUnicode("[" + log.getDateTime() + "] " + sender + ": " + message) + "\\par\n");
                        }
                    }
                } else if ("CHAT_STARTED".equals(log.getActionType()) || 
                          "CHAT_ENDED".equals(log.getActionType()) ||
                          "MANAGER_JOINED".equals(log.getActionType())) {
                    writer.write(escapeRTFWithUnicode("[" + log.getDateTime() + "] [מערכת]: " + log.getDescription()) + "\\par\n");
                }
            }
            
            // הוספת הודעות מ-ChatManager אם יש (עבור שיחות פעילות)
            if (messages != null && !messages.isEmpty()) {
                for (model.ChatMessage msg : messages) {
                    // בדיקה אם ההודעה כבר בלוגים
                    boolean found = false;
                    for (LogEntry log : chatLogs) {
                        if ("CHAT_MESSAGE".equals(log.getActionType()) && 
                            log.getDescription().contains(msg.getSenderUsername()) &&
                            log.getDescription().contains(msg.getMessage())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if (msg.getMessageType() == model.ChatMessage.MessageType.SYSTEM) {
                            writer.write(escapeRTFWithUnicode("[" + msg.getTimestamp() + "] [מערכת]: " + msg.getMessage()) + "\\par\n");
                        } else {
                            writer.write(escapeRTFWithUnicode("[" + msg.getTimestamp() + "] " + msg.getSenderUsername() + ": " + msg.getMessage()) + "\\par\n");
                        }
                    }
                }
            }
            
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
                // Unicode characters (including Hebrew) - use RTF unicode escape
                char backslash = '\\';
                result.append(backslash).append("u").append((int)c).append("?");
            }
        }
        return result.toString();
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String toJsonLogEntry(LogEntry log) {
        String chatIdJson = log.getChatId() != null ? 
            String.format(",\"chatId\":\"%s\"", escapeJson(log.getChatId())) : "";
        return String.format(
            "{\"actionType\":\"%s\",\"description\":\"%s\",\"dateTime\":\"%s\"%s}",
            escapeJson(log.getActionType()), escapeJson(log.getDescription()), escapeJson(log.getDateTime()), chatIdJson
        );
    }
    
    // ========== Discount Management ==========
    
    /**
     * Gets all discount percentages for customer types.
     * 
     * @return a Map of customerType to discount percentage
     */
    public Map<String, Double> getAllDiscounts() {
        return discountManager.getAllDiscounts();
    }
    
    /**
     * Sets the discount percentage for a customer type.
     * 
     * @param customerType the customer type ("NEW", "RETURNING", or "VIP")
     * @param discountPercentage the discount percentage (0.0 to 100.0)
     * @throws IllegalArgumentException if customerType is invalid or discount is out of range
     */
    public void setDiscount(String customerType, double discountPercentage) throws IOException {
        discountManager.setDiscount(customerType, discountPercentage);
        saveDiscounts();
        
        // Log the change
        LogEntry entry = new LogEntry(
                "UPDATE_DISCOUNT",
                "Discount for " + customerType + " customers updated to " + discountPercentage + "%",
                java.time.LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        saveLogs();
    }
    
    /**
     * Gets the employee number for a given username.
     * For admin users, returns "0".
     * For other users, looks up the employee number from the username mapping.
     * 
     * @param username the username
     * @param role the user's role
     * @return the employee number ("0" for admin, or the actual employee number for others)
     */
    public String getEmployeeNumberByUsername(String username, String role) {
        if ("admin".equals(role)) {
            return "0";
        }
        String employeeNumber = employeeManager.getEmployeeNumberByUsername(username);
        return employeeNumber != null ? employeeNumber : "0"; // Default to "0" if not found
    }
    
}
