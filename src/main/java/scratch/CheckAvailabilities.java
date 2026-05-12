package scratch;

import utils.myDataBase;
import java.sql.*;

public class CheckAvailabilities {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.err.println("❌ Erreur DB");
            return;
        }

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM availability");
            int count = 0;
            while (rs.next()) {
                System.out.println("Availability ID: " + rs.getInt("availabilityId") +
                                   ", Date: " + rs.getDate("date") +
                                   ", Time: " + rs.getTime("time") +
                                   ", Status: " + rs.getString("statusRDV") +
                                   ", Doctor ID: " + rs.getInt("doctorId"));
                count++;
            }
            if (count == 0) {
                System.out.println("Table availability is empty.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
