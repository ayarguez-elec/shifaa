
import java.sql.*;
import utils.myDataBase;

public class CheckData {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.err.println("Connection failed");
            return;
        }
        try {
            System.out.println("--- USERS ---");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users LIMIT 5");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                }
                System.out.println("");
            }

            System.out.println("\n--- APPOINTMENTS ---");
            rs = st.executeQuery("SELECT * FROM appointment LIMIT 5");
            rsmd = rs.getMetaData();
            columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
