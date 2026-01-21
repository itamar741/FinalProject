package model;

import model.managers.DiscountManager;

/**
 * Abstract base class representing a customer in the system.
 * Implements the Template Method Pattern - defines the structure with abstract calculatePrice() method.
 * Each customer type (NewCustomer, ReturningCustomer, VipCustomer) implements its own price calculation logic.
 * 
 * @author FinalProject
 */
public abstract class Customer {

    protected String fullName;
    protected String idNumber;
    protected String phone;
    protected static DiscountManager discountManager;  // Shared discount manager for all customers

    /**
     * Constructs a new Customer with the specified details.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number (unique identifier)
     * @param phone the customer's phone number
     */
    public Customer(String fullName, String idNumber, String phone) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
    }

    /**
     * Gets the customer's full name.
     * 
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the customer's ID number.
     * 
     * @return the ID number (unique identifier)
     */
    public String getIdNumber() {
        return idNumber;
    }

    /**
     * Gets the customer's phone number.
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * Sets the customer's full name.
     * 
     * @param fullName the new full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    /**
     * Sets the customer's phone number.
     * 
     * @param phone the new phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the DiscountManager to be used by all Customer instances.
     * This allows customers to access current discount percentages.
     * 
     * @param manager the DiscountManager instance
     */
    public static void setDiscountManager(DiscountManager manager) {
        discountManager = manager;
    }
    
    /**
     * Gets the customer type string for this customer.
     * Used to retrieve the appropriate discount percentage.
     * 
     * @return "NEW", "RETURNING", or "VIP"
     */
    public abstract String getCustomerType();
    
    /**
     * Calculates the final price after applying customer-specific discount.
     * This is the template method - each customer type implements its own calculation.
     * Uses DiscountManager to get the current discount percentage.
     * 
     * @param basePrice the base price before discount (product price * quantity)
     * @return the final price after discount
     */
    public abstract double calculatePrice(double basePrice);
}
