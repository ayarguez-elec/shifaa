package services;

import entities.HealthLog;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthLogService {

    private Connection con = MyDataBase.getInstance().getConnection();

    public HealthLogService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS `health_log` (" +
                "`logId` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`userId` INT, " +
                "`date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "`type` VARCHAR(50), " +
                "`result` TEXT, " +
                "`severity` INT, " +
                "FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE" +
                ")";
        try {
            Statement st = con.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table health_log : " + e.getMessage());
        }
    }

    public void add(HealthLog log) throws SQLException {
        String sql = "INSERT INTO `health_log` (`userId`, `type`, `result`, `severity`, `date`) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, log.getUserId());
        pst.setString(2, log.getType());
        pst.setString(3, log.getResult());
        pst.setInt(4, log.getSeverity());
        pst.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        pst.executeUpdate();
    }

    public List<HealthLog> getByUserId(int userId) throws SQLException {
        List<HealthLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM `health_log` WHERE `userId` = ? ORDER BY `date` DESC";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            HealthLog log = new HealthLog();
            log.setLogId(rs.getInt("logId"));
            log.setUserId(rs.getInt("userId"));
            log.setDate(rs.getTimestamp("date"));
            log.setType(rs.getString("type"));
            log.setResult(rs.getString("result"));
            log.setSeverity(rs.getInt("severity"));
            logs.add(log);
        }
        return logs;
    }

    public List<HealthLog> getAll() throws SQLException {
        List<HealthLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM `health_log` ORDER BY `date` DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            HealthLog log = new HealthLog();
            log.setLogId(rs.getInt("logId"));
            log.setUserId(rs.getInt("userId"));
            log.setDate(rs.getTimestamp("date"));
            log.setType(rs.getString("type"));
            log.setResult(rs.getString("result"));
            log.setSeverity(rs.getInt("severity"));
            logs.add(log);
        }
        return logs;
    }
}
