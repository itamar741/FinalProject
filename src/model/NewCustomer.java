package model;

/**
 * Represents a new customer (first-time buyer).
 * New customers pay the full price without any discount.
 * 
 * @author FinalProject
 */
public class NewCustomer extends Customer {

    /**
     * Constructs a new NewCustomer.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number
     * @param phone the customer's phone number
     */
    public NewCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    /**
     * Gets the customer type string.
     * 
     * @return "NEW"
     */
    @Override
    public String getCustomerType() {
        return "NEW";
    }
    
    /**
     * Calculates the price for a new customer.
     * Uses DiscountManager to get the current discount percentage for NEW customers.
     * 
     * @param basePrice the base price before discount
     * @return the price after applying discount (if any)
     */
    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice; // Default: no discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("NEW");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
