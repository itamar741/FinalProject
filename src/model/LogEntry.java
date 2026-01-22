package model;


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

    
    public String getActionType() {
        return actionType;
    }

   
    public String getDescription() {
        return description;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getChatId() {
        return chatId;
    }
}
