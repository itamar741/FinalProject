package controller;
import model.Product;
import model.managers.*;
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
    private StorageManager storageManager;

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
        chatManager = new ChatManager(sessionManager, employeeManager);
        
        // טעינת נתונים משמירה קיימת
        loadAllData();
    }
    
    /**
     * טעינת כל הנתונים מהשמירה
     */
    private void loadAllData() {
        try {
            loadUsers();
            loadEmployees();
            loadCustomers();
            loadProducts();
            loadBranches();
            loadInventory();
            loadSales();
            loadLogs();
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            // ממשיכים עם נתונים ריקים
        }
    }
    
    /**
     * שמירת כל הנתונים לשמירה
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
                if (!empData.active) {
                    try {
                        employeeManager.setEmployeeActive(empData.employeeNumber, false);
                    } catch (EmployeeNotFoundException e) {
                        // עובד לא נמצא, מדלג
                    }
                }
            } catch (DuplicateEmployeeException e) {
                // כבר קיים, מדלג
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

    public Session login(String username, String password, Socket socket)
            throws InvalidCredentialsException, UserAlreadyLoggedInException {
        
        User user = authenticationManager.authenticate(username, password);
        
        Session session = sessionManager.createSession(
            username,
            user.getEmployeeNumber(),
            user.getBranchId(),
            user.getUserType(),  // שונה מ-getRole()
            socket
        );
        
        LogEntry entry = new LogEntry(
            "LOGIN",
            "User " + username + " (" + user.getUserType() + ") logged in from " + socket.getRemoteSocketAddress(),
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
    
    public Session getSession(Socket socket) {
        return sessionManager.getSession(socket);
    }
    
    // Getters למנהלים
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    
    public EmployeeManager getEmployeeManager() {
        return employeeManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }

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
     * עדכון פרטי לקוח
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
     * מחיקת לקוח
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
     * הוספת מוצר חדש למלאי (יוצר את המוצר אם לא קיים)
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
     * הסרת כמות מהמלאי
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
     * מחיקת מוצר מהמערכת (רק ADMIN)
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


    public void sellProduct(String productId,
                            int quantity,
                            String branchId,
                            String employeeNumber,
                            String customerId)
            throws InvalidQuantityException,
            InsufficientStockException,
            InactiveProductException {

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
        LogEntry entry = new LogEntry(
                "SALE",
                "Sold product " + productId +
                        " (price=" + product.getPrice() + ")" +
                        ", qty=" + quantity +
                        ", basePrice=" + basePrice +
                        ", finalPrice=" + finalPrice +
                        ", branch=" + branchId +
                        ", customerId=" + customerId,
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
     * חישוב מחיר למכירה (לפני ביצוע המכירה)
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
     * יצירת משתמש חדש (רק ADMIN)
     */
    public void createUser(String username,
                          String password,
                          String employeeNumber,
                          String userType,
                          String branchId)
            throws WeakPasswordException, DuplicateUserException {
        
        // בדיקה אם המשתמש כבר קיים
        if (authenticationManager.userExists(username)) {
            throw new DuplicateUserException("User with username " + username + " already exists");
        }
        
        // בדיקה ש-userType תקין
        if (!userType.equals("ADMIN") && !userType.equals("EMPLOYEE")) {
            throw new IllegalArgumentException("UserType must be ADMIN or EMPLOYEE");
        }
        
        authenticationManager.createUser(username, password, employeeNumber, userType, branchId);
        
        LogEntry entry = new LogEntry(
                "CREATE_USER",
                "User created: " + username + ", type=" + userType + ", employee=" + employeeNumber + ", branch=" + branchId,
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
     * עדכון משתמש (רק ADMIN)
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
     * השבתת/הפעלת משתמש (רק ADMIN)
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
     * קבלת כל המשתמשים (רק ADMIN)
     */
    public Map<String, User> getAllUsers() {
        return authenticationManager.getAllUsers();
    }
    
    /**
     * קבלת משתמש לפי username (רק ADMIN)
     */
    public User getUser(String username) throws UserNotFoundException {
        User user = authenticationManager.getUser(username);
        if (user == null) {
            throw new UserNotFoundException("User " + username + " not found");
        }
        return user;
    }
    
    /**
     * מחיקת משתמש מהמערכת (רק ADMIN)
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
     * יצירת עובד חדש (רק ADMIN)
     */
    public void createEmployee(String fullName,
                              String idNumber,
                              String phone,
                              String bankAccount,
                              String employeeNumber,
                              String role,
                              String branchId)
            throws DuplicateEmployeeException {
        
        employeeManager.addEmployee(fullName, idNumber, phone, bankAccount, employeeNumber, role, branchId);
        
        LogEntry entry = new LogEntry(
                "CREATE_EMPLOYEE",
                "Employee created: " + fullName + ", number=" + employeeNumber + ", role=" + role + ", branch=" + branchId,
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
     * עדכון פרטי עובד (רק ADMIN)
     */
    public void updateEmployee(String employeeNumber,
                              String fullName,
                              String phone,
                              String bankAccount,
                              String role,
                              String branchId)
            throws EmployeeNotFoundException {
        
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
     * השבתת/הפעלת עובד (רק ADMIN)
     */
    public void setEmployeeActive(String employeeNumber, boolean active)
            throws EmployeeNotFoundException {
        
        employeeManager.setEmployeeActive(employeeNumber, active);
        
        LogEntry entry = new LogEntry(
                "SET_EMPLOYEE_ACTIVE",
                "Employee " + employeeNumber + " set to " + (active ? "active" : "inactive"),
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
     * מחיקת עובד (רק ADMIN)
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
     * קבלת כל העובדים (רק ADMIN)
     */
    public Map<String, Employee> getAllEmployees() {
        return employeeManager.getAllEmployees();
    }
    
    /**
     * קבלת עובד לפי מספר עובד (רק ADMIN)
     */
    public Employee getEmployee(String employeeNumber)
            throws EmployeeNotFoundException {
        return employeeManager.getEmployee(employeeNumber);
    }
    
    /**
     * קבלת עובדים לפי סניף (רק ADMIN)
     */
    public Map<String, Employee> getEmployeesByBranch(String branchId) {
        return employeeManager.getEmployeesByBranch(branchId);
    }
    
    // ========== List Methods for GUI ==========
    
    /**
     * קבלת כל הלקוחות (להצגה בממשק)
     */
    public Map<String, Customer> getAllCustomersForDisplay() {
        return customerManager.getAllCustomers();
    }
    
    /**
     * קבלת כל המוצרים (להצגה בממשק)
     */
    public Map<String, Product> getAllProductsForDisplay() {
        return productManager.getAllProducts();
    }
    
    /**
     * קבלת כמות במלאי לפי מוצר וסניף
     * אם branchId = "ALL", מחזיר את הסכום מכל הסניפים (ADMIN only)
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
     * דוח מכירות לפי סניף
     * מחזיר רשימת ReportEntry עם סיכום לפי סניף
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
     * דוח מכירות לפי מוצר
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
     * דוח מכירות לפי קטגוריה
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
     * דוח מכירות יומי (לפי תאריך)
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
     * משתמש מבקש צ'אט
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
     * שליחת הודעה בצ'אט
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
     * סיום צ'אט
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
     * מנהל מצטרף לצ'אט
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
     * רשימת משתמשים פנויים מסניף אחר
     * @deprecated לא בשימוש יותר - משתמשים ב-getWaitingRequestsForBranch
     */
    @Deprecated
    public List<String> getAvailableUsers(String excludeBranchId) {
        return chatManager.getAvailableUsers(excludeBranchId);
    }
    
    /**
     * קבלת רשימת בקשות ממתינות לסניף מסוים
     */
    public List<model.ChatRequest> getWaitingRequestsForBranch(String branchId) {
        return chatManager.getWaitingRequestsForBranch(branchId);
    }
    
    /**
     * משתמש מאשר בקשה לצ'אט
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
     * קבלת כל הלוגים
     */
    public List<LogEntry> getAllLogs() {
        return logManager.getLogs();
    }
    
    /**
     * קבלת כל הלוגים של שיחה מסוימת
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
     * קבלת פרטי שיחה מלאים (לוגים + ChatSession אם קיים)
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
     * שמירת שיחה ל-RTF
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
    
}
