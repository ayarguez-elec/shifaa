package scratch;

import entities.availability;
import services.availabilityServices;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class AddSampleAvailabilities {
    public static void main(String[] args) {
        availabilityServices service = new availabilityServices();
        
        // ID du médecin à qui on ajoute des disponibilités (ex: Dr. John Doe)
        int doctorId = 1; 

        LocalDate today = LocalDate.now();
        
        try {
            System.out.println("⏳ Ajout de disponibilités pour le médecin ID " + doctorId + "...");

            // 1. Demain matin (09:00 - Libre)
            availability a1 = new availability();
            a1.setDoctorId(doctorId);
            a1.setDate(Date.valueOf(today.plusDays(1)));
            a1.setTime(Time.valueOf("09:00:00"));
            a1.setStatusRDVFromString("libre");
            service.add(a1);

            // 2. Demain matin (10:30 - Libre)
            availability a2 = new availability();
            a2.setDoctorId(doctorId);
            a2.setDate(Date.valueOf(today.plusDays(1)));
            a2.setTime(Time.valueOf("10:30:00"));
            a2.setStatusRDVFromString("libre");
            service.add(a2);

            // 3. Après-demain (14:00 - Occupé)
            availability a3 = new availability();
            a3.setDoctorId(doctorId);
            a3.setDate(Date.valueOf(today.plusDays(2)));
            a3.setTime(Time.valueOf("14:00:00"));
            a3.setStatusRDVFromString("occupe");
            service.add(a3);
            
            // 4. Dans 3 jours (11:00 - Libre)
            availability a4 = new availability();
            a4.setDoctorId(doctorId);
            a4.setDate(Date.valueOf(today.plusDays(3)));
            a4.setTime(Time.valueOf("11:00:00"));
            a4.setStatusRDVFromString("libre");
            service.add(a4);

            System.out.println("✅ Succès : 4 créneaux de disponibilité ont été ajoutés !");
            System.out.println("Vous pouvez maintenant ouvrir le DashboardPatient pour voir le calendrier se colorer.");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
