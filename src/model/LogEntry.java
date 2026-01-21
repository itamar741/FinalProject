package model;

/**
 * Represents a log entry in the system.
 * Logs track all system activities including user actions, sales, customer operations, and chat events.
 * Chat-related logs include an optional chatId to link them to specific chat sessions.
 * 
 * @author FinalProject
 */
public class LogEntry {

    private String actionType;
    private String description;
    private String dateTime;
    private String chatId; // Optional - null if not related to a chat

    /**
     * Constructs a new LogEntry without a chatId.
     * 
     * @param actionType the type of action (e.g., "LOGIN", "SALE", "ADD_CUSTOMER")
     * @param description a detailed description of the action
     * @param dateTime the date and time when the action occurred (ISO format string)
     */
    public LogEntry(String actionType,
                    String description,
                    String dateTime) {
        this(actionType, description, dateTime, null);
    }

    /**
     * Constructs a new LogEntry with an optional chatId.
     * 
     * @param actionType the type of action (e.g., "CHAT_STARTED", "CHAT_MESSAGE", "CHAT_ENDED")
     * @param description a detailed description of the action
     * @param dateTime the date and time when the action occurred (ISO format string)
     * @param chatId the chat ID if this log is related to a chat session, null otherwise
     */
    public LogEntry(String actionType,
                    String description,
                    String dateTime,
                    String chatId) {

        this.actionType = actionType;
        this.description = description;
        this.dateTime = dateTime;
        this.chatId = chatId;
    }

    /**
     * Gets the action type.
     * 
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Gets the description of the action.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the date and time when the action occurred.
     * 
     * @return the date/time string (ISO format)
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Gets the chat ID if this log is related to a chat session.
     * 
     * @return the chat ID, or null if not related to a chat
     */
    public String getChatId() {
        return chatId;
    }
}
