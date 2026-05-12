package scratch;

import utils.myDataBase;
import java.sql.*;

public class ForceEmail {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            String[] tables = {"user", "users"};
            for (String table : tables) {
                try {
                    String sql = "UPDATE " + table + " SET email = 'chayma.gjima@esprit.tn' WHERE (firstName LIKE '%chayma%' OR prenom LIKE '%chayma%') AND role='patient'";
                    Statement st = con.createStatement();
                    int rows = st.executeUpdate(sql);
                    System.out.println("Updated " + rows + " rows in " + table);
                } catch (Exception e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
