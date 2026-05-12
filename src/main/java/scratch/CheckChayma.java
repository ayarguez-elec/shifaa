package scratch;

import utils.myDataBase;
import java.sql.*;

public class CheckChayma {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            String[] tables = {"user", "users", "patient"};
            for (String table : tables) {
                try {
                    String[] idCols = {"id", "userId"};
                    for (String idCol : idCols) {
                        try {
                            String sql = "SELECT " + idCol + ", firstName, lastName, prenom, nom, email FROM " + table + " WHERE role='patient'";
                            Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(sql);
                            while(rs.next()) {
                                System.out.println("ID: " + rs.getInt(1) + " | " + rs.getString(2) + " " + rs.getString(3) + " | " + rs.getString(4) + " " + rs.getString(5) + " | " + rs.getString(6) + " (Table: " + table + ")");
                            }
                        } catch (SQLException e) {}
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
