package controller;

import entities.Pharmacy;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.PharmacyServices;
import tests.MainFx;

import java.util.Optional;

public class AdminDeletePharmacyController {

    @FXML private TextField pharmacyIdField;
    @FXML private Label messageLabel;

    private static int selectedPharmacyId = 0;

    public static void setSelectedPharmacyId(int id) {
        selectedPharmacyId = id;
    }

    @FXML
    public void initialize() {
        if (selectedPharmacyId != 0) {
            pharmacyIdField.setText(String.valueOf(selectedPharmacyId));
            selectedPharmacyId = 0;
        }
    }

    @FXML
    public void deletePharmacy() {
        try {
            if (pharmacyIdField.getText().trim().isEmpty()) {
                messageLabel.setText("Veuillez entrer l'ID de la pharmacie.");
                showPopup("Erreur", "Veuillez entrer l'ID de la pharmacie à supprimer.", Alert.AlertType.ERROR);
                return;
            }

            int id = Integer.parseInt(pharmacyIdField.getText().trim());

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Voulez-vous vraiment supprimer cette pharmacie ?");

            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setPharmacyId(id);

                PharmacyServices service = new PharmacyServices();
                service.delete(pharmacy);

                messageLabel.setText("Pharmacie supprimée avec succès.");
                showPopup("Succès", "Pharmacie supprimée avec succès.", Alert.AlertType.INFORMATION);

                pharmacyIdField.clear();
            } else {
                messageLabel.setText("Suppression annulée.");
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("ID invalide.");
            showPopup("Erreur", "L'ID doit être un nombre.", Alert.AlertType.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur lors de la suppression.");
            showPopup("Erreur", "Impossible de supprimer la pharmacie.", Alert.AlertType.ERROR);
        }
    }

    private void showPopup(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack() {
        MainFx.changeScene("/adminpharmacies.fxml");
    }
}