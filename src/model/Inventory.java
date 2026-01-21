package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;

/**
 * Manages the inventory for a single branch.
 * Tracks product quantities using a Map structure.
 * Provides methods for adding products, selling products, and removing products from inventory.
 * 
 * @author FinalProject
 */
public class Inventory {

    private Map<Product, Integer> products;

    /**
     * Constructs a new empty Inventory.
     */
    public Inventory() {
        this.products = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Adds a product to the inventory with the specified quantity.
     * If the product already exists, the quantity is added to the existing quantity.
     * 
     * @param product the product to add
     * @param quantity the quantity to add (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     */
    public void addProduct(Product product, int quantity)
            throws InvalidQuantityException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        // Synchronize for atomic read-modify-write operation
        synchronized (products) {
            Integer currentQuantity = products.get(product);
            products.put(product,
                    currentQuantity == null ? quantity : currentQuantity + quantity);
        }
    }

    /**
     * Sells a product from the inventory (reduces quantity).
     * Validates that sufficient stock is available.
     * 
     * @param product the product to sell
     * @param quantity the quantity to sell (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock available
     */
    public void sellProduct(Product product, int quantity)
            throws InvalidQuantityException,
            InsufficientStockException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        // Synchronize for atomic check-and-update operation
        synchronized (products) {
            Integer currentQuantity = products.get(product);

            if (currentQuantity == null || currentQuantity < quantity) {
                throw new InsufficientStockException("Not enough stock for product");
            }

            int newQuantity = currentQuantity - quantity;
            if (newQuantity == 0) {
                products.remove(product);
            } else {
                products.put(product, newQuantity);
            }
        }
    }

    /**
     * Gets the current quantity of a product in inventory.
     * 
     * @param product the product to check
     * @return the quantity available, or 0 if the product is not in inventory
     */
    public int getProductQuantity(Product product) {
        synchronized (products) {
            Integer quantity = products.get(product);
            return quantity == null ? 0 : quantity;
        }
    }
    
    /**
     * Removes a specified quantity of a product from inventory.
     * If the resulting quantity is 0, the product is removed from the inventory map.
     * 
     * @param product the product to remove
     * @param quantity the quantity to remove (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock to remove
     */
    public void removeProduct(Product product, int quantity)
            throws InvalidQuantityException, InsufficientStockException {
        
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        
        // Synchronize for atomic check-and-update operation
        synchronized (products) {
            Integer currentQuantity = products.get(product);
            
            if (currentQuantity == null || currentQuantity < quantity) {
                throw new InsufficientStockException("Not enough stock to remove");
            }
            
            int newQuantity = currentQuantity - quantity;
            if (newQuantity == 0) {
                products.remove(product);
            } else {
                products.put(product, newQuantity);
            }
        }
    }
    
    /**
     * Gets all products and their quantities in the inventory.
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of Product to quantity
     */
    public Map<Product, Integer> getAllProducts() {
        synchronized (products) {
            return new HashMap<>(products);
        }
    }
    
    /**
     * Sets the entire inventory from a provided map.
     * Used for loading inventory from storage.
     * 
     * @param products the map of products and quantities to set
     */
    public void setProducts(Map<Product, Integer> products) {
        synchronized (this.products) {
            this.products = Collections.synchronizedMap(new HashMap<>(products));
        }
    }
    
    /**
     * Loads a product into inventory with a specific quantity.
     * Used during data loading - does not validate quantity (allows 0 or negative for data integrity).
     * 
     * @param product the product to load
     * @param quantity the quantity to set
     */
    public void loadProduct(Product product, int quantity) {
        if (product != null && quantity >= 0) {
            synchronized (products) {
                products.put(product, quantity);
            }
        }
    }
}
