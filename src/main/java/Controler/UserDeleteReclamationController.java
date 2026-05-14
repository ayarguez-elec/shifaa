package Controler;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ReclamationServices;
import tests.Main;

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
        reclamation = tests.Main.selectedReclamation;

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

            tests.Main.clearSelection();
            tests.Main.changeScene("/userreclamations.fxml");
        }
    }

    @FXML
    private void retourUserReclamations() {
        tests.Main.clearSelection();
        tests.Main.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void ouvrirUserDashboard() {
        tests.Main.changeScene("/userdashboard.fxml");
    }

    @FXML
    private void ouvrirUserReclamations() {
        tests.Main.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void ouvrirUserReponses() {
        tests.Main.clearSelection();
        tests.Main.changeScene("/userreponses.fxml");
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
