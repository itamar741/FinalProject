package model;

public class Customer {

    private String fullName;
    private String idNumber;
    private String phone;
    private String customerType;

    public Customer(String fullName,
                    String idNumber,
                    String phone,
                    String customerType) {

        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.customerType = customerType;
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

    public String getCustomerType() {
        return customerType;
    }


}
