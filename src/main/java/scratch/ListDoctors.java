package scratch;

import utils.myDataBase;
import java.sql.*;

public class ListDoctors {
    public static void main(String[] args) {
        Connection con = myDataBase.getInstance().getConnection();
        if (con == null) {
            System.err.println("❌ Erreur: Impossible de se connecter à la base 'shefaa'.");
            return;
        }

        System.out.println("🔍 Recherche des médecins dans la base 'shefaa'...\n");
        
        String[] tables = {"user", "users", "doctor"};
        boolean foundAny = false;

        for (String table : tables) {
            try {
                Statement st = con.createStatement();
                ResultSet rs;
                
                System.out.println("--- Table: " + table + " ---");
                
                if (table.equals("doctor")) {
                    rs = st.executeQuery("SELECT id, nom, specialite FROM doctor");
                } else if (table.equals("users")) {
                    rs = st.executeQuery("SELECT specialty, userId, firstName, lastName FROM users WHERE role = 'doctor'");
                } else {
                    rs = st.executeQuery("SELECT specialty, id, firstName, lastName FROM " + table + " WHERE role IN ('medecin', 'doctor', 'DOCTOR')");
                }

                int count = 0;
                while (rs.next()) {
                    count++;
                    foundAny = true;
                    String spec, idStr, name;

                    if (table.equals("doctor")) {
                        idStr = rs.getString(1);
                        name = rs.getString(2);
                        spec = rs.getString(3);
                    } else {
                        spec = rs.getString(1);
                        idStr = rs.getString(2);
                        name = rs.getString(3) + " " + rs.getString(4);
                    }

                    System.out.println(String.format("[%s] Dr. %-20s | Spécialité: %s", idStr, name, spec));
                }
                
                if (count == 0) System.out.println("(Aucun médecin trouvé)");
                System.out.println();
                
            } catch (SQLException e) {
                // Table doesn't exist or columns differ, skip
            }
        }

        if (!foundAny) {
            System.out.println("❌ Aucun médecin n'a été trouvé dans les tables scannées.");
            System.out.println("Suggestion: Exécutez 'scratch.AddDoctorsPro' pour en ajouter.");
        }
    }
}
