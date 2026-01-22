package model;

/**
 * Represents a product in the system.
 * Products are identified by their productId, which is used for equality comparison.

 */
public class Product {
    private String productId;
    private String name;
    private String category;
    private double price;

    public Product(String productId, String name, String category, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
    }
    
   
    public String getProductId() {
        return productId;
    }

   
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

   
    public String getCategory() {
        return category;
    }
   
    public void setPrice(double price) {
        this.price = price;
    }
    
   
    public void setCategory(String category) {
        this.category = category;
    }   
    
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Compares this product with another object for equality.
     * Two products are equal if they have the same productId.
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
     */
    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}
