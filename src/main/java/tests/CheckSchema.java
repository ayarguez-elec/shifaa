package tests;

import utils.MyDataBase;
import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("No connection");
            return;
        }
        try {
            System.out.println("--- TABLES ---");
            DatabaseMetaData meta = cnx.getMetaData();
            ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println(tableName);
                System.out.println(" Columns for " + tableName + ":");
                ResultSet crs = meta.getColumns(null, null, tableName, "%");
                while (crs.next()) {
                    System.out.println("  - " + crs.getString("COLUMN_NAME") + " (" + crs.getString("TYPE_NAME") + ")");
                }
                crs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
