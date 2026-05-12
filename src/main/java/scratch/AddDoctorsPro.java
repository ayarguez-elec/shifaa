package scratch;

import utils.myDataBase;
import java.sql.*;

public class AddDoctorsPro {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.err.println("❌ Erreur: Impossible de se connecter à la base de données.");
            return;
        }

        try {
            Statement st = con.createStatement();
            
            // On vérifie sur quelle table on travaille (user ou users)
            String table = "user";
            try {
                st.executeQuery("SELECT 1 FROM user LIMIT 1");
            } catch (SQLException e) {
                table = "users";
            }
            
            System.out.println("ℹ️ Utilisation de la table: " + table);

            // Ajout de nouveaux médecins avec des spécialités variées
            String[][] doctors = {
                {"Cardiologie", "Selma", "Brahimi", "medecin"},
                {"Dermatologie", "Yassine", "Zaid", "medecin"},
                {"Gynécologie", "Amel", "Nasri", "medecin"},
                {"Neurologie", "Sami", "Karray", "medecin"},
                {"Ophtalmologie", "Ines", "Gharbi", "medecin"},
                {"Psychiatrie", "Firas", "Masmoudi", "medecin"},
                {"Radiologie", "Nizar", "Chaker", "medecin"},
                {"Urologie", "Hedi", "Belhaj", "medecin"}
            };

            String insertSql = "INSERT INTO " + table + " (specialite, specialty, prenom, firstName, nom, lastName, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertSql);
            
            int addedCount = 0;
            for (String[] d : doctors) {
                // On vérifie si le médecin existe déjà pour éviter les doublons
                PreparedStatement checkPst = con.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE prenom = ? AND nom = ?");
                checkPst.setString(1, d[1]);
                checkPst.setString(2, d[2]);
                ResultSet rs = checkPst.executeQuery();
                
                if (rs.next() && rs.getInt(1) == 0) {
                    pst.setString(1, d[0]); pst.setString(2, d[0]);
                    pst.setString(3, d[1]); pst.setString(4, d[1]);
                    pst.setString(5, d[2]); pst.setString(6, d[2]);
                    pst.setString(7, d[3]);
                    pst.executeUpdate();
                    System.out.println("✅ Ajouté: Dr. " + d[1] + " " + d[2] + " (" + d[0] + ")");
                    addedCount++;
                } else {
                    System.out.println("⚠️ Déjà existant: Dr. " + d[1] + " " + d[2]);
                }
            }

            System.out.println("\n🎉 Opération terminée. " + addedCount + " nouveaux médecins ajoutés.");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
