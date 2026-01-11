package model.managers;
import model.Branch;
import java.util.*;

public class BranchManager {

    private Map<String, Branch> branches;

    public BranchManager() {
        branches = new HashMap<>();
        // יצירת סניפים ברירת מחדל
        branches.put("B1", new Branch("B1"));
        branches.put("B2", new Branch("B2"));
    }
    
    /**
     * יצירת סניף חדש
     */
    public void addBranch(String branchId) {
        if (!branches.containsKey(branchId)) {
            branches.put(branchId, new Branch(branchId));
        }
    }
    
    /**
     * קבלת סניף לפי ID
     */
    public Branch getBranch(String branchId) {
        return branches.get(branchId);
    }
    
    /**
     * קבלת כל הסניפים (לשמירה)
     */
    public Map<String, Branch> getAllBranches() {
        return new HashMap<>(branches);
    }
    
    /**
     * קבלת רשימת ID-ים של סניפים (לשמירה)
     */
    public List<String> getBranchIds() {
        return new ArrayList<>(branches.keySet());
    }
    
    /**
     * טעינת רשימת סניפים (מטעינה)
     */
    public void loadBranches(List<String> branchIds) {
        branches.clear();
        for (String branchId : branchIds) {
            branches.put(branchId, new Branch(branchId));
        }
        // אם אין סניפים, יוצר ברירת מחדל
        if (branches.isEmpty()) {
            branches.put("B1", new Branch("B1"));
            branches.put("B2", new Branch("B2"));
        }
    }
}
