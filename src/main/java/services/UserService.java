package services;

import utils.MyDataBase;

import java.sql.*;

/**
 * FIXED UserService — uses firstName + lastName from your actual schema
 */
public class UserService {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public String getUserName(int userId) {
        String req = "SELECT firstName, lastName, email FROM user WHERE userId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String first = rs.getString("firstName");
                String last = rs.getString("lastName");
                if (first != null && !first.isEmpty()) {
                    if (last != null && !last.isEmpty()) {
                        return first + " " + last;
                    }
                    return first;
                }
                return rs.getString("email"); // fallback
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "User #" + userId;
    }

    public String getInitials(int userId) {
        String req = "SELECT firstName, lastName FROM user WHERE userId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String first = rs.getString("firstName");
                String last = rs.getString("lastName");
                StringBuilder initials = new StringBuilder();
                if (first != null && !first.isEmpty()) {
                    initials.append(Character.toUpperCase(first.charAt(0)));
                }
                if (last != null && !last.isEmpty()) {
                    initials.append(Character.toUpperCase(last.charAt(0)));
                }
                if (initials.length() > 0) {
                    return initials.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "U" + userId;
    }

    public String getRole(int userId) {
        String req = "SELECT role FROM user WHERE userId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "patient";
    }
}