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
    
  
    public String getBranchId() { return branchId; }
    
    public String getProductId() { return productId; }
   
    public String getProductName() { return productName; }
  
    public String getCategory() { return category; }
   
    public int getQuantity() { return quantity; }
    
    public double getTotalRevenue() { return totalRevenue; }

    public String getDate() { return date; }
}
