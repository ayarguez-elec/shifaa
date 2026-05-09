package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/shefaa?useSSL=false&serverTimezone=UTC";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private MyDataBase() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion à la base de données établie avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        // Vérifier si la connexion est encore valide
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnexion à la base de données...");
                instance = new MyDataBase();
                connection = instance.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}