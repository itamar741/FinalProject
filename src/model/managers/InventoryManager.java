package model.managers;

import model.Branch;
import model.Inventory;
import model.Product;

import model.exceptions.InvalidQuantityException;
import model.exceptions.InsufficientStockException;

/**
 * Manages inventory operations for branches.
 * Acts as a facade to Branch and Inventory classes, providing a clean API for inventory management.
 * Each branch maintains its own separate inventory.
 * 
 * @author FinalProject
 */
public class InventoryManager {

    /**
     * Adds a product to a branch's inventory.
     * 
     * @param branch the branch to add the product to
     * @param product the product to add
     * @param quantity the quantity to add (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     */
    public void addProductToInventory(Branch branch,
                                      Product product,
                                      int quantity)
            throws InvalidQuantityException {

        Inventory inventory = branch.getInventory();
        inventory.addProduct(product, quantity);
    }

    /**
     * Alias for addProductToInventory - adds a product to a branch's inventory.
     * 
     * @param branch the branch to add the product to
     * @param product the product to add
     * @param quantity the quantity to add (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     */
    public void addProduct(Branch branch,
                           Product product,
                           int quantity)
            throws InvalidQuantityException {

        addProductToInventory(branch, product, quantity);
    }

    /**
     * Sells a product from a branch's inventory (reduces quantity).
     * 
     * @param branch the branch to sell from
     * @param product the product to sell
     * @param quantity the quantity to sell (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock available
     */
    public void sellProduct(Branch branch,
                            Product product,
                            int quantity)
            throws InvalidQuantityException,
            InsufficientStockException {

        Inventory inventory = branch.getInventory();
        inventory.sellProduct(product, quantity);
    }

    /**
     * Gets the current quantity of a product in a branch's inventory.
     * 
     * @param branch the branch to check
     * @param product the product to check
     * @return the quantity available, or 0 if product is not in inventory
     */
    public int getProductQuantity(Branch branch, Product product) {
        return branch.getInventory().getProductQuantity(product);
    }
    
    /**
     * Removes a specified quantity of a product from a branch's inventory.
     * 
     * @param branch the branch to remove from
     * @param product the product to remove
     * @param quantity the quantity to remove (must be greater than 0)
     * @throws InvalidQuantityException if quantity is less than or equal to 0
     * @throws InsufficientStockException if there is not enough stock to remove
     */
    public void removeProduct(Branch branch,
                             Product product,
                             int quantity)
            throws InvalidQuantityException, InsufficientStockException {
        
        Inventory inventory = branch.getInventory();
        inventory.removeProduct(product, quantity);
    }
}
