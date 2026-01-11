package model;

public class Branch {

    private String branchId;
    private Inventory inventory;

    public Branch(String branchId) {
        this.branchId = branchId;
        this.inventory = new Inventory();
    }

    public String getBranchId() {
        return branchId;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
