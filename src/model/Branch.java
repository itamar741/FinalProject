package model;

/**
 * Represents a branch (store location) in the system.
 * Each branch has its own separate inventory managed independently.
 * 
 * @author FinalProject
 */
public class Branch {

    private String branchId;
    private Inventory inventory;

    /**
     * Constructs a new Branch with the specified ID.
     * A new empty inventory is created for this branch.
     * 
     * @param branchId the unique branch identifier
     */
    public Branch(String branchId) {
        this.branchId = branchId;
        this.inventory = new Inventory();
    }

    /**
     * Gets the branch ID.
     * 
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * Gets the branch's inventory.
     * Each branch maintains its own separate inventory.
     * 
     * @return the Inventory object for this branch
     */
    public Inventory getInventory() {
        return inventory;
    }
}
