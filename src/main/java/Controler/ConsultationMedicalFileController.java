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

    // Données courantes pour la modification
    private medicalFile currentMedicalFile;
    private userDS currentPatient;

    private services.userservDS userService = new services.userservDS();

    @FXML
    public void initialize() {
        chargerProfilMedecin();
    }

    private void chargerProfilMedecin() {
        if (lblDoctorName != null) lblDoctorName.setText("Dr. Saif Gharbi");
        java.util.List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty() && imgDoctor != null) {
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
        if (patient != null && lblPatientName != null) {
            String name = patient.getFullName();
            if (name == null || name.trim().isEmpty() || name.equals("null null")) {
                lblPatientName.setText("Patient Inconnu");
            } else {
                lblPatientName.setText(name);
            }
        }
        
        if (mf != null) {
            if (lblDossierId != null) lblDossierId.setText("Dossier #" + mf.getIdfile());
            if (lblCreationDate != null) lblCreationDate.setText(mf.getCreationdate() != null ? mf.getCreationdate().toString() : "N/A");
            if (lblLastVisit != null) lblLastVisit.setText(mf.getLastvisit() != null ? mf.getLastvisit().toString() : "N/A");
            
            if (lblStatus != null) {
                String status = mf.getPatientstatus() != null ? mf.getPatientstatus().toLowerCase() : "stable";
                lblStatus.setText(status.toUpperCase());
                
                // Coloration dynamique du badge de statut
                if (status.contains("stable")) {
                    lblStatus.setStyle("-fx-background-color: #C6F6D5; -fx-text-fill: #22543D; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20;");
                } else if (status.contains("amelioration") || status.contains("amélioration")) {
                    lblStatus.setStyle("-fx-background-color: #FEFCBF; -fx-text-fill: #744210; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20;");
                } else if (status.contains("critique")) {
                    lblStatus.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #822727; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20;");
                } else {
                    lblStatus.setStyle("-fx-background-color: #EDF2F7; -fx-text-fill: #2D3748; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20;");
                }
            }

            if (txtHistory != null) txtHistory.setText(mf.getGeneralhistory() != null ? mf.getGeneralhistory() : "Aucun historique");
            if (txtAntecedents != null) txtAntecedents.setText(mf.getAntecedents() != null ? mf.getAntecedents() : "Aucun antécédent");
            if (txtAllergies != null) txtAllergies.setText(mf.getAllergies() != null ? mf.getAllergies() : "Aucune allergie");
            if (txtFollowup != null) txtFollowup.setText(mf.getFollowup() != null ? mf.getFollowup() : "Aucun suivi");
        }

        // Stocker les données pour la modification ultérieure
        this.currentMedicalFile = mf;
        this.currentPatient = patient;

        // Charger la photo
        String b64 = patient.getUserphoto();
        if (b64 != null && !b64.trim().isEmpty() && patientPhoto != null) {
            try {
                // Essayer d'abord comme Base64
                if (b64.length() > 50) { // Probablement du Base64
                    String clean = b64.trim().replaceAll("\\s+", "");
                    byte[] bytes = java.util.Base64.getDecoder().decode(clean);
                    patientPhoto.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else {
                    // Sinon essayer comme fichier
                    File file = new File(b64);
                    if (file.exists()) {
                        patientPhoto.setImage(new Image(file.toURI().toString()));
                    } else if (getClass().getResourceAsStream("/" + b64) != null) {
                        patientPhoto.setImage(new Image(getClass().getResourceAsStream("/" + b64)));
                    }
                }
            } catch (Exception e) {
                patientPhoto.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            }
        } else if (patientPhoto != null) {
            patientPhoto.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        }
    }

    @FXML
    public void closeWindow(javafx.event.ActionEvent event) {
        // Retour vers la liste du portail docteur
        tests.Main.showDoctorPatients();
    }

    @FXML
    public void ouvrirModification(javafx.event.ActionEvent event) {
        if (currentMedicalFile != null && currentPatient != null) {
            tests.Main.showModifierDossier(currentMedicalFile, currentPatient);
        }
    }

    @FXML
    private void supprimerDossier() {
        if (currentMedicalFile == null) return;

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce dossier médical ?");
        alert.setContentText("Cette action est irréversible. Voulez-vous continuer ?");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                services.medicalFileServ service = new services.medicalFileServ();
                service.delete(currentMedicalFile.getIdfile());
                
                // Retourner à la liste des patients
                tests.Main.showDoctorPatients();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
