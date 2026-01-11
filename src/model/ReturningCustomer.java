package model;

public class ReturningCustomer extends Customer {

    public ReturningCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone);
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 0.95;
    }
}
