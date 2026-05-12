package scratch;

import utils.myDataBase;
import java.sql.*;

public class DebugDB {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            if (con == null) {
                System.out.println("Connection failed.");
                return;
            }
            String[] tables = {"user", "users", "patient"};
            for (String table : tables) {
                System.out.println("--- Table: " + table + " ---");
                try (Statement st = con.createStatement()) {
                    // Describe table
                    ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 1");
                    ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        System.out.print(meta.getColumnName(i) + " (" + meta.getColumnTypeName(i) + ") | ");
                    }
                    System.out.println();
                    
                    // Count patients
                    rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE role = 'patient'");
                    if (rs.next()) System.out.println("Patients found: " + rs.getInt(1));
                    
                    // Sample data
                    rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 5");
                    while (rs.next()) {
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            System.out.print(rs.getString(i) + " | ");
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("Error or table not found: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
