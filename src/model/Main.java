package model;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;
import model.exceptions.InactiveProductException;
import model.exceptions.DuplicateCustomerException;
import model.managers.CustomerManager;
import model.managers.LogManager;
import model.managers.SalesManager;


public class Main {

    public static void main(String[] args) {


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
    /*private static void addCustomerSafe(CustomerManager manager, Customer customer) {
        try {
            manager.addCustomer(customer);
            System.out.println("Customer added successfully");
        } catch (DuplicateCustomerException e) {
            System.out.println("Customer error: " + e.getMessage());
        }
    }*/

}
