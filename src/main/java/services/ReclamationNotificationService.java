package services;

import entities.ReclamationNotification;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService
 * Handles all notification DB operations
 */
public class ReclamationNotificationService {

    private Connection cnx;

    public ReclamationNotificationService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    // ────────────────────────────────────────────────────────────
    //  WRITE
    // ────────────────────────────────────────────────────────────

    public void add(ReclamationNotification n) {
        String sql = "INSERT INTO reclamation_notification (userId, reclamationId, type, message, isRead) "
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

    public void markAsRead(int notificationId) {
        String sql = "UPDATE reclamation_notification SET isRead = 1 WHERE notificationId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAsRead() error: " + e.getMessage());
        }
    }

    public void markAllAsRead(int userId) {
        String sql = "UPDATE reclamation_notification SET isRead = 1 WHERE userId = ? AND isRead = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAllAsRead() error: " + e.getMessage());
        }
    }

    public void delete(int notificationId) {
        String sql = "DELETE FROM reclamation_notification WHERE notificationId = ?";
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

    public List<ReclamationNotification> getByUser(int userId) {
        List<ReclamationNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation_notification WHERE userId = ? ORDER BY createdAt DESC";
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

    public List<ReclamationNotification> getUnreadByUser(int userId) {
        List<ReclamationNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation_notification WHERE userId = ? AND isRead = 0 ORDER BY createdAt DESC";
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

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM reclamation_notification WHERE userId = ? AND isRead = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] countUnread() error: " + e.getMessage());
        }
        return 0;
    }

    public List<ReclamationNotification> getRecent(int userId, int limit) {
        List<ReclamationNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation_notification WHERE userId = ? ORDER BY createdAt DESC LIMIT ?";
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
    //  STATIC TRIGGER HELPERS (inchangés)
    // ────────────────────────────────────────────────────────────

    public static void triggerReponseAjoutee(int reclamationId, int ownerUserId, String subject) {
        try {
            ReclamationNotificationService svc = new ReclamationNotificationService();
            String message = "Une réponse a été ajoutée à votre réclamation : \"" + subject + "\".";
            ReclamationNotification n = new ReclamationNotification(ownerUserId, reclamationId, "REPONSE_AJOUTEE", message);
            svc.add(n);
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerReponseAjoutee() error: " + e.getMessage());
        }
    }

    public static void triggerStatutChange(int reclamationId, int ownerUserId, String subject, String newStatus) {
        try {
            ReclamationNotificationService svc = new ReclamationNotificationService();
            String message = "Le statut de votre réclamation \"" + subject + "\" est passé à : " + newStatus + ".";
            String type = "Résolu".equalsIgnoreCase(newStatus) || "resolved".equalsIgnoreCase(newStatus)
                    ? "RESOLUTION" : "STATUT_CHANGE";
            ReclamationNotification n = new ReclamationNotification(ownerUserId, reclamationId, type, message);
            svc.add(n);
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerStatutChange() error: " + e.getMessage());
        }
    }

    public static void triggerEscaladeAuto(int reclamationId, String subject, String ownerName) {
        try {
            ReclamationNotificationService svc = new ReclamationNotificationService();
            String msgAdmin = "Escalade automatique : la réclamation \"" + subject
                    + "\" de " + ownerName + " est urgente et sans réponse depuis plus de 3 jours.";
            svc.add(new ReclamationNotification(1, reclamationId, "ESCALADE_AUTO", msgAdmin));
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerEscaladeAuto() error: " + e.getMessage());
        }
    }

    public static void triggerEscaladeManuelle(int reclamationId, int ownerUserId, String subject, String reason) {
        try {
            ReclamationNotificationService svc = new ReclamationNotificationService();
            String msgAdmin = "Escalade manuelle demandée pour \"" + subject + "\" — Raison : " + reason;
            svc.add(new ReclamationNotification(1, reclamationId, "ESCALADE_MANUELLE", msgAdmin));

            String msgUser = "Votre demande d'escalade pour \"" + subject + "\" a bien été envoyée à l'administrateur.";
            svc.add(new ReclamationNotification(ownerUserId, reclamationId, "ESCALADE_MANUELLE", msgUser));
        } catch (Exception e) {
            System.err.println("[NotificationService] triggerEscaladeManuelle() error: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    //  PRIVATE HELPER
    // ────────────────────────────────────────────────────────────

    private ReclamationNotification mapRow(ResultSet rs) throws SQLException {
        ReclamationNotification n = new ReclamationNotification();
        n.setNotificationId(rs.getInt("notificationId"));
        n.setUserId(rs.getInt("userId"));
        n.setReclamationId(rs.getInt("reclamationId"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("isRead") == 1);
        n.setCreatedAt(rs.getTimestamp("createdAt"));
        return n;
    }
}
