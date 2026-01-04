package model;

import java.util.Map;
import java.util.HashMap;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;
import model.exceptions.InactiveProductException;

public class Inventory {

    private String branchId;
    private Map<Product, Integer> products;

    public Inventory(String branchId) {
        this.branchId = branchId;
        this.products = new HashMap<>();
    }

    public String getBranchId() {
        return branchId;
    }

    public Map<Product, Integer> getProducts() {
        return products;
    }

    public void addProduct(Product product, int quantity)
            throws InvalidQuantityException {

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        Integer currentQuantity = products.get(product);

        if (currentQuantity == null) {
            products.put(product, quantity);
        } else {
            products.put(product, currentQuantity + quantity);
        }
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

        if (quantity == null) {
            return 0;
        }

        return quantity;
    }
}
