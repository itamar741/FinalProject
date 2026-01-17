package model;

/**
 * מייצג בקשה לצ'אט בתור
 */
public class ChatRequest {
    private String requestId;
    private String requesterUsername;
    private String requesterBranchId;
    private long requestTime;
    private RequestStatus status;
    
    public enum RequestStatus {
        PENDING,    // ממתין להתאמה
        MATCHED,    // נמצאה התאמה
        CANCELLED   // בוטל
    }
    
    public ChatRequest(String requestId, String requesterUsername, String requesterBranchId) {
        this.requestId = requestId;
        this.requesterUsername = requesterUsername;
        this.requesterBranchId = requesterBranchId;
        this.requestTime = System.currentTimeMillis();
        this.status = RequestStatus.PENDING;
    }
    
    // Getters
    public String getRequestId() {
        return requestId;
    }
    
    public String getRequesterUsername() {
        return requesterUsername;
    }
    
    public String getRequesterBranchId() {
        return requesterBranchId;
    }
    
    public long getRequestTime() {
        return requestTime;
    }
    
    public RequestStatus getStatus() {
        return status;
    }
    
    // Setters
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
