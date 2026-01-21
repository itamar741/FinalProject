package model;

/**
 * Represents a single row in a sales report.
 * Used to aggregate and display sales data by branch, product, category, or date.
 * 
 * @author FinalProject
 */
public class ReportEntry {
    private String branchId;
    private String productId;
    private String productName;
    private String category;
    private int quantity;
    private double totalRevenue;  // Total revenue amount
    private String date;  // Date only (date portion, not time)
    
    /**
     * Constructs a new ReportEntry with the specified details.
     * 
     * @param branchId the branch ID (may be empty for category reports)
     * @param productId the product ID (may be empty for branch/category reports)
     * @param productName the product name (may be empty for branch/category reports)
     * @param category the product category
     * @param quantity the total quantity sold
     * @param totalRevenue the total revenue generated
     * @param date the date of the sale (date portion only)
     */
    public ReportEntry(String branchId, String productId, String productName, 
                      String category, int quantity, double totalRevenue, String date) {
        this.branchId = branchId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.totalRevenue = totalRevenue;
        this.date = date;
    }
    
    /**
     * Gets the branch ID.
     * 
     * @return the branch ID, or empty string if not applicable
     */
    public String getBranchId() { return branchId; }
    
    /**
     * Gets the product ID.
     * 
     * @return the product ID, or empty string if not applicable
     */
    public String getProductId() { return productId; }
    
    /**
     * Gets the product name.
     * 
     * @return the product name, or empty string if not applicable
     */
    public String getProductName() { return productName; }
    
    /**
     * Gets the product category.
     * 
     * @return the category
     */
    public String getCategory() { return category; }
    
    /**
     * Gets the total quantity sold.
     * 
     * @return the quantity
     */
    public int getQuantity() { return quantity; }
    
    /**
     * Gets the total revenue generated.
     * 
     * @return the total revenue
     */
    public double getTotalRevenue() { return totalRevenue; }
    
    /**
     * Gets the date of the sale.
     * 
     * @return the date (date portion only, not time)
     */
    public String getDate() { return date; }
}
