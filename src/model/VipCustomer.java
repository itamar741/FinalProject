package model;


public class VipCustomer extends Customer {

    public VipCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    @Override
    public String getCustomerType() {
        return "VIP";
    }
    

    @Override
    public double calculatePrice(double basePrice) {
        if (discountManager == null) {
            return basePrice * 0.85; // Default: 15% discount if manager not set
        }
        double discountPercent = discountManager.getDiscount("VIP");
        return basePrice * (1.0 - discountPercent / 100.0);
    }
}
