package scratch;

import utils.myDataBase;
import java.sql.*;

public class AddDoctors {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.err.println("Failed to connect to database.");
            return;
        }

        try {
            Statement st = con.createStatement();
            
            // 1. Add columns if they don't exist
            try { st.executeUpdate("ALTER TABLE user ADD COLUMN specialite VARCHAR(100)"); } catch (SQLException e) {}
            try { st.executeUpdate("ALTER TABLE user ADD COLUMN prenom VARCHAR(100)"); } catch (SQLException e) {}
            try { st.executeUpdate("ALTER TABLE user ADD COLUMN nom VARCHAR(100)"); } catch (SQLException e) {}
            
            // 2. Insert sample doctors
            String[][] doctors = {
                {"Cardiologue", "Ahmed", "Mansour", "medecin"},
                {"Pédiatre", "Sami", "Ben Ali", "medecin"},
                {"Généraliste", "Lina", "Gharbi", "medecin"},
                {"Dermatologue", "Yassine", "Zaid", "medecin"},
                {"Cardiologue", "Selma", "Brahim", "medecin"}
            };

            String insertSql = "INSERT INTO user (specialite, prenom, nom, role) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertSql);
            
            for (String[] d : doctors) {
                pst.setString(1, d[0]);
                pst.setString(2, d[1]);
                pst.setString(3, d[2]);
                pst.setString(4, d[3]);
                pst.executeUpdate();
                System.out.println("Added: Dr. " + d[1] + " " + d[2] + " (" + d[0] + ")");
            }

            System.out.println("Sample doctors added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
