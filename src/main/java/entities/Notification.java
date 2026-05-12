package entities;

import java.sql.Timestamp;

/**
 * Entity: notification
 * Types: REPONSE_AJOUTEE | STATUT_CHANGE | ESCALADE_AUTO | ESCALADE_MANUELLE | RESOLUTION
 */
public class Notification {

    private int notificationId;
    private int userId;
    private int reclamationId;
    private String type;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    // ── Constructors ────────────────────────────────────────────

    public Notification() {
    }

    public Notification(int userId, int reclamationId, String type, String message) {
        this.userId = userId;
        this.reclamationId = reclamationId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    // ── Getters & Setters ───────────────────────────────────────

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ── Helpers ─────────────────────────────────────────────────

    /**
     * Returns an icon character suitable for the notification type.
     */
    public String getIcon() {
        if ("REPONSE_AJOUTEE".equals(type))    { return "💬"; }
        if ("STATUT_CHANGE".equals(type))       { return "🔄"; }
        if ("ESCALADE_AUTO".equals(type))       { return "🚨"; }
        if ("ESCALADE_MANUELLE".equals(type))   { return "⚠️"; }
        if ("RESOLUTION".equals(type))          { return "✅"; }
        return "🔔";
    }

    /**
     * Returns a short human-readable label for the type.
     */
    public String getTypeLabel() {
        if ("REPONSE_AJOUTEE".equals(type))    { return "Réponse reçue"; }
        if ("STATUT_CHANGE".equals(type))       { return "Statut modifié"; }
        if ("ESCALADE_AUTO".equals(type))       { return "Escalade automatique"; }
        if ("ESCALADE_MANUELLE".equals(type))   { return "Escalade demandée"; }
        if ("RESOLUTION".equals(type))          { return "Résolu"; }
        return "Notification";
    }

    @Override
    public String toString() {
        return "Notification{id=" + notificationId + ", userId=" + userId
                + ", reclamationId=" + reclamationId + ", type=" + type
                + ", isRead=" + isRead + "}";
    }
}