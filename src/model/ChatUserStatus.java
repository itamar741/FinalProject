package model;

/**
 * Enumeration of possible user statuses in the chat system.
 * Used by ChatManager to track user availability and prevent duplicate chat requests.
 * 
 * @author FinalProject
 */
public enum ChatUserStatus {
    /** User is available and can accept chat requests */
    AVAILABLE,
    
    /** User is currently in an active chat session */
    IN_CHAT,
    
    /** User is waiting in the queue for a chat match */
    IN_QUEUE
}
