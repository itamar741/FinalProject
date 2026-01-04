package model;

public class LogEntry {

    private String actionType;
    private String description;
    private String dateTime;

    public LogEntry(String actionType,
                    String description,
                    String dateTime) {

        this.actionType = actionType;
        this.description = description;
        this.dateTime = dateTime;
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
}
