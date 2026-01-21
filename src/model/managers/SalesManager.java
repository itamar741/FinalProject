package model.managers;

import model.Sale;

import java.util.Collections;
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
        this.sales = Collections.synchronizedList(new ArrayList<>());
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
     * Returns a defensive copy to prevent external modification and ensure thread-safe iteration.
     * 
     * @return a copy of the list of all sales
     */
    public List<Sale> getSales() {
        synchronized (sales) {
            return new ArrayList<>(sales);
        }
    }
}
