package storage;

import model.Customer;
import model.NewCustomer;
import model.ReturningCustomer;
import model.VipCustomer;

/**
 * מחלקת DTO לשמירת Customer ב-JSON
 */
public class CustomerData {
    public String fullName;
    public String idNumber;
    public String phone;
    public String customerType;  // NEW, RETURNING, VIP
    
    // Default constructor for JSON deserialization
    public CustomerData() {}
    
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
