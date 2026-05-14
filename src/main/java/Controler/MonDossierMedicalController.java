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
    @FXML private Label lblDoctorName;
    @FXML private Label lblDoctorSpeciality;
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
        
        // Charger le premier dossier par défaut
        List<medicalFile> dossiers = medicalService.getByPatientId(patientId);
        if (dossiers != null && !dossiers.isEmpty()) {
            afficherDossier(dossiers.get(0));
        } else {
            afficherDossierVide();
        }
        
        // Charger la sidebar
        chargerSidebar(patientId);
    }

    public void chargerDossierSpecifique(int idfile) {
        medicalFile mf = medicalService.getById(idfile);
        if (mf != null) {
            this.currentPatientId = mf.getPatientId();
            afficherDossier(mf);
            chargerSidebar(currentPatientId);
        }
    }

    private void afficherDossier(medicalFile mf) {
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
        
        if (lblLastVisit != null) lblLastVisit.setText(mf.getLastvisit() != null ? mf.getLastvisit().toString() : "Aucune visite enregistrée");
        if (lblCreationDate != null) lblCreationDate.setText(mf.getCreationdate() != null ? mf.getCreationdate().toString() : "N/A");
        if (lblHistory != null) lblHistory.setText(mf.getGeneralhistory() != null && !mf.getGeneralhistory().isEmpty() ? mf.getGeneralhistory() : "Aucune information");
        if (lblAntecedents != null) lblAntecedents.setText(mf.getAntecedents() != null && !mf.getAntecedents().isEmpty() ? mf.getAntecedents() : "Aucun antécédent déclaré");
        if (lblAllergies != null) lblAllergies.setText(mf.getAllergies() != null && !mf.getAllergies().isEmpty() ? mf.getAllergies() : "Aucune allergie connue");
        if (lblFollowup != null) lblFollowup.setText(mf.getFollowup() != null && !mf.getFollowup().isEmpty() ? mf.getFollowup() : "Pas de suivi particulier");
        
        // Charger le médecin traitant
        chargerInfosMedecin(mf.getDoctorId());
    }

    private void afficherDossierVide() {
        lblStatus.setText("-");
        lblLastVisit.setText("-");
        lblCreationDate.setText("-");
        lblHistory.setText("Aucun dossier trouvé.");
        lblAntecedents.setText("-");
        lblAllergies.setText("-");
        lblFollowup.setText("-");
        chargerInfosMedecin(0);
    }
    private void chargerInfosMedecin(int doctorId) {
        try (java.sql.Connection con = utils.DatabaseConnection.getInstance().getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            
            java.sql.ResultSet rs = null;
            if (doctorId > 0) {
                // Chercher le médecin associé à ce dossier
                rs = stmt.executeQuery("SELECT * FROM user WHERE userId = " + doctorId + " AND (role = 'doctor' OR role = 'médecin')");
            }
            
            if (rs == null || !rs.next()) {
                // S'il n'a pas de médecin associé (ou ancien dossier), on prend le premier médecin du système
                rs = stmt.executeQuery("SELECT * FROM user WHERE role = 'doctor' OR role = 'médecin' LIMIT 1");
                if (!rs.next()) {
                    if (lblDoctorName != null) lblDoctorName.setText("Non assigné");
                    if (lblDoctorSpeciality != null) lblDoctorSpeciality.setText("-");
                    return;
                }
            }
            
            String prenom = rs.getString("firstName");
            String nom = rs.getString("lastName");
            String specialite = null;
            
            try { specialite = rs.getString("specialty"); } catch(Exception e) {}
            if (specialite == null) {
                try { specialite = rs.getString("speciality"); } catch(Exception e) {}
            }
            
            if (lblDoctorName != null) lblDoctorName.setText("Dr. " + prenom + " " + nom);
            if (lblDoctorSpeciality != null) lblDoctorSpeciality.setText(specialite != null && !specialite.trim().isEmpty() ? specialite : "Médecin Généraliste");
            
        } catch (Exception e) {
            e.printStackTrace();
            if (lblDoctorName != null) lblDoctorName.setText("Erreur de chargement");
        }
    }

    private void chargerSidebar(int patientId) {
        userDS user = userService.getById(patientId);
        if (user != null) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            if (lblPatientName != null) lblPatientName.setText(fullName);
            
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
                        if (imgPatient != null) imgPatient.setImage(image);
                        if (circleImage != null) circleImage.setFill(new ImagePattern(image));
                        if (imgPatient != null) imgPatient.setVisible(false);
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
    private void retourDashboard(ActionEvent event) {
        tests.Main.showPatientMedicalFiles();
    }
}
