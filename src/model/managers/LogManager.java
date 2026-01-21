package model.managers;

import model.LogEntry;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages system log entries.
 * Maintains a list of all log entries for tracking system activities.
 * 
 * @author FinalProject
 */
public class LogManager {

    private List<LogEntry> logs;

    /**
     * Constructs a new LogManager with an empty logs list.
     */
    public LogManager() {
        this.logs = new ArrayList<>();
    }

    /**
     * Adds a log entry to the logs.
     * 
     * @param logEntry the log entry to add (ignored if null)
     */
    public void addLog(LogEntry logEntry) {
        if (logEntry == null) {
            return;
        }

        logs.add(logEntry);
    }

    /**
     * Gets all log entries.
     * Returns the actual list (not a copy) for performance - caller should not modify.
     * 
     * @return the list of all log entries
     */
    public List<LogEntry> getLogs() {
        return logs;
    }
}
