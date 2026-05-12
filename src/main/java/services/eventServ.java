package services;

import entities.event;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class eventServ implements iCrud<event> {

    private final Connection connection;

    public eventServ() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    // ── AJOUTER ───────────────────────────────────────────────────────────────
    @Override
    public void ajouter(event e) {
        String sql = "INSERT INTO event (titre, type, description, date, time, lieu, status, image_path, capacite_max) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getType());
            ps.setString(3, e.getDescription());
            ps.setDate(4,   e.getDate() != null ? Date.valueOf(e.getDate()) : null);
            ps.setString(5, e.getTime());
            ps.setString(6, e.getLieu());
            ps.setString(7, e.getStatus());
            ps.setString(8, e.getImagePath());
            ps.setInt(9,    e.getCapaciteMax());
            ps.executeUpdate();
            System.out.println("✅ Événement ajouté : " + e.getTitre());
        } catch (SQLException ex) {
            System.err.println("❌ Erreur ajouter event : " + ex.getMessage());
        }
    }

    // ── MODIFIER ──────────────────────────────────────────────────────────────
    @Override
    public void modifier(event e) {
        String sql = "UPDATE event SET titre=?, type=?, description=?, date=?, time=?, lieu=?, status=?, image_path=?, capacite_max=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getType());
            ps.setString(3, e.getDescription());
            ps.setDate(4,   e.getDate() != null ? Date.valueOf(e.getDate()) : null);
            ps.setString(5, e.getTime());
            ps.setString(6, e.getLieu());
            ps.setString(7, e.getStatus());
            ps.setString(8, e.getImagePath());
            ps.setInt(9,    e.getCapaciteMax());
            ps.setInt(10,   e.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ Événement modifié." : "⚠️ Aucun événement trouvé (id=" + e.getId() + ")");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur modifier event : " + ex.getMessage());
        }
    }

    // ── SUPPRIMER ─────────────────────────────────────────────────────────────
    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM event WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ Événement supprimé (id=" + id + ")" : "⚠️ Introuvable (id=" + id + ")");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur supprimer event : " + ex.getMessage());
        }
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @Override
    public List<event> getAll() {
        List<event> list = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY date DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ Erreur getAll events : " + ex.getMessage());
        }
        return list;
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @Override
    public event getById(int id) {
        String sql = "SELECT * FROM event WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException ex) {
            System.err.println("❌ Erreur getById event : " + ex.getMessage());
        }
        return null;
    }

    // ── RECHERCHER PAR TITRE ──────────────────────────────────────────────────
    public List<event> rechercherParTitre(String mot) {
        List<event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE titre LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + mot + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ Erreur recherche event : " + ex.getMessage());
        }
        return list;
    }

    // ── FILTRER PAR STATUT ────────────────────────────────────────────────────
    public List<event> filtrerParStatut(String statut) {
        List<event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE status=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ Erreur filtre statut : " + ex.getMessage());
        }
        return list;
    }

    private event map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date");
        return new event(
            rs.getInt("id"),
            rs.getString("titre"),
            rs.getString("type"),
            rs.getString("description"),
            d != null ? d.toLocalDate() : null,
            rs.getString("time"),
            rs.getString("lieu"),
            rs.getString("status"),
            // Handle possible missing column gracefully in case DB isn't updated yet
            hasColumn(rs, "image_path") ? rs.getString("image_path") : null,
            hasColumn(rs, "capacite_max") ? rs.getInt("capacite_max") : 0
        );
    }

    // ── NOUVELLES FONCTIONNALITÉS COORDINATEUR ───────────────────────────────

    /**
     * Retourne le nombre d'inscrits pour un événement donné.
     */
    public int getParticipantCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM event_participant WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("❌ getParticipantCount : " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Vérifie si l'événement est complet.
     */
    public boolean isEventFull(int eventId) {
        event e = getById(eventId);
        if (e == null || e.getCapaciteMax() <= 0) return false;
        return getParticipantCount(eventId) >= e.getCapaciteMax();
    }
    
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}
