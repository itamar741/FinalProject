package model.managers;

import model.Branch;
import model.Inventory;
import model.Product;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;
import model.exceptions.InactiveProductException;

public class InventoryManager {

    public void addProductToInventory(Branch branch,
                                      Product product,
                                      int quantity)
            throws InvalidQuantityException {

        Inventory inventory = branch.getInventory();
        inventory.addProduct(product, quantity);
    }

    //alias
    public void addProduct(Branch branch,
                           Product product,
                           int quantity)
            throws InvalidQuantityException {

        addProductToInventory(branch, product, quantity);
    }

    public void sellProduct(Branch branch,
                            Product product,
                            int quantity)
            throws InvalidQuantityException,
            InsufficientStockException,
            InactiveProductException {

        Inventory inventory = branch.getInventory();
        inventory.sellProduct(product, quantity);
    }

    public int getProductQuantity(Branch branch, Product product) {
        return branch.getInventory().getProductQuantity(product);
    }
}
