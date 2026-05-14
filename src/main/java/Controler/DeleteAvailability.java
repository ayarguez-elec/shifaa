package Controler;

import entities.availability;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.availabilityServices;
import java.sql.SQLException;

public class DeleteAvailability {

    @FXML private TextField availabilityIdField;
    @FXML private TextField previewDoctor, previewDate, previewTime, previewStatus;
    @FXML private Label messageLabel;

    private final availabilityServices service = new availabilityServices();
    private availability current;

    @FXML
    void rechercherDisponibilite() {
        try {
            int id = Integer.parseInt(availabilityIdField.getText().trim());

            // ✅ CORRECTION : getAvailabilityById() au lieu de getById()
            current = service.getAvailabilityById(id);

            if (current == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Aucune disponibilité trouvée avec l'ID " + id);
                clearPreview();
            } else {
                previewDoctor.setText(String.valueOf(current.getDoctorId()));
                previewDate.setText(String.valueOf(current.getDate()));
                previewTime.setText(String.valueOf(current.getTime()));

                // ✅ CORRECTION : getStatusRDVAsString() car getStatusRDV() retourne un enum
                previewStatus.setText(current.getStatusRDVAsString());

                messageLabel.setStyle("-fx-text-fill: #2D3748;");
                messageLabel.setText("Disponibilité trouvée. Cliquez sur Supprimer pour confirmer.");
            }
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID invalide.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void supprimerDisponibilite() {
        if (current == null) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Veuillez d'abord rechercher une disponibilité.");
            return;
        }
        try {
            service.delete(current);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Disponibilité #" + current.getAvailabilityId() + " supprimée !");
            clearPreview();
            availabilityIdField.clear();
            current = null;
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    private void clearPreview() {
        previewDoctor.clear();
        previewDate.clear();
        previewTime.clear();
        previewStatus.clear();
    }
}
