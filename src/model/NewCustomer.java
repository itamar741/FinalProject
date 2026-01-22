package model;

public class NewCustomer extends Customer {

 
    public NewCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }


    @Override
    public String getCustomerType() {
        return "NEW";
    }
 
    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice; // Default: no discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("NEW");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
