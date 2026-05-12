package entities;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Reclamation {

    private int reclamationId;
    private String subject;
    private String description;
    private Date reclamDate;
    private String statusreclam;
    private String priority;
    private int userId;
    private String category;
    private Timestamp lastResponseDate;
    private boolean isEscalated;

    // NEW: Attachments support
    private List<Attachment> attachments = new ArrayList<>();

    public Reclamation() {
        this.attachments = new ArrayList<>();
    }

    public Reclamation(int reclamationId, String subject, String description, Date reclamDate,
                       String statusreclam, String priority, int userId) {
        this(reclamationId, subject, description, reclamDate, statusreclam, priority, userId, null, null);
    }

    public Reclamation(String subject, String description, Date reclamDate,
                       String statusreclam, String priority, int userId) {
        this(0, subject, description, reclamDate, statusreclam, priority, userId, null, null);
    }

    public Reclamation(int reclamationId, String subject, String description, Date reclamDate,
                       String statusreclam, String priority, int userId,
                       String category, Timestamp lastResponseDate) {
        this.reclamationId = reclamationId;
        this.subject = subject;
        this.description = description;
        this.reclamDate = reclamDate;
        this.statusreclam = statusreclam;
        this.priority = priority;
        this.userId = userId;
        this.category = category;
        this.lastResponseDate = lastResponseDate;
        this.isEscalated = false;
        this.attachments = new ArrayList<>();
    }

    // ── Getters & Setters ────────────────────────────────────────

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getReclamDate() { return reclamDate; }
    public void setReclamDate(Date reclamDate) { this.reclamDate = reclamDate; }

    public String getStatusreclam() { return statusreclam; }
    public void setStatusreclam(String statusreclam) { this.statusreclam = statusreclam; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Timestamp getLastResponseDate() { return lastResponseDate; }
    public void setLastResponseDate(Timestamp lastResponseDate) { this.lastResponseDate = lastResponseDate; }

    public boolean isEscalated() { return isEscalated; }
    public void setEscalated(boolean escalated) { this.isEscalated = escalated; }

    // ==================== ATTACHMENTS ====================
    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public void addAttachment(Attachment attachment) {
        if (attachment != null) {
            this.attachments.add(attachment);
        }
    }
}