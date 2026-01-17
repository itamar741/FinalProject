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
     * עדכון פרטי לקוח
     */
    public void updateCustomer(String idNumber,
                               String fullName,
                               String phone,
                               String customerType) {
        Customer customer = customers.get(idNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + idNumber + " not found");
        }
        
        // קבלת הערכים הנוכחיים
        String newFullName = (fullName != null && !fullName.trim().isEmpty()) ? fullName : customer.getFullName();
        String newPhone = (phone != null && !phone.trim().isEmpty()) ? phone : customer.getPhone();
        String newType = (customerType != null && !customerType.trim().isEmpty()) ? customerType.toUpperCase() : getCustomerType(customer);
        
        // אם סוג הלקוח השתנה, צריך ליצור לקוח חדש מסוג אחר
        String currentType = getCustomerType(customer);
        if (!currentType.equals(newType)) {
            // יצירת לקוח חדש מסוג אחר
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
            // רק עדכון שם וטלפון
            customer.setFullName(newFullName);
            customer.setPhone(newPhone);
        }
    }
    
    /**
     * קבלת סוג לקוח (לבדיקה)
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
     * מחיקת לקוח
     */
    public void deleteCustomer(String idNumber) {
        Customer customer = customers.remove(idNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + idNumber + " not found");
        }
    }
    
    /**
     * קבלת כל הלקוחות (לשמירה)
     */
    public Map<String, Customer> getAllCustomers() {
        return new HashMap<>(customers);
    }
}
