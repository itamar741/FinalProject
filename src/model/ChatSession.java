package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an active chat session between users.
 * Tracks participants, start time, and session status.
 * Managers can join existing chats as additional participants.
 */
public class ChatSession {
    private String chatId;
    private Set<String> participants;  // usernames of all participants
    private long startTime;
    private SessionStatus status;
    
    /**
     * Enumeration of possible chat session statuses.
     */
    public enum SessionStatus {
        ACTIVE,    // Chat is currently active
        ENDED      // Chat has ended
    }
    
    /**
     * Constructs a new ChatSession between two users.
     * 
     * chatId the unique chat identifier
     *  user1 the first participant's username
     *  user2 the second participant's username
     */
    public ChatSession(String chatId, String user1, String user2) {
        this.chatId = chatId;
        this.participants = new HashSet<>();
        this.participants.add(user1);
        this.participants.add(user2);
        this.startTime = System.currentTimeMillis();
        this.status = SessionStatus.ACTIVE;
    }
    
    /**
     * Gets the chat ID.
     * 
     * @return the chat ID
     */
    public String getChatId() {
        return chatId;
    }
    
    /**
     * Gets a copy of the participants set.
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Set of participant usernames
     */
    public Set<String> getParticipants() {
        return new HashSet<>(participants);
    }
    
    /**
     * Gets the chat start time as a timestamp (milliseconds since epoch).
     * 
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the current session status.
     * 
     * @return the status
     */
    public SessionStatus getStatus() {
        return status;
    }
    
    /**
     * Checks if the chat session is currently active.
     * 
     * @return true if active, false if ended
     */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }
    
    /**
     * Adds a participant to the chat (e.g., when a manager joins).
     * 
     * @param username the username of the participant to add
     */
    public void addParticipant(String username) {
        participants.add(username);
    }
    
    /**
     * Removes a participant from the chat.
     * 
     * @param username the username of the participant to remove
     */
    public void removeParticipant(String username) {
        participants.remove(username);
    }
    
    /**
     * Checks if a user is a participant in this chat.
     * 
     * @param username the username to check
     * @return true if the user is a participant, false otherwise
     */
    public boolean hasParticipant(String username) {
        return participants.contains(username);
    }
    
    /**
     * Ends the chat session.
     * Marks the session as ENDED.
     */
    public void end() {
        this.status = SessionStatus.ENDED;
    }
    
    /**
     * Gets the number of participants in the chat.
     * 
     * @return the participant count
     */
    public int getParticipantCount() {
        return participants.size();
    }
}
