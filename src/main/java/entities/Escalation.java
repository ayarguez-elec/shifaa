package entities;

import java.sql.Timestamp;

public class Escalation {
    private int escalationId;
    private int reclamationId;
    private int userId;
    private String reason;
    private String status;
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    public Escalation() {}

    public int getEscalationId() { return escalationId; }
    public void setEscalationId(int escalationId) { this.escalationId = escalationId; }
    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}