package model.managers;

import model.LogEntry;

import java.util.List;
import java.util.ArrayList;

public class LogManager {

    private List<LogEntry> logs;

    public LogManager() {
        this.logs = new ArrayList<>();
    }

    public void addLog(LogEntry logEntry) {
        if (logEntry == null) {
            return;
        }

        logs.add(logEntry);
    }

    public List<LogEntry> getLogs() {
        return logs;
    }
}
