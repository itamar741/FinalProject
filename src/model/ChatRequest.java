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
     * requestId the unique request identifier
     * requesterUsername the username of the user requesting the chat
     * requesterBranchId the branch ID of the requester
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
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Gets the username of the user who requested the chat.
     */
    public String getRequesterUsername() {
        return requesterUsername;
    }
    
    /**
     * Gets the branch ID of the requester.
     * Used to ensure users from different branches are matched.
     */
    public String getRequesterBranchId() {
        return requesterBranchId;
    }
    
    /**
     * Gets the request timestamp (milliseconds since epoch).
     * Used for FIFO ordering in the queue.
     */
    public long getRequestTime() {
        return requestTime;
    }
    
    /**
     * Gets the current status of this request.
     */
    public RequestStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status of this request.
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
