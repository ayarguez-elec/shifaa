package scratch;
import java.sql.*;
import utils.myDataBase;

public class CheckUsers {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.out.println("Connection failed");
            return;
        }
        String[] tables = {"user", "users"};
        for (String t : tables) {
            System.out.println("--- Table: " + t + " ---");
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM " + t + " LIMIT 5")) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                for (int i = 1; i <= cols; i++) System.out.print(md.getColumnName(i) + " | ");
                System.out.println();
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) System.out.print(rs.getString(i) + " | ");
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Table " + t + " error: " + e.getMessage());
            }
        }
    }
}
