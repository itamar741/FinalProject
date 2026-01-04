package model;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;
import model.exceptions.InactiveProductException;
import model.exceptions.DuplicateCustomerException;


public class Main {

    public static void main(String[] args) {

        // ===== יצירת סניפים =====
        Branch branch1 = new Branch("B1");
        Branch branch2 = new Branch("B2");

        // ===== יצירת עובדים =====
        Employee emp1 = new Employee(
                "Dana Cohen",
                "123456789",
                "050-1111111",
                "BANK-001",
                "E1",
                "Seller",
                "B1"
        );

        Employee emp2 = new Employee(
                "Amit Levi",
                "987654321",
                "050-2222222",
                "BANK-002",
                "E2",
                "Seller",
                "B2"
        );

        // ===== יצירת מוצרים =====
        Product p1 = new Product("P1", "Milk", "Food");
        Product p2 = new Product("P2", "Bread", "Food");

        // ===== יצירת מלאי =====
        Inventory inventoryB1 = new Inventory("B1");

        // ===== Managers =====
        SalesManager salesManager = new SalesManager();
        CustomerManager customerManager = new CustomerManager();
        LogManager logManager = new LogManager();

        // ===== לקוח =====
        Customer customer = new Customer(
                "Noam Bar",
                "123456789",
                "050-3333333",
                "NEW"
        );
        addCustomerSafe(customerManager, customer);

        // ===== try / catch לפי המצגת =====
        try {
            inventoryB1.addProduct(p1, 10);
            inventoryB1.addProduct(p2, 20);

            inventoryB1.sellProduct(p1, 2);

            Sale sale = new Sale(
                    p1,
                    2,
                    "B1",
                    emp1.getEmployeeNumber(),
                    "2024-01-01 10:00"
            );

            salesManager.addSale(sale);

            logManager.addLog(new LogEntry(
                    "SALE",
                    "Sold 2 units of Milk in branch B1",
                    "2024-01-01 10:00"
            ));

        } catch (InvalidQuantityException e) {
            System.out.println("Invalid quantity: " + e.getMessage());

        } catch (InactiveProductException e) {
            System.out.println("Inactive product: " + e.getMessage());

        } catch (InsufficientStockException e) {
            System.out.println("Stock error: " + e.getMessage());
        }
        p1.setActive(false);
        performSale(inventoryB1, p1, 1);

        // ===== בדיקות =====
        System.out.println("Remaining quantity of Milk in B1: "
                + inventoryB1.getProductQuantity(p1));

        System.out.println("Total sales: " + salesManager.getSales().size());
        System.out.println("Total customers: " + customerManager.getCustomers().size());
        System.out.println("Total logs: " + logManager.getLogs().size());
    }


    private static void performSale(Inventory inventory, Product product, int quantity) {
        try {
            inventory.sellProduct(product, quantity);
            System.out.println("Sale successful");

        } catch (InvalidQuantityException |
                 InactiveProductException |
                 InsufficientStockException e) {

            System.out.println("Sale failed: " + e.getMessage());
        }
    }
    private static void addCustomerSafe(CustomerManager manager, Customer customer) {
        try {
            manager.addCustomer(customer);
            System.out.println("Customer added successfully");
        } catch (DuplicateCustomerException e) {
            System.out.println("Customer error: " + e.getMessage());
        }
    }

}
