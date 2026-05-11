package services;

import entities.medicalFile;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class medicalFileServ {

    private Connection getConnection() throws SQLException {
        Connection con = DatabaseConnection.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            throw new SQLException("❌ Connexion à la base de données perdue.");
        }
        return con;
    }

    // ================= AJOUTER =================
    public void add(medicalFile mf) throws SQLException {
        String sql = "INSERT INTO medicalfile (patientId, creationdate, lastvisit, patientstatus, generalhistory, antecedents, allergies, followup) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, mf.getPatientId());
            pstmt.setDate(2, Date.valueOf(mf.getCreationdate()));
            pstmt.setDate(3, mf.getLastvisit() != null ? Date.valueOf(mf.getLastvisit()) : null);
            pstmt.setString(4, mf.getPatientstatus());
            pstmt.setString(5, mf.getGeneralhistory());
            pstmt.setString(6, mf.getAntecedents());
            pstmt.setString(7, mf.getAllergies());
            pstmt.setString(8, mf.getFollowup());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    mf.setIdfile(rs.getInt(1));
                }
            }
        }
    }

    // ================= METTRE À JOUR =================
    public void update(medicalFile mf) throws SQLException {
        String sql = "UPDATE medicalfile SET creationdate = ?, lastvisit = ?, patientstatus = ?, generalhistory = ?, antecedents = ?, allergies = ?, followup = ? WHERE idfile = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(mf.getCreationdate()));
            pstmt.setDate(2, mf.getLastvisit() != null ? Date.valueOf(mf.getLastvisit()) : null);
            pstmt.setString(3, mf.getPatientstatus());
            pstmt.setString(4, mf.getGeneralhistory());
            pstmt.setString(5, mf.getAntecedents());
            pstmt.setString(6, mf.getAllergies());
            pstmt.setString(7, mf.getFollowup());
            pstmt.setInt(8, mf.getIdfile());
            pstmt.executeUpdate();
        }
    }

    // ================= RECHERCHES =================
    public List<medicalFile> searchByPatientName(String nom, String prenom) {
        List<medicalFile> files = new ArrayList<>();
        String sql = "SELECT m.* FROM medicalfile m INNER JOIN user u ON m.patientId = u.userId WHERE LOWER(u.lastName) = ? AND LOWER(u.firstName) = ? AND u.role = 'patient' ORDER BY m.creationdate DESC";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nom.toLowerCase());
            pstmt.setString(2, prenom.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                files.add(mapResultSetToMedicalFile(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur searchByPatientName: " + e.getMessage());
        }
        return files;
    }

    public List<medicalFile> searchByPatientNameOrSurname(String keyword) {
        List<medicalFile> files = new ArrayList<>();
        String sql = "SELECT m.* FROM medicalfile m INNER JOIN user u ON m.patientId = u.userId WHERE (LOWER(u.lastName) LIKE ? OR LOWER(u.firstName) LIKE ?) AND u.role = 'patient' ORDER BY m.creationdate DESC";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            String term = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                files.add(mapResultSetToMedicalFile(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur searchByPatientNameOrSurname: " + e.getMessage());
        }
        return files;
    }

    public List<medicalFile> getAll() {
        List<medicalFile> files = new ArrayList<>();
        String sql = "SELECT m.* FROM medicalfile m INNER JOIN user u ON m.patientId = u.userId WHERE u.role = 'patient' ORDER BY m.creationdate DESC";
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                files.add(mapResultSetToMedicalFile(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll: " + e.getMessage());
        }
        return files;
    }

    public medicalFile getById(int idfile) {
        String sql = "SELECT * FROM medicalfile WHERE idfile = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, idfile);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMedicalFile(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById: " + e.getMessage());
        }
        return null;
    }

    public List<medicalFile> getByPatientId(int patientId) {
        List<medicalFile> files = new ArrayList<>();
        String sql = "SELECT m.* FROM medicalfile m INNER JOIN user u ON m.patientId = u.userId WHERE m.patientId = ? AND u.role = 'patient' ORDER BY m.creationdate DESC";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                files.add(mapResultSetToMedicalFile(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByPatientId: " + e.getMessage());
        }
        return files;
    }

    public void delete(int idfile) throws SQLException {
        String sql = "DELETE FROM medicalfile WHERE idfile = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, idfile);
            pstmt.executeUpdate();
        }
    }

    public java.util.Map<String, Integer> getStatsByMonth() {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        // Utiliser une requête qui fonctionne sur MySQL pour grouper par mois
        String sql = "SELECT MONTH(creationdate) as monthNum, COUNT(*) as count FROM medicalfile GROUP BY monthNum ORDER BY monthNum";
        
        String[] monthNames = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                               "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int monthNum = rs.getInt("monthNum");
                if (monthNum >= 1 && monthNum <= 12) {
                    stats.put(monthNames[monthNum - 1], rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur stats: " + e.getMessage());
        }
        return stats;
    }

    public java.util.Map<String, Integer> getStatsByStatus() {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT patientstatus, COUNT(*) as count FROM medicalfile GROUP BY patientstatus ORDER BY count DESC";

        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String status = rs.getString("patientstatus");
                if (status == null || status.trim().isEmpty()) {
                    status = "Non defini";
                }
                stats.put(status, rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur stats status: " + e.getMessage());
        }
        return stats;
    }

    private medicalFile mapResultSetToMedicalFile(ResultSet rs) throws SQLException {
        medicalFile mf = new medicalFile();
        mf.setIdfile(rs.getInt("idfile"));
        mf.setPatientId(rs.getInt("patientId"));
        Date d1 = rs.getDate("creationdate");
        if (d1 != null) mf.setCreationdate(d1.toLocalDate());
        Date d2 = rs.getDate("lastvisit");
        if (d2 != null) mf.setLastvisit(d2.toLocalDate());
        mf.setPatientstatus(rs.getString("patientstatus"));
        mf.setGeneralhistory(rs.getString("generalhistory"));
        mf.setAntecedents(rs.getString("antecedents"));
        mf.setAllergies(rs.getString("allergies"));
        mf.setFollowup(rs.getString("followup"));
        return mf;
    }
}
