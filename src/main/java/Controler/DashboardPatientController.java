package Controler;

import entities.userDS;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class DashboardPatientController {

    @FXML private Label lblNomPatient;

    private userDS patientConnecte;

    public void setPatientConnecte(userDS patient) {
        this.patientConnecte = patient;
        lblNomPatient.setText("Connecté : " + patient.getFirstName() + " " + patient.getLastName());
    }

    @FXML
    public void consulterDossier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MonDossier.fxml"));
            Parent root = loader.load();

            MonDossierController controller = loader.getController();
            controller.setPatientConnecte(patientConnecte);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("SHIFAA - Mon dossier médical");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void voirRendezVous(ActionEvent event) {
        // À implémenter
    }

    @FXML
    public void contacterSupport(ActionEvent event) {
        // À implémenter
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
            e.printStackTrace();
        }
    }
}