package services;

import entities.MedicationReminder;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationService {

    private Connection con = MyDataBase.getInstance().getConnection();

    public MedicationService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS `medication_reminder` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`userId` INT, " +
                "`medName` VARCHAR(255), " +
                "`dosage` VARCHAR(100), " +
                "`time` VARCHAR(10), " +
                "`taken` BOOLEAN DEFAULT FALSE, " +
                "FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE" +
                ")";
        try {
            Statement st = con.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur creation table medication_reminder : " + e.getMessage());
        }
    }

    public void add(MedicationReminder m) throws SQLException {
        String sql = "INSERT INTO `medication_reminder` (`userId`, `medName`, `dosage`, `time`, `taken`) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, m.getUserId());
        pst.setString(2, m.getMedName());
        pst.setString(3, m.getDosage());
        pst.setString(4, m.getTime());
        pst.setBoolean(5, m.isTaken());
        pst.executeUpdate();
    }

    public void updateStatus(int id, boolean taken) throws SQLException {
        String sql = "UPDATE `medication_reminder` SET `taken` = ? WHERE `id` = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setBoolean(1, taken);
        pst.setInt(2, id);
        pst.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM `medication_reminder` WHERE `id` = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public List<MedicationReminder> getByUserId(int userId) throws SQLException {
        List<MedicationReminder> list = new ArrayList<>();
        String sql = "SELECT * FROM `medication_reminder` WHERE `userId` = ? ORDER BY `time` ASC";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            MedicationReminder m = new MedicationReminder();
            m.setId(rs.getInt("id"));
            m.setUserId(rs.getInt("userId"));
            m.setMedName(rs.getString("medName"));
            m.setDosage(rs.getString("dosage"));
            m.setTime(rs.getString("time"));
            m.setTaken(rs.getBoolean("taken"));
            list.add(m);
        }
        return list;
    }
}
