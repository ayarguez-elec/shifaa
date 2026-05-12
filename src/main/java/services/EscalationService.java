package services;

import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXED EscalationService — works with or without Escalation entity class
 * Uses raw SQL and returns simple data structures
 */
public class EscalationService {

    private Connection cnx;

    public EscalationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(int reclamationId, int userId, String reason) {
        String req = "INSERT INTO escalation (reclamationId, userId, reason, status, createdAt) VALUES (?, ?, ?, 'En attente', NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reclamationId);
            ps.setInt(2, userId);
            ps.setString(3, reason);
            ps.executeUpdate();

            // Mark reclamation as escalated
            String markEsc = "UPDATE reclamation SET isEscalated = TRUE WHERE reclamationId = ?";
            try (PreparedStatement psMark = cnx.prepareStatement(markEsc)) {
                psMark.setInt(1, reclamationId);
                psMark.executeUpdate();
            }

            System.out.println("✅ Escalation added for reclamation #" + reclamationId);
        } catch (SQLException ex) {
            System.out.println("Erreur ajout escalation : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean hasOpenEscalation(int reclamationId) {
        String req = "SELECT COUNT(*) FROM escalation WHERE reclamationId = ? AND status = 'En attente'";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur vérification escalation : " + e.getMessage());
        }
        return false;
    }

    public int resolveEscalation(int reclamationId) {
        String req = "UPDATE escalation SET status = 'Résolu', resolvedAt = NOW() WHERE reclamationId = ? AND status = 'En attente'";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, reclamationId);
            ps.executeUpdate();

            String unmark = "UPDATE reclamation SET isEscalated = FALSE WHERE reclamationId = ?";
            try (PreparedStatement psUnmark = cnx.prepareStatement(unmark)) {
                psUnmark.setInt(1, reclamationId);
                psUnmark.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erreur résolution escalation : " + e.getMessage());
        }
        return reclamationId;
    }

    public List<java.util.Map<String, Object>> getAllOpen() {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String req = "SELECT e.*, r.subject, r.priority, r.statusreclam FROM escalation e " +
                "JOIN reclamation r ON r.reclamationId = e.reclamationId " +
                "WHERE e.status = 'En attente' ORDER BY e.createdAt ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("escalationId", rs.getInt("escalationId"));
                map.put("reclamationId", rs.getInt("reclamationId"));
                map.put("userId", rs.getInt("userId"));
                map.put("reason", rs.getString("reason"));
                map.put("subject", rs.getString("subject"));
                map.put("priority", rs.getString("priority"));
                map.put("statusreclam", rs.getString("statusreclam"));
                map.put("createdAt", rs.getTimestamp("createdAt"));
                list.add(map);
            }
        } catch (SQLException e) {
            System.out.println("Erreur chargement escalations : " + e.getMessage());
        }
        return list;
    }
}