package storage;

import model.Product;

/**
 * DTO class for storing Product in JSON.
 * Implements DTO Pattern - separates Model from storage format.
 * 
 * @author FinalProject
 */
public class ProductData {
    public String productId;
    public String name;
    public String category;
    public double price;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public ProductData() {}
    
    /**
     * Constructs ProductData from a Product object.
     * 
     * @param product the Product object to convert
     */
    public ProductData(Product product) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.price = product.getPrice();
    }
    
    /**
     * Converts this DTO to a Product object.
     * 
     * @return a Product object with all fields set
     */
    public Product toProduct() {
        return new Product(productId, name, category, price);
    }
}
