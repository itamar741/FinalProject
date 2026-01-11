package storage;

import model.Product;

/**
 * מחלקת DTO לשמירת Product ב-JSON
 */
public class ProductData {
    public String productId;
    public String name;
    public String category;
    public double price;
    public boolean active;
    
    // Default constructor for JSON deserialization
    public ProductData() {}
    
    public ProductData(Product product) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.active = product.isActive();
    }
    
    public Product toProduct() {
        Product product = new Product(productId, name, category, price);
        product.setActive(active);
        return product;
    }
}
