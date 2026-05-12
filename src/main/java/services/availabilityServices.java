package services;

import entities.availability;
import utils.myDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class availabilityServices implements ICrud<availability> {

    private Connection con = myDataBase.getInstance().getConnection();

    // ─── Validation: vérifie que le doctorId existe dans la base ───────────────
    /**
     * Retourne true si un utilisateur avec cet ID existe dans la table `user`
     * (ou `doctor` selon votre schéma). On essaie les tables connues.
     */
    public boolean doctorExists(int doctorId) {
        // Liste des noms de colonnes/tables à essayer selon le schéma
        String[][] candidates = {
            {"user",   "id"},
            {"users",  "id"},
            {"doctor", "id"},
            {"doctor", "doctorId"},
            {"user",   "userId"}
        };
        for (String[] candidate : candidates) {
            try {
                String sql = "SELECT COUNT(*) FROM `" + candidate[0] + "` WHERE `" + candidate[1] + "` = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, doctorId);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return true;
            } catch (SQLException ignored) {
                // Table ou colonne n'existe pas — essayer la suivante
            }
        }
        return false;
    }

    /**
     * Retourne une map {id → nom_affiché} de tous les médecins disponibles.
     * Essaie plusieurs schémas de tables possibles.
     */
    public Map<Integer, String> getAvailableDoctors() {
        Map<Integer, String> doctors = new LinkedHashMap<>();
        if (con == null) return doctors;
        // Schéma 1 : table user avec colonne role
        String[][] queries = {
            {"SELECT id, CONCAT(prenom, ' ', nom) FROM `user` WHERE role = 'DOCTOR' OR role = 'doctor' OR role = 'medecin'"},
            {"SELECT id, CONCAT(firstName, ' ', lastName) FROM `user` WHERE role = 'DOCTOR'"},
            {"SELECT id, nom FROM `doctor`"},
            {"SELECT id, name FROM `doctor`"},
            {"SELECT doctorId, CONCAT(prenom, ' ', nom) FROM `doctor`"},
        };
        for (String[] q : queries) {
            try {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(q[0]);
                while (rs.next()) {
                    doctors.put(rs.getInt(1), "#" + rs.getInt(1) + " — " + rs.getString(2));
                }
                if (!doctors.isEmpty()) return doctors;
            } catch (SQLException ignored) {}
        }
        // Fallback: récupérer les doctorIds distincts déjà dans availability
        if (doctors.isEmpty()) {
            try {
                String sql = "SELECT DISTINCT doctorId FROM availability ORDER BY doctorId";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt(1);
                    doctors.put(id, "Médecin ID " + id);
                }
            } catch (SQLException ignored) {}
        }
        return doctors;
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public void add(availability availability) throws SQLException {
        if (con == null) throw new SQLException("Connexion à la base de données non disponible.");

        // Validation FK avant insertion pour un message clair
        if (!doctorExists(availability.getDoctorId())) {
            throw new SQLException(
                "Le médecin ID " + availability.getDoctorId() +
                " n'existe pas dans la base de données. " +
                "Veuillez utiliser un ID valide."
            );
        }

        String sql = "INSERT INTO availability (date, time, statusRDV, doctorId) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setDate(1, new java.sql.Date(availability.getDate().getTime()));
        pst.setTime(2, new java.sql.Time(availability.getTime().getTime()));
        pst.setString(3, availability.getStatusRDVAsString());
        pst.setInt(4, availability.getDoctorId());
        pst.executeUpdate();
        System.out.println("Créneau ajouté !");
    }

    @Override
    public void delete(availability availability) throws SQLException {
        if (con == null) throw new SQLException("Connexion à la base de données non disponible.");
        String sql = "DELETE FROM availability WHERE availabilityId = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, availability.getAvailabilityId());
        pst.executeUpdate();
    }

    @Override
    public List<availability> getAll() throws SQLException {
        List<availability> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT * FROM availability";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            availability a = new availability();
            a.setAvailabilityId(rs.getInt("availabilityId"));
            a.setDate(rs.getDate("date"));
            a.setTime(rs.getTime("time"));
            a.setStatusRDVFromString(rs.getString("statusRDV"));
            a.setDoctorId(rs.getInt("doctorId"));
            list.add(a);
        }
        return list;
    }

    @Override
    public void update(availability availability) throws SQLException {
        if (con == null) throw new SQLException("Connexion à la base de données non disponible.");

        if (!doctorExists(availability.getDoctorId())) {
            throw new SQLException(
                "Le médecin ID " + availability.getDoctorId() +
                " n'existe pas dans la base de données."
            );
        }

        String sql = "UPDATE availability SET date=?, time=?, statusRDV=?, doctorId=? WHERE availabilityId=?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setDate(1, new java.sql.Date(availability.getDate().getTime()));
        pst.setTime(2, new java.sql.Time(availability.getTime().getTime()));
        pst.setString(3, availability.getStatusRDVAsString());
        pst.setInt(4, availability.getDoctorId());
        pst.setInt(5, availability.getAvailabilityId());
        pst.executeUpdate();
    }

    // ─── Méthodes additionnelles ───────────────────────────────────────────────

    public List<availability> getAvailabilitiesByDoctor(int doctorId) throws SQLException {
        List<availability> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT * FROM availability WHERE doctorId = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, doctorId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            availability a = new availability();
            a.setAvailabilityId(rs.getInt("availabilityId"));
            a.setDate(rs.getDate("date"));
            a.setTime(rs.getTime("time"));
            a.setStatusRDVFromString(rs.getString("statusRDV"));
            a.setDoctorId(rs.getInt("doctorId"));
            list.add(a);
        }
        return list;
    }

    public List<availability> getFreeAvailabilities() throws SQLException {
        List<availability> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT * FROM availability WHERE statusRDV = 'libre'";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            availability a = new availability();
            a.setAvailabilityId(rs.getInt("availabilityId"));
            a.setDate(rs.getDate("date"));
            a.setTime(rs.getTime("time"));
            a.setStatusRDVFromString(rs.getString("statusRDV"));
            a.setDoctorId(rs.getInt("doctorId"));
            list.add(a);
        }
        return list;
    }

    public availability getAvailabilityById(int id) throws SQLException {
        if (con == null) return null;
        String sql = "SELECT * FROM availability WHERE availabilityId = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            availability a = new availability();
            a.setAvailabilityId(rs.getInt("availabilityId"));
            a.setDate(rs.getDate("date"));
            a.setTime(rs.getTime("time"));
            a.setStatusRDVFromString(rs.getString("statusRDV"));
            a.setDoctorId(rs.getInt("doctorId"));
            return a;
        }
        return null;
    }

    public boolean isTimeSlotAvailable(int doctorId, java.util.Date date, java.util.Date time) throws SQLException {
        if (con == null) return false;
        String sql = "SELECT COUNT(*) FROM availability WHERE doctorId=? AND date=? AND time=? AND statusRDV='libre'";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, doctorId);
        pst.setDate(2, new java.sql.Date(date.getTime()));
        pst.setTime(3, new java.sql.Time(time.getTime()));
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return rs.getInt(1) > 0;
        return false;
    }
}