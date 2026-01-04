package model;

import java.util.Map;
import java.util.HashMap;
import model.exceptions.DuplicateCustomerException;

public class CustomerManager {

    private Map<String, Customer> customers;

    public CustomerManager() {
        this.customers = new HashMap<>();
    }

    public void addCustomer(Customer customer)
            throws DuplicateCustomerException {

        String id = customer.getIdNumber();

        if (customers.containsKey(id)) {
            throw new DuplicateCustomerException(
                    "Customer with ID " + id + " already exists"
            );
        }

        customers.put(id, customer);
    }

    public Customer getCustomerById(String idNumber) {
        return customers.get(idNumber);
    }

    public Map<String, Customer> getCustomers() {
        return customers;
    }
}
