package model;

/**
 * Represents a single message in a chat session.
 * Messages can be either user text messages or system messages (e.g., "Manager joined").
 * Includes serialization to JSON for client-server communication.
 * 
 * @author FinalProject
 */
public class ChatMessage {
    private String chatId;
    private String senderUsername;
    private String message;
    private long timestamp;
    private MessageType messageType;
    
    /**
     * Enumeration of message types.
     */
    public enum MessageType {
        TEXT,      // Regular user text message
        SYSTEM     // System message (e.g., "Manager joined chat")
    }
    
    /**
     * Constructs a new ChatMessage.
     * 
     * @param chatId the chat ID this message belongs to
     * @param senderUsername the username of the sender (or "SYSTEM" for system messages)
     * @param message the message content
     * @param messageType the type of message (TEXT or SYSTEM)
     */
    public ChatMessage(String chatId, String senderUsername, String message, MessageType messageType) {
        this.chatId = chatId;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.messageType = messageType;
    }
    
    /**
     * Gets the chat ID this message belongs to.
     * 
     * @return the chat ID
     */
    public String getChatId() {
        return chatId;
    }
    
    /**
     * Gets the sender's username.
     * 
     * @return the sender username, or "SYSTEM" for system messages
     */
    public String getSenderUsername() {
        return senderUsername;
    }
    
    /**
     * Gets the message content.
     * 
     * @return the message text
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the message timestamp (milliseconds since epoch).
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the message type.
     * 
     * @return the message type (TEXT or SYSTEM)
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * Converts this message to JSON format.
     * Used for serialization when sending messages to clients.
     * 
     * @return a JSON string representation of this message
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
    
    /**
     * Escapes special JSON characters in a string.
     * 
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
