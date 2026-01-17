package model;

public class LogEntry {

    private String actionType;
    private String description;
    private String dateTime;
    private String chatId; // אופציונלי - null אם לא רלוונטי

    public LogEntry(String actionType,
                    String description,
                    String dateTime) {
        this(actionType, description, dateTime, null);
    }

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
