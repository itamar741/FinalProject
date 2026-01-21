package model;

/**
 * Represents a sale transaction in the system.
 * Stores complete sale information including product, quantity, pricing (before and after discount),
 * branch, employee, customer, and timestamp.
 * 
 * @author FinalProject
 */
public class Sale {

    private Product product;
    private int quantity;
    private String branchId;
    private String employeeNumber;
    private String customerId;
    private String dateTime;
    private double basePrice;  // Price before discount (product price * quantity)
    private double finalPrice; // Final price after customer discount

    /**
     * Constructs a new Sale with the specified details.
     * 
     * @param product the product being sold
     * @param quantity the quantity sold
     * @param branchId the branch ID where the sale occurred
     * @param employeeNumber the employee number who made the sale
     * @param customerId the customer ID who made the purchase
     * @param dateTime the date and time of the sale (ISO format string)
     * @param basePrice the price before discount (product price * quantity)
     * @param finalPrice the final price after applying customer discount
     */
    public Sale(Product product,
                int quantity,
                String branchId,
                String employeeNumber,
                String customerId,
                String dateTime,
                double basePrice,
                double finalPrice) {

        this.product = product;
        this.quantity = quantity;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        this.customerId = customerId;
        this.dateTime = dateTime;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
    }

    /**
     * Gets the product that was sold.
     * 
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the quantity sold.
     * 
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the branch ID where the sale occurred.
     * 
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * Gets the employee number who made the sale.
     * 
     * @return the employee number
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    
    /**
     * Gets the customer ID who made the purchase.
     * 
     * @return the customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Gets the date and time of the sale.
     * 
     * @return the date/time string (ISO format)
     */
    public String getDateTime() {
        return dateTime;
    }
    
    /**
     * Gets the base price before discount.
     * This is calculated as product price * quantity.
     * 
     * @return the base price
     */
    public double getBasePrice() {
        return basePrice;
    }
    
    /**
     * Gets the final price after applying customer discount.
     * 
     * @return the final price
     */
    public double getFinalPrice() {
        return finalPrice;
    }
}
