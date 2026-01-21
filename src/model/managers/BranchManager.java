package model.managers;
import model.Branch;
import java.util.Collections;
import java.util.*;

/**
 * Manages branch data and operations.
 * Creates default branches B1 and B2 on initialization.
 * Each branch maintains its own separate inventory.
 * 
 * @author FinalProject
 */
public class BranchManager {

    private Map<String, Branch> branches;

    /**
     * Constructs a new BranchManager with default branches B1 and B2.
     */
    public BranchManager() {
        branches = Collections.synchronizedMap(new HashMap<>());
        // Create default branches
        branches.put("B1", new Branch("B1"));
        branches.put("B2", new Branch("B2"));
    }
    
    /**
     * Adds a new branch to the system.
     * Does nothing if branch already exists.
     * 
     * @param branchId the unique branch identifier
     */
    public void addBranch(String branchId) {
        synchronized (branches) {
            if (!branches.containsKey(branchId)) {
                branches.put(branchId, new Branch(branchId));
            }
        }
    }
    
    /**
     * Gets a branch by ID.
     * 
     * @param branchId the branch ID
     * @return the Branch object, or null if not found
     */
    public Branch getBranch(String branchId) {
        synchronized (branches) {
            return branches.get(branchId);
        }
    }
    
    /**
     * Gets all branches (for saving to storage).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of branchId to Branch
     */
    public Map<String, Branch> getAllBranches() {
        synchronized (branches) {
            return new HashMap<>(branches);
        }
    }
    
    /**
     * Gets a list of all branch IDs (for saving to storage).
     * 
     * @return a list of branch IDs
     */
    public List<String> getBranchIds() {
        synchronized (branches) {
            return new ArrayList<>(branches.keySet());
        }
    }
    
    /**
     * Loads branches from a list of branch IDs (for loading from storage).
     * If the list is empty, creates default branches B1 and B2.
     * 
     * @param branchIds the list of branch IDs to load
     */
    public void loadBranches(List<String> branchIds) {
        synchronized (branches) {
            branches.clear();
            for (String branchId : branchIds) {
                branches.put(branchId, new Branch(branchId));
            }
            // If no branches, create defaults
            if (branches.isEmpty()) {
                branches.put("B1", new Branch("B1"));
                branches.put("B2", new Branch("B2"));
            }
        }
    }
}
