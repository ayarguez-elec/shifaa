package scratch;
import java.sql.*;
import utils.myDataBase;

public class ListTables {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        try {
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                System.out.println(rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
