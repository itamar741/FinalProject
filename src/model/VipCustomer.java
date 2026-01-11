package model;

public class VipCustomer extends Customer {

    public VipCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 0.85; // 15% הנחה
    }
}
