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
import java.util.List;
import java.util.Vector;
import model.ReportEntry;
import model.ChatMessage;
import model.ChatSession;
import model.ChatUserStatus;
import model.ChatRequest;
import model.managers.PermissionChecker;

/**
 * Handles communication with a single client.
 * Implements Runnable for Thread-per-Client architecture.
 * Reads commands from the client, routes them to SystemController, and sends responses.
 * Uses a simple text protocol with commands separated by semicolons.
 * 
 * @author FinalProject
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SystemController controller;
    private final Vector<Socket> connectedClients;
    private Session currentSession;
    private boolean isAuthenticated = false;

    /**
     * Constructs a new ClientHandler for a client connection.
     * 
     * @param socket the socket connection to the client
     * @param controller the SystemController to route commands to
     * @param connectedClients the Vector containing all connected client sockets
     */
    public ClientHandler(Socket socket, SystemController controller, Vector<Socket> connectedClients) {
        this.socket = socket;
        this.controller = controller;
        this.connectedClients = connectedClients;
    }

    /**
     * Main run loop for handling client communication.
     * Reads commands from the client, processes them, and sends responses.
     * Handles authentication and routes authenticated commands to appropriate handlers.
     * Automatically logs out the user when connection is closed.
     */
    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {
            out.println("CONNECTED");
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
                         WeakPasswordException |
                         UserNotFoundException |
                         EmployeeNotFoundException |
                         InvalidIdNumberException |
                         InvalidPhoneException e) {
                    out.println("ERROR;" + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Client IO error");
        } finally {
            if (isAuthenticated) {
                controller.logout(socket);
            }
            
            // Remove socket from Vector and close it
            synchronized (connectedClients) {
                connectedClients.remove(socket);
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    /**
     * Handles a command from the client.
     * Parses the command string and routes to appropriate handler.
     * 
     * @param line the command line from the client (format: "COMMAND;param1;param2;...")
     * @return the response string to send to the client
     * @throws various exceptions depending on the command and operation result
     */
    private String handleCommand(String line)
            throws DuplicateCustomerException,
            DuplicateUserException,
            DuplicateEmployeeException,
            InvalidQuantityException,
            InsufficientStockException,
            InvalidCredentialsException,
            UserAlreadyLoggedInException,
            UnauthorizedException,
            WeakPasswordException,
            UserNotFoundException,
            EmployeeNotFoundException,
            InvalidIdNumberException,
            InvalidPhoneException,
            IOException {
        
        String[] parts = line.split(";");
        
        switch (parts[0]) {
            case "LOGIN":
                if (parts.length < 3) {
                    throw new IllegalArgumentException("LOGIN requires username and password");
                }
                currentSession = controller.login(parts[1], parts[2], socket);
                isAuthenticated = true;
                return "LOGIN_SUCCESS;" + currentSession.getRole() + ";" + currentSession.getBranchId();
            
            case "LOGOUT":
                controller.logout(socket);
                isAuthenticated = false;
                currentSession = null;
                return "LOGOUT_SUCCESS";
            
            default:
                if (!isAuthenticated) {
                    throw new UnauthorizedException("You must login first");
                }
                return handleAuthenticatedCommand(line);
        }
    }

    private String handleAuthenticatedCommand(String line)
        throws DuplicateCustomerException,
        DuplicateUserException,
        DuplicateEmployeeException,
        InvalidQuantityException,
        InsufficientStockException,
        UnauthorizedException,
        WeakPasswordException,
        UserNotFoundException,
        EmployeeNotFoundException,
        InvalidIdNumberException,
        InvalidPhoneException,
        IOException {

    if (currentSession == null) {
        throw new UnauthorizedException("Session expired");
    }

    String[] parts = line.split(";");
    String role = currentSession.getRole();
    String userBranchId = currentSession.getBranchId();

    switch (parts[0]) {
        case "ADD_CUSTOMER": {
            if (parts.length < 5) {
                throw new IllegalArgumentException("ADD_CUSTOMER requires: fullName;idNumber;phone;customerType");
            }
            String customerType = parts[4].toUpperCase();
            if (!customerType.equals("NEW") && 
                !customerType.equals("RETURNING") && 
                !customerType.equals("VIP")) {
                throw new IllegalArgumentException("Invalid customer type. Must be: NEW, RETURNING, or VIP");
            }
            
            controller.addCustomer(parts[1], parts[2], parts[3], customerType);
            
            return "OK;Customer added successfully";
        }

        case "UPDATE_CUSTOMER": {
            if (parts.length < 5) {
                throw new IllegalArgumentException("UPDATE_CUSTOMER requires: idNumber;fullName;phone;customerType");
            }
            String customerType = parts[4].toUpperCase();
            if (!customerType.equals("NEW") && 
                !customerType.equals("RETURNING") && 
                !customerType.equals("VIP")) {
                throw new IllegalArgumentException("Invalid customer type. Must be: NEW, RETURNING, or VIP");
            }
            
            controller.updateCustomer(parts[1], parts[2], parts[3], customerType);
            
            return "OK;Customer updated successfully";
        }

        case "DELETE_CUSTOMER": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_CUSTOMER requires: idNumber");
            }
            controller.deleteCustomer(parts[1]);
            
            return "OK;Customer deleted successfully";
        }

        case "ADD_PRODUCT_TO_INVENTORY": {
            if (parts.length < 4) {
                throw new IllegalArgumentException("ADD_PRODUCT_TO_INVENTORY requires: productId;quantity;branchId");
            }
            String requestedBranchId = parts[3];
            if (!PermissionChecker.canAccessBranch(role, userBranchId, requestedBranchId)) {
                throw new UnauthorizedException("You can only add products to your own branch (" + currentSession.getBranchId() + "). Only ADMIN can add to any branch");
            }
            int quantity;
            try {
                quantity = Integer.parseInt(parts[2]);
                if (quantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            controller.addProductToInventory(parts[1], quantity, requestedBranchId);
            
            return "OK;Product added to inventory successfully";
        }

        case "ADD_PRODUCT": {
            if (parts.length < 7) {
                throw new IllegalArgumentException("ADD_PRODUCT requires: productId;name;category;price;quantity;branchId");
            }
            String requestedBranchId = parts[6];
            if (!PermissionChecker.canAccessBranch(role, userBranchId, requestedBranchId)) {
                throw new UnauthorizedException("You can only add products to your own branch (" + currentSession.getBranchId() + "). Only ADMIN can add to any branch");
            }
            double price;
            try {
                price = Double.parseDouble(parts[4]);
                if (price < 0) {
                    throw new IllegalArgumentException("Price cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid price format: " + parts[4]);
            }
            int quantity;
            try {
                quantity = Integer.parseInt(parts[5]);
                if (quantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[5]);
            }
            if (parts[1].trim().isEmpty() || parts[2].trim().isEmpty() || parts[3].trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID, name, and category cannot be empty");
            }
            controller.addProduct(parts[1], parts[2], parts[3], price, quantity, requestedBranchId);
            
            return "OK;Product added successfully";
        }

        case "REMOVE_FROM_INVENTORY": {
            if (parts.length < 4) {
                throw new IllegalArgumentException("REMOVE_FROM_INVENTORY requires: productId;quantity;branchId");
            }
            String requestedBranchId = parts[3];
            if (!PermissionChecker.canAccessBranch(role, userBranchId, requestedBranchId)) {
                throw new UnauthorizedException("You can only remove products from your own branch (" + userBranchId + "). Only admin can remove from any branch");
            }
            int quantity;
            try {
                quantity = Integer.parseInt(parts[2]);
                if (quantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            controller.removeFromInventory(parts[1], quantity, requestedBranchId);
            
            return "OK;Product removed from inventory successfully";
        }

        case "DELETE_PRODUCT": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_PRODUCT requires: productId");
            }
            if (!PermissionChecker.canDeleteProduct(role)) {
                throw new UnauthorizedException("Only admin can delete products");
            }
            
            controller.deleteProduct(parts[1]);
            
            return "OK;Product deleted successfully";
        }

        case "CALCULATE_PRICE": {
            if (parts.length < 4) {
                throw new IllegalArgumentException("CALCULATE_PRICE requires: productId;quantity;customerId");
            }
            int quantity;
            try {
                quantity = Integer.parseInt(parts[2]);
                if (quantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            double finalPrice = controller.calculatePrice(parts[1], quantity, parts[3]);
            
            return "OK;" + finalPrice;
        }

        case "SELL":
            if (parts.length < 5) {
                throw new IllegalArgumentException("SELL requires: productId;quantity;branchId;customerId");
            }
            String sellBranchId = parts[3];
            if (!PermissionChecker.canAccessBranch(role, userBranchId, sellBranchId)) {
                throw new UnauthorizedException("You can only sell products from your own branch (" + userBranchId + "). Only admin can sell from any branch");
            }
            String username = currentSession.getUsername();
            String sellEmployeeNumber = controller.getEmployeeNumberByUsername(username, role);
            int sellQuantity;
            try {
                sellQuantity = Integer.parseInt(parts[2]);
                if (sellQuantity <= 0) {
                    throw new InvalidQuantityException("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new InvalidQuantityException("Invalid quantity format: " + parts[2]);
            }
            
            controller.sellProduct(parts[1], sellQuantity, sellBranchId, sellEmployeeNumber, parts[4]);
            
            return "OK;Sale completed successfully";

        case "GET_DISCOUNTS": {
            Map<String, Double> discounts = controller.getAllDiscounts();
            StringBuilder discountsList = new StringBuilder("OK;");
            for (Map.Entry<String, Double> entry : discounts.entrySet()) {
                discountsList.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
            }
            if (discountsList.length() > 3) {
                discountsList.setLength(discountsList.length() - 1);
            }
            return discountsList.toString();
        }
        
        case "SET_DISCOUNT": {
            if (parts.length < 3) {
                throw new IllegalArgumentException("SET_DISCOUNT requires: customerType;discountPercentage");
            }
            String customerType = parts[1];
            double discountPercentage;
            try {
                discountPercentage = Double.parseDouble(parts[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid discount percentage: " + parts[2]);
            }
            controller.setDiscount(customerType, discountPercentage);
            return "OK;Discount updated successfully";
        }
        
        case "LIST_CUSTOMERS": {
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
            Map<String, Product> products = controller.getAllProductsForDisplay();
            StringBuilder productsList = new StringBuilder("OK;");
            String listBranchId = PermissionChecker.canViewAllBranches(role) ? "ALL" : userBranchId;
            for (Product p : products.values()) {
                int quantity = 0;
                if (PermissionChecker.canViewAllBranches(role)) {
                    quantity = controller.getInventoryQuantity(p.getProductId(), "ALL");
                } else {
                    quantity = controller.getInventoryQuantity(p.getProductId(), userBranchId);
                }
                
                productsList.append(p.getProductId()).append(":")
                            .append(p.getName()).append(":")
                            .append(p.getCategory()).append(":")
                            .append(p.getPrice()).append(":")
                            .append(quantity).append("|");
            }
            return productsList.toString();
        }
            
        case "LIST_PRODUCTS_BY_BRANCH": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("LIST_PRODUCTS_BY_BRANCH requires: branchId");
            }
            String requestedBranchId = parts[1];
            if (!PermissionChecker.canAccessBranch(role, userBranchId, requestedBranchId)) {
                throw new UnauthorizedException("You can only view products from your own branch (" + userBranchId + "). Only admin can view any branch");
            }
            Map<String, Product> products = controller.getAllProductsForDisplay();
            StringBuilder productsList = new StringBuilder("OK;");
            for (Product p : products.values()) {
                int quantity = controller.getInventoryQuantity(p.getProductId(), requestedBranchId);
                
                productsList.append(p.getProductId()).append(":")
                            .append(p.getName()).append(":")
                            .append(p.getCategory()).append(":")
                            .append(p.getPrice()).append(":")
                            .append(quantity).append("|");
            }
            return productsList.toString();
        }

        case "REPORT_SALES_BY_BRANCH": {
            String branchId = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : null;
            if (!PermissionChecker.canViewAllBranches(role) && (branchId == null || !branchId.equals(userBranchId))) {
                branchId = userBranchId;
            }
            List<ReportEntry> report = controller.getSalesReportByBranch(branchId);
            StringBuilder json = new StringBuilder("{\"reportType\":\"SALES_BY_BRANCH\",\"branchId\":\"" + 
                escapeJson(branchId != null ? branchId : "ALL") + "\",\"entries\":[");
            for (int i = 0; i < report.size(); i++) {
                ReportEntry entry = report.get(i);
                if (i > 0) json.append(",");
                json.append("{\"branchId\":\"").append(escapeJson(entry.getBranchId()))
                    .append("\",\"quantity\":").append(entry.getQuantity())
                    .append(",\"totalRevenue\":").append(String.format("%.2f", entry.getTotalRevenue()))
                    .append("}");
            }
            json.append("]}");
            
            return "OK;" + json.toString();
        }

        case "REPORT_SALES_BY_PRODUCT": {
            String productId = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : null;
            List<ReportEntry> report = controller.getSalesReportByProduct(productId);
            StringBuilder json = new StringBuilder("{\"reportType\":\"SALES_BY_PRODUCT\",\"productId\":\"" + 
                escapeJson(productId != null ? productId : "ALL") + "\",\"entries\":[");
            for (int i = 0; i < report.size(); i++) {
                ReportEntry entry = report.get(i);
                if (i > 0) json.append(",");
                json.append("{\"branchId\":\"").append(escapeJson(entry.getBranchId()))
                    .append("\",\"productId\":\"").append(escapeJson(entry.getProductId()))
                    .append("\",\"productName\":\"").append(escapeJson(entry.getProductName()))
                    .append("\",\"category\":\"").append(escapeJson(entry.getCategory()))
                    .append("\",\"quantity\":").append(entry.getQuantity())
                    .append(",\"totalRevenue\":").append(String.format("%.2f", entry.getTotalRevenue()))
                    .append(",\"date\":\"").append(escapeJson(entry.getDate()))
                    .append("\"}");
            }
            json.append("]}");
            
            return "OK;" + json.toString();
        }

        case "REPORT_SALES_BY_CATEGORY": {
            String category = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : null;
            List<ReportEntry> report = controller.getSalesReportByCategory(category);
            StringBuilder json = new StringBuilder("{\"reportType\":\"SALES_BY_CATEGORY\",\"category\":\"" + 
                escapeJson(category != null ? category : "ALL") + "\",\"entries\":[");
            for (int i = 0; i < report.size(); i++) {
                ReportEntry entry = report.get(i);
                if (i > 0) json.append(",");
                json.append("{\"category\":\"").append(escapeJson(entry.getCategory()))
                    .append("\",\"quantity\":").append(entry.getQuantity())
                    .append(",\"totalRevenue\":").append(String.format("%.2f", entry.getTotalRevenue()))
                    .append("}");
            }
            json.append("]}");
            
            return "OK;" + json.toString();
        }

        case "REPORT_DAILY_SALES": {
            String date = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : null;
            String branchId = (parts.length > 2 && !parts[2].isEmpty()) ? parts[2] : null;
            if (!PermissionChecker.canViewAllBranches(role) && (branchId == null || !branchId.equals(userBranchId))) {
                branchId = userBranchId;
            }
            List<ReportEntry> report = controller.getDailySalesReport(date, branchId);
            StringBuilder json = new StringBuilder("{\"reportType\":\"DAILY_SALES\",\"date\":\"" + 
                escapeJson(date != null ? date : "ALL") + "\",\"branchId\":\"" + 
                escapeJson(branchId != null ? branchId : "ALL") + "\",\"entries\":[");
            for (int i = 0; i < report.size(); i++) {
                ReportEntry entry = report.get(i);
                if (i > 0) json.append(",");
                json.append("{\"branchId\":\"").append(escapeJson(entry.getBranchId()))
                    .append("\",\"productId\":\"").append(escapeJson(entry.getProductId()))
                    .append("\",\"productName\":\"").append(escapeJson(entry.getProductName()))
                    .append("\",\"category\":\"").append(escapeJson(entry.getCategory()))
                    .append("\",\"quantity\":").append(entry.getQuantity())
                    .append(",\"totalRevenue\":").append(String.format("%.2f", entry.getTotalRevenue()))
                    .append(",\"date\":\"").append(escapeJson(entry.getDate()))
                    .append("\"}");
            }
            json.append("]}");
            
            return "OK;" + json.toString();
        }

        case "CREATE_USER":
            if (parts.length < 5) {
                throw new IllegalArgumentException("CREATE_USER requires: username;password;role;branchId");
            }
            String targetBranchId = parts[4];
            if (!PermissionChecker.canCreateUser(role, userBranchId, targetBranchId)) {
                throw new UnauthorizedException("You do not have permission to create users for branch " + targetBranchId);
            }
            controller.createUser(parts[1], parts[2], parts[3], targetBranchId);
            return "OK;User created successfully";

        case "UPDATE_USER":
            if (!PermissionChecker.canManageUsers(role)) {
                throw new UnauthorizedException("Only admin can update users");
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
            if (!PermissionChecker.canManageUsers(role)) {
                throw new UnauthorizedException("Only admin can activate/deactivate users");
            }
            if (parts.length < 3) {
                throw new IllegalArgumentException("SET_USER_ACTIVE requires: username;active");
            }
            controller.setUserActive(parts[1], Boolean.parseBoolean(parts[2]));
            return "OK;User status updated successfully";

        case "LIST_USERS":
            if (!PermissionChecker.canManageUsers(role)) {
                throw new UnauthorizedException("Only admin can list users");
            }
            Map<String, User> users = controller.getAllUsers();
            StringBuilder usersList = new StringBuilder("OK;");
            for (User u : users.values()) {
                usersList.append(u.getUsername()).append(":")
                         .append(u.getRole()).append(":")
                         .append(u.getBranchId()).append(":")
                         .append(u.isActive() ? "active" : "inactive").append("|");
            }
            return usersList.toString();

        case "GET_USER":
            if (!PermissionChecker.canManageUsers(role)) {
                throw new UnauthorizedException("Only admin can get user details");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_USER requires: username");
            }
            User user = controller.getUser(parts[1]);
            return "OK;" + user.getUsername() + ":" + user.getRole() + ":" + 
                   user.getBranchId() + ":" + 
                   (user.isActive() ? "active" : "inactive");

        case "DELETE_USER":
            if (!PermissionChecker.canManageUsers(role)) {
                throw new UnauthorizedException("Only admin can delete users");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_USER requires: username");
            }
            if (parts[1].equals(currentSession.getUsername())) {
                throw new IllegalArgumentException("Cannot delete your own user account while logged in");
            }
            controller.deleteUser(parts[1]);
            return "OK;User deleted successfully";

        case "CREATE_EMPLOYEE":
            if (!PermissionChecker.canCreateEmployee(role)) {
                throw new UnauthorizedException("Only admin and cashier can create employees");
            }
            if (parts.length < 10) {
                throw new IllegalArgumentException("CREATE_EMPLOYEE requires: fullName;idNumber;phone;bankAccount;employeeNumber;username;password;role;branchId");
            }
            String employeeTargetBranchId = parts[9];
            if (!PermissionChecker.canCreateEmployeeForBranch(role, userBranchId, employeeTargetBranchId)) {
                throw new UnauthorizedException("You can only create employees for your own branch");
            }
            controller.createEmployee(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9]);
            return "OK;Employee and user created successfully";

        case "UPDATE_EMPLOYEE":
            if (!PermissionChecker.canManageEmployees(role)) {
                throw new UnauthorizedException("Only admin can update employees");
            }
            if (parts.length < 7) {
                throw new IllegalArgumentException("UPDATE_EMPLOYEE requires: employeeNumber;fullName;phone;bankAccount;role;branchId");
            }
            controller.updateEmployee(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
            return "OK;Employee updated successfully";

        case "DELETE_EMPLOYEE":
            if (!PermissionChecker.canManageEmployees(role)) {
                throw new UnauthorizedException("Only admin can delete employees");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("DELETE_EMPLOYEE requires: employeeNumber");
            }
            controller.deleteEmployee(parts[1]);
            return "OK;Employee deleted successfully";

        case "LIST_EMPLOYEES":
            if (!PermissionChecker.canViewEmployees(role)) {
                throw new UnauthorizedException("Only admin and cashier can list employees");
            }
            Map<String, Employee> employees = controller.getAllEmployees();
            StringBuilder employeesList = new StringBuilder("OK;");
            for (Employee emp : employees.values()) {
                employeesList.append(emp.getEmployeeNumber()).append(":")
                             .append(emp.getFullName()).append(":")
                             .append(emp.getIdNumber()).append(":")
                             .append(emp.getPhone()).append(":")
                             .append(emp.getBankAccount()).append(":")
                             .append(emp.getRole()).append(":")
                             .append(emp.getBranchId()).append("|");
            }
            return employeesList.toString();

        case "GET_EMPLOYEE":
            if (!PermissionChecker.canManageEmployees(role)) {
                throw new UnauthorizedException("Only admin can get employee details");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_EMPLOYEE requires: employeeNumber");
            }
            Employee employee = controller.getEmployee(parts[1]);
            return "OK;" + employee.getEmployeeNumber() + ":" + employee.getFullName() + ":" + 
                   employee.getIdNumber() + ":" + employee.getPhone() + ":" + 
                   employee.getBankAccount() + ":" + employee.getRole() + ":" + 
                   employee.getBranchId();

        case "LIST_EMPLOYEES_BY_BRANCH":
            if (!PermissionChecker.canViewEmployees(role)) {
                throw new UnauthorizedException("Only admin and cashier can list employees by branch");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("LIST_EMPLOYEES_BY_BRANCH requires: branchId");
            }
            String requestedBranchId = parts[1];
            // Cashier can only view their own branch
            if (!PermissionChecker.canAccessBranch(role, userBranchId, requestedBranchId)) {
                throw new UnauthorizedException("You can only view employees from your own branch");
            }
            Map<String, Employee> branchEmployees = controller.getEmployeesByBranch(requestedBranchId);
            StringBuilder branchEmployeesList = new StringBuilder("OK;");
            for (Employee emp : branchEmployees.values()) {
                branchEmployeesList.append(emp.getEmployeeNumber()).append(":")
                                   .append(emp.getFullName()).append(":")
                                   .append(emp.getIdNumber()).append(":")
                                   .append(emp.getPhone()).append(":")
                                   .append(emp.getBankAccount()).append(":")
                                   .append(emp.getRole()).append(":")
                                   .append(emp.getBranchId()).append("|");
            }
            return branchEmployeesList.toString();

        case "REQUEST_CHAT": {
            String requestChatUsername = currentSession.getUsername();
            String branchId = currentSession.getBranchId();
            String result = controller.requestChat(requestChatUsername, branchId);
            return result;
        }

        case "SEND_MESSAGE": {
            if (parts.length < 3) {
                throw new IllegalArgumentException("SEND_MESSAGE requires: chatId;message");
            }
            String chatId = parts[1];
            String message = parts[2];
            String sender = currentSession.getUsername();
            controller.sendChatMessage(chatId, sender, message);
            return "OK;Message sent";
        }

        case "GET_CHAT_MESSAGES": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_CHAT_MESSAGES requires: chatId");
            }
            String chatId = parts[1];
            List<model.ChatMessage> messages = controller.getChatHistory(chatId);
            StringBuilder json = new StringBuilder("OK;{\"chatId\":\"").append(escapeJson(chatId))
                .append("\",\"messages\":[");
            for (int i = 0; i < messages.size(); i++) {
                model.ChatMessage msg = messages.get(i);
                json.append(msg.toJson());
                if (i < messages.size() - 1) json.append(",");
            }
            json.append("]}");
            return json.toString();
        }

        case "END_CHAT": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("END_CHAT requires: chatId");
            }
            String chatId = parts[1];
            String endChatUsername = currentSession.getUsername();
            String branchId = currentSession.getBranchId();
            controller.endChat(chatId);
            String otherBranchId = findOtherBranchId(branchId);
            if (otherBranchId != null) {
                List<model.ChatRequest> waitingRequests = controller.getWaitingRequestsForBranch(otherBranchId);
                if (!waitingRequests.isEmpty()) {
                    StringBuilder notification = new StringBuilder("OK;Chat ended;WAITING:");
                    for (int i = 0; i < waitingRequests.size(); i++) {
                        model.ChatRequest req = waitingRequests.get(i);
                        notification.append(req.getRequestId()).append(":").append(req.getRequesterUsername());
                        if (i < waitingRequests.size() - 1) notification.append("|");
                    }
                    return notification.toString();
                }
            }
            
            return "OK;Chat ended";
        }

        case "JOIN_CHAT": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("JOIN_CHAT requires: chatId");
            }
            if (!PermissionChecker.canJoinChat(role)) {
                throw new UnauthorizedException("Only admin and manager can join existing chats");
            }
            String chatId = parts[1];
            String joinChatUsername = currentSession.getUsername();
            controller.joinChatAsManager(chatId, joinChatUsername);
            return "OK;User joined chat";
        }

        case "GET_WAITING_REQUESTS": {
            String branchId = currentSession.getBranchId();
            String otherBranchId = findOtherBranchId(branchId);
            if (otherBranchId == null) {
                return "OK;";
            }
            
            List<model.ChatRequest> waitingRequests = controller.getWaitingRequestsForBranch(otherBranchId);
            StringBuilder result = new StringBuilder("OK;");
            for (int i = 0; i < waitingRequests.size(); i++) {
                model.ChatRequest req = waitingRequests.get(i);
                result.append(req.getRequestId()).append(":").append(req.getRequesterUsername());
                if (i < waitingRequests.size() - 1) result.append("|");
            }
            return result.toString();
        }

        case "ACCEPT_CHAT_REQUEST": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("ACCEPT_CHAT_REQUEST requires: requestId");
            }
            String requestId = parts[1];
            String acceptingUsername = currentSession.getUsername();
            String result = controller.acceptChatRequest(acceptingUsername, requestId);
            return result;
        }

        case "GET_CHAT_HISTORY": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_CHAT_HISTORY requires: chatId");
            }
            String chatId = parts[1];
            List<model.ChatMessage> messages = controller.getChatHistory(chatId);
            StringBuilder json = new StringBuilder("OK;{\"chatId\":\"").append(escapeJson(chatId))
                .append("\",\"messages\":[");
            for (int i = 0; i < messages.size(); i++) {
                model.ChatMessage msg = messages.get(i);
                json.append(msg.toJson());
                if (i < messages.size() - 1) json.append(",");
            }
            json.append("]}");
            return json.toString();
        }

        case "CANCEL_CHAT_REQUEST": {
            String cancelRequestUsername = currentSession.getUsername();
            boolean cancelled = controller.cancelChatRequest(cancelRequestUsername);
            if (cancelled) {
                return "OK;Chat request cancelled";
            } else {
                return "ERROR;No pending chat request found";
            }
        }

        case "GET_USER_CHAT": {
            String getUserChatUsername = currentSession.getUsername();
            model.ChatSession chat = controller.getUserChat(getUserChatUsername);
            if (chat == null) {
                return "OK;NO_CHAT";
            }
            StringBuilder result = new StringBuilder("OK;CHAT;");
            result.append(chat.getChatId()).append(";");
            result.append(chat.getStartTime()).append(";");
            result.append(chat.getParticipantCount()).append(";");
            for (String participant : chat.getParticipants()) {
                result.append(participant).append(",");
            }
            return result.toString();
        }

        case "GET_USER_CHAT_STATUS": {
            String getUserChatStatusUsername = currentSession.getUsername();
            model.ChatUserStatus status = controller.getUserChatStatus(getUserChatStatusUsername);
            return "OK;" + status.name();
        }

        case "GET_LOGS": {
            List<model.LogEntry> logs = controller.getAllLogs();
            StringBuilder json = new StringBuilder("OK;[");
            for (int i = 0; i < logs.size(); i++) {
                model.LogEntry log = logs.get(i);
                String chatIdJson = log.getChatId() != null ? 
                    String.format(",\"chatId\":\"%s\"", escapeJson(log.getChatId())) : "";
                json.append(String.format(
                    "{\"actionType\":\"%s\",\"description\":\"%s\",\"dateTime\":\"%s\"%s}",
                    escapeJson(log.getActionType()), escapeJson(log.getDescription()), 
                    escapeJson(log.getDateTime()), chatIdJson
                ));
                if (i < logs.size() - 1) json.append(",");
            }
            json.append("]");
            return json.toString();
        }

        case "GET_CHAT_DETAILS": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("GET_CHAT_DETAILS requires: chatId");
            }
            String chatId = parts[1];
            String result = controller.getChatDetails(chatId);
            return "OK;" + result;
        }

        case "SAVE_CHAT_TO_RTF": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("SAVE_CHAT_TO_RTF requires: chatId");
            }
            String chatId = parts[1];
            try {
                String fileName = controller.saveChatToRTF(chatId);
                return "OK;Saved;" + fileName;
            } catch (IOException e) {
                return "ERROR;" + e.getMessage();
            }
        }

        case "BROADCAST": {
            if (parts.length < 2) {
                throw new IllegalArgumentException("BROADCAST requires: message");
            }
            String message = parts[1];
            broadcastMessage(message, socket);
            return "OK;Message broadcasted to all clients";
        }

        default:
            throw new IllegalArgumentException("Unknown command: " + parts[0]);
    }
    }
    
    /**
     * Broadcasts a message to all connected clients except the sender.
     * Iterates through the Vector<Socket> and sends the message to each client.
     * Removes failed sockets from the Vector if IOException occurs.
     * 
     * @param message the message to broadcast
     * @param senderSocket the socket of the client sending the message (excluded from broadcast)
     */
    private void broadcastMessage(String message, Socket senderSocket) {
        synchronized (connectedClients) {
            // Use iterator to safely remove elements during iteration
            for (int i = connectedClients.size() - 1; i >= 0; i--) {
                Socket clientSocket = connectedClients.get(i);
                
                // Skip sender and closed sockets
                if (clientSocket == senderSocket || clientSocket.isClosed()) {
                    continue;
                }
                
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("BROADCAST;" + message);
                } catch (IOException e) {
                    // Remove failed socket from Vector
                    connectedClients.remove(i);
                    System.err.println("Removed failed socket from broadcast: " + e.getMessage());
                }
            }
        }
    }
    
    private String findOtherBranchId(String currentBranchId) {
        Map<String, model.Session> allSessions = controller.getSessionManager().getAllActiveSessions();
        for (model.Session session : allSessions.values()) {
            if (!session.getBranchId().equals(currentBranchId)) {
                return session.getBranchId();
            }
        }
        return null;
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
