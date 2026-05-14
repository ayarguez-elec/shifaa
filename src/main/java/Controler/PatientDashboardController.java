package Controler;

import entities.Prescription;
import entities.medicalFile;
import entities.userDS;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import services.Prescriptionserv;
import services.medicalFileServ;
import services.userservDS;
import tests.Main;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import entities.CalendarEvent;
import entities.Prescription;
import java.util.Random;

public class PatientDashboardController {

    @FXML private Label lblDate;
    @FXML private Label lblWelcome;
    @FXML private Label lblNextAppointment;
    @FXML private javafx.scene.layout.VBox vboxRappels;
    @FXML private Label lblNextDoctor;
    @FXML private Label lblActivePrescriptions;
    @FXML private Label lblLastVisit;
    @FXML private Label lblTotalDocuments;
    @FXML private Label lblHealthTip;

    private Prescriptionserv prescriptionService = new Prescriptionserv();
    private medicalFileServ medicalFileService = new medicalFileServ();
    private services.userservDS userService = new services.userservDS();
    private services.CalendarEventService calendarService = new services.CalendarEventService();

    private String[] healthTips = {
        "Pensez à boire au moins 1.5L d'eau aujourd'hui pour rester hydraté.",
        "Une marche de 30 minutes par jour améliore considérablement votre santé cardiaque.",
        "Le sommeil est crucial : essayez de dormir entre 7 et 8 heures par nuit.",
        "Réduisez votre consommation de sucre pour maintenir une énergie stable toute la journée.",
        "N'oubliez pas de faire des pauses régulières si vous travaillez devant un écran.",
        "Consommer des fruits et légumes de saison booste votre système immunitaire."
    };

    @FXML
    public void initialize() {
        setCurrentDate();
        loadPatientData();
        loadHealthTip();
    }

    private void setCurrentDate() {
        LocalDate now = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.FRENCH);
        String formattedDate = now.format(formatter);
        lblDate.setText(formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1));
    }

    private void loadPatientData() {
        int patientId = Main.currentUserId;
        userDS patient = userService.getById(patientId);
        
        if (patient != null) {
            lblWelcome.setText("Bonjour, " + patient.getFirstName() + " !");
        }

        try {
            // 1. Charger les ordonnances actives
            List<Prescription> prescriptions = prescriptionService.getByPatientId(patientId);
            lblActivePrescriptions.setText(String.valueOf(prescriptions.size()));
            
            // Affichage dynamique des rappels
            vboxRappels.getChildren().clear();
            if (prescriptions.isEmpty()) {
                vboxRappels.setManaged(false);
                vboxRappels.setVisible(false);
            } else {
                vboxRappels.setManaged(true);
                vboxRappels.setVisible(true);
                // Afficher les 2 dernières prescriptions comme rappels
                int count = 0;
                for (Prescription p : prescriptions) {
                    if (count >= 2) break;
                    vboxRappels.getChildren().add(createRappelNode(p));
                    count++;
                }
            }

            // 2. Charger les infos du dossier médical
            List<medicalFile> dossiers = medicalFileService.getByPatientId(patientId);
            if (!dossiers.isEmpty()) {
                medicalFile lastFile = dossiers.get(0); // La liste est triée par date desc
                lblLastVisit.setText(lastFile.getLastvisit() != null ? lastFile.getLastvisit().toString() : "N/A");
                lblTotalDocuments.setText(String.valueOf(dossiers.size()));
            } else {
                lblLastVisit.setText("Aucune visite");
                lblTotalDocuments.setText("0");
            }

            // 3. Charger le prochain rendez-vous depuis le calendrier
            CalendarEvent nextEvent = calendarService.getNextEventForUser(patientId);
            if (nextEvent != null) {
                lblNextAppointment.setText(nextEvent.getDate().toString() + " à " + nextEvent.getTime());
                lblNextDoctor.setText(nextEvent.getTitle()); // Souvent le titre contient le nom du médecin ou type de rdv
            } else {
                lblNextAppointment.setText("Aucun RDV");
                lblNextDoctor.setText("Prévoyez une consultation");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.Node createRappelNode(Prescription p) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(15);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hbox.setStyle("-fx-background-color: #F0FFF4; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #C6F6D5; -fx-border-radius: 10;");
        
        Label icon = new Label("💊");
        icon.setStyle("-fx-font-size: 24;");
        
        javafx.scene.layout.VBox texts = new javafx.scene.layout.VBox(2);
        Label title = new Label("Traitement : " + p.getPrescription());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #22543D;");
        Label sub = new Label("Durée : " + p.getTreatmentDuration() + " - Ajoutée le " + p.getPrescriptionDate());
        sub.setStyle("-fx-font-size: 12; -fx-text-fill: #2F855A;");
        
        texts.getChildren().addAll(title, sub);
        hbox.getChildren().addAll(icon, texts);
        return hbox;
    }
    private void loadHealthTip() {
        Random random = new Random();
        lblHealthTip.setText(healthTips[random.nextInt(healthTips.length)]);
    }

    @FXML
    public void prendreRDV() {
        System.out.println("Redirection vers la prise de RDV...");
        Main.showPatientRequestAppointment();
    }

    @FXML
    public void consulterDossier() {
        // Redirection vers la consultation du dossier patient
        Main.showPatientMedicalFile();
    }

    @FXML
    public void voirOrdonnances() {
        // Redirection vers la liste des ordonnances du patient
        Main.showPatientPrescriptions();
    }

    @FXML
    public void goToHealthStats() {
        Main.showPatientHealthStats();
    }
}
