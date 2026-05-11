package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/shefaa";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à la base 'shefaa' établie");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}