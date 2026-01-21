package model;

/**
 * Represents a VIP customer (premium member).
 * VIP customers receive a 15% discount on their purchases.
 * 
 * @author FinalProject
 */
public class VipCustomer extends Customer {

    /**
     * Constructs a new VipCustomer.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number
     * @param phone the customer's phone number
     */
    public VipCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    /**
     * Gets the customer type string.
     * 
     * @return "VIP"
     */
    @Override
    public String getCustomerType() {
        return "VIP";
    }
    
    /**
     * Calculates the price for a VIP customer.
     * Uses DiscountManager to get the current discount percentage for VIP customers.
     * 
     * @param basePrice the base price before discount
     * @return the price after applying discount
     */
    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice * 0.85; // Default: 15% discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("VIP");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
