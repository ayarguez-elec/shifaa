package scratch;

import utils.myDataBase;
import java.sql.*;

public class CheckUserSchema {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            Statement st = con.createStatement();
            
            System.out.println("--- Table: user ---");
            try {
                ResultSet rs = st.executeQuery("DESCRIBE user");
                while (rs.next()) {
                    System.out.println(rs.getString("Field") + " - " + rs.getString("Type"));
                }
            } catch (SQLException e) { System.out.println(e.getMessage()); }
            
            System.out.println("\n--- Table: users ---");
            try {
                ResultSet rs = st.executeQuery("DESCRIBE users");
                while (rs.next()) {
                    System.out.println(rs.getString("Field") + " - " + rs.getString("Type"));
                }
            } catch (SQLException e) { System.out.println(e.getMessage()); }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
