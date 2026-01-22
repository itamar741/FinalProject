package model;


public class ReturningCustomer extends Customer {

  
    public ReturningCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }


    @Override
    public String getCustomerType() {
        return "RETURNING";
    }
    

    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice * 0.95; // Default: 5% discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("RETURNING");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
