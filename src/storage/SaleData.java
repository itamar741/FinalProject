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
    public String customerId;
    public String dateTime;
    public double basePrice;   // מחיר לפני הנחה
    public double finalPrice; // מחיר סופי
    
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
        this.customerId = sale.getCustomerId();
        this.dateTime = sale.getDateTime();
        this.basePrice = sale.getBasePrice();
        this.finalPrice = sale.getFinalPrice();
    }
    
    public Sale toSale() {
        Product product = new Product(productId, productName, productCategory, productPrice);
        // אם basePrice ו-finalPrice לא קיימים (למקרה של טעינה מ-JSON ישן), נחשב אותם
        double calculatedBasePrice = basePrice > 0 ? basePrice : productPrice * quantity;
        double calculatedFinalPrice = finalPrice > 0 ? finalPrice : calculatedBasePrice;
        // אם customerId לא קיים (למקרה של מכירות ישנות), נשתמש ב-null או במחרוזת ריקה
        String saleCustomerId = (customerId != null && !customerId.isEmpty()) ? customerId : "";
        return new Sale(product, quantity, branchId, employeeNumber, saleCustomerId, dateTime, calculatedBasePrice, calculatedFinalPrice);
    }
}
