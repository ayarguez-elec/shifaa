package controller;

import entities.Pharmacy;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.PharmacyServices;
import tests.MainFx;

import java.io.File;
import java.util.Optional;

public class AdminAddPharmacyController {

    @FXML private ImageView imagePreview;
    @FXML private Label imageLabel;

    @FXML private TextField pharmacyNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField openingHoursField;
    @FXML private ComboBox<String> pharmacyStatusCombo;

    private PharmacyServices pharmacyServices;
    private String selectedImagePath;

    @FXML
    public void initialize() {
        pharmacyServices = new PharmacyServices();
        selectedImagePath = "";

        if (pharmacyStatusCombo != null) {
            pharmacyStatusCombo.setItems(FXCollections.observableArrayList(
                    "Jour",
                    "Pharmacie de nuit"
            ));
            pharmacyStatusCombo.setValue("Jour");
        }
    }

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de la pharmacie");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();

            if (imageLabel != null) {
                imageLabel.setText(selectedFile.getName());
            }

            if (imagePreview != null) {
                imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            }
        }
    }

    @FXML
    public void removeImage() {
        selectedImagePath = "";

        if (imagePreview != null) {
            imagePreview.setImage(null);
        }

        if (imageLabel != null) {
            imageLabel.setText("Aucune image sélectionnée");
        }
    }

    @FXML
    public void addPharmacy() {
        try {
            String pharmacyName = pharmacyNameField == null ? "" : safe(pharmacyNameField.getText());
            String address = addressField == null ? "" : safe(addressField.getText());
            String phone = phoneField == null ? "" : safe(phoneField.getText());
            String email = emailField == null ? "" : safe(emailField.getText());
            String openingHours = openingHoursField == null ? "" : safe(openingHoursField.getText());
            String pharmacyStatus = pharmacyStatusCombo == null ? "Jour" : safe(pharmacyStatusCombo.getValue());

            if (pharmacyName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir le nom de la pharmacie.");
                return;
            }

            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir le téléphone de la pharmacie.");
                return;
            }

            if (openingHours.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir les horaires de la pharmacie.");
                return;
            }

            if (address.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir l'adresse de la pharmacie.");
                return;
            }

            if (pharmacyStatus.isEmpty()) {
                pharmacyStatus = "Jour";
            }

            Pharmacy pharmacy = new Pharmacy();
            pharmacy.setPharmacyName(pharmacyName);
            pharmacy.setAddress(address);
            pharmacy.setPhone(phone);
            pharmacy.setEmail(email);
            pharmacy.setOpeningHours(openingHours);
            pharmacy.setPharmacyStatus(pharmacyStatus);
            pharmacy.setImage(selectedImagePath);

            pharmacyServices.add(pharmacy);

            showAlertAndGo(
                    Alert.AlertType.INFORMATION,
                    "Succès",
                    "Pharmacie ajoutée avec succès.",
                    "/adminpharmacies.fxml"
            );

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter la pharmacie : " + e.getMessage());
        }
    }

    @FXML
    public void goDashboard() {
        MainFx.changeScene("/admindashboard.fxml");
    }

    @FXML
    public void goProducts() {
        MainFx.changeScene("/adminproducts.fxml");
    }

    @FXML
    public void goPharmacies() {
        MainFx.changeScene("/adminpharmacies.fxml");
    }

    @FXML
    public void goBack() {
        MainFx.changeScene("/adminpharmacies.fxml");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertAndGo(Alert.AlertType type, String title, String message, String scenePath) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            MainFx.changeScene(scenePath);
        }
    }
}