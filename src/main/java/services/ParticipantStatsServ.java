package services;

import utils.MyDataBase;

import java.sql.*;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  ParticipantStatsServ — Analyse Comportementale                 ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Fournit des statistiques avancées sur le comportement          ║
 * ║  des participants : fidélité, taux de présence, annulations,    ║
 * ║  événements favoris, fréquence de participation.               ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class ParticipantStatsServ {

    private final Connection cnx = MyDataBase.getInstance().getConnection();

    // ── 1. Statistiques individuelles d'un participant ────────────────────────

    public Map<String, Object> getProfileStats(int participantId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql =
            "SELECT " +
            "  COUNT(*)                                                  AS total_resa,     " +
            "  SUM(statut IN ('CONFIRME','CHECKED_IN'))                  AS nb_confirme,    " +
            "  SUM(statut = 'REFUSE')                                    AS nb_refuse,      " +
            "  SUM(statut = 'ANNULE')                                    AS nb_annule,      " +
            "  SUM(statut = 'LISTE_D_ATTENTE')                           AS nb_attente,     " +
            "  SUM(statut = 'CHECKED_IN')                                AS nb_checkin,     " +
            "  COALESCE(SUM(nombre_places), 0)                           AS total_places,   " +
            "  MIN(date_demande)                                         AS premiere_resa,  " +
            "  MAX(date_demande)                                         AS derniere_resa   " +
            "FROM reservation WHERE participant_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total    = rs.getInt("total_resa");
                int conf     = rs.getInt("nb_confirme");
                int refuse   = rs.getInt("nb_refuse");
                int annule   = rs.getInt("nb_annule");
                int attente  = rs.getInt("nb_attente");
                int checkin  = rs.getInt("nb_checkin");
                int places   = rs.getInt("total_places");

                stats.put("total_reservations",   total);
                stats.put("nb_confirmees",         conf);
                stats.put("nb_refusees",           refuse);
                stats.put("nb_annulees",           annule);
                stats.put("nb_liste_attente",      attente);
                stats.put("nb_presences_reelles",  checkin);
                stats.put("total_places_reservees",places);
                stats.put("premiere_reservation",  rs.getString("premiere_resa"));
                stats.put("derniere_reservation",  rs.getString("derniere_resa"));

                // Taux calculés
                if (total > 0) {
                    stats.put("taux_confirmation", pct(conf,   total));
                    stats.put("taux_annulation",   pct(annule, total));
                    stats.put("taux_refus",        pct(refuse, total));
                }
                if (conf > 0) {
                    stats.put("taux_presence_reelle", pct(checkin, conf));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getProfileStats : " + e.getMessage());
        }

        // Événements favoris (type d'événements le plus fréquenté)
        stats.put("categories_favorites", getFavoriteCategories(participantId));
        return stats;
    }

    // ── 2. Événements favoris d'un participant ────────────────────────────────

    public List<Map<String, Object>> getFavoriteCategories(int participantId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT e.type, COUNT(*) AS nb " +
            "FROM reservation r JOIN event e ON r.event_id = e.id " +
            "WHERE r.participant_id = ? AND r.statut IN ('CONFIRME','CHECKED_IN') " +
            "GROUP BY e.type ORDER BY nb DESC LIMIT 3";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("type", rs.getString("type"));
                m.put("nb_participations", rs.getInt("nb"));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ getFavoriteCategories : " + e.getMessage());
        }
        return result;
    }

    // ── 3. Top participants actifs ────────────────────────────────────────────

    public List<Map<String, Object>> getTopActifs(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT p.id, p.nom, p.prenom, p.email, p.badge, p.priority_level, " +
            "       p.loyalty_points, COUNT(r.id) AS nb_resa, " +
            "       SUM(r.statut IN ('CONFIRME','CHECKED_IN')) AS nb_conf " +
            "FROM participant p " +
            "JOIN reservation r ON p.id = r.participant_id " +
            "GROUP BY p.id ORDER BY nb_conf DESC, p.loyalty_points DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapParticipantRow(rs));
        } catch (SQLException e) {
            System.err.println("❌ getTopActifs : " + e.getMessage());
        }
        return result;
    }

    // ── 4. Participants VIP ───────────────────────────────────────────────────

    public List<Map<String, Object>> getVipParticipants() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT p.id, p.nom, p.prenom, p.email, p.badge, p.priority_level, " +
            "       p.loyalty_points, p.attendance_count " +
            "FROM participant p WHERE p.priority_level = 'VIP' " +
            "ORDER BY p.loyalty_points DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.add(mapParticipantRow(rs));
        } catch (SQLException e) {
            System.err.println("❌ getVipParticipants : " + e.getMessage());
        }
        return result;
    }

    // ── 5. Participants les plus fidèles ──────────────────────────────────────

    public List<Map<String, Object>> getMostLoyal(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT p.id, p.nom, p.prenom, p.email, p.badge, " +
            "       p.priority_level, p.loyalty_points " +
            "FROM participant p ORDER BY p.loyalty_points DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapParticipantRow(rs));
        } catch (SQLException e) {
            System.err.println("❌ getMostLoyal : " + e.getMessage());
        }
        return result;
    }

    // ── 6. Participants avec le plus d'annulations ────────────────────────────

    public List<Map<String, Object>> getMostCancelled(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT p.id, p.nom, p.prenom, p.email, p.badge, p.priority_level, " +
            "       p.cancellation_count AS nb_annulations " +
            "FROM participant p WHERE p.cancellation_count > 0 " +
            "ORDER BY p.cancellation_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = mapParticipantRow(rs);
                m.put("nb_annulations", rs.getInt("nb_annulations"));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ getMostCancelled : " + e.getMessage());
        }
        return result;
    }

    // ── 7. Statistiques globales tous participants ────────────────────────────

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            stats.put("total_participants", getCount("SELECT COUNT(*) FROM participant"));
            stats.put("vip_count",   getCount("SELECT COUNT(*) FROM participant WHERE priority_level='VIP'"));
            stats.put("high_count",  getCount("SELECT COUNT(*) FROM participant WHERE priority_level='HIGH'"));
            stats.put("gold_count",  getCount("SELECT COUNT(*) FROM participant WHERE badge='Gold'"));
            stats.put("silver_count",getCount("SELECT COUNT(*) FROM participant WHERE badge='Silver'"));

            String avgSql = "SELECT AVG(loyalty_points), MAX(loyalty_points) FROM participant";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(avgSql)) {
                if (rs.next()) {
                    stats.put("moyenne_points", Math.round(rs.getDouble(1) * 10.0) / 10.0);
                    stats.put("max_points",     rs.getInt(2));
                }
            }

            // Distribution par badge
            String distSql = "SELECT badge, COUNT(*) AS nb FROM participant GROUP BY badge";
            Map<String, Integer> dist = new LinkedHashMap<>();
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(distSql)) {
                while (rs.next()) dist.put(rs.getString("badge"), rs.getInt("nb"));
            }
            stats.put("distribution_badges", dist);

        } catch (SQLException e) {
            System.err.println("❌ getGlobalStats : " + e.getMessage());
        }
        return stats;
    }

    // ── 8. Notifications d'un participant ─────────────────────────────────────

    public List<Map<String, Object>> getNotifications(int participantId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT * FROM participant_notifications " +
                     "WHERE participant_id=? ORDER BY sent_at DESC LIMIT 20";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",      rs.getInt("id"));
                m.put("type",    rs.getString("type"));
                m.put("sujet",   rs.getString("sujet"));
                m.put("message", rs.getString("message"));
                m.put("sent_at", rs.getString("sent_at"));
                m.put("is_read", rs.getBoolean("is_read"));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ getNotifications : " + e.getMessage());
        }
        return result;
    }

    // ── 9. Sauvegarder une notification ──────────────────────────────────────

    public void saveNotification(int participantId, String type, String sujet, String message) {
        String sql = "INSERT INTO participant_notifications (participant_id, type, sujet, message) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setString(2, type);
            ps.setString(3, sujet);
            ps.setString(4, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ saveNotification : " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int getCount(String sql) throws SQLException {
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String pct(int part, int total) {
        return Math.round(part * 100.0 / total * 10.0) / 10.0 + "%";
    }

    private Map<String, Object> mapParticipantRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",             rs.getInt("id"));
        m.put("nom",            rs.getString("nom") + " " + rs.getString("prenom"));
        m.put("email",          rs.getString("email"));
        m.put("badge",          safeGet(rs, "badge",          "Bronze"));
        m.put("priority_level", safeGet(rs, "priority_level", "LOW"));
        m.put("loyalty_points", safeGetInt(rs, "loyalty_points", 0));
        return m;
    }

    private String safeGet(ResultSet rs, String col, String def) {
        try { return rs.getString(col); } catch (SQLException e) { return def; }
    }

    private int safeGetInt(ResultSet rs, String col, int def) {
        try { return rs.getInt(col); } catch (SQLException e) { return def; }
    }
}
