package model.managers;

import java.util.HashMap;
import java.util.Map;

import model.Customer;
import model.NewCustomer;
import model.ReturningCustomer;
import model.VipCustomer;

import model.exceptions.DuplicateCustomerException;

public class CustomerManager {

    private Map<String, Customer> customers;

    public CustomerManager() {
        customers = new HashMap<>();
    }

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

    public Customer getCustomerById(String idNumber) {
        return customers.get(idNumber);
    }
    
    /**
     * קבלת כל הלקוחות (לשמירה)
     */
    public Map<String, Customer> getAllCustomers() {
        return new HashMap<>(customers);
    }
}
