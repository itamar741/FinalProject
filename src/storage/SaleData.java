package storage;

import model.Sale;
import model.Product;

/**
 * מחלקת DTO לשמירת Sale ב-JSON
 */
public class SaleData {
    public String productId;
    public String productName;
    public String productCategory;
    public double productPrice;
    public int quantity;
    public String branchId;
    public String employeeNumber;
    public String dateTime;
    
    // Default constructor for JSON deserialization
    public SaleData() {}
    
    public SaleData(Sale sale) {
        Product product = sale.getProduct();
        this.productId = product.getProductId();
        this.productName = product.getName();
        this.productCategory = product.getCategory();
        this.productPrice = product.getPrice();
        this.quantity = sale.getQuantity();
        this.branchId = sale.getBranchId();
        this.employeeNumber = sale.getEmployeeNumber();
        this.dateTime = sale.getDateTime();
    }
    
    public Sale toSale() {
        Product product = new Product(productId, productName, productCategory, productPrice);
        return new Sale(product, quantity, branchId, employeeNumber, dateTime);
    }
}
