package model;

/**
 * Represents a chat request in the queue.
 * Used by ChatManager to manage FIFO queue of chat requests.
 * Tracks the requester, their branch, request time, and current status.
 * 
 * @author FinalProject
 */
public class ChatRequest {
    private String requestId;
    private String requesterUsername;
    private String requesterBranchId;
    private long requestTime;
    private RequestStatus status;
    
    /**
     * Enumeration of possible request statuses.
     */
    public enum RequestStatus {
        PENDING,    // Waiting to be matched
        MATCHED,    // Successfully matched with another user
        CANCELLED   // Request was cancelled
    }
    
    /**
     * Constructs a new ChatRequest.
     * New requests are created with PENDING status.
     * 
     * @param requestId the unique request identifier
     * @param requesterUsername the username of the user requesting the chat
     * @param requesterBranchId the branch ID of the requester
     */
    public ChatRequest(String requestId, String requesterUsername, String requesterBranchId) {
        this.requestId = requestId;
        this.requesterUsername = requesterUsername;
        this.requesterBranchId = requesterBranchId;
        this.requestTime = System.currentTimeMillis();
        this.status = RequestStatus.PENDING;
    }
    
    /**
     * Gets the unique request identifier.
     * 
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Gets the username of the user who requested the chat.
     * 
     * @return the requester username
     */
    public String getRequesterUsername() {
        return requesterUsername;
    }
    
    /**
     * Gets the branch ID of the requester.
     * Used to ensure users from different branches are matched.
     * 
     * @return the requester branch ID
     */
    public String getRequesterBranchId() {
        return requesterBranchId;
    }
    
    /**
     * Gets the request timestamp (milliseconds since epoch).
     * Used for FIFO ordering in the queue.
     * 
     * @return the request time
     */
    public long getRequestTime() {
        return requestTime;
    }
    
    /**
     * Gets the current status of this request.
     * 
     * @return the request status
     */
    public RequestStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status of this request.
     * 
     * @param status the new status
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
