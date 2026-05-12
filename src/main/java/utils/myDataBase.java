package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class myDataBase {
    final String USERNAME = "root";
    final String URL = "jdbc:mysql://localhost:3306/shefaa";
    final String PASSWORD = "";
    Connection connection;
    static myDataBase instance;

    private myDataBase() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/shefaa", "root", "");
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static myDataBase getInstance() {
        if (instance == null) {
            instance = new myDataBase();
        }

        return instance;
    }

    public Connection getConnection() {
        return this.connection;
    }
}