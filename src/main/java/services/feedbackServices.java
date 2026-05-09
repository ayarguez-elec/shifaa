package services;

import entities.feedback;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class feedbackServices implements ICrud<feedback> {

    private Connection con = MyDataBase.getInstance().getConnection();

    @Override
    public void add(feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (userId, targetType, targetId, rating, comment, creationDate, isAnonyme) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, feedback.getUserId());
        pst.setString(2, feedback.getTargetType().name());  // enum to string
        pst.setInt(3, feedback.getTargetId());
        pst.setInt(4, feedback.getRating());
        pst.setString(5, feedback.getComment());
        pst.setTimestamp(6, feedback.getCreationDate());
        pst.setBoolean(7, feedback.isAnonyme());
        pst.executeUpdate();
        System.out.println("Feedback added successfully!");
    }

    @Override
    public void delete(feedback feedback) throws SQLException {
        String sql = "DELETE FROM feedback WHERE feedbackId = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, feedback.getFeedbackId());
        pst.executeUpdate();
        System.out.println("Feedback deleted!");
    }

    @Override
    public List<feedback> getAll() throws SQLException {
        List<feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedback";
        Statement st = con.createStatement();
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
            list.add(f);
        }
        return list;
    }

    @Override
    public void update(feedback feedback) throws SQLException {
        String sql = "UPDATE feedback SET userId=?, targetType=?, targetId=?, rating=?, comment=?, creationDate=?, isAnonyme=? WHERE feedbackId=?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, feedback.getUserId());
        pst.setString(2, feedback.getTargetType().name());
        pst.setInt(3, feedback.getTargetId());
        pst.setInt(4, feedback.getRating());
        pst.setString(5, feedback.getComment());
        pst.setTimestamp(6, feedback.getCreationDate());
        pst.setBoolean(7, feedback.isAnonyme());
        pst.setInt(8, feedback.getFeedbackId());
        pst.executeUpdate();
        System.out.println("Feedback updated successfully!");
    }
}