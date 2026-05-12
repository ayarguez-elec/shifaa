package controller;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ReclamationServices;
import tests.MainFx;

import java.util.Optional;

public class UserDeleteReclamationController {

    @FXML
    private Label idLabel;

    @FXML
    private Label subjectLabel;

    @FXML
    private Label messageLabel;

    private final ReclamationServices reclamationServices = new ReclamationServices();

    private Reclamation reclamation;

    @FXML
    public void initialize() {
        reclamation = MainFx.selectedReclamation;

        if (reclamation != null) {
            idLabel.setText(String.valueOf(reclamation.getReclamationId()));
            subjectLabel.setText(reclamation.getSubject());
        }
    }

    @FXML
    private void supprimerReclamation() {
        if (reclamation == null) {
            showWarning("Erreur", "Aucune réclamation sélectionnée.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            reclamationServices.delete(reclamation.getReclamationId());

            showInfo("Succès", "Réclamation supprimée avec succès.");

            MainFx.clearSelection();
            MainFx.changeScene("/userreclamations.fxml");
        }
    }

    @FXML
    private void retourUserReclamations() {
        MainFx.clearSelection();
        MainFx.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void ouvrirUserDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }

    @FXML
    private void ouvrirUserReclamations() {
        MainFx.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void ouvrirUserReponses() {
        MainFx.clearSelection();
        MainFx.changeScene("/userreponses.fxml");
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}