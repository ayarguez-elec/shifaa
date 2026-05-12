package services;

import utils.MyDataBase;
import java.sql.*;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         ANALYTICS SERVICE  –  API 3                             ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Handles complex queries for events, participants, and bookings. ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class AnalyticsServ {

    private final Connection cnx = MyDataBase.getInstance().getConnection();

    // ── 1. EVENT STATISTICS ──────────────────────────────────────────────────

    public Map<String, Object> getEventStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            // Total events
            stats.put("total_evenements", getCount("SELECT COUNT(*) FROM event"));
            
            // Waitlist global
            stats.put("total_en_liste_attente", getCount("SELECT COUNT(*) FROM reservation WHERE statut = 'LISTE_D_ATTENTE'"));

            // Status breakdown
            stats.put("actifs", getCount("SELECT COUNT(*) FROM event WHERE status IN ('Planifié', 'En cours')"));
            stats.put("termines", getCount("SELECT COUNT(*) FROM event WHERE status = 'Terminé'"));
            stats.put("annules", getCount("SELECT COUNT(*) FROM event WHERE status = 'Annulé'"));

            // Most popular events (by confirmed places)
            List<Map<String, Object>> popular = new ArrayList<>();
            String sql = "SELECT e.titre, SUM(r.nombre_places) as total_places " +
                         "FROM event e JOIN reservation r ON e.id = r.event_id " +
                         "WHERE r.statut = 'CONFIRME' " +
                         "GROUP BY e.id ORDER BY total_places DESC LIMIT 5";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("titre", rs.getString("titre"));
                    item.put("places", rs.getInt("total_places"));
                    popular.add(item);
                }
            }
            stats.put("plus_populaires", popular);

        } catch (SQLException e) {
            System.err.println("❌ getEventStats : " + e.getMessage());
        }
        return stats;
    }

    // ── 2. PARTICIPANT STATISTICS ────────────────────────────────────────────

    public Map<String, Object> getParticipantStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            stats.put("total_participants", getCount("SELECT COUNT(*) FROM participant"));
            
            // Average places per reservation
            String avgSql = "SELECT AVG(nombre_places) FROM reservation WHERE statut = 'CONFIRME'";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(avgSql)) {
                if (rs.next()) stats.put("moyenne_places_par_resa", Math.round(rs.getDouble(1) * 100.0) / 100.0);
            }

            // Accept/Refuse rates + raw counts for charts
            int total = getCount("SELECT COUNT(*) FROM reservation");
            stats.put("total_reservations", total);
            if (total > 0) {
                int conf    = getCount("SELECT COUNT(*) FROM reservation WHERE statut IN ('CONFIRME', 'CHECKED_IN')");
                int ref     = getCount("SELECT COUNT(*) FROM reservation WHERE statut = 'REFUSE'");
                int wait    = getCount("SELECT COUNT(*) FROM reservation WHERE statut = 'LISTE_D_ATTENTE'");
                int annule  = getCount("SELECT COUNT(*) FROM reservation WHERE statut = 'ANNULE'");
                int checked = getCount("SELECT COUNT(*) FROM reservation WHERE statut = 'CHECKED_IN'");

                // Raw counts (used directly by charts)
                stats.put("count_confirme",      conf);
                stats.put("count_refuse",         ref);
                stats.put("count_attente",        wait);
                stats.put("count_annule",         annule);
                stats.put("count_checkin",        checked);

                stats.put("taux_acceptation",     Math.round((double) conf  / total * 1000.0) / 10.0 + "%");
                stats.put("taux_refus",           Math.round((double) ref   / total * 1000.0) / 10.0 + "%");
                stats.put("taux_liste_attente",   Math.round((double) wait  / total * 1000.0) / 10.0 + "%");

                if (conf > 0) {
                    stats.put("taux_presence_checkin",
                        Math.round((double) checked / conf * 1000.0) / 10.0 + "%");
                }
            } else {
                // Zero defaults so charts don't crash on empty DB
                stats.put("count_confirme", 0);
                stats.put("count_refuse",    0);
                stats.put("count_attente",   0);
                stats.put("count_annule",    0);
                stats.put("count_checkin",   0);
            }

            // Most active participants
            List<Map<String, Object>> active = new ArrayList<>();
            String topPartSql = "SELECT p.nom, p.prenom, COUNT(r.id) as nb_resa " +
                                "FROM participant p JOIN reservation r ON p.id = r.participant_id " +
                                "GROUP BY p.id ORDER BY nb_resa DESC LIMIT 5";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(topPartSql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("nom", rs.getString("nom") + " " + rs.getString("prenom"));
                    item.put("reservations", rs.getInt("nb_resa"));
                    active.add(item);
                }
            }
            stats.put("plus_actifs", active);

        } catch (SQLException e) {
            System.err.println("❌ getParticipantStats : " + e.getMessage());
        }
        return stats;
    }

    // ── 3. SEAT STATISTICS ───────────────────────────────────────────────────

    public Map<String, Object> getSeatStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            String sql = "SELECT SUM(capacite_max) as total_cap, " +
                         "(SELECT SUM(nombre_places) FROM reservation WHERE statut = 'CONFIRME') as total_pris " +
                         "FROM event";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    int cap = rs.getInt("total_cap");
                    int pris = rs.getInt("total_pris");
                    stats.put("capacite_globale", cap);
                    stats.put("places_occupées", pris);
                    stats.put("places_disponibles", cap - pris);
                    if (cap > 0) {
                        stats.put("saturation_globale", Math.round((double) pris / cap * 1000.0) / 10.0 + "%");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getSeatStats : " + e.getMessage());
        }
        return stats;
    }

    // ── 4. TEMPORAL ANALYSIS ──────────────────────────────────────────────────

    public Map<String, Object> getTemporalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            // Reservations per day (last 7 days)
            List<Map<String, Object>> daily = new ArrayList<>();
            String sql = "SELECT DATE(date_demande) as jour, COUNT(*) as nb " +
                         "FROM reservation " +
                         "WHERE date_demande >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                         "GROUP BY jour ORDER BY jour ASC";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", rs.getString("jour"));
                    item.put("count", rs.getInt("nb"));
                    daily.add(item);
                }
            }
            stats.put("reservations_7_derniers_jours", daily);

            // Peak hours
            List<Map<String, Object>> peak = new ArrayList<>();
            String peakSql = "SELECT HOUR(date_demande) as heure, COUNT(*) as nb " +
                             "FROM reservation " +
                             "GROUP BY heure ORDER BY nb DESC LIMIT 3";
            try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(peakSql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("heure", rs.getInt("heure") + "h");
                    item.put("count", rs.getInt("nb"));
                    peak.add(item);
                }
            }
            stats.put("pics_horaires", peak);

        } catch (SQLException e) {
            System.err.println("❌ getTemporalStats : " + e.getMessage());
        }
        return stats;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private int getCount(String sql) throws SQLException {
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
