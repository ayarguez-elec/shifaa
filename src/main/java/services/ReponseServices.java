package services;

import entities.Reponse;
import entities.Reclamation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseServices {

    private Connection cnx;

    public ReponseServices() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public void add(Reponse r) {
        String req = "INSERT INTO reclamation_reponse (reclamationId, content, responseDate, adminId) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getReclamationId());
            ps.setString(2, r.getContent());
            ps.setTimestamp(3, r.getResponseDate() != null ? r.getResponseDate() : new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, r.getAdminId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                r.setResponseId(rs.getInt(1));
            }

            System.out.println("✅ Response added successfully! ID = " + r.getResponseId());

            // AUTO-UPDATE: Set reclamation status to "En cours" when first response is added
            ReclamationServices recSvc = new ReclamationServices();
            Reclamation rec = recSvc.getById(r.getReclamationId());
            if (rec != null && !"Résolu".equalsIgnoreCase(rec.getStatusreclam())) {
                recSvc.updateStatus(r.getReclamationId(), "En cours");

                // Update lastResponseDate on reclamation
                String updateDate = "UPDATE reclamation SET lastResponseDate = NOW() WHERE reclamationId = ?";
                try (PreparedStatement psDate = cnx.prepareStatement(updateDate)) {
                    psDate.setInt(1, r.getReclamationId());
                    psDate.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur ajout réponse : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add response: " + e.getMessage(), e);
        }
    }

    public void update(Reponse r) {
        String req = "UPDATE reclamation_reponse SET content = ?, responseDate = ? WHERE responseId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, r.getContent());
            ps.setTimestamp(2, r.getResponseDate());
            ps.setInt(3, r.getResponseId());
            ps.executeUpdate();
            System.out.println("✅ Response #" + r.getResponseId() + " updated.");
        } catch (SQLException e) {
            System.out.println("Erreur modification réponse : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int responseId) {
        String req = "DELETE FROM reclamation_reponse WHERE responseId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, responseId);
            ps.executeUpdate();
            System.out.println("✅ Response #" + responseId + " deleted.");
        } catch (SQLException e) {
            System.out.println("Erreur suppression réponse : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation_reponse";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur affichage réponses : " + e.getMessage());
        }
        return list;
    }

    public List<Reponse> getByReclamation(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation_reponse WHERE reclamationId = ? ORDER BY responseDate DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur affichage réponses par réclamation : " + e.getMessage());
        }
        return list;
    }

    public Reponse getById(int responseId) {
        String req = "SELECT * FROM reclamation_reponse WHERE responseId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, responseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche réponse : " + e.getMessage());
        }
        return null;
    }

    private Reponse mapResultSet(ResultSet rs) throws SQLException {
        // FIXED: Correct parameter order matching Reponse constructor:
        // Reponse(int responseId, String content, Timestamp responseDate, int adminId, int reclamationId)
        Timestamp responseDate;
        try {
            responseDate = rs.getTimestamp("responseDate");
        } catch (SQLException e) {
            // If column is DATE type, convert to Timestamp
            Date date = rs.getDate("responseDate");
            responseDate = date != null ? new Timestamp(date.getTime()) : null;
        }

        return new Reponse(
                rs.getInt("responseId"),        // 1. responseId
                rs.getString("content"),         // 2. content
                responseDate,                    // 3. responseDate (handles both DATE and TIMESTAMP)
                rs.getInt("adminId"),           // 4. adminId
                rs.getInt("reclamationId")      // 5. reclamationId
        );
    }
}
