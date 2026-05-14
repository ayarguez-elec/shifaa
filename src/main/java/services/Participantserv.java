package services;

import entities.Participant;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Participantserv implements iCrud<Participant> {

    private final Connection cnx = MyDataBase.getInstance().getConnection();

    // ── AJOUTER ───────────────────────────────────────────────────────────────
    @Override
    public void ajouter(Participant p) {
        String email = p.getEmail();
        if (email == null || !email.contains("@")) {
            System.err.println("❌ Email invalide : " + email);
            return;
        }
        String sql = "INSERT INTO participant (nom, prenom, email, telephone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelephone());
            ps.executeUpdate();
            System.out.println("✅ Participant ajouté : " + p.getNom() + " " + p.getPrenom());
        } catch (SQLException ex) {
            System.err.println("❌ ajouter participant : " + ex.getMessage());
        }
    }

    // ── MODIFIER ──────────────────────────────────────────────────────────────
    @Override
    public void modifier(Participant p) {
        String sql = "UPDATE participant SET nom=?, prenom=?, email=?, telephone=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelephone());
            ps.setInt   (5, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Participant modifié id=" + p.getId());
        } catch (SQLException ex) {
            System.err.println("❌ modifier participant : " + ex.getMessage());
        }
    }

    // ── SUPPRIMER ─────────────────────────────────────────────────────────────
    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM participant WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Participant supprimé id=" + id);
        } catch (SQLException ex) {
            System.err.println("❌ supprimer participant : " + ex.getMessage());
        }
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @Override
    public List<Participant> getAll() {
        List<Participant> liste = new ArrayList<>();
        String sql = "SELECT * FROM participant ORDER BY nom ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            System.err.println("❌ getAll participant : " + ex.getMessage());
        }
        return liste;
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @Override
    public Participant getById(int id) {
        String sql = "SELECT * FROM participant WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            System.err.println("❌ getById participant : " + ex.getMessage());
        }
        return null;
    }

    // ── RECHERCHE PAR NOM ─────────────────────────────────────────────────────
    public List<Participant> rechercherParNom(String mot) {
        List<Participant> liste = new ArrayList<>();
        String sql = "SELECT * FROM participant WHERE nom LIKE ? OR prenom LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + mot + "%");
            ps.setString(2, "%" + mot + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            System.err.println("❌ rechercherParNom : " + ex.getMessage());
        }
        return liste;
    }

    // ── PARTICIPANTS D'UN ÉVÉNEMENT ───────────────────────────────────────────
    public List<Participant> getByEvent(int eventId) {
        List<Participant> liste = new ArrayList<>();
        String sql = "SELECT p.* FROM participant p " +
                "JOIN event_participant ep ON p.id = ep.participant_id " +
                "WHERE ep.event_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            System.err.println("❌ getByEvent : " + ex.getMessage());
        }
        return liste;
    }

    // ── INSCRIRE UN PARTICIPANT À UN ÉVÉNEMENT ────────────────────────────────
    public void inscrireAEvent(int participantId, int eventId) {
        // Vérification de la capacité (Business Logic Coordinateur)
        eventServ eServ = new eventServ();
        if (eServ.isEventFull(eventId)) {
            System.err.println("⚠️ L'événement est COMPLET. Impossible d'inscrire plus de participants.");
            return;
        }

        String sql = "INSERT IGNORE INTO event_participant (event_id, participant_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, participantId);
            ps.executeUpdate();
            System.out.println("✅ Participant " + participantId + " inscrit à l'event " + eventId);
        } catch (SQLException ex) {
            System.err.println("❌ inscrireAEvent : " + ex.getMessage());
        }
    }

    // ── GET ALL enriched with loyalty data ───────────────────────────────────
    public List<Participant> getAllEnriched() {
        LoyaltyService loyalty = new LoyaltyService();
        List<Participant> list = getAll();
        list.forEach(p -> loyalty.enrich(p));
        return list;
    }

    // ── MAPPER ResultSet → Participant ────────────────────────────────────────
    private Participant mapRow(ResultSet rs) throws SQLException {
        int    pts    = safeInt(rs, "loyalty_points",     0);
        String prio   = safeStr(rs, "priority_level",     "LOW");
        String badge  = safeStr(rs, "badge",              "Bronze");
        int    att    = safeInt(rs, "attendance_count",   0);
        int    ann    = safeInt(rs, "cancellation_count", 0);

        Participant.PriorityLevel pl;
        Participant.Badge         bg;
        try { pl = Participant.PriorityLevel.valueOf(prio); }
        catch (Exception e) { pl = Participant.PriorityLevel.LOW; }
        try { bg = Participant.Badge.valueOf(badge); }
        catch (Exception e) { bg = Participant.Badge.Bronze; }

        return new Participant(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("telephone"),
                pts, pl, bg, att, ann
        );
    }

    private int safeInt(ResultSet rs, String col, int def) {
        try { rs.findColumn(col); return rs.getInt(col); } catch (SQLException e) { return def; }
    }

    private String safeStr(ResultSet rs, String col, String def) {
        try { rs.findColumn(col); return rs.getString(col); } catch (SQLException e) { return def; }
    }
}
