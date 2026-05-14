package Controler;

import entities.userDS;
import entities.medicalFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import services.medicalFileServ;

import java.util.List;

public class MonDossierController {

    // ================= FXML COMPONENTS =================
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtPatientId;
    @FXML private TextField txtIdFile;
    @FXML private TextField txtCreationDate;
    @FXML private TextField txtLastVisit;
    @FXML private TextField txtStatus;
    @FXML private TextArea txtGeneralHistory;
    @FXML private TextArea txtAntecedents;
    @FXML private TextArea txtAllergies;

    // ================= SERVICES =================
    private medicalFileServ medicalFileService;

    // ================= PATIENT CONNECTÉ =================
    private userDS patientConnecte;

    public MonDossierController() {
        medicalFileService = new medicalFileServ();
    }

    // Cette méthode est appelée depuis le dashboard du patient
    public void setPatientConnecte(userDS patient) {
        this.patientConnecte = patient;
        chargerDossier();
    }

    private void chargerDossier() {
        if (patientConnecte == null) {
            showAlert("Erreur", "Aucun patient connecté");
            return;
        }

        // Afficher les informations du patient
        txtNom.setText(patientConnecte.getLastName());
        txtPrenom.setText(patientConnecte.getFirstName());
        txtPatientId.setText(String.valueOf(patientConnecte.getUserId()));

        try {
            // Récupérer le dossier médical du patient
            List<medicalFile> dossiers = medicalFileService.getByPatientId(patientConnecte.getUserId());

            if (dossiers == null || dossiers.isEmpty()) {
                showAlert("Information", "Aucun dossier médical trouvé");
                return;
            }

            // Prendre le dossier le plus récent
            medicalFile mf = dossiers.get(0);
            afficherDossier(mf);

        } catch (Exception e) {
            showAlert("Erreur", "Problème lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherDossier(medicalFile mf) {
        txtIdFile.setText(String.valueOf(mf.getIdfile()));

        txtCreationDate.setText(mf.getCreationdate() != null ? mf.getCreationdate().toString() : "Non renseigné");
        txtLastVisit.setText(mf.getLastvisit() != null ? mf.getLastvisit().toString() : "Non renseigné");

        // Traduire le statut en français
        String status = mf.getPatientstatus();
        if (status != null) {
            switch (status) {
                case "stable":
                    txtStatus.setText("Stable");
                    break;
                case "en amélioration":
                    txtStatus.setText("En amélioration");
                    break;
                case "critique":
                    txtStatus.setText("Critique");
                    break;
                default:
                    txtStatus.setText(status);
            }
        } else {
            txtStatus.setText("Non renseigné");
        }

        txtGeneralHistory.setText(mf.getGeneralhistory() != null ? mf.getGeneralhistory() : "Aucun historique");
        txtAntecedents.setText(mf.getAntecedents() != null ? mf.getAntecedents() : "Aucun antécédent");
        txtAllergies.setText(mf.getAllergies() != null ? mf.getAllergies() : "Aucune allergie");
    }

    @FXML
    public void retourDashboard(ActionEvent event) {
        try {
            // Retourner au dashboard du patient
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardPatient.fxml"));
            Parent root = loader.load();

            DashboardPatientController controller = loader.getController();
            controller.setPatientConnecte(patientConnecte);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("SHIFAA - Dashboard Patient");
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de retourner: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void deconnexion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageConnexion.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SHIFAA - Connexion");
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
