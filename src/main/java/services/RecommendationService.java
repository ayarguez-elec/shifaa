package services;

import api.ApiUtils;
import entities.event;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent Health Event Recommendation Engine
 *
 * Score = CategoryAffinity(0-40) + Popularity(0-25) + Availability(0-15)
 *       + Recency(0-10) + Collaborative(0-10)
 * Max theoretical score = 100
 */
public class RecommendationService {

    private final Connection cnx;

    public RecommendationService() {
        this.cnx = MyDataBase.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════════════════════════════════════

    /** Personalized recommendations for one participant. */
    public List<RecommendedEvent> getPersonalized(int participantId, int limit) {
        Map<String, Double> affinity   = computeCategoryAffinity(participantId);
        Set<Integer>        reserved   = getReservedEventIds(participantId);
        Map<Integer, Integer> collab   = computeCollaborative(participantId);

        List<RecommendedEvent> scored = new ArrayList<>();
        for (event ev : getUpcomingEvents()) {
            if (reserved.contains(ev.getId())) continue;
            double score  = scoreEvent(ev, affinity, collab);
            String reason = buildReason(ev, affinity, collab);
            scored.add(new RecommendedEvent(ev, score, reason));
        }

        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<RecommendedEvent> top = scored.stream().limit(limit).collect(Collectors.toList());
        persistRecommendations(participantId, top);
        return top;
    }

    /** Events with the most reservations recently (trending). */
    public List<Map<String, Object>> getTrending(int limit) {
        String sql =
            "SELECT e.id, e.titre, e.type, e.date, e.lieu, e.status, e.capacite_max," +
            " COUNT(r.id) AS recent_count" +
            " FROM event e" +
            " JOIN reservation r ON e.id = r.event_id" +
            " WHERE r.statut IN ('EN_ATTENTE','CONFIRME','CHECKED_IN','LISTE_D_ATTENTE')" +
            "   AND e.date >= CURDATE()" +
            " GROUP BY e.id ORDER BY recent_count DESC LIMIT ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                int confirmed = rs.getInt("recent_count");
                int cap       = rs.getInt("capacite_max");
                m.put("rank",               rank++);
                m.put("eventId",            rs.getInt("id"));
                m.put("title",              rs.getString("titre"));
                m.put("type",               rs.getString("type"));
                m.put("date",               safeDate(rs, "date"));
                m.put("lieu",               rs.getString("lieu"));
                m.put("status",             rs.getString("status"));
                m.put("recentReservations", confirmed);
                m.put("availableSeats",     Math.max(0, cap - confirmed));
                m.put("trendScore",         Math.round(Math.min(confirmed / 10.0, 1.0) * 100));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] trending: " + e.getMessage());
        }
        return result;
    }

    /** Events with the most confirmed participants (popular). */
    public List<Map<String, Object>> getPopular(int limit) {
        String sql =
            "SELECT e.id, e.titre, e.type, e.date, e.lieu, e.status, e.capacite_max," +
            " COUNT(r.id) AS confirmed_count," +
            " ROUND(COUNT(r.id) * 100.0 / NULLIF(e.capacite_max,0), 1) AS fill_rate" +
            " FROM event e" +
            " LEFT JOIN reservation r ON e.id = r.event_id AND r.statut IN ('CONFIRME','CHECKED_IN')" +
            " GROUP BY e.id ORDER BY confirmed_count DESC LIMIT ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                int    conf     = rs.getInt("confirmed_count");
                double fillRate = rs.getDouble("fill_rate");
                m.put("rank",                 rank++);
                m.put("eventId",              rs.getInt("id"));
                m.put("title",                rs.getString("titre"));
                m.put("type",                 rs.getString("type"));
                m.put("date",                 safeDate(rs, "date"));
                m.put("lieu",                 rs.getString("lieu"));
                m.put("confirmedParticipants",conf);
                m.put("fillRate",             fillRate + "%");
                m.put("popularityScore",      Math.round(fillRate * 0.6 + conf * 2.0));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] popular: " + e.getMessage());
        }
        return result;
    }

    /** Upcoming events filtered by type/category. */
    public List<Map<String, Object>> getByCategory(String category, int limit) {
        String sql =
            "SELECT e.id, e.titre, e.type, e.date, e.lieu, e.status, e.capacite_max," +
            " COUNT(r.id) AS total_res" +
            " FROM event e" +
            " LEFT JOIN reservation r ON e.id = r.event_id AND r.statut IN ('CONFIRME','CHECKED_IN')" +
            " WHERE e.type = ? AND e.date >= CURDATE()" +
            " GROUP BY e.id ORDER BY e.date ASC LIMIT ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                int conf = rs.getInt("total_res");
                int cap  = rs.getInt("capacite_max");
                m.put("eventId",              rs.getInt("id"));
                m.put("title",                rs.getString("titre"));
                m.put("type",                 rs.getString("type"));
                m.put("date",                 safeDate(rs, "date"));
                m.put("lieu",                 rs.getString("lieu"));
                m.put("status",               rs.getString("status"));
                m.put("confirmedParticipants",conf);
                m.put("availableSeats",        Math.max(0, cap - conf));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] byCategory: " + e.getMessage());
        }
        return result;
    }

    /** Recommendation history for a participant. */
    public List<Map<String, Object>> getHistory(int participantId, int limit) {
        String sql =
            "SELECT rh.id, rh.event_id, e.titre, e.type, e.date," +
            " rh.score, rh.reason, rh.recommended_at, rh.was_clicked, rh.was_reserved" +
            " FROM recommendation_history rh" +
            " JOIN event e ON rh.event_id = e.id" +
            " WHERE rh.participant_id = ?" +
            " ORDER BY rh.recommended_at DESC LIMIT ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("historyId",      rs.getInt("id"));
                m.put("eventId",        rs.getInt("event_id"));
                m.put("title",          rs.getString("titre"));
                m.put("type",           rs.getString("type"));
                m.put("date",           safeDate(rs, "date"));
                m.put("score",          rs.getDouble("score"));
                m.put("reason",         rs.getString("reason"));
                m.put("recommendedAt",  String.valueOf(rs.getTimestamp("recommended_at")));
                m.put("wasClicked",     rs.getBoolean("was_clicked"));
                m.put("wasReserved",    rs.getBoolean("was_reserved"));
                result.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] history: " + e.getMessage());
        }
        return result;
    }

    /** Global recommendation statistics and analytics. */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (Statement st = cnx.createStatement()) {
            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) FROM recommendation_history");
            int total = rs.next() ? rs.getInt(1) : 0;

            rs = st.executeQuery("SELECT COUNT(*) FROM recommendation_history WHERE was_clicked=1");
            int clicks = rs.next() ? rs.getInt(1) : 0;

            rs = st.executeQuery("SELECT COUNT(*) FROM recommendation_history WHERE was_reserved=1");
            int conversions = rs.next() ? rs.getInt(1) : 0;

            rs = st.executeQuery("SELECT AVG(score) FROM recommendation_history");
            double avgScore = rs.next() ? rs.getDouble(1) : 0;

            rs = st.executeQuery(
                "SELECT e.type, COUNT(*) as cnt FROM recommendation_history rh" +
                " JOIN event e ON rh.event_id = e.id GROUP BY e.type ORDER BY cnt DESC LIMIT 1");
            String topCat = (rs.next()) ? rs.getString("type") : "N/A";

            stats.put("totalRecommendations", total);
            stats.put("totalClicks",          clicks);
            stats.put("totalConversions",     conversions);
            stats.put("avgScore",             Math.round(avgScore * 10.0) / 10.0);
            stats.put("topRecommendedCategory", topCat);
            stats.put("clickRate",       total > 0 ? Math.round(clicks      * 1000.0 / total) / 10.0 + "%" : "0%");
            stats.put("conversionRate",  total > 0 ? Math.round(conversions * 1000.0 / total) / 10.0 + "%" : "0%");

            // Category distribution
            rs = st.executeQuery(
                "SELECT e.type, COUNT(*) as cnt FROM recommendation_history rh" +
                " JOIN event e ON rh.event_id = e.id GROUP BY e.type ORDER BY cnt DESC");
            StringBuilder catJson = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) catJson.append(",");
                catJson.append("{\"category\":\"").append(ApiUtils.escapeJson(rs.getString("type")))
                       .append("\",\"count\":").append(rs.getInt("cnt")).append("}");
                first = false;
            }
            catJson.append("]");
            stats.put("__categoryDistributionJson", catJson.toString()); // raw JSON marker

        } catch (SQLException e) {
            System.err.println("❌ [Reco] stats: " + e.getMessage());
        }
        return stats;
    }

    /** Compute and persist participant's favorite category in participant_preferences. */
    public void refreshPreferences(int participantId) {
        Map<String, Double> affinity = computeCategoryAffinity(participantId);
        if (affinity.isEmpty()) return;

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(affinity.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        String fav    = sorted.get(0).getKey();
        String second = sorted.size() > 1 ? sorted.get(1).getKey() : null;
        int total     = getParticipantReservationCount(participantId);

        String sql =
            "INSERT INTO participant_preferences (participant_id, favorite_category, second_category, interaction_count, last_computed)" +
            " VALUES (?,?,?,?,NOW())" +
            " ON DUPLICATE KEY UPDATE favorite_category=VALUES(favorite_category)," +
            "   second_category=VALUES(second_category), interaction_count=VALUES(interaction_count), last_computed=NOW()";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setString(2, fav);
            ps.setString(3, second);
            ps.setInt(4, total);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ [Reco] refreshPrefs: " + e.getMessage());
        }
    }

    /** Mark a recommendation as clicked (for CTR tracking). */
    public void markClicked(int participantId, int eventId) {
        runUpdate(
            "UPDATE recommendation_history SET was_clicked=1" +
            " WHERE participant_id=? AND event_id=? ORDER BY recommended_at DESC LIMIT 1",
            participantId, eventId);
    }

    /** Mark a recommendation as converted (participant actually reserved). */
    public void markReserved(int participantId, int eventId) {
        runUpdate(
            "UPDATE recommendation_history SET was_reserved=1" +
            " WHERE participant_id=? AND event_id=?",
            participantId, eventId);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SCORE ENGINE
    // ══════════════════════════════════════════════════════════════════════

    private double scoreEvent(event ev, Map<String, Double> affinity, Map<Integer, Integer> collab) {
        double score = 0;

        // 1. Category affinity (0–40) — main signal
        score += affinity.getOrDefault(ev.getType(), 0.0) * 40.0;

        // 2. Popularity (0–25)
        int reservedCount = getReservationCount(ev.getId());
        score += Math.min(reservedCount / 10.0, 1.0) * 25.0;

        // 3. Availability (0–15) — penalize near-full events
        int cap = ev.getCapaciteMax();
        double fill = (cap > 0) ? (double) reservedCount / cap : 0.0;
        score += (1.0 - Math.min(fill, 1.0)) * 15.0;

        // 4. Recency (0–10) — upcoming events within 30 days get priority
        if (ev.getDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), ev.getDate());
            if (days >= 0 && days <= 30) {
                score += ((30.0 - days) / 30.0) * 10.0;
            }
        }

        // 5. Collaborative signal (0–10) — similar participants liked this
        int colScore = collab.getOrDefault(ev.getId(), 0);
        score += Math.min(colScore / 5.0, 1.0) * 10.0;

        return Math.round(score * 10.0) / 10.0;
    }

    private String buildReason(event ev, Map<String, Double> affinity, Map<Integer, Integer> collab) {
        String type = ev.getType() != null ? ev.getType() : "";
        double cat  = affinity.getOrDefault(type, 0.0);
        if (cat >= 0.5) return "Parfait pour vos intérêts en " + type;
        if (cat >= 0.2) return "Similaire à vos événements passés (" + type + ")";
        if (collab.getOrDefault(ev.getId(), 0) > 0) return "Apprécié par des participants au profil similaire";
        if (getReservationCount(ev.getId()) > 5)     return "Événement populaire dans votre région";
        return "Recommandé selon votre historique de santé";
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATA HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /** Returns normalized type → weight map (weights sum to 1). */
    private Map<String, Double> computeCategoryAffinity(int participantId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        int total = 0;
        String sql =
            "SELECT e.type, COUNT(*) AS cnt FROM reservation r" +
            " JOIN event e ON r.event_id = e.id" +
            " WHERE r.participant_id = ? AND r.statut IN ('CONFIRME','CHECKED_IN')" +
            " GROUP BY e.type ORDER BY cnt DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                counts.put(rs.getString("type"), cnt);
                total += cnt;
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] affinity: " + e.getMessage());
        }
        Map<String, Double> affinity = new LinkedHashMap<>();
        int t = total;
        counts.forEach((k, v) -> affinity.put(k, t > 0 ? (double) v / t : 0.0));
        return affinity;
    }

    /** Event IDs where participant has an active (non-cancelled) reservation. */
    private Set<Integer> getReservedEventIds(int participantId) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT event_id FROM reservation WHERE participant_id=? AND statut NOT IN ('ANNULE','REFUSE')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) {
            System.err.println("❌ [Reco] reserved: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Finds events liked by participants who share at least one event category
     * with the target participant (user-based collaborative filtering).
     * Returns: eventId → number of similar participants who attended.
     */
    private Map<Integer, Integer> computeCollaborative(int participantId) {
        Map<Integer, Integer> scores = new HashMap<>();
        String sql =
            "SELECT r2.event_id, COUNT(DISTINCT r2.participant_id) AS sim_count" +
            " FROM reservation r1" +
            " JOIN event e1 ON r1.event_id = e1.id" +
            " JOIN event e2 ON e1.type = e2.type" +
            " JOIN reservation r2 ON r2.event_id = e2.id AND r2.participant_id != ?" +
            " WHERE r1.participant_id = ?" +
            "   AND r1.statut IN ('CONFIRME','CHECKED_IN')" +
            "   AND r2.statut IN ('CONFIRME','CHECKED_IN')" +
            " GROUP BY r2.event_id ORDER BY sim_count DESC LIMIT 20";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setInt(2, participantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) scores.put(rs.getInt("event_id"), rs.getInt("sim_count"));
        } catch (SQLException e) {
            System.err.println("❌ [Reco] collaborative: " + e.getMessage());
        }
        return scores;
    }

    private List<event> getUpcomingEvents() {
        List<event> list = new ArrayList<>();
        String sql =
            "SELECT id, titre, type, description, date, time, lieu, status, image_path, capacite_max" +
            " FROM event WHERE date >= CURDATE() AND status != 'Annulé' ORDER BY date ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Date d = rs.getDate("date");
                list.add(new event(
                    rs.getInt("id"),        rs.getString("titre"),
                    rs.getString("type"),   rs.getString("description"),
                    d != null ? d.toLocalDate() : null,
                    rs.getString("time"),   rs.getString("lieu"),
                    rs.getString("status"), rs.getString("image_path"),
                    rs.getInt("capacite_max")));
            }
        } catch (SQLException e) {
            System.err.println("❌ [Reco] upcoming: " + e.getMessage());
        }
        return list;
    }

    private int getReservationCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE event_id=? AND statut IN ('EN_ATTENTE','CONFIRME','CHECKED_IN','LISTE_D_ATTENTE')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ [Reco] count: " + e.getMessage());
        }
        return 0;
    }

    private int getParticipantReservationCount(int participantId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE participant_id=? AND statut IN ('CONFIRME','CHECKED_IN')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    private void persistRecommendations(int participantId, List<RecommendedEvent> recs) {
        String sql = "INSERT INTO recommendation_history (participant_id, event_id, score, reason) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (RecommendedEvent r : recs) {
                ps.setInt(1, participantId);
                ps.setInt(2, r.getEvent().getId());
                ps.setDouble(3, r.getScore());
                ps.setString(4, r.getReason());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("⚠️ [Reco] persist: " + e.getMessage());
        }
    }

    private void runUpdate(String sql, Object... params) {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) ps.setInt(i + 1, (Integer) params[i]);
                else ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ [Reco] runUpdate: " + e.getMessage());
        }
    }

    private String safeDate(ResultSet rs, String col) {
        try { java.sql.Date d = rs.getDate(col); return d != null ? d.toString() : ""; }
        catch (SQLException e) { return ""; }
    }
}
