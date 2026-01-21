package model.managers;

/**
 * Utility class for checking user permissions based on role.
 * Provides static methods to check if a user with a specific role can perform various operations.
 * 
 * Roles:
 * - admin: All permissions, both branches
 * - manager: All operations for their branch only, can join existing chats, cannot create employees
 * - salesman: All operations except creating users and joining existing chats, can request chats
 * - cashier: Can create employees and users for their branch only, can request chats, cannot join existing chats
 * 
 * @author FinalProject
 */
public class PermissionChecker {
    
    /**
     * Checks if a user with the given role can create employees.
     * Admin and cashier can create employees.
     * 
     * @param role the user's role
     * @return true if the role can create employees, false otherwise
     */
    public static boolean canCreateEmployee(String role) {
        return "admin".equals(role) || "cashier".equals(role);
    }
    
    /**
     * Checks if a user with the given role can create employees for a specific branch.
     * Admin can create for any branch, cashier only for their own branch.
     * 
     * @param role the user's role
     * @param userBranchId the branch ID of the user making the request
     * @param targetBranchId the branch ID where the new employee will be created
     * @return true if the role can create employees for the target branch, false otherwise
     */
    public static boolean canCreateEmployeeForBranch(String role, String userBranchId, String targetBranchId) {
        if ("admin".equals(role)) {
            return true; // Admin can create for any branch
        }
        if ("cashier".equals(role)) {
            return userBranchId != null && userBranchId.equals(targetBranchId); // Cashier only for their branch
        }
        return false;
    }
    
    /**
     * Checks if a user with the given role can view/manage employees.
     * Admin and cashier can view employees.
     * 
     * @param role the user's role
     * @return true if the role can view employees, false otherwise
     */
    public static boolean canViewEmployees(String role) {
        return "admin".equals(role) || "cashier".equals(role);
    }
    
    /**
     * Checks if a user with the given role can manage employees (update, delete, activate/deactivate).
     * Only admin can manage employees.
     * 
     * @param role the user's role
     * @return true if the role can manage employees, false otherwise
     */
    public static boolean canManageEmployees(String role) {
        return "admin".equals(role);
    }
    
    /**
     * Checks if a user with the given role can create users.
     * Admin and cashier can create users.
     * Cashier can only create users for their own branch.
     * 
     * @param role the user's role
     * @param userBranchId the branch ID of the user making the request
     * @param targetBranchId the branch ID where the new user will be created
     * @return true if the role can create users for the target branch, false otherwise
     */
    public static boolean canCreateUser(String role, String userBranchId, String targetBranchId) {
        if ("admin".equals(role)) {
            return true; // Admin can create users for any branch
        }
        if ("cashier".equals(role)) {
            return userBranchId != null && userBranchId.equals(targetBranchId); // Cashier only for their branch
        }
        return false;
    }
    
    /**
     * Checks if a user with the given role can join existing chats.
     * Admin and manager can join existing chats.
     * 
     * @param role the user's role
     * @return true if the role can join existing chats, false otherwise
     */
    public static boolean canJoinChat(String role) {
        return "admin".equals(role) || "manager".equals(role);
    }
    
    /**
     * Checks if a user with the given role can request chats.
     * All roles can request chats.
     * 
     * @param role the user's role
     * @return true if the role can request chats, false otherwise
     */
    public static boolean canRequestChat(String role) {
        return true; // All roles can request chats
    }
    
    /**
     * Checks if a user with the given role can access a specific branch.
     * Admin can access all branches, others can only access their own branch.
     * 
     * @param role the user's role
     * @param userBranchId the branch ID of the user
     * @param targetBranchId the branch ID to access
     * @return true if the role can access the target branch, false otherwise
     */
    public static boolean canAccessBranch(String role, String userBranchId, String targetBranchId) {
        if ("admin".equals(role)) {
            return true; // Admin can access all branches
        }
        return userBranchId != null && userBranchId.equals(targetBranchId);
    }
    
    /**
     * Checks if a user with the given role can view logs.
     * Admin, manager, and salesman can view logs.
     * 
     * @param role the user's role
     * @return true if the role can view logs, false otherwise
     */
    public static boolean canViewLogs(String role) {
        return "admin".equals(role) || "manager".equals(role) || "salesman".equals(role);
    }
    
    /**
     * Checks if a user with the given role can manage users (view, update, delete, activate/deactivate).
     * Only admin can manage users.
     * 
     * @param role the user's role
     * @return true if the role can manage users, false otherwise
     */
    public static boolean canManageUsers(String role) {
        return "admin".equals(role);
    }
    
    /**
     * Checks if a user with the given role can delete products.
     * Only admin can delete products.
     * 
     * @param role the user's role
     * @return true if the role can delete products, false otherwise
     */
    public static boolean canDeleteProduct(String role) {
        return "admin".equals(role);
    }
    
    /**
     * Checks if a user with the given role can view all branches.
     * Admin can view all branches, others can only view their own branch.
     * 
     * @param role the user's role
     * @return true if the role can view all branches, false otherwise
     */
    public static boolean canViewAllBranches(String role) {
        return "admin".equals(role);
    }
}
