package model;

import java.util.HashSet;
import java.util.Set;

/**
 * מייצג צ'אט פעיל בין משתמשים
 */
public class ChatSession {
    private String chatId;
    private Set<String> participants;  // usernames
    private long startTime;
    private SessionStatus status;
    
    public enum SessionStatus {
        ACTIVE,    // צ'אט פעיל
        ENDED      // צ'אט הסתיים
    }
    
    public ChatSession(String chatId, String user1, String user2) {
        this.chatId = chatId;
        this.participants = new HashSet<>();
        this.participants.add(user1);
        this.participants.add(user2);
        this.startTime = System.currentTimeMillis();
        this.status = SessionStatus.ACTIVE;
    }
    
    // Getters
    public String getChatId() {
        return chatId;
    }
    
    public Set<String> getParticipants() {
        return new HashSet<>(participants);
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }
    
    // Methods
    public void addParticipant(String username) {
        participants.add(username);
    }
    
    public void removeParticipant(String username) {
        participants.remove(username);
    }
    
    public boolean hasParticipant(String username) {
        return participants.contains(username);
    }
    
    public void end() {
        this.status = SessionStatus.ENDED;
    }
    
    public int getParticipantCount() {
        return participants.size();
    }
}
