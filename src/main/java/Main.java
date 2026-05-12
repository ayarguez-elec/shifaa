import entities.appointment;
import services.appointmentServices;
import entities.availability;
import entities.availability.StatusRDV;
import services.availabilityServices;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
public class Main {
    public static void main(String[] args) {
       /*try {
            // ajouter un rdv
            appointmentServices appointmentServices = new appointmentServices();

            Date appointmentDate = Date.valueOf(LocalDate.of(2026, 4, 25)); // 25 avril 2026
            Time appointmentTime = Time.valueOf(LocalTime.of(14, 30)); // 14:30 (2:30 PM)
            String reason = "Consultation de routine";
            String type = "Consultation";
            appointment.PaymentMethod paymentMethod = appointment.PaymentMethod.carte_bancaire;
            int patientId = 1;  // Assurez-vous que ce patient existe dans la base de données
            int doctorId = 2;   // Assurez-vous que ce médecin existe dans la base de données

            appointment newAppointment = new appointment(
                    appointmentDate,
                    appointmentTime,
                    reason,
                    type,
                    paymentMethod,
                    patientId,
                    doctorId
            );

            appointmentServices.add(newAppointment);

            System.out.println("Test réussi ! Le rendez-vous a été ajouté.");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du rendez-vous : " + e.getMessage());
            e.printStackTrace();
        }
    } */
        // update rdv
        /*testUpdateAppointment();
    }

    public static void testUpdateAppointment() {
        try {
            appointmentServices appointmentServices = new appointmentServices();

            // Étape 1: Afficher tous les rendez-vous existants
            System.out.println("=== Liste des rendez-vous avant mise à jour ===");
            for (appointment a : appointmentServices.getAll()) {
                System.out.println("ID: " + a.getAppointmentId() +
                        " | Date: " + a.getAppointmentDate() +
                        " | Heure: " + a.getAppointmentTime() +
                        " | Raison: " + a.getReason() +
                        " | Type: " + a.getType() +
                        " | Patient ID: " + a.getPatientId() +
                        " | Médecin ID: " + a.getDoctorId() +
                        " | Paiement: " + a.getPaymentMethodAsString());
            }

            // Étape 2: Récupérer un rendez-vous spécifique à modifier (exemple: ID = 1)
            int appointmentIdToUpdate = 1;
            appointment existingAppointment = appointmentServices.getAppointmentById(appointmentIdToUpdate);

            if (existingAppointment != null) {
                System.out.println("\n=== Rendez-vous à modifier ===");
                System.out.println("ID: " + existingAppointment.getAppointmentId());
                System.out.println("Ancienne date: " + existingAppointment.getAppointmentDate());
                System.out.println("Ancienne heure: " + existingAppointment.getAppointmentTime());
                System.out.println("Ancienne raison: " + existingAppointment.getReason());

                // Étape 3: Modifier les attributs du rendez-vous
                existingAppointment.setAppointmentDate(Date.valueOf(LocalDate.of(2026, 5, 15)));
                existingAppointment.setAppointmentTime(Time.valueOf(LocalTime.of(16, 0)));
                existingAppointment.setReason("Consultation d'urgence modifiée");
                existingAppointment.setType("Urgence modifiée");
                existingAppointment.setPaymentMethod(appointment.PaymentMethod.carte_bancaire);
                existingAppointment.setPatientId(2);
                existingAppointment.setDoctorId(3);

                // Étape 4: Mettre à jour le rendez-vous dans la base de données
                appointmentServices.update(existingAppointment);
                System.out.println("\n✓ Rendez-vous mis à jour avec succès !");

                // Étape 5: Vérifier les modifications
                appointment updatedAppointment = appointmentServices.getAppointmentById(appointmentIdToUpdate);
                System.out.println("\n=== Rendez-vous après mise à jour ===");
                System.out.println("ID: " + updatedAppointment.getAppointmentId());
                System.out.println("Nouvelle date: " + updatedAppointment.getAppointmentDate());
                System.out.println("Nouvelle heure: " + updatedAppointment.getAppointmentTime());
                System.out.println("Nouvelle raison: " + updatedAppointment.getReason());
                System.out.println("Nouveau type: " + updatedAppointment.getType());
                System.out.println("Nouveau mode de paiement: " + updatedAppointment.getPaymentMethodAsString());
                System.out.println("Nouveau patient ID: " + updatedAppointment.getPatientId());
                System.out.println("Nouveau médecin ID: " + updatedAppointment.getDoctorId());
            } else {
                System.out.println("Aucun rendez-vous trouvé avec l'ID: " + appointmentIdToUpdate);
            }

            // Étape 6: Afficher tous les rendez-vous après mise à jour
            System.out.println("\n=== Liste des rendez-vous après mise à jour ===");
            for (appointment a : appointmentServices.getAll()) {
                System.out.println("ID: " + a.getAppointmentId() +
                        " | Date: " + a.getAppointmentDate() +
                        " | Heure: " + a.getAppointmentTime() +
                        " | Raison: " + a.getReason() +
                        " | Type: " + a.getType() +
                        " | Patient ID: " + a.getPatientId() +
                        " | Médecin ID: " + a.getDoctorId() +
                        " | Paiement: " + a.getPaymentMethodAsString());
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        } */
        // ajout et supprimer
                /*try {
                    appointmentServices appointmentServices = new appointmentServices();

                    // Étape 1: Créer et ajouter un nouveau rendez-vous
                    System.out.println("=== Création d'un nouveau rendez-vous ===");
                    appointment newAppointment = new appointment(
                            Date.valueOf(LocalDate.of(2026, 5, 20)),
                            Time.valueOf(LocalTime.of(14, 0)),
                            "Consultation test",
                            "Test",
                            appointment.PaymentMethod.espece,
                            1,
                            2
                    );

                    appointmentServices.add(newAppointment);
                    System.out.println("✓ Nouveau rendez-vous ajouté");

                    // Étape 2: Récupérer le dernier rendez-vous ajouté
                    java.util.List<appointment> allAppointments = appointmentServices.getAll();
                    appointment appointmentToDelete = allAppointments.get(allAppointments.size() - 1);

                    System.out.println("\n=== Rendez-vous créé et prêt à être supprimé ===");
                    System.out.println("ID: " + appointmentToDelete.getAppointmentId());
                    System.out.println("Date: " + appointmentToDelete.getAppointmentDate());
                    System.out.println("Heure: " + appointmentToDelete.getAppointmentTime());
                    System.out.println("Raison: " + appointmentToDelete.getReason());

                    // Étape 3: Supprimer le rendez-vous
                    appointmentServices.delete(appointmentToDelete);
                    System.out.println("\n✓ Rendez-vous supprimé avec succès !");

                    // Étape 4: Vérifier la suppression
                    appointment checkAppointment = appointmentServices.getAppointmentById(appointmentToDelete.getAppointmentId());
                    if (checkAppointment == null) {
                        System.out.println("✓ Vérification réussie: Le rendez-vous a bien été supprimé");
                    }

                } catch (Exception e) {
                    System.err.println("Erreur : " + e.getMessage());
                    e.printStackTrace();
                }*/
        //affichage rdv
        /*try {
            appointmentServices appointmentServices = new appointmentServices();

            // Afficher tous les rendez-vous existants
            System.out.println("=== Liste des rendez-vous existants ===");
            java.util.List<appointment> allAppointments = appointmentServices.getAll();

            if (allAppointments.isEmpty()) {
                System.out.println("Aucun rendez-vous trouvé dans la base de données.");
            } else {
                for (appointment a : allAppointments) {
                    System.out.println("ID: " + a.getAppointmentId() +
                            " | Date: " + a.getAppointmentDate() +
                            " | Heure: " + a.getAppointmentTime() +
                            " | Raison: " + a.getReason() +
                            " | Type: " + a.getType() +
                            " | Patient ID: " + a.getPatientId() +
                            " | Médecin ID: " + a.getDoctorId() +
                            " | Paiement: " + a.getPaymentMethodAsString());
                }
                System.out.println("\nNombre total de rendez-vous: " + allAppointments.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        } */

        /*availabilityServices service = new availabilityServices();
        try {
            List<availability> list = service.getAll();
            for (availability a : list) {
                System.out.println(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
        //add availablilty
        /*try {
            // Créer une instance du service
            availabilityServices service = new availabilityServices();

            // Tester l'ajout d'un créneau de disponibilité
            testAddAvailability(service);

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAddAvailability(availabilityServices service) {
        try {
            Date date = Date.valueOf("2026-06-15");
            Time time = Time.valueOf("14:30:00");
            availability.StatusRDV status = availability.StatusRDV.libre;
            int doctorId = 15;
            availability avail = new availability(date, time, status, doctorId);
            service.add(avail);
            System.out.println("Test réussi ! Le créneau a été ajouté.");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
        }*/
        //update availability

        /*availabilityServices service = new availabilityServices();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // Tester avec l'ID 1 de votre table
            System.out.println("Test update pour availabilityId = 1");

            availability a = service.getAvailabilityById(1);

            if (a != null) {
                System.out.println("Avant update: " + sdf.format(a.getDate()) + " - " + a.getStatusRDVAsString());

                // Changer le statut
                a.setStatusRDVFromString("reserve");

                // Appliquer la mise à jour
                service.update(a);

                // Vérifier
                availability a2 = service.getAvailabilityById(1);
                System.out.println("Après update: " + sdf.format(a2.getDate()) + " - " + a2.getStatusRDVAsString());
                System.out.println(" Test réussi !");
            } else {
                System.out.println(" Aucune donnée trouvée pour l'ID 1");
            }

        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    */
        //delete availibilty
        /*availabilityServices service = new availabilityServices();

        try {
            // Récupérer tous les IDs avant suppression
            List<availability> liste = service.getAll();
            System.out.println("IDs avant suppression:");
            for (availability a : liste) {
                System.out.println("- ID: " + a.getAvailabilityId());
            }

            // Supprimer ID 4
            availability a4 = service.getAvailabilityById(4);
            if (a4 != null) {
                service.delete(a4);
                System.out.println("\n✅ ID 4 supprimé");
            }

            // Supprimer ID 2
            availability a2 = service.getAvailabilityById(2);
            if (a2 != null) {
                service.delete(a2);
                System.out.println("✅ ID 2 supprimé");
            }

            // Afficher les IDs restants
            List<availability> listeFinale = service.getAll();
            System.out.println("\nIDs après suppression:");
            for (availability a : listeFinale) {
                System.out.println("- ID: " + a.getAvailabilityId());
            }

            System.out.println("\nTotal restant: " + listeFinale.size());

        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }*/
    }}