package model;

public class Sale {

    private Product product;
    private int quantity;
    private String branchId;
    private String employeeNumber;
    private String customerId;
    private String dateTime;
    private double basePrice;  // מחיר לפני הנחה (מחיר מוצר * כמות)
    private double finalPrice; // מחיר סופי (לאחר הנחה)

    public Sale(Product product,
                int quantity,
                String branchId,
                String employeeNumber,
                String customerId,
                String dateTime,
                double basePrice,
                double finalPrice) {

        this.product = product;
        this.quantity = quantity;
        this.branchId = branchId;
        this.employeeNumber = employeeNumber;
        this.customerId = customerId;
        this.dateTime = dateTime;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
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
    
    public String getCustomerId() {
        return customerId;
    }

    public String getDateTime() {
        return dateTime;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
    
    public double getFinalPrice() {
        return finalPrice;
    }
}
