package services;

import entities.Reclamation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entities.Attachment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ReclamationServices {

    private Connection cnx;

    public ReclamationServices() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Reclamation r) {
        if (r.getCategory() == null || r.getCategory().trim().isEmpty()) {
            String suggested = suggestCategory(r.getSubject(), r.getDescription());
            r.setCategory(suggested);
        }

        String req = "INSERT INTO reclamation "
                + "(subject, description, reclamDate, statusreclam, priority, userId, category, lastResponseDate, isEscalated) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, FALSE)";

        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getSubject());
            ps.setString(2, r.getDescription());
            ps.setDate(3, r.getReclamDate());
            ps.setString(4, r.getStatusreclam() != null ? r.getStatusreclam() : "En attente");
            ps.setString(5, r.getPriority()     != null ? r.getPriority()     : "Moyenne");
            ps.setInt(6, r.getUserId());
            ps.setString(7, r.getCategory());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) r.setReclamationId(rs.getInt(1));

            System.out.println("✅ Reclamation added! ID=" + r.getReclamationId());
        } catch (SQLException e) {
            System.out.println("Erreur ajout réclamation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(Reclamation r) {
        String req = "UPDATE reclamation SET subject=?, description=?, reclamDate=?, statusreclam=?, "
                + "priority=?, userId=?, category=? WHERE reclamationId=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, r.getSubject());
            ps.setString(2, r.getDescription());
            ps.setDate(3, r.getReclamDate());
            ps.setString(4, r.getStatusreclam());
            ps.setString(5, r.getPriority());
            ps.setInt(6, r.getUserId());
            ps.setString(7, r.getCategory());
            ps.setInt(8, r.getReclamationId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur modification réclamation : " + e.getMessage());
        }
    }

    public void updateStatus(int reclamationId, String statusreclam) {
        String req = "UPDATE reclamation SET statusreclam=? WHERE reclamationId=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, statusreclam);
            ps.setInt(2, reclamationId);
            ps.executeUpdate();
            System.out.println("✅ Status → '" + statusreclam + "' for #" + reclamationId);
        } catch (SQLException e) {
            System.out.println("Erreur modification statut : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            Reclamation r = getById(reclamationId);
            if (r != null) {
                NotificationService.triggerStatutChange(
                        reclamationId, r.getUserId(), r.getSubject(), statusreclam);
                if ("Résolu".equalsIgnoreCase(statusreclam) || "resolved".equalsIgnoreCase(statusreclam)) {
                    new EscalationService().resolveEscalation(reclamationId);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Notification/Escalation cleanup (non-critical): " + e.getMessage());
        }
    }

    public void delete(int reclamationId) {
        for (String tbl : new String[]{"reponse", "escalation", "notification"}) {
            try (PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM " + tbl + " WHERE reclamationId=?")) {
                ps.setInt(1, reclamationId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("⚠️ Could not delete from " + tbl + ": " + e.getMessage());
            }
        }
        try (PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM reclamation WHERE reclamationId=?")) {
            ps.setInt(1, reclamationId);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Reclamation #" + reclamationId + " deleted."
                    : "⚠️ No reclamation #" + reclamationId);
        } catch (SQLException e) {
            System.out.println("Erreur suppression réclamation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reclamation")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getAll : " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Reclamation> getByUser(int userId) {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation WHERE userId=? ORDER BY reclamationId DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = mapRow(rs);
                try { r.setEscalated(rs.getBoolean("isEscalated")); } catch (Exception ignored) {}
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getByUser : " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Reclamation getById(int reclamationId) {
        String req = "SELECT * FROM reclamation WHERE reclamationId=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Erreur getById : " + e.getMessage());
        }
        return null;
    }

    // ─── DASHBOARD ANALYTICS ─────────────────────────────────────────────────

    public long getTotalReclamations() {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reclamation")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getReclamationsByStatus(String status) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM reclamation WHERE LOWER(statusreclam)=LOWER(?)")) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getReclamationsByPriority(String priority) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM reclamation WHERE LOWER(priority)=LOWER(?)")) {
            ps.setString(1, priority);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getAverageResponseTime() {
        String req = "SELECT AVG(DATEDIFF(lastResponseDate, reclamDate)) "
                + "FROM reclamation WHERE lastResponseDate IS NOT NULL AND reclamDate IS NOT NULL";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Erreur avg response time : " + e.getMessage());
        }
        return 0.0;
    }

    public long getUrgentesNonRepondues() {
        // ── FIX: use NOT EXISTS instead of NOT IN to avoid NULL-poisoning ──
        String req = """
                SELECT COUNT(*) FROM reclamation r
                WHERE LOWER(r.priority) = 'urgente'
                  AND NOT EXISTS (
                      SELECT 1 FROM reponse rep
                      WHERE rep.reclamationId = r.reclamationId
                  )
                """;
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.out.println("⚠️ getUrgentesNonRepondues: " + e.getMessage());
        }
        return 0;
    }

    public List<String> getMostCommonCategories(int limit) {
        List<String> list = new ArrayList<>();
        String req = "SELECT category, COUNT(*) as cnt FROM reclamation "
                + "WHERE category IS NOT NULL GROUP BY category ORDER BY cnt DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(rs.getString("category") + " (" + rs.getInt("cnt") + ")");
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public String suggestCategory(String subject, String description) {
        String text = (subject + " " + description).toLowerCase().trim();
        if (text.contains("rendez") || text.contains("consult") || text.contains("médecin")
                || text.contains("rdv") || text.contains("appointment")) return "Rendez-vous";
        if (text.contains("facture") || text.contains("paiement") || text.contains("prix")
                || text.contains("tarif") || text.contains("argent"))    return "Facturation";
        if (text.contains("médicament") || text.contains("ordonnance")
                || text.contains("pharmacie"))                            return "Médicaments";
        if (text.contains("urgence") || text.contains("grave")
                || text.contains("doul") || text.contains("hospital"))   return "Urgence";
        return "Autre";
    }

    public List<Reclamation> getRecentReclamations(int days) {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation "
                + "WHERE reclamDate >= DATE_SUB(NOW(), INTERVAL ? DAY) "
                + "ORDER BY reclamDate DESC LIMIT 20";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Returns escalated reclamations (manual OR auto-SLA breach).
     *
     * ── FIX: replaced NOT IN with NOT EXISTS ──────────────────────────────
     * NOT IN returns no rows when the subquery contains even one NULL,
     * causing escaladed reclamations to silently disappear.
     * NOT EXISTS is NULL-safe and is also faster with an index on reclamationId.
     */
    public List<Reclamation> getEscalatedReclamations(int slaThresholdDays) {
        List<Reclamation> list = new ArrayList<>();

        // 1. Manually escalated (entry exists in escalation table, still open)
        String manualSql = """
                SELECT r.* FROM reclamation r
                INNER JOIN escalation e ON e.reclamationId = r.reclamationId
                WHERE e.status = 'En attente'
                ORDER BY e.createdAt ASC
                """;
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(manualSql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("⚠️ Manual escalation query failed: " + e.getMessage());
        }

        // 2. Auto-SLA breach: urgent, no response yet, older than threshold days
        //    ── KEY FIX: NOT EXISTS instead of NOT IN ──
        String autoSql = """
                SELECT r.* FROM reclamation r
                WHERE LOWER(r.priority) = 'urgente'
                  AND LOWER(r.statusreclam) IN ('en attente', 'en cours')
                  AND DATEDIFF(NOW(), r.reclamDate) > ?
                  AND NOT EXISTS (
                      SELECT 1 FROM reponse rep
                      WHERE rep.reclamationId = r.reclamationId
                  )
                ORDER BY r.reclamDate ASC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(autoSql)) {
            ps.setInt(1, slaThresholdDays);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation rec = mapRow(rs);
                boolean duplicate = list.stream()
                        .anyMatch(x -> x.getReclamationId() == rec.getReclamationId());
                if (!duplicate) list.add(rec);
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Auto-escalation query failed: " + e.getMessage());
        }

        return list;
    }

    public java.util.Map<String, Integer> getDailyTrend(int days) {
        java.util.LinkedHashMap<String, Integer> map = new java.util.LinkedHashMap<>();
        String req = """
                SELECT DATE(reclamDate) as day, COUNT(*) as cnt
                FROM reclamation
                WHERE reclamDate >= DATE_SUB(NOW(), INTERVAL ? DAY)
                GROUP BY day ORDER BY day ASC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("day"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    private Reclamation mapRow(ResultSet rs) throws SQLException {
        Timestamp lastResp = null;
        try { lastResp = rs.getTimestamp("lastResponseDate"); }
        catch (SQLException e1) {
            try { lastResp = rs.getTimestamp("last_response_date"); }
            catch (SQLException e2) { /* column absent */ }
        }
        return new Reclamation(
                rs.getInt("reclamationId"),
                rs.getString("subject"),
                rs.getString("description"),
                rs.getDate("reclamDate"),
                rs.getString("statusreclam"),
                rs.getString("priority"),
                rs.getInt("userId"),
                rs.getString("category"),
                lastResp
        );
    }
    // ====================== ATTACHMENT METHODS ======================

    public void addAttachments(int reclamationId, List<File> files) {
        if (files == null || files.isEmpty()) return;

        String baseDir = "attachments/reclamations/";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String sql = "INSERT INTO attachment (reclamationId, fileName, filePath, fileType, fileSize) " +
                "VALUES (?, ?, ?, ?, ?)";

        for (File file : files) {
            try {
                String uniqueName = reclamationId + "_" + System.currentTimeMillis() + "_" + file.getName();
                String relativePath = baseDir + uniqueName;

                Files.copy(file.toPath(), Paths.get(relativePath), StandardCopyOption.REPLACE_EXISTING);

                String fileType = file.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|webp)$")
                        ? "image" : "document";

                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setInt(1, reclamationId);
                    ps.setString(2, file.getName());
                    ps.setString(3, relativePath);
                    ps.setString(4, fileType);
                    ps.setLong(5, file.length());
                    ps.executeUpdate();
                }

                System.out.println("✅ Attachment saved: " + file.getName());

            } catch (Exception e) {
                System.err.println("❌ Failed to save attachment " + file.getName() + " : " + e.getMessage());
            }
        }
    }

    public List<Attachment> getAttachments(int reclamationId) {
        List<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM attachment WHERE reclamationId = ? ORDER BY uploadedAt ASC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Attachment att = new Attachment();
                att.setId(rs.getInt("id"));
                att.setReclamationId(rs.getInt("reclamationId"));
                att.setFileName(rs.getString("fileName"));
                att.setFilePath(rs.getString("filePath"));
                att.setFileType(rs.getString("fileType"));
                att.setFileSize(rs.getLong("fileSize"));
                try {
                    att.setUploadedAt(rs.getTimestamp("uploadedAt").toLocalDateTime());
                } catch (Exception ignored) {}
                attachments.add(att);
            }
        } catch (SQLException e) {
            System.err.println("Error loading attachments for #" + reclamationId + ": " + e.getMessage());
        }
        return attachments;
    }

    public void deleteAttachment(int attachmentId) {
        String sql = "DELETE FROM attachment WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, attachmentId);
            ps.executeUpdate();
            System.out.println("🗑 Attachment deleted: " + attachmentId);
        } catch (SQLException e) {
            System.err.println("Error deleting attachment: " + e.getMessage());
        }
    }
}