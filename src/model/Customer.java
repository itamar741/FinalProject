package model;

public abstract class Customer {

    protected String fullName;
    protected String idNumber;
    protected String phone;

    public Customer(String fullName, String idNumber, String phone) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public abstract double calculatePrice(double basePrice);
}


