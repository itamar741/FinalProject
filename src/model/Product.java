package model;

public class Product {
    private String productId;
    private String name;
    private String category;
    private double price;
    private boolean active;

    public Product(String productId, String name, String category, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.active = true;
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
    
    public boolean isActive() {
        return active;
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
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;
        return productId.equals(product.productId);
    }

    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}
