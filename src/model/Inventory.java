package model;

import java.util.HashMap;
import java.util.Map;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;
import model.exceptions.InactiveProductException;

public class Inventory {

    private Map<Product, Integer> products;

    public Inventory() {
        this.products = new HashMap<>();
    }

    public void addProduct(Product product, int quantity)
            throws InvalidQuantityException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        Integer currentQuantity = products.get(product);
        products.put(product,
                currentQuantity == null ? quantity : currentQuantity + quantity);
    }

    public void sellProduct(Product product, int quantity)
            throws InvalidQuantityException,
            InsufficientStockException,
            InactiveProductException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        if (!product.isActive()) {
            throw new InactiveProductException("Product is inactive");
        }

        Integer currentQuantity = products.get(product);

        if (currentQuantity == null || currentQuantity < quantity) {
            throw new InsufficientStockException("Not enough stock for product");
        }

        products.put(product, currentQuantity - quantity);
    }

    public int getProductQuantity(Product product) {
        Integer quantity = products.get(product);
        return quantity == null ? 0 : quantity;
    }
    
    /**
     * הסרת כמות מהמלאי
     */
    public void removeProduct(Product product, int quantity)
            throws InvalidQuantityException, InsufficientStockException {
        
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        
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
    
    /**
     * קבלת כל המוצרים במלאי (לשמירה)
     */
    public Map<Product, Integer> getAllProducts() {
        return new HashMap<>(products);
    }
    
    /**
     * טעינת מוצרים למלאי (מטעינה)
     */
    public void setProducts(Map<Product, Integer> products) {
        this.products = new HashMap<>(products);
    }
    
    /**
     * הוספת מוצר למלאי עם כמות ספציפית (לטעינה - לא בודק תקינות)
     */
    public void loadProduct(Product product, int quantity) {
        if (product != null && quantity >= 0) {
            products.put(product, quantity);
        }
    }
}
