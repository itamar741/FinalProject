package model;

public class Sale {

    private Product product;
    private int quantity;
    private String branchId;
    private String employeeNumber;
    private String dateTime;

    public Sale(Product product,
                int quantity,
                String branchId,
                String employeeNumber,
                String dateTime) {

        this.product = product;
        this.quantity = quantity;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        this.dateTime = dateTime;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getDateTime() {
        return dateTime;
    }
}
