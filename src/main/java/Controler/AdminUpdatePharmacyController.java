package Controler;

import entities.Pharmacy;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.PharmacyServices;
import tests.Main;

import java.io.File;
import java.util.List;

public class AdminUpdatePharmacyController {

    @FXML private TextField pharmacyIdField;

    @FXML private ImageView imagePreview;
    @FXML private Label imageLabel;

    @FXML private TextField pharmacyNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField openingHoursField;
    @FXML private ComboBox<String> pharmacyStatusCombo;

    private PharmacyServices pharmacyServices;

    private static Pharmacy selectedPharmacy;
    private String selectedImagePath = "";

    public static void setSelectedPharmacy(Pharmacy pharmacy) {
        selectedPharmacy = pharmacy;
    }

    @FXML
    public void initialize() {
        pharmacyServices = new PharmacyServices();

        if (pharmacyStatusCombo != null) {
            pharmacyStatusCombo.setItems(FXCollections.observableArrayList(
                    "Jour",
                    "Pharmacie de nuit"
            ));
            pharmacyStatusCombo.setValue("Jour");
        }

        if (selectedPharmacy != null) {
            loadSelectedPharmacy();
        }
    }

    @FXML
    public void loadPharmacy() {
        if (pharmacyIdField == null) {
            loadSelectedPharmacy();
            return;
        }

        String idText = safe(pharmacyIdField.getText());

        if (idText.isEmpty()) {
            if (selectedPharmacy != null) {
                loadSelectedPharmacy();
            } else {
                showAlert(Alert.AlertType.WARNING, "ID obligatoire", "Veuillez saisir l'ID de la pharmacie.");
            }
            return;
        }

        int pharmacyId;

        try {
            pharmacyId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "ID invalide", "Veuillez saisir un ID numérique.");
            return;
        }

        try {
            List<Pharmacy> pharmacies = pharmacyServices.getAll();

            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getPharmacyId() == pharmacyId) {
                    selectedPharmacy = pharmacy;
                    loadSelectedPharmacy();
                    return;
                }
            }

            showAlert(Alert.AlertType.ERROR, "Introuvable", "Aucune pharmacie trouvée avec cet ID.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de chercher la pharmacie.");
        }
    }

    private void loadSelectedPharmacy() {
        if (selectedPharmacy == null) {
            return;
        }

        if (pharmacyIdField != null) {
            pharmacyIdField.setText(String.valueOf(selectedPharmacy.getPharmacyId()));
        }

        if (pharmacyNameField != null) {
            pharmacyNameField.setText(safe(selectedPharmacy.getPharmacyName()));
        }

        if (addressField != null) {
            addressField.setText(safe(selectedPharmacy.getAddress()));
        }

        if (phoneField != null) {
            phoneField.setText(safe(selectedPharmacy.getPhone()));
        }

        if (emailField != null) {
            emailField.setText(safe(selectedPharmacy.getEmail()));
        }

        if (openingHoursField != null) {
            openingHoursField.setText(safe(selectedPharmacy.getOpeningHours()));
        }

        if (pharmacyStatusCombo != null) {
            pharmacyStatusCombo.setValue(normalizeStatusForCombo(selectedPharmacy.getPharmacyStatus()));
        }

        selectedImagePath = safe(selectedPharmacy.getImage());

        if (imageLabel != null) {
            if (selectedImagePath.isEmpty()) {
                imageLabel.setText("Aucune image sélectionnée");
            } else {
                imageLabel.setText(new File(selectedImagePath).getName());
            }
        }

        loadImagePreview();
    }

    private String normalizeStatusForCombo(String status) {
        String value = safe(status);

        if (value.equalsIgnoreCase("Nuit")
                || value.equalsIgnoreCase("Pharmacie nuit")
                || value.equalsIgnoreCase("Pharmacie de nuit")) {
            return "Pharmacie de nuit";
        }

        return "Jour";
    }

    private void loadImagePreview() {
        try {
            if (imagePreview == null) {
                return;
            }

            if (selectedImagePath == null || selectedImagePath.trim().isEmpty()) {
                imagePreview.setImage(null);
                return;
            }

            File file = new File(selectedImagePath);

            if (file.exists()) {
                imagePreview.setImage(new Image(file.toURI().toString()));
            } else {
                imagePreview.setImage(null);
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement image pharmacie : " + e.getMessage());
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
    public void updatePharmacy() {
        if (selectedPharmacy == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune pharmacie sélectionnée.");
            return;
        }

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

        try {
            selectedPharmacy.setPharmacyName(pharmacyName);
            selectedPharmacy.setAddress(address);
            selectedPharmacy.setPhone(phone);
            selectedPharmacy.setEmail(email);
            selectedPharmacy.setOpeningHours(openingHours);
            selectedPharmacy.setPharmacyStatus(pharmacyStatus);
            selectedPharmacy.setImage(selectedImagePath);

            pharmacyServices.update(selectedPharmacy);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Pharmacie modifiée avec succès.");
            Main.showAdminPharmacies();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la pharmacie : " + e.getMessage());
        }
    }

    @FXML
    public void goDashboard() {
        Main.showAdminDashboard();
    }

    @FXML
    public void goProducts() {
        Main.showAdminProducts();
    }

    @FXML
    public void goPharmacies() {
        Main.showAdminPharmacies();
    }

    @FXML
    public void goBack() {
        Main.showAdminPharmacies();
    }

    @FXML
    public void cancelUpdate() {
        Main.showAdminPharmacies();
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
}
