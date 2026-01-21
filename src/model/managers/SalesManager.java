package model.managers;

import model.Sale;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages sales records.
 * Maintains a list of all sales transactions in the system.
 * 
 * @author FinalProject
 */
public class SalesManager {

    private List<Sale> sales;

    /**
     * Constructs a new SalesManager with an empty sales list.
     */
    public SalesManager() {
        this.sales = new ArrayList<>();
    }

    /**
     * Adds a sale to the sales records.
     * 
     * @param sale the sale to add (ignored if null)
     */
    public void addSale(Sale sale) {
        if (sale == null) {
            return;
        }

        sales.add(sale);
    }

    /**
     * Gets all sales records.
     * Returns the actual list (not a copy) for performance - caller should not modify.
     * 
     * @return the list of all sales
     */
    public List<Sale> getSales() {
        return sales;
    }
}
