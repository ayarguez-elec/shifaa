package tests;

import utils.DatabaseConnection;
import java.sql.*;

public class TestDatabase {

    public static void main(String[] args) {
        System.out.println("=== TEST DE LA BASE DE DONNEES ===");
        System.out.println();

        // 1. Tester la connexion
        testConnexion();

        // 2. Tester la structure de la table user
        testUserTable();

        // 3. Tester la structure de la table medicalfile
        testMedicalFileTable();
    }

    public static void testConnexion() {
        System.out.println("1. TEST CONNEXION");
        System.out.println("----------------");

        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection connection = db.getConnection();

        if (connection != null) {
            System.out.println("✅ Connexion etablie avec succes");
        } else {
            System.out.println("❌ Echec de connexion");
        }
        System.out.println();
    }

    public static void testUserTable() {
        System.out.println("2. STRUCTURE TABLE 'user'");
        System.out.println("------------------------");

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection connection = db.getConnection();

            // Vérifier si la table existe
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, "user", null);

            if (!tables.next()) {
                System.out.println("❌ La table 'user' n'existe pas!");
                return;
            }

            // Afficher les colonnes
            ResultSet columns = meta.getColumns(null, null, "user", null);

            System.out.println("Colonnes de la table 'user':");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                System.out.println("  - " + columnName + " (" + columnType + ")");
            }

            // Afficher le contenu
            System.out.println();
            System.out.println("Contenu de la table 'user':");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM user");

            // Récupérer les noms des colonnes dynamiquement
            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  Patient " + count + ":");
                for (int i = 1; i <= columnCount; i++) {
                    String colName = rsMeta.getColumnName(i);
                    String value = rs.getString(i);
                    System.out.println("    " + colName + " = " + value);
                }
            }

            if (count == 0) {
                System.out.println("  Aucun patient trouve");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    public static void testMedicalFileTable() {
        System.out.println("3. STRUCTURE TABLE 'medicalfile'");
        System.out.println("--------------------------------");

        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection connection = db.getConnection();

            // Vérifier si la table existe
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, "medicalfile", null);

            if (!tables.next()) {
                System.out.println("❌ La table 'medicalfile' n'existe pas!");
                return;
            }

            // Afficher les colonnes
            ResultSet columns = meta.getColumns(null, null, "medicalfile", null);

            System.out.println("Colonnes de la table 'medicalfile':");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                System.out.println("  - " + columnName + " (" + columnType + ")");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}