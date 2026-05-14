package services;

import entities.feedback;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class feedbackServices implements IServiceCrud<feedback> {

    private Connection getCon() {
        return MyDataBase.getInstance().getConnection();
    }

    public feedbackServices() {
        ensureAvatarUrlColumn();
    }

    private void ensureAvatarUrlColumn() {
        try {
            Connection conn = getCon();
            if (conn == null) return;
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "feedback", "avatar_url");
            if (!rs.next()) {
                conn.createStatement().execute("ALTER TABLE `feedback` ADD COLUMN `avatar_url` LONGTEXT DEFAULT NULL");
                System.out.println("Colonne avatar_url ajoutée à la table feedback.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification colonne avatar_url: " + e.getMessage());
        }
    }

    @Override
    public void add(feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (userId, targetType, targetId, rating, comment, creationDate, isAnonyme, avatar_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = getCon().prepareStatement(sql);
        pst.setInt(1, feedback.getUserId());
        pst.setString(2, feedback.getTargetType().name());  // enum to string
        pst.setInt(3, feedback.getTargetId());
        pst.setInt(4, feedback.getRating());
        pst.setString(5, feedback.getComment());
        pst.setTimestamp(6, feedback.getCreationDate());
        pst.setBoolean(7, feedback.isAnonyme());
        pst.setString(8, feedback.getAvatarUrl());
        pst.executeUpdate();
        System.out.println("Feedback added successfully!");
    }

    @Override
    public void delete(feedback feedback) throws SQLException {
        String sql = "DELETE FROM feedback WHERE feedbackId = ?";
        PreparedStatement pst = getCon().prepareStatement(sql);
        pst.setInt(1, feedback.getFeedbackId());
        pst.executeUpdate();
        System.out.println("Feedback deleted!");
    }

    @Override
    public List<feedback> getAll() throws SQLException {
        List<feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedback";
        Statement st = getCon().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            feedback f = new feedback();
            f.setFeedbackId(rs.getInt("feedbackId"));
            f.setUserId(rs.getInt("userId"));
            f.setTargetType(feedback.TargetType.valueOf(rs.getString("targetType")));
            f.setTargetId(rs.getInt("targetId"));
            f.setRating(rs.getInt("rating"));
            f.setComment(rs.getString("comment"));
            f.setCreationDate(rs.getTimestamp("creationDate"));
            f.setAnonyme(rs.getBoolean("isAnonyme"));
            try {
                f.setAvatarUrl(rs.getString("avatar_url"));
            } catch (SQLException e) {
                f.setAvatarUrl(null);
            }
            list.add(f);
        }
        return list;
    }

    @Override
    public void update(feedback feedback) throws SQLException {
        String sql = "UPDATE feedback SET userId=?, targetType=?, targetId=?, rating=?, comment=?, creationDate=?, isAnonyme=?, avatar_url=? WHERE feedbackId=?";
        PreparedStatement pst = getCon().prepareStatement(sql);
        pst.setInt(1, feedback.getUserId());
        pst.setString(2, feedback.getTargetType().name());
        pst.setInt(3, feedback.getTargetId());
        pst.setInt(4, feedback.getRating());
        pst.setString(5, feedback.getComment());
        pst.setTimestamp(6, feedback.getCreationDate());
        pst.setBoolean(7, feedback.isAnonyme());
        pst.setString(8, feedback.getAvatarUrl());
        pst.setInt(9, feedback.getFeedbackId());
        pst.executeUpdate();
        System.out.println("Feedback updated successfully!");
    }
}
