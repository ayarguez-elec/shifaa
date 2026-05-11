package services;

import entities.userDS;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class userservDS {

    private Connection getConnection() throws SQLException {
        Connection con = DatabaseConnection.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            throw new SQLException("❌ Connexion à la base de données perdue.");
        }
        return con;
    }

    public List<userDS> searchByNameOrSurname(String keyword) {
        List<userDS> users = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return users;
        }

        String searchTerm = "%" + keyword.trim().toLowerCase() + "%";
        String sql = "SELECT * FROM user WHERE (LOWER(firstName) LIKE ? OR LOWER(lastName) LIKE ?) AND role = 'patient'";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userDS user = new userDS();
                user.setUserId(rs.getInt("userId"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setUserphoto(rs.getString("userPhoto"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }
        return users;
    }

    public List<userDS> searchAllByNameOrSurname(String keyword) {
        List<userDS> users = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return users;
        String searchTerm = "%" + keyword.trim().toLowerCase() + "%";
        String sql = "SELECT * FROM user WHERE LOWER(firstName) LIKE ? OR LOWER(lastName) LIKE ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userDS user = new userDS();
                user.setUserId(rs.getInt("userId"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setUserphoto(rs.getString("userPhoto"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur searchAll: " + e.getMessage());
        }
        return users;
    }

    public userDS getById(int userId) {
        String sql = "SELECT * FROM user WHERE userId = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                userDS user = new userDS();
                user.setUserId(rs.getInt("userId"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setUserphoto(rs.getString("userPhoto"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(getOptionalString(rs, "phoneNumber", "phone_number", "phone", "numero", "number", "numTel", "num_tel", "tel", "telephone"));
                user.setSpeciality(getOptionalString(rs, "speciality", "specialite", "specialty", "specialityName"));
                user.setImageSignature(getOptionalString(rs, "imageSignature"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById: " + e.getMessage());
        }
        return null;
    }

    public List<userDS> getAll() {
        List<userDS> users = new ArrayList<>();
        String sql = "SELECT userId, firstName, lastName, userPhoto, role, email FROM user WHERE role = 'patient' ORDER BY lastName";
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                userDS user = new userDS();
                user.setUserId(rs.getInt("userId"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setUserphoto(rs.getString("userPhoto"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll: " + e.getMessage());
        }
        return users;
    }

    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM user WHERE userId = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    private String getOptionalString(ResultSet rs, String... columnNames) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (String columnName : columnNames) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
                    return rs.getString(i);
                }
            }
        }
        return null;
    }
}
