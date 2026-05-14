package entities;

import java.sql.Timestamp;

public class HealthLog {
    private int logId;
    private int userId;
    private Timestamp date;
    private String type; // "Routine" or "Symptom"
    private String result;
    private int severity; // 1 to 10

    public HealthLog() {}

    public HealthLog(int userId, String type, String result, int severity) {
        this.userId = userId;
        this.type = type;
        this.result = result;
        this.severity = severity;
        this.date = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
}
