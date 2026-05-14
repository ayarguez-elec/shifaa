package services;

import entities.Participant;
import entities.Participant.Badge;
import entities.Participant.PriorityLevel;
import utils.MyDataBase;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  LoyaltyService — Moteur de Fidélité Intelligent                ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Calcule en temps réel le score, badge et niveau de priorité    ║
 * ║  de chaque participant à partir de son historique réservation.  ║
 * ║                                                                  ║
 * ║  Score :                                                         ║
 * ║    +10 pts  par réservation CONFIRMÉE                            ║
 * ║    +20 pts  bonus par présence réelle (CHECKED_IN)              ║
 * ║    - 5 pts  par annulation                                       ║
 * ║                                                                  ║
 * ║  Badge :                                                         ║
 * ║    Bronze   0–49 pts                                             ║
 * ║    Silver  50–149 pts                                            ║
 * ║    Gold   150+ pts                                               ║
 * ║                                                                  ║
 * ║  Priorité :                                                      ║
 * ║    LOW     < 2 confirmées  OU taux annulation > 50%             ║
 * ║    MEDIUM  2–4 confirmées                                        ║
 * ║    HIGH    5–9 confirmées, taux annulation < 20%                ║
 * ║    VIP     10+ confirmées OU score ≥ 150                        ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class LoyaltyService {

    private final Connection cnx = MyDataBase.getInstance().getConnection();

    // ── Score ─────────────────────────────────────────────────────────────────

    public int computeScore(int participantId) {
        try {
            String sql =
                "SELECT " +
                "  SUM(CASE WHEN statut IN ('CONFIRME','CHECKED_IN') THEN 10 ELSE 0 END) AS pts_conf, " +
                "  SUM(CASE WHEN statut = 'CHECKED_IN'               THEN 20 ELSE 0 END) AS pts_att,  " +
                "  SUM(CASE WHEN statut = 'ANNULE'                   THEN  5 ELSE 0 END) AS pts_ann   " +
                "FROM reservation WHERE participant_id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, participantId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int score = rs.getInt("pts_conf") + rs.getInt("pts_att") - rs.getInt("pts_ann");
                    return Math.max(0, score);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [Loyalty] computeScore : " + e.getMessage());
        }
        return 0;
    }

    // ── Badge ─────────────────────────────────────────────────────────────────

    public Badge computeBadge(int score) {
        if (score >= 150) return Badge.Gold;
        if (score >= 50)  return Badge.Silver;
        return Badge.Bronze;
    }

    // ── Priority ──────────────────────────────────────────────────────────────

    public PriorityLevel computePriority(int participantId) {
        try {
            String sql =
                "SELECT " +
                "  SUM(statut IN ('CONFIRME','CHECKED_IN','LISTE_D_ATTENTE')) AS nb_conf, " +
                "  SUM(statut = 'ANNULE')                                     AS nb_ann,  " +
                "  SUM(statut = 'CHECKED_IN')                                 AS nb_att   " +
                "FROM reservation WHERE participant_id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, participantId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int conf = rs.getInt("nb_conf");
                    int ann  = rs.getInt("nb_ann");
                    int att  = rs.getInt("nb_att");
                    int total = conf + ann;
                    double cancRate = total > 0 ? (double) ann / total : 0.0;
                    int score = computeScore(participantId);

                    if (conf >= 10 || score >= 150) return PriorityLevel.VIP;
                    if (conf >= 5  && cancRate < 0.20) return PriorityLevel.HIGH;
                    if (conf >= 2  && cancRate < 0.50) return PriorityLevel.MEDIUM;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [Loyalty] computePriority : " + e.getMessage());
        }
        return PriorityLevel.LOW;
    }

    // ── Full profile enrichment ───────────────────────────────────────────────

    /**
     * Calcule score + badge + priorité et met à jour la table participant.
     */
    public void recalculateAndPersist(int participantId) {
        int score = computeScore(participantId);
        Badge badge = computeBadge(score);
        PriorityLevel prio = computePriority(participantId);

        // Count attendance and cancellations
        int[] counts = getAttendanceCancellation(participantId);

        String sql = "UPDATE participant SET loyalty_points=?, badge=?, priority_level=?, " +
                     "attendance_count=?, cancellation_count=?, last_activity=NOW() WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setString(2, badge.name());
            ps.setString(3, prio.name());
            ps.setInt(4, counts[0]);
            ps.setInt(5, counts[1]);
            ps.setInt(6, participantId);
            ps.executeUpdate();
            System.out.printf("🏅 [Loyalty] #%d → %s | %s | %d pts%n",
                participantId, badge.name(), prio.name(), score);
        } catch (SQLException e) {
            System.err.println("❌ [Loyalty] recalculate : " + e.getMessage());
        }
    }

    /**
     * Enrich a Participant object with live loyalty data (no DB write).
     */
    public Participant enrich(Participant p) {
        int score = computeScore(p.getId());
        p.setLoyaltyPoints(score);
        p.setBadge(computeBadge(score));
        p.setPriorityLevel(computePriority(p.getId()));
        int[] counts = getAttendanceCancellation(p.getId());
        p.setAttendanceCount(counts[0]);
        p.setCancellationCount(counts[1]);
        return p;
    }

    // ── Progress to next badge ────────────────────────────────────────────────

    public Map<String, Object> getBadgeProgress(int participantId) {
        Map<String, Object> info = new LinkedHashMap<>();
        int score = computeScore(participantId);
        Badge badge = computeBadge(score);
        info.put("score_actuel", score);
        info.put("badge_actuel", badge.name());
        switch (badge) {
            case Bronze:
                info.put("prochain_badge", "Silver");
                info.put("pts_manquants",  Math.max(0, 50 - score));
                info.put("progression_pct", score * 2);  // 0→100 for 0→50
                break;
            case Silver:
                info.put("prochain_badge", "Gold");
                info.put("pts_manquants",  Math.max(0, 150 - score));
                info.put("progression_pct", (score - 50) * 100 / 100);  // 50→150
                break;
            case Gold:
                info.put("prochain_badge", "MAX");
                info.put("pts_manquants",  0);
                info.put("progression_pct", 100);
                break;
        }
        return info;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int[] getAttendanceCancellation(int participantId) {
        String sql = "SELECT SUM(statut='CHECKED_IN') AS att, SUM(statut='ANNULE') AS ann " +
                     "FROM reservation WHERE participant_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("att"), rs.getInt("ann")};
        } catch (SQLException e) {
            System.err.println("❌ [Loyalty] getAttCancellation : " + e.getMessage());
        }
        return new int[]{0, 0};
    }
}
