package model.managers;

import model.LogEntry;

import java.util.Collections;
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
        this.logs = Collections.synchronizedList(new ArrayList<>());
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
     * Returns a defensive copy to prevent external modification and ensure thread-safe iteration.
     * 
     * @return a copy of the list of all log entries
     */
    public List<LogEntry> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }
}
