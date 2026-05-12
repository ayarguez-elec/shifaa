package services;

import entities.Notification;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService
 * Handles all notification DB operations and provides static trigger helpers
 * called by ReponseServices, ReclamationServices, and EscalationService.
 */
public class NotificationService {

    private Connection cnx;

    public NotificationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ────────────────────────────────────────────────────────────
    //  WRITE
    // ────────────────────────────────────────────────────────────

    /**
     * Persist a new notification row.
     */
    public void add(Notification n) {
        String sql = "INSERT INTO notification (userId, reclamationId, type, message, isRead) "
                + "VALUES (?, ?, ?, ?, 0)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, n.getUserId());
            ps.setInt(2, n.getReclamationId());
            ps.setString(3, n.getType());
            ps.setString(4, n.getMessage());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] add() error: " + e.getMessage());
        }
    }

    /**
     * Mark a single notification as read.
     */
    public void markAsRead(int notificationId) {
        String sql = "UPDATE notification SET isRead = 1 WHERE notificationId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAsRead() error: " + e.getMessage());
        }
    }

    /**
     * Mark ALL unread notifications for a user as read.
     */
    public void markAllAsRead(int userId) {
        String sql = "UPDATE notification SET isRead = 1 WHERE userId = ? AND isRead = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAllAsRead() error: " + e.getMessage());
        }
    }

    /**
     * Delete a single notification.
     */
    public void delete(int notificationId) {
        String sql = "DELETE FROM notification WHERE notificationId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] delete() error: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    //  READ
    // ────────────────────────────────────────────────────────────

    /**
     * All notifications for a user, most recent first.
     */
    public List<Notification> getByUser(int userId) {
        List<Notification> list = new ArrayList<Notification>();
        String sql = "SELECT * FROM notification WHERE userId = ? ORDER BY createdAt DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] getByUser() error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Only UNREAD notifications for a user.
     */
    public List<Notification> getUnreadByUser(int userId) {
        List<Notification> list = new ArrayList<Notification>();
        String sql = "SELECT * FROM notification WHERE userId = ? AND isRead = 0 ORDER BY createdAt DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] getUnreadByUser() error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Count of unread notifications for a user. Used for badge display.
     */
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE userId = ? AND isRead = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { return rs.getInt(1); }
        } catch (SQLException e) {
            System.err.println("[NotificationService] countUnread() error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Last N notifications for a user (for sidebar preview panel).
     */
    public List<Notification> getRecent(int userId, int limit) {
        List<Notification> list = new ArrayList<Notification>();
        String sql = "SELECT * FROM notification WHERE userId = ? ORDER BY createdAt DESC LIMIT ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] getRecent() error: " + e.getMessage());
        }
        return list;
    }

    // ────────────────────────────────────────────────────────────
    //  STATIC TRIGGER HELPERS
    //  Call these from other services to fire notifications.
    // ────────────────────────────────────────────────────────────

    /**
     * Trigger: admin added a response → notify the reclamation's owner (userId).
     *
     * Call from ReponseServices.add() AFTER the INSERT succeeds.
     *
     * @param reclamationId the reclamation that received a response
     * @param ownerUserId   userId from reclamation.userId (the patient / user)
     * @param subject       reclamation subject for the message text
     */
    public static void triggerReponseAjoutee(int reclamationId, int ownerUserId, String subject) {
        try {
            NotificationService svc = new NotificationService();
            String message = "Une réponse a été ajoutée à votre réclamation : \"" + subject + "\".";
            Notification n = new Notification(ownerUserId, reclamationId, "REPONSE_AJOUTEE", message);
            svc.add(n);
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerReponseAjoutee() error: " + e.getMessage());
        }
    }

    /**
     * Trigger: reclamation status changed → notify the owner.
     *
     * Call from ReclamationServices.updateStatus() AFTER the UPDATE succeeds.
     *
     * @param reclamationId the reclamation
     * @param ownerUserId   userId from reclamation.userId
     * @param subject       reclamation subject
     * @param newStatus     the new status value
     */
    public static void triggerStatutChange(int reclamationId, int ownerUserId, String subject, String newStatus) {
        try {
            NotificationService svc = new NotificationService();
            String message = "Le statut de votre réclamation \"" + subject + "\" est passé à : " + newStatus + ".";
            String type = "Résolu".equalsIgnoreCase(newStatus) || "resolved".equalsIgnoreCase(newStatus)
                    ? "RESOLUTION" : "STATUT_CHANGE";
            Notification n = new Notification(ownerUserId, reclamationId, type, message);
            svc.add(n);
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerStatutChange() error: " + e.getMessage());
        }
    }

    /**
     * Trigger: automatic escalation detected → notify admin (userId=1).
     *
     * Call from EscalationService.runAutoEscalation() for each escalated reclamation.
     *
     * @param reclamationId the reclamation
     * @param subject       reclamation subject
     * @param ownerName     name of the user who filed the reclamation
     */
    public static void triggerEscaladeAuto(int reclamationId, String subject, String ownerName) {
        try {
            NotificationService svc = new NotificationService();
            // Notify admin
            String msgAdmin = "Escalade automatique : la réclamation \"" + subject
                    + "\" de " + ownerName + " est urgente et sans réponse depuis plus de 3 jours.";
            svc.add(new Notification(1, reclamationId, "ESCALADE_AUTO", msgAdmin));
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerEscaladeAuto() error: " + e.getMessage());
        }
    }

    /**
     * Trigger: user manually requests escalation → notify admin (userId=1) AND confirm to user.
     *
     * Call from EscalationService.requestManualEscalation().
     *
     * @param reclamationId the reclamation
     * @param ownerUserId   the user who requested escalation
     * @param subject       reclamation subject
     * @param reason        the reason text the user provided
     */
    public static void triggerEscaladeManuelle(int reclamationId, int ownerUserId, String subject, String reason) {
        try {
            NotificationService svc = new NotificationService();
            // Notify admin
            String msgAdmin = "Escalade manuelle demandée pour \"" + subject + "\" — Raison : " + reason;
            svc.add(new Notification(1, reclamationId, "ESCALADE_MANUELLE", msgAdmin));
            // Confirm to user
            String msgUser = "Votre demande d'escalade pour \"" + subject + "\" a bien été envoyée à l'administrateur.";
            svc.add(new Notification(ownerUserId, reclamationId, "ESCALADE_MANUELLE", msgUser));
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerEscaladeManuelle() error: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ────────────────────────────────────────────────────────────

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notificationId"));
        n.setUserId(rs.getInt("userId"));
        n.setReclamationId(rs.getInt("reclamationId"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("isRead") == 1);
        n.setCreatedAt(rs.getTimestamp("createdAt"));
        return n;
    }

    public List<Notification> getUnread(int i) {
        return List.of();
    }
}