package model.managers;

import java.util.HashMap;
import java.util.Map;

import model.Customer;
import model.NewCustomer;
import model.ReturningCustomer;
import model.VipCustomer;

import model.exceptions.DuplicateCustomerException;

/**
 * Manages customer data and operations.
 * Implements Factory Pattern - creates different customer types (NewCustomer, ReturningCustomer, VipCustomer)
 * based on the customerType parameter.
 * 
 * @author FinalProject
 */
public class CustomerManager {

    private Map<String, Customer> customers;

    /**
     * Constructs a new CustomerManager with an empty customer map.
     */
    public CustomerManager() {
        customers = new HashMap<>();
    }

    /**
     * Adds a new customer to the system.
     * Uses Factory Pattern to create the appropriate customer type.
     * 
     * @param fullName the customer's full name
     * @param idNumber the customer's ID number (unique identifier)
     * @param phone the customer's phone number
     * @param customerType the customer type ("NEW", "RETURNING", or "VIP")
     * @throws DuplicateCustomerException if a customer with the same ID number already exists
     * @throws IllegalArgumentException if customerType is unknown
     */
    public void addCustomer(String fullName,
                            String idNumber,
                            String phone,
                            String customerType)
            throws DuplicateCustomerException {

        if (customers.containsKey(idNumber)) {
            throw new DuplicateCustomerException(
                    "Customer with ID " + idNumber + " already exists"
            );
        }

        Customer customer;

        switch (customerType) {
            case "NEW":
                customer = new NewCustomer(fullName, idNumber, phone);
                break;

            case "RETURNING":
                customer = new ReturningCustomer(fullName, idNumber, phone);
                break;

            case "VIP":
                customer = new VipCustomer(fullName, idNumber, phone);
                break;

            default:
                throw new IllegalArgumentException("Unknown customer type");
        }

        customers.put(idNumber, customer);
    }

    /**
     * Gets a customer by ID number.
     * 
     * @param idNumber the customer's ID number
     * @return the Customer object, or null if not found
     */
    public Customer getCustomerById(String idNumber) {
        return customers.get(idNumber);
    }
    
    /**
     * Updates customer details.
     * If customer type changes, creates a new customer object of the new type.
     * 
     * @param idNumber the customer's ID number
     * @param fullName the new full name (null or empty to keep current)
     * @param phone the new phone number (null or empty to keep current)
     * @param customerType the new customer type (null or empty to keep current)
     * @throws IllegalArgumentException if customer not found or invalid customer type
     */
    public void updateCustomer(String idNumber,
                               String fullName,
                               String phone,
                               String customerType) {
        Customer customer = customers.get(idNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + idNumber + " not found");
        }
        
        // Get current values
        String newFullName = (fullName != null && !fullName.trim().isEmpty()) ? fullName : customer.getFullName();
        String newPhone = (phone != null && !phone.trim().isEmpty()) ? phone : customer.getPhone();
        String newType = (customerType != null && !customerType.trim().isEmpty()) ? customerType.toUpperCase() : getCustomerType(customer);
        
        // If customer type changed, create new customer object of new type
        String currentType = getCustomerType(customer);
        if (!currentType.equals(newType)) {
            // Create new customer of different type
            Customer newCustomer;
            switch (newType) {
                case "NEW":
                    newCustomer = new NewCustomer(newFullName, idNumber, newPhone);
                    break;
                case "RETURNING":
                    newCustomer = new ReturningCustomer(newFullName, idNumber, newPhone);
                    break;
                case "VIP":
                    newCustomer = new VipCustomer(newFullName, idNumber, newPhone);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid customer type: " + newType);
            }
            customers.put(idNumber, newCustomer);
        } else {
            // Only update name and phone
            customer.setFullName(newFullName);
            customer.setPhone(newPhone);
        }
    }
    
    /**
     * Gets the customer type string for a Customer object.
     * Used for type checking and conversion.
     * 
     * @param customer the customer to check
     * @return "NEW", "RETURNING", or "VIP"
     */
    private String getCustomerType(Customer customer) {
        if (customer instanceof VipCustomer) {
            return "VIP";
        } else if (customer instanceof ReturningCustomer) {
            return "RETURNING";
        } else {
            return "NEW";
        }
    }
    
    /**
     * Deletes a customer from the system.
     * 
     * @param idNumber the customer's ID number
     * @throws IllegalArgumentException if customer not found
     */
    public void deleteCustomer(String idNumber) {
        Customer customer = customers.remove(idNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + idNumber + " not found");
        }
    }
    
    /**
     * Gets all customers (for saving to storage).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of idNumber to Customer
     */
    public Map<String, Customer> getAllCustomers() {
        return new HashMap<>(customers);
    }
}
