package model;

import java.util.List;
import java.util.ArrayList;

public class SalesManager {

    private List<Sale> sales;

    public SalesManager() {
        this.sales = new ArrayList<>();
    }

    public void addSale(Sale sale) {
        if (sale == null) {
            return;
        }

        sales.add(sale);
    }

    public List<Sale> getSales() {
        return sales;
    }
}
