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
import storage.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.io.IOException;


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


    public void addProductToCatalog(String productId,
        String name,
        String category,
        double price) {
        Product product = productManager.getProduct(productId, name, category, price);
        LogEntry entry = new LogEntry(
            "ADD_PRODUCT_TO_CATALOG",
            "Product added to catalog: id=" + productId +
            ", name=" + name +
            ", category=" + category +
            ", price=" + price,
            LocalDateTime.now().toString()
        );
        logManager.addLog(entry);
        try {
            saveProducts();
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
                dateTime
        );

        salesManager.addSale(sale);

        // 7. לוג
        LogEntry entry = new LogEntry(
                "SALE",
                "Sold product " + productId +
                        ", qty=" + quantity +
                        ", branch=" + branchId +
                        ", customer=" + customerId +
                        ", finalPrice=" + finalPrice,
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
}
