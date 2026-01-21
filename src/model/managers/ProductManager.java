package model.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import model.Product;

/**
 * Manages product data and operations.
 * Products are identified by their productId.
 * 
 * @author FinalProject
 */
public class ProductManager {

    private Map<String, Product> products;

    /**
     * Constructs a new ProductManager with an empty product map.
     */
    public ProductManager() {
        products = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Gets a product by ID, creating it if it doesn't exist.
     * If the product exists, updates its price if different (tolerance: 0.01).
     * 
     * @param productId the product ID
     * @param name the product name
     * @param category the product category
     * @param price the product price
     * @return the Product object (existing or newly created)
     */
    public Product getProduct(String productId, String name, String category, double price) {
        // Synchronize for atomic check-and-put operation
        synchronized (products) {
            Product product = products.get(productId);

            if (product == null) {
                product = new Product(productId, name, category, price);
                products.put(productId, product);
            } else {
                // Update price if different (tolerance: 0.01)
                if (Math.abs(product.getPrice() - price) > 0.01) {
                    product.setPrice(price);
                }
            }

            return product;
        }
    }

    /**
     * Gets an existing product by ID (does not create if not found).
     * 
     * @param productId the product ID
     * @return the Product object, or null if not found
     */
    public Product getExistingProduct(String productId) {
        synchronized (products) {
            return products.get(productId);
        }
    }
    
    /**
     * Gets all products (for saving to storage).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of productId to Product
     */
    public Map<String, Product> getAllProducts() {
        synchronized (products) {
            return new HashMap<>(products);
        }
    }
    
    /**
     * Adds a product directly (for loading from storage).
     * Used during data loading - does not validate or update existing products.
     * 
     * @param product the product to add
     */
    public void addProductDirectly(Product product) {
        if (product != null) {
            synchronized (products) {
                products.put(product.getProductId(), product);
            }
        }
    }
    
    /**
     * Deletes a product from the system.
     * 
     * @param productId the product ID to delete
     */
    public void deleteProduct(String productId) {
        synchronized (products) {
            products.remove(productId);
        }
    }

}
