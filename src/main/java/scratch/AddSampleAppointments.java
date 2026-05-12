package scratch;

import entities.appointment;
import services.appointmentServices;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

public class AddSampleAppointments {
    public static void main(String[] args) {
        appointmentServices service = new appointmentServices();
        int doctorId = 1; // Dr. John Doe selon la capture d'écran
        int patientId = 14; // Aya Nguez

        LocalDate today = LocalDate.now();
        
        try {
            // Rendez-vous 1: Aujourd'hui à 09:00
            appointment a1 = new appointment();
            a1.setDoctorId(doctorId);
            a1.setPatientId(patientId);
            a1.setAppointmentDate(Date.valueOf(today));
            a1.setAppointmentTime(Time.valueOf("09:00:00"));
            a1.setType("Suivi");
            a1.setReason("Contrôle post-opératoire");
            a1.setPaymentMethodFromString("espèce");
            a1.setStatus("CONFIRME");
            service.add(a1);

            // Rendez-vous 2: Aujourd'hui à 11:30
            appointment a2 = new appointment();
            a2.setDoctorId(doctorId);
            a2.setPatientId(11); // Olivia Taylor
            a2.setAppointmentDate(Date.valueOf(today));
            a2.setAppointmentTime(Time.valueOf("11:30:00"));
            a2.setType("Urgence");
            a2.setReason("Forte fièvre et toux");
            a2.setPaymentMethodFromString("carte bancaire");
            a2.setStatus("EN_ATTENTE");
            service.add(a2);

            // Rendez-vous 3: Aujourd'hui à 14:00
            appointment a3 = new appointment();
            a3.setDoctorId(doctorId);
            a3.setPatientId(5); // Emma Thompson
            a3.setAppointmentDate(Date.valueOf(today));
            a3.setAppointmentTime(Time.valueOf("14:00:00"));
            a3.setType("Consultation");
            a3.setReason("Renouvellement ordonnance");
            a3.setPaymentMethodFromString("espèce");
            a3.setStatus("EN_ATTENTE");
            service.add(a3);

            System.out.println("✅ 3 rendez-vous de test ajoutés pour le Dr. ID 1 aujourd'hui !");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
