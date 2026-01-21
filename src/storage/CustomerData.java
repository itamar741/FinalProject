package storage;

import model.Customer;
import model.NewCustomer;
import model.ReturningCustomer;
import model.VipCustomer;

/**
 * DTO class for storing Customer in JSON.
 * Implements DTO Pattern - separates Model from storage format.
 * Stores customer type as string to enable reconstruction of correct Customer subclass.
 * 
 * @author FinalProject
 */
public class CustomerData {
    public String fullName;
    public String idNumber;
    public String phone;
    public String customerType;  // NEW, RETURNING, VIP
    
    /**
     * Default constructor for JSON deserialization.
     */
    public CustomerData() {}
    
    /**
     * Constructs CustomerData from a Customer object.
     * Determines customer type by checking instance type.
     * 
     * @param customer the Customer object to convert
     */
    public CustomerData(Customer customer) {
        this.fullName = customer.getFullName();
        this.idNumber = customer.getIdNumber();
        this.phone = customer.getPhone();
        
        // קביעת סוג לקוח
        if (customer instanceof NewCustomer) {
            this.customerType = "NEW";
        } else if (customer instanceof ReturningCustomer) {
            this.customerType = "RETURNING";
        } else if (customer instanceof VipCustomer) {
            this.customerType = "VIP";
        }
    }
    
    /**
     * Converts this DTO to a Customer object.
     * Uses Factory Pattern to create the appropriate Customer subclass.
     * 
     * @return a Customer object (NewCustomer, ReturningCustomer, or VipCustomer)
     * @throws IllegalArgumentException if customerType is unknown
     */
    public Customer toCustomer() {
        switch (customerType) {
            case "NEW":
                return new NewCustomer(fullName, idNumber, phone);
            case "RETURNING":
                return new ReturningCustomer(fullName, idNumber, phone);
            case "VIP":
                return new VipCustomer(fullName, idNumber, phone);
            default:
                throw new IllegalArgumentException("Unknown customer type: " + customerType);
        }
    }
}
