package Controler;

import entities.Prescription;
import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import services.Prescriptionserv;
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MesOrdonnancesPatientController implements Initializable {

    @FXML private VBox ordonnancesList;

    private Prescriptionserv prescriptionService = new Prescriptionserv();
    private userServices userService = new userServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerMesOrdonnances();
    }

    private void chargerMesOrdonnances() {
        ordonnancesList.getChildren().clear();
        int patientId = Main.currentUserId;
        
        try {
            List<Prescription> ordonnances = prescriptionService.getByPatientId(patientId);
            
            if (ordonnances.isEmpty()) {
                Label emptyLabel = new Label("Vous n'avez pas encore d'ordonnance enregistrée.");
                emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #718096; -fx-padding: 50;");
                ordonnancesList.getChildren().add(emptyLabel);
                return;
            }

            List<user> allUsers = userService.getAll();
            
            for (Prescription p : ordonnances) {
                user doctor = findUserById(allUsers, p.getDoctorId());
                ordonnancesList.getChildren().add(createOrdonnanceCard(p, doctor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private user findUserById(List<user> users, int id) {
        for (user u : users) {
            if (u.getUserId() == id) return u;
        }
        return null;
    }

    private HBox createOrdonnanceCard(Prescription p, user doctor) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        // Icon Area
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinWidth(60);
        iconBox.setMinHeight(60);
        iconBox.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 12;");
        Label iconLabel = new Label("💊");
        iconLabel.setStyle("-fx-font-size: 24;");
        iconBox.getChildren().add(iconLabel);
        
        // Info Area
        VBox infoBox = new VBox(5);
        String doctorName = (doctor != null) ? "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() : "Médecin Inconnu";
        String specialty = (doctor != null && doctor.getSpecialty() != null) ? doctor.getSpecialty() : "Médecin";
        
        Label lblDoctor = new Label(doctorName);
        lblDoctor.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
        
        Label lblSpecialty = new Label(specialty);
        lblSpecialty.setStyle("-fx-font-size: 14; -fx-text-fill: #FF5C5C; -fx-font-weight: bold;");
        
        Label lblDate = new Label("Prescrit le : " + p.getPrescriptionDate().toString());
        lblDate.setStyle("-fx-font-size: 13; -fx-text-fill: #718096;");
        
        infoBox.getChildren().addAll(lblDoctor, lblSpecialty, lblDate);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Button
        Button btnVoir = new Button("Détails");
        btnVoir.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnVoir.setOnAction(e -> {
            Main.showConsultationPrescriptionPatient(p);
        });
        
        card.getChildren().addAll(iconBox, infoBox, spacer, btnVoir);
        
        return card;
    }
}
