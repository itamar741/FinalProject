package storage;

import model.Sale;
import model.Product;

/**
 * DTO class for storing Sale in JSON.
 * Implements DTO Pattern - separates Model from storage format.
 * Stores product details inline (denormalized) for easier querying and reporting.
 * 
 * @author FinalProject
 */
public class SaleData {
    public String productId;
    public String productName;
    public String productCategory;
    public double productPrice;
    public int quantity;
    public String branchId;
    public String employeeNumber;
    public String customerId;
    public String dateTime;
    public double basePrice;   // Price before discount
    public double finalPrice; // Final price after customer discount
    
    /**
     * Default constructor for JSON deserialization.
     */
    public SaleData() {}
    
    /**
     * Constructs SaleData from a Sale object.
     * Extracts product details from the Sale's Product object.
     * 
     * @param sale the Sale object to convert
     */
    public SaleData(Sale sale) {
        Product product = sale.getProduct();
        this.productId = product.getProductId();
        this.productName = product.getName();
        this.productCategory = product.getCategory();
        this.productPrice = product.getPrice();
        this.quantity = sale.getQuantity();
        this.branchId = sale.getBranchId();
        this.employeeNumber = sale.getEmployeeNumber();
        this.customerId = sale.getCustomerId();
        this.dateTime = sale.getDateTime();
        this.basePrice = sale.getBasePrice();
        this.finalPrice = sale.getFinalPrice();
    }
    
    /**
     * Converts this DTO to a Sale object.
     * Reconstructs the Product object and handles backward compatibility for old JSON data.
     * 
     * @return a Sale object with all fields set
     */
    public Sale toSale() {
        Product product = new Product(productId, productName, productCategory, productPrice);
        // If basePrice and finalPrice don't exist (for loading from old JSON), calculate them
        double calculatedBasePrice = basePrice > 0 ? basePrice : productPrice * quantity;
        double calculatedFinalPrice = finalPrice > 0 ? finalPrice : calculatedBasePrice;
        // If customerId doesn't exist (for old sales), use empty string
        String saleCustomerId = (customerId != null && !customerId.isEmpty()) ? customerId : "";
        return new Sale(product, quantity, branchId, employeeNumber, saleCustomerId, dateTime, calculatedBasePrice, calculatedFinalPrice);
    }
}
