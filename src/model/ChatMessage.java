package model;

/**
 * מייצג הודעה בצ'אט
 */
public class ChatMessage {
    private String chatId;
    private String senderUsername;
    private String message;
    private long timestamp;
    private MessageType messageType;
    
    public enum MessageType {
        TEXT,      // הודעת טקסט רגילה
        SYSTEM     // הודעת מערכת (למשל: "מנהל הצטרף")
    }
    
    public ChatMessage(String chatId, String senderUsername, String message, MessageType messageType) {
        this.chatId = chatId;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.messageType = messageType;
    }
    
    // Getters
    public String getChatId() {
        return chatId;
    }
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * המרה ל-JSON (פשוט)
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"chatId\":\"").append(escapeJson(chatId)).append("\",");
        json.append("\"senderUsername\":\"").append(escapeJson(senderUsername)).append("\",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\",");
        json.append("\"timestamp\":").append(timestamp).append(",");
        json.append("\"messageType\":\"").append(messageType.name()).append("\"");
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
