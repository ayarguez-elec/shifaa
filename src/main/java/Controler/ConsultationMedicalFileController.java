package Controler;

import entities.userDS;
import entities.medicalFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;

public class ConsultationMedicalFileController {

    @FXML private Label lblPatientName;
    @FXML private Label lblDossierId;
    @FXML private Label lblCreationDate;
    @FXML private Label lblLastVisit;
    @FXML private Label lblStatus;
    @FXML private Label txtHistory;
    @FXML private Label txtAntecedents;
    @FXML private Label txtAllergies;
    @FXML private Label txtFollowup;
    @FXML private ImageView patientPhoto;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;

    private services.userservDS userService = new services.userservDS();

    @FXML
    public void initialize() {
        chargerProfilMedecin();
    }

    private void chargerProfilMedecin() {
        lblDoctorName.setText("Dr. Saif Gharbi");
        java.util.List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty()) {
                    try {
                        java.io.File file = new java.io.File(photoPath);
                        if (file.exists()) {
                            imgDoctor.setImage(new Image(file.toURI().toString()));
                        } else if (getClass().getResource("/" + photoPath) != null) {
                            imgDoctor.setImage(new Image(getClass().getResourceAsStream("/" + photoPath)));
                        }
                    } catch (Exception e) {}
                }
                break;
            }
        }
    }

    public void setDossierData(medicalFile mf, userDS patient) {
        lblPatientName.setText(patient.getFullName());
        lblDossierId.setText("Dossier #" + mf.getIdfile());
        lblCreationDate.setText(mf.getCreationdate() != null ? mf.getCreationdate().toString() : "N/A");
        lblLastVisit.setText(mf.getLastvisit() != null ? mf.getLastvisit().toString() : "N/A");
        lblStatus.setText(mf.getPatientstatus() != null ? mf.getPatientstatus() : "stable");

        txtHistory.setText(mf.getGeneralhistory() != null ? mf.getGeneralhistory() : "Aucun historique");
        txtAntecedents.setText(mf.getAntecedents() != null ? mf.getAntecedents() : "Aucun antécédent");
        txtAllergies.setText(mf.getAllergies() != null ? mf.getAllergies() : "Aucune allergie");
        txtFollowup.setText(mf.getFollowup() != null ? mf.getFollowup() : "Aucun suivi");

        // Charger la photo
        String photoPath = patient.getUserphoto();
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            try {
                File file = new File(photoPath);
                if (file.exists()) {
                    patientPhoto.setImage(new Image(file.toURI().toString()));
                } else if (getClass().getResourceAsStream("/" + photoPath) != null) {
                    patientPhoto.setImage(new Image(getClass().getResourceAsStream("/" + photoPath)));
                }
            } catch (Exception e) {
                // Image par défaut si erreur
            }
        }
    }

    @FXML
    public void closeWindow(javafx.event.ActionEvent event) {
        ((javafx.scene.Node) event.getSource()).getScene().getWindow().hide();
    }
}
