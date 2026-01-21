package model.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages customer discount percentages for different customer types.
 * Stores discount rates for NEW, RETURNING, and VIP customers.
 * Discounts are stored as percentages (e.g., 5.0 means 5% discount).
 * 
 * @author FinalProject
 */
public class DiscountManager {
    
    private Map<String, Double> discounts;  // customerType -> discount percentage
    
    /**
     * Constructs a new DiscountManager with default discount values.
     * Default values: NEW=0%, RETURNING=5%, VIP=15%
     */
    public DiscountManager() {
        discounts = Collections.synchronizedMap(new HashMap<>());
        // Set default values
        discounts.put("NEW", 0.0);
        discounts.put("RETURNING", 5.0);
        discounts.put("VIP", 15.0);
    }
    
    /**
     * Gets the discount percentage for a customer type.
     * 
     * @param customerType the customer type ("NEW", "RETURNING", or "VIP")
     * @return the discount percentage (0.0 to 100.0)
     */
    public double getDiscount(String customerType) {
        synchronized (discounts) {
            Double discount = discounts.get(customerType);
            if (discount == null) {
                return 0.0; // Default to no discount if type not found
            }
            return discount;
        }
    }
    
    /**
     * Sets the discount percentage for a customer type.
     * 
     * @param customerType the customer type ("NEW", "RETURNING", or "VIP")
     * @param discountPercentage the discount percentage (0.0 to 100.0)
     * @throws IllegalArgumentException if customerType is invalid or discount is out of range
     */
    public void setDiscount(String customerType, double discountPercentage) {
        if (!customerType.equals("NEW") && !customerType.equals("RETURNING") && !customerType.equals("VIP")) {
            throw new IllegalArgumentException("Invalid customer type: " + customerType);
        }
        if (discountPercentage < 0.0 || discountPercentage > 100.0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        synchronized (discounts) {
            discounts.put(customerType, discountPercentage);
        }
    }
    
    /**
     * Gets all discount percentages (for saving to storage).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of customerType to discount percentage
     */
    public Map<String, Double> getAllDiscounts() {
        synchronized (discounts) {
            return new HashMap<>(discounts);
        }
    }
    
    /**
     * Sets all discount percentages (for loading from storage).
     * 
     * @param discountsMap a Map of customerType to discount percentage
     */
    public void setAllDiscounts(Map<String, Double> discountsMap) {
        if (discountsMap != null) {
            synchronized (discounts) {
                discounts = Collections.synchronizedMap(new HashMap<>(discountsMap));
            }
        }
    }
}
