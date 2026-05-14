package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static final String URL      = "jdbc:mysql://localhost:3306/shefaa";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static MyDataBase instance;
    private Connection connection;

    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion MySQL établie → shefaa");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            System.err.println("   → Vérifiez que MySQL tourne et que la base 'shefaa' existe.");
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() { return connection; } public Connection getConnection() {
        return connection;
    }
}
