package model;

/**
 * Represents a product in the system.
 * Products are identified by their productId, which is used for equality comparison.
 * Products can be active or inactive (inactive products cannot be sold).
 * 
 * @author FinalProject
 */
public class Product {
    private String productId;
    private String name;
    private String category;
    private double price;
    private boolean active;

    /**
     * Constructs a new Product with the specified details.
     * New products are created as active by default.
     * 
     * @param productId the unique product identifier
     * @param name the product name
     * @param category the product category
     * @param price the product price
     */
    public Product(String productId, String name, String category, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.active = true;
    }
    
    /**
     * Gets the product's unique identifier.
     * 
     * @return the product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Gets the product name.
     * 
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the product price.
     * 
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the product category.
     * 
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Checks if the product is active.
     * Only active products can be sold.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the product price.
     * 
     * @param price the new price
     */
    public void setPrice(double price) {
        this.price = price;
    }
    
    /**
     * Sets the product category.
     * 
     * @param category the new category
     */
    public void setCategory(String category) {
        this.category = category;
    }   
    
    /**
     * Sets the product ID.
     * 
     * @param productId the new product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    /**
     * Sets the product name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the product active status.
     * 
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Compares this product with another object for equality.
     * Two products are equal if they have the same productId.
     * 
     * @param o the object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;
        return productId.equals(product.productId);
    }

    /**
     * Returns the hash code for this product.
     * Hash code is based on productId.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}
