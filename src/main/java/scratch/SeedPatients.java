package scratch;

import utils.myDataBase;
import java.sql.*;

public class SeedPatients {
    public static void main(String[] args) {
        try {
            Connection con = myDataBase.getInstance().getConnection();
            String[][] patients = {
                {"Aya", "Rguez"},
                {"Youssef", "Kallel"},
                {"Amine", "Trabelsi"},
                {"Sonia", "Masmoudi"}
            };
            
            // Try users table first
            String insertSql = "INSERT INTO users (firstName, lastName, prenom, nom, role) VALUES (?, ?, ?, ?, 'patient')";
            try (PreparedStatement pst = con.prepareStatement(insertSql)) {
                for (String[] p : patients) {
                    pst.setString(1, p[0]);
                    pst.setString(2, p[1]);
                    pst.setString(3, p[0]);
                    pst.setString(4, p[1]);
                    pst.executeUpdate();
                }
                System.out.println("✓ Patients seeded into users table.");
            } catch (SQLException e1) {
                // Try user table
                String insertSql2 = "INSERT INTO user (prenom, nom, role) VALUES (?, ?, 'patient')";
                try (PreparedStatement pst = con.prepareStatement(insertSql2)) {
                    for (String[] p : patients) {
                        pst.setString(1, p[0]);
                        pst.setString(2, p[1]);
                        pst.executeUpdate();
                    }
                    System.out.println("✓ Patients seeded into user table.");
                } catch (SQLException e2) {
                    System.err.println("X Failed to seed patients: " + e2.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
