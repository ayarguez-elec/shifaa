package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    // ── Modifiez ces valeurs selon votre config MySQL ─────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/sante_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // ← mettez votre mot de passe ici
    // ─────────────────────────────────────────────────────────────────────────

    private static MyDataBase instance;
    private Connection connection;

    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion MySQL établie.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            System.err.println("   → Vérifiez que MySQL tourne et que la base 'sante_db' existe.");
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}