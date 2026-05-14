package Controler;

import entities.medicalFile;
import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import services.medicalFileServ;
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MesDossiersPatientController implements Initializable {

    @FXML private VBox dossiersList;

    private medicalFileServ medicalService = new medicalFileServ();
    private userServices userService = new userServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerMesDossiers();
    }

    private void chargerMesDossiers() {
        dossiersList.getChildren().clear();
        int patientId = Main.currentUserId;
        
        List<medicalFile> dossiers = medicalService.getByPatientId(patientId);
        
        if (dossiers.isEmpty()) {
            Label emptyLabel = new Label("Vous n'avez pas encore de dossier médical enregistré.");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #718096; -fx-padding: 50;");
            dossiersList.getChildren().add(emptyLabel);
            return;
        }

        try {
            List<user> allUsers = userService.getAll();
            
            for (medicalFile mf : dossiers) {
                user doctor = findUserById(allUsers, mf.getDoctorId());
                dossiersList.getChildren().add(createDossierCard(mf, doctor));
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

    private HBox createDossierCard(medicalFile mf, user doctor) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        // Icon Area
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinWidth(60);
        iconBox.setMinHeight(60);
        iconBox.setStyle("-fx-background-color: #EBF8FF; -fx-background-radius: 12;");
        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 24;");
        iconBox.getChildren().add(iconLabel);
        
        // Info Area
        VBox infoBox = new VBox(5);
        String doctorName = (doctor != null) ? "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() : "Médecin Inconnu";
        String specialty = (doctor != null && doctor.getSpecialty() != null) ? doctor.getSpecialty() : "Spécialité non renseignée";
        
        Label lblDoctor = new Label(doctorName);
        lblDoctor.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
        
        Label lblSpecialty = new Label(specialty);
        lblSpecialty.setStyle("-fx-font-size: 14; -fx-text-fill: #FF5C5C; -fx-font-weight: bold;");
        
        Label lblDate = new Label("Créé le : " + mf.getCreationdate().toString() + " | Statut : " + mf.getPatientstatus());
        lblDate.setStyle("-fx-font-size: 13; -fx-text-fill: #718096;");
        
        infoBox.getChildren().addAll(lblDoctor, lblSpecialty, lblDate);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Button
        Button btnVoir = new Button("Consulter");
        btnVoir.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnVoir.setOnAction(e -> {
            Main.showMonDossierMedicalDetail(mf.getIdfile());
        });
        
        card.getChildren().addAll(iconBox, infoBox, spacer, btnVoir);
        
        return card;
    }
}
