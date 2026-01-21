package model;

/**
 * Represents a returning customer (repeat buyer).
 * Returning customers receive a 5% discount on their purchases.
 * 
 * @author FinalProject
 */
public class ReturningCustomer extends Customer {

    /**
     * Constructs a new ReturningCustomer.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number
     * @param phone the customer's phone number
     */
    public ReturningCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    /**
     * Gets the customer type string.
     * 
     * @return "RETURNING"
     */
    @Override
    public String getCustomerType() {
        return "RETURNING";
    }
    
    /**
     * Calculates the price for a returning customer.
     * Uses DiscountManager to get the current discount percentage for RETURNING customers.
     * 
     * @param basePrice the base price before discount
     * @return the price after applying discount
     */
    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice * 0.95; // Default: 5% discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("RETURNING");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
