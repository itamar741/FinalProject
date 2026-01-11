package model;

public class NewCustomer extends Customer {

    public NewCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice;
    }
}
