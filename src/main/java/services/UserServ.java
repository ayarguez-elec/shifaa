package services;

import entities.User;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServ implements iCrud<User> {
    private final Connection connection;

    public UserServ() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(User u) {
        String sql = "INSERT INTO users (email, password_hash, role, profile_image_path) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getProfileImagePath());
            ps.executeUpdate();
            System.out.println("✅ User added: " + u.getEmail());
        } catch (SQLException ex) {
            System.err.println("❌ Error adding user: " + ex.getMessage());
        }
    }

    @Override
    public void modifier(User u) {
        String sql = "UPDATE users SET email=?, password_hash=?, role=?, profile_image_path=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getProfileImagePath());
            ps.setInt(5, u.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ User updated." : "⚠️ No user found (id=" + u.getId() + ")");
        } catch (SQLException ex) {
            System.err.println("❌ Error updating user: " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ User deleted (id=" + id + ")" : "⚠️ User not found (id=" + id + ")");
        } catch (SQLException ex) {
            System.err.println("❌ Error deleting user: " + ex.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ Error fetching users: " + ex.getMessage());
        }
        return list;
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException ex) {
            System.err.println("❌ Error getById user: " + ex.getMessage());
        }
        return null;
    }

    // Additional helper to fetch by email (used for login)
    public User getByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException ex) {
            System.err.println("❌ Error getByEmail user: " + ex.getMessage());
        }
        return null;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("profile_image_path")
        );
    }
}
