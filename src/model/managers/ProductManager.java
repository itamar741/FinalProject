package model.managers;

import java.util.HashMap;
import java.util.Map;

import model.Product;

public class ProductManager {

    private Map<String, Product> products;

    public ProductManager() {
        products = new HashMap<>();
    }

    public Product getProduct(String productId, String name, String category, double price) {
        Product product = products.get(productId);

        if (product == null) {
            product = new Product(productId, name, category, price);
            products.put(productId, product);
        } else {
            // עדכון מחיר אם שונה
            if (Math.abs(product.getPrice() - price) > 0.01) {
                product.setPrice(price);
            }
        }

        return product;
    }
    
    // Overload למקרה שצריך בלי מחיר (להתאמה לאחור - לא מומלץ)
    public Product getProduct(String productId, String name, String category) {
        return getProduct(productId, name, category, 0.0);
    }

    public Product getExistingProduct(String productId) {
        return products.get(productId);
    }
    
    /**
     * קבלת כל המוצרים (לשמירה)
     */
    public Map<String, Product> getAllProducts() {
        return new HashMap<>(products);
    }
    
    /**
     * הוספת מוצר ישיר (לטעינה)
     */
    public void addProductDirectly(Product product) {
        if (product != null) {
            products.put(product.getProductId(), product);
        }
    }

}
