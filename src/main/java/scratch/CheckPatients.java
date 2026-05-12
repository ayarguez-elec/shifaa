package scratch;

import utils.myDataBase;
import java.sql.*;

public class CheckPatients {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            String[] tables = {"user", "users", "patient"};
            for (String table : tables) {
                try {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE role = 'patient'");
                    if (rs.next()) {
                        System.out.println("Table " + table + ": " + rs.getInt(1) + " patients found.");
                    }
                } catch (SQLException e) {
                    System.out.println("Table " + table + " error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
