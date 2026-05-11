package Controler;

import entities.medicalFile;
import entities.userDS;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.medicalFileServ;
import services.userservDS;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MonDossierMedicalController {

    @FXML private Label lblStatus;
    @FXML private Label lblLastVisit;
    @FXML private Label lblCreationDate;
    @FXML private Label lblHistory;
    @FXML private Label lblAntecedents;
    @FXML private Label lblAllergies;
    @FXML private Label lblFollowup;
    
    // Sidebar components
    @FXML private ImageView imgPatient;
    @FXML private Circle circleImage;
    @FXML private Label lblPatientName;

    private medicalFileServ medicalService = new medicalFileServ();
    private userservDS userService = new userservDS();
    private int currentPatientId;

    public void chargerDonneesPatient(int patientId) {
        this.currentPatientId = patientId;
        
        // Charger le dossier
        List<medicalFile> dossiers = medicalService.getByPatientId(patientId);
        if (dossiers != null && !dossiers.isEmpty()) {
            medicalFile mf = dossiers.get(0);
            lblStatus.setText(mf.getPatientstatus() != null ? mf.getPatientstatus() : "N/A");
            lblLastVisit.setText(mf.getLastvisit() != null ? mf.getLastvisit().toString() : "Aucune visite enregistrée");
            lblCreationDate.setText(mf.getCreationdate() != null ? mf.getCreationdate().toString() : "N/A");
            lblHistory.setText(mf.getGeneralhistory() != null && !mf.getGeneralhistory().isEmpty() ? mf.getGeneralhistory() : "Aucune information");
            lblAntecedents.setText(mf.getAntecedents() != null && !mf.getAntecedents().isEmpty() ? mf.getAntecedents() : "Aucun antécédent déclaré");
            lblAllergies.setText(mf.getAllergies() != null && !mf.getAllergies().isEmpty() ? mf.getAllergies() : "Aucune allergie connue");
            lblFollowup.setText(mf.getFollowup() != null && !mf.getFollowup().isEmpty() ? mf.getFollowup() : "Pas de suivi particulier");
        } else {
            lblHistory.setText("Aucun dossier médical n'a encore été créé pour vous.");
        }
        
        // Charger la sidebar
        chargerSidebar(patientId);
    }

    private void chargerSidebar(int patientId) {
        userDS user = userService.getById(patientId);
        if (user != null) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            lblPatientName.setText(fullName);
            
            String photoPath = user.getUserphoto();
            if (photoPath != null && !photoPath.trim().isEmpty()) {
                try {
                    Image image = null;
                    File file = new File(photoPath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else if (getClass().getResource("/" + photoPath) != null) {
                        image = new Image(getClass().getResourceAsStream("/" + photoPath));
                    }
                    
                    if (image != null) {
                        imgPatient.setImage(image);
                        circleImage.setFill(new ImagePattern(image));
                        imgPatient.setVisible(false);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur photo sidebar: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void allerAuDashboard(Event event) {
        retourDashboard(new ActionEvent(event.getSource(), null));
    }

    @FXML
    public void ouvrirMesPrescriptions(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesPrescriptions.fxml"));
            Parent root = loader.load();
            MesPrescriptionsController controller = loader.getController();
            controller.chargerPrescriptionsPatient(currentPatientId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Ordonnances");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void retourDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PatientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard Patient");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
