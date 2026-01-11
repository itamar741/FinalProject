package server;

import controller.SystemController;
import model.exceptions.*;
import model.Session;
import model.User;
import model.Employee;
import model.Customer;
import model.Product;
import model.Branch;
import model.VipCustomer;
import model.ReturningCustomer;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SystemController controller;
    private Session currentSession;
    private boolean isAuthenticated = false;

    public ClientHandler(Socket socket, SystemController controller) {
        this.socket = socket;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {
            out.println("CONNECTED");
            
            // שלב אימות - חובה לפני כל פעולה אחרת
            String line;
            while ((line = in.readLine()) != null) {
                
                if (line.equalsIgnoreCase("EXIT")) {
                    if (isAuthenticated) {
                        controller.logout(socket);
                    }
                    break;
                }
                
                try {
                    String response = handleCommand(line);
                    out.println(response);
                } catch (InvalidCredentialsException | 
                         UserAlreadyLoggedInException |
                         UnauthorizedException e) {
                    out.println("AUTH_ERROR;" + e.getMessage());
                } catch (DuplicateCustomerException |
                         DuplicateUserException |
                         DuplicateEmployeeException |
                         InvalidQuantityException |
                         InsufficientStockException |
                         InactiveProductException |
                         WeakPasswordException |
                         UserNotFoundException |
                         EmployeeNotFoundException e) {
                    out.println("ERROR;" + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Client IO error");
        } finally {
            // אם המשתמש לא התנתק בצורה מסודרת
            if (isAuthenticated) {
                controller.logout(socket);
            }
        }
    }

    private String handleCommand(String line)
            throws DuplicateCustomerException,
            DuplicateUserException,
            DuplicateEmployeeException,
            InvalidQuantityException,
            InsufficientStockException,
            InactiveProductException,
            InvalidCredentialsException,
            UserAlreadyLoggedInException,
            UnauthorizedException,
            WeakPasswordException,
            UserNotFoundException,
            EmployeeNotFoundException {
        
        String[] parts = line.split(";");
        
        switch (parts[0]) {
            case "LOGIN":
                if (parts.length < 3) {
                    throw new IllegalArgumentException("LOGIN requires username and password");
                }
                currentSession = controller.login(parts[1], parts[2], socket);
                isAuthenticated = true;
                return "LOGIN_SUCCESS;" + currentSession.getUserType() + ";" + currentSession.getBranchId() + ";" + currentSession.getEmployeeNumber();
            
            case "LOGOUT":
                controller.logout(socket);
                isAuthenticated = false;
                currentSession = null;
                return "LOGOUT_SUCCESS";
            
            default:
                // כל פקודה אחרת דורשת אימות
                if (!isAuthenticated) {
                    throw new UnauthorizedException("You must login first");
                }
                
                // המשך עם הפקודות הקיימות...
                return handleAuthenticatedCommand(line);
        }
    }

           
    private String handleAuthenticatedCommand(String line)
        throws DuplicateCustomerException,
        DuplicateUserException,
        DuplicateEmployeeException,
        InvalidQuantityException,
        InsufficientStockException,
        InactiveProductException,
        UnauthorizedException,
        WeakPasswordException,
        UserNotFoundException,
        EmployeeNotFoundException {

    if (currentSession == null) {
        throw new UnauthorizedException("Session expired");
    }

    String[] parts = line.split(";");
    String userType = currentSession.getUserType();  // שונה מ-getRole()

    switch (parts[0]) {
        case "ADD_CUSTOMER": {
            // בדיקת פרמטרים
            if (parts.length < 5) {
                throw new IllegalArgumentException("ADD_CUSTOMER requires: fullName;idNumber;phone;customerType");
            }
            
            // ADMIN ו-EMPLOYEE יכולים להוסיף לקוחות
            // (אין צורך בבדיקת הרשאה מיוחדת, כל משתמש מחובר יכול)
            
            // בדיקת תקינות סוג לקוח
            String customerType = parts[4].toUpperCase();
            if (!customerType.equals("NEW") && 
                !customerType.equals("RETURNING") && 
                !customerType.equals("VIP")) {
                throw new IllegalArgumentException("Invalid customer type. Must be: NEW, RETURNING, or VIP");
            }
            
            controller.addCustomer(
                    parts[1],  // fullName
                    parts[2],  // idNumber
                    parts[3],  // phone
                    customerType
            );
            
            return "OK;Customer added successfully";
        }

        case "ADD_PRODUCT_TO_INVENTORY": {
            // בדיקת פרמטרים
            if (parts.length < 4) {
                throw new IllegalArgumentException("ADD_PRODUCT_TO_INVENTORY requires: productId;quantity;branchId");
            }
            
            // ADMIN ו-EMPLOYEE יכולים להוסיף למלאי
            // EMPLOYEE יכול רק לסניף שלו, ADMIN יכול לכל סניף
            
            String requestedBranchId = parts[3];
            
            // בדיקה: EMPLOYEE יכול להוסיף רק לסניף שלו
            if (!userType.equals("ADMIN") && 
                !currentSession.getBranchId().equals(requestedBranchId)) {
                throw new UnauthorizedException("You can only add products to your own branch (" + currentSession.getBranchId() + "). Only ADMIN can add to any branch");
            }
            
            // בדיקת תקינות כמות
            int quantity;
            try {
                quantity = Integer.parseInt(parts[2]);
                if (quantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            controller.addProductToInventory(
                    parts[1],  // productId
                    quantity,
                    requestedBranchId
            );
            
            return "OK;Product added to inventory successfully";
        }

        case "ADD_PRODUCT_TO_CATALOG":
            // בדיקת פרמטרים
            if (parts.length < 5) {
                throw new IllegalArgumentException("ADD_PRODUCT_TO_CATALOG requires: productId;name;category;price");
            }
            
            // רק ADMIN יכול להוסיף לקטלוג
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can add products to catalog");
            }
            
            // בדיקת תקינות מחיר
            double price;
            try {
                price = Double.parseDouble(parts[4]);
                if (price < 0) {
                    throw new IllegalArgumentException("Price cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid price format: " + parts[4]);
            }
            
            // בדיקה שכל השדות לא ריקים
            if (parts[1].trim().isEmpty() || 
                parts[2].trim().isEmpty() || 
                parts[3].trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID, name, and category cannot be empty");
            }
            
            controller.addProductToCatalog(
                    parts[1],  // productId
                    parts[2],  // name
                    parts[3],  // category
                    price
            );
            
            return "OK;Product added to catalog successfully";

        case "SELL":
            // בדיקת פרמטרים
            if (parts.length < 6) {
                throw new IllegalArgumentException("SELL requires: productId;quantity;branchId;employeeNumber;customerId");
            }
            
            // ADMIN ו-EMPLOYEE יכולים למכור
            // EMPLOYEE יכול למכור רק בסניף שלו, ADMIN בכל סניף
            
            String sellBranchId = parts[3];
            
            // בדיקה: EMPLOYEE יכול למכור רק בסניף שלו
            if (!userType.equals("ADMIN") && 
                !currentSession.getBranchId().equals(sellBranchId)) {
                throw new UnauthorizedException("You can only sell products from your own branch (" + currentSession.getBranchId() + "). Only ADMIN can sell from any branch");
            }
            
            // בדיקת employeeNumber - EMPLOYEE חייב להשתמש במספר שלו, ADMIN יכול להשתמש בכל מספר
            String sellEmployeeNumber = parts[4];
            if (!userType.equals("ADMIN") && 
                !currentSession.getEmployeeNumber().equals(sellEmployeeNumber)) {
                throw new UnauthorizedException("You can only sell products under your own employee number (" + currentSession.getEmployeeNumber() + "). Only ADMIN can use any employee number");
            }
            
            // בדיקת תקינות כמות
            int sellQuantity;
            try {
                sellQuantity = Integer.parseInt(parts[2]);
                if (sellQuantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            controller.sellProduct(
                    parts[1],  // productId
                    sellQuantity,
                    sellBranchId,
                    sellEmployeeNumber,
                    parts[5]   // customerId
            );
            
            return "OK;Sale completed successfully";

        // ========== List Commands ==========
        case "LIST_CUSTOMERS": {
            // כל המשתמשים המחוברים יכולים לראות לקוחות
            Map<String, Customer> customers = controller.getAllCustomersForDisplay();
            StringBuilder customersList = new StringBuilder("OK;");
            for (Customer c : customers.values()) {
                String customerType = "NEW";
                if (c instanceof VipCustomer) {
                    customerType = "VIP";
                } else if (c instanceof ReturningCustomer) {
                    customerType = "RETURNING";
                }
                customersList.append(c.getIdNumber()).append(":")
                             .append(c.getFullName()).append(":")
                             .append(c.getPhone()).append(":")
                             .append(customerType).append("|");
            }
            return customersList.toString();
        }
            
        case "LIST_PRODUCTS": {
            // כל המשתמשים המחוברים יכולים לראות מוצרים
            Map<String, Product> products = controller.getAllProductsForDisplay();
            StringBuilder productsList = new StringBuilder("OK;");
            
            // קבלת branchId - אם ADMIN אז כל הסניפים, אחרת הסניף של המשתמש
            String listBranchId = userType.equals("ADMIN") ? "ALL" : currentSession.getBranchId();
            
            for (Product p : products.values()) {
                // אם ADMIN, נציג את הסכום הכולל מכל הסניפים
                int quantity = 0;
                if (userType.equals("ADMIN")) {
                    // סכום מכל הסניפים - נשתמש ב-ALL כסימן שצריך לסכם הכל
                    quantity = controller.getInventoryQuantity(p.getProductId(), "ALL");
                } else {
                    // כמות בסניף של המשתמש
                    quantity = controller.getInventoryQuantity(p.getProductId(), currentSession.getBranchId());
                }
                
                productsList.append(p.getProductId()).append(":")
                            .append(p.getName()).append(":")
                            .append(p.getCategory()).append(":")
                            .append(p.getPrice()).append(":")
                            .append(p.isActive() ? "active" : "inactive").append(":")
                            .append(quantity).append("|");
            }
            return productsList.toString();
        }

        // ========== Admin Commands - User Management ==========
        case "CREATE_USER":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can create users");
            }
            if (parts.length < 6) {
                throw new IllegalArgumentException("CREATE_USER requires: username;password;employeeNumber;userType;branchId");
            }
            String createUserType = parts[4].toUpperCase();
            if (!createUserType.equals("ADMIN") && !createUserType.equals("EMPLOYEE")) {
                throw new IllegalArgumentException("UserType must be ADMIN or EMPLOYEE");
            }
            controller.createUser(parts[1], parts[2], parts[3], createUserType, parts[5]);
            return "OK;User created successfully";

        case "UPDATE_USER":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can update users");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("UPDATE_USER requires at least: username");
            }
            String updateUsername = parts[1];
            String newPassword = parts.length > 2 ? parts[2] : null;
            String newBranchId = parts.length > 3 ? parts[3] : null;
            Boolean active = parts.length > 4 ? Boolean.parseBoolean(parts[4]) : null;
            controller.updateUser(updateUsername, newPassword, newBranchId, active);
            return "OK;User updated successfully";

        case "SET_USER_ACTIVE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can activate/deactivate users");
            }
            if (parts.length < 3) {
                throw new IllegalArgumentException("SET_USER_ACTIVE requires: username;active");
            }
            controller.setUserActive(parts[1], Boolean.parseBoolean(parts[2]));
            return "OK;User status updated successfully";

        case "LIST_USERS":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can list users");
            }
            Map<String, User> users = controller.getAllUsers();
            StringBuilder usersList = new StringBuilder("OK;");
            for (User u : users.values()) {
                usersList.append(u.getUsername()).append(":")
                         .append(u.getUserType()).append(":")
                         .append(u.getEmployeeNumber()).append(":")
                         .append(u.getBranchId()).append(":")
                         .append(u.isActive() ? "active" : "inactive").append("|");
            }
            return usersList.toString();

        case "GET_USER":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can get user details");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_USER requires: username");
            }
            User user = controller.getUser(parts[1]);
            return "OK;" + user.getUsername() + ":" + user.getUserType() + ":" + 
                   user.getEmployeeNumber() + ":" + user.getBranchId() + ":" + 
                   (user.isActive() ? "active" : "inactive");

        case "DELETE_USER":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can delete users");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_USER requires: username");
            }
            // מניעת מחיקת המשתמש המחובר בעצמו
            if (parts[1].equals(currentSession.getUsername())) {
                throw new IllegalArgumentException("Cannot delete your own user account while logged in");
            }
            controller.deleteUser(parts[1]);
            return "OK;User deleted successfully";

        // ========== Admin Commands - Employee Management ==========
        case "CREATE_EMPLOYEE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can create employees");
            }
            if (parts.length < 8) {
                throw new IllegalArgumentException("CREATE_EMPLOYEE requires: fullName;idNumber;phone;bankAccount;employeeNumber;role;branchId");
            }
            controller.createEmployee(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
            return "OK;Employee created successfully";

        case "UPDATE_EMPLOYEE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can update employees");
            }
            if (parts.length < 7) {
                throw new IllegalArgumentException("UPDATE_EMPLOYEE requires: employeeNumber;fullName;phone;bankAccount;role;branchId");
            }
            controller.updateEmployee(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
            return "OK;Employee updated successfully";

        case "SET_EMPLOYEE_ACTIVE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can activate/deactivate employees");
            }
            if (parts.length < 3) {
                throw new IllegalArgumentException("SET_EMPLOYEE_ACTIVE requires: employeeNumber;active");
            }
            controller.setEmployeeActive(parts[1], Boolean.parseBoolean(parts[2]));
            return "OK;Employee status updated successfully";

        case "DELETE_EMPLOYEE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can delete employees");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_EMPLOYEE requires: employeeNumber");
            }
            controller.deleteEmployee(parts[1]);
            return "OK;Employee deleted successfully";

        case "LIST_EMPLOYEES":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can list employees");
            }
            Map<String, Employee> employees = controller.getAllEmployees();
            StringBuilder employeesList = new StringBuilder("OK;");
            for (Employee emp : employees.values()) {
                employeesList.append(emp.getEmployeeNumber()).append(":")
                             .append(emp.getFullName()).append(":")
                             .append(emp.getIdNumber()).append(":")
                             .append(emp.getPhone()).append(":")
                             .append(emp.getRole()).append(":")
                             .append(emp.getBranchId()).append(":")
                             .append(emp.isActive() ? "active" : "inactive").append("|");
            }
            return employeesList.toString();

        case "GET_EMPLOYEE":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can get employee details");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_EMPLOYEE requires: employeeNumber");
            }
            Employee employee = controller.getEmployee(parts[1]);
            return "OK;" + employee.getEmployeeNumber() + ":" + employee.getFullName() + ":" + 
                   employee.getIdNumber() + ":" + employee.getPhone() + ":" + 
                   employee.getBankAccount() + ":" + employee.getRole() + ":" + 
                   employee.getBranchId() + ":" + (employee.isActive() ? "active" : "inactive");

        case "LIST_EMPLOYEES_BY_BRANCH":
            if (!userType.equals("ADMIN")) {
                throw new UnauthorizedException("Only ADMIN can list employees by branch");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("LIST_EMPLOYEES_BY_BRANCH requires: branchId");
            }
            Map<String, Employee> branchEmployees = controller.getEmployeesByBranch(parts[1]);
            StringBuilder branchEmployeesList = new StringBuilder("OK;");
            for (Employee emp : branchEmployees.values()) {
                branchEmployeesList.append(emp.getEmployeeNumber()).append(":")
                                   .append(emp.getFullName()).append(":")
                                   .append(emp.getRole()).append(":")
                                   .append(emp.isActive() ? "active" : "inactive").append("|");
            }
            return branchEmployeesList.toString();

        default:
            throw new IllegalArgumentException("Unknown command: " + parts[0]);
    }
}
}
