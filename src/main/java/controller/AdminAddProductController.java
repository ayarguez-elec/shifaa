package controller;

import entities.Pharmacy;
import entities.Product;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.PharmacyServices;
import services.ProductServices;
import services.RxNormService;
import tests.MainFx;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class AdminAddProductController {

    @FXML private ImageView imagePreview;
    @FXML private Label imageLabel;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> rxNormResults;
    @FXML private TextField descriptionField;
    @FXML private TextField dosageField;
    @FXML private TextField formField;
    @FXML private TextField laboratoryField;
    @FXML private TextField therapeuticClassField;
    @FXML private TextField priceField;
    @FXML private TextField categoryField;

    private ProductServices productServices;
    private PharmacyServices pharmacyServices;
    private RxNormService rxNormService;

    private String selectedImagePath;

    @FXML
    public void initialize() {
        productServices = new ProductServices();
        pharmacyServices = new PharmacyServices();
        rxNormService = new RxNormService();
        selectedImagePath = "";

        setupRxNormResults();
    }

    private void setupRxNormResults() {
        if (rxNormResults == null) {
            return;
        }

        rxNormResults.setVisibleRowCount(6);
        rxNormResults.setPromptText("Resultats RxNorm");

        rxNormResults.valueProperty().addListener((observable, oldValue, selectedName) -> {
            if (selectedName != null && !selectedName.trim().isEmpty() && nameField != null) {
                nameField.setText(selectedName.trim());
            }
        });
    }

    @FXML
    private void verifyDrug() {
        String drugName = safe(nameField.getText());

        if (drugName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir le nom du medicament.");
            return;
        }

        if (rxNormResults != null) {
            rxNormResults.getItems().clear();
            rxNormResults.setPromptText("Recherche en cours...");
            rxNormResults.setDisable(true);
        }

        Task<List<String>> searchTask = new Task<>() {
            @Override
            protected List<String> call() {
                return rxNormService.searchDrugNames(drugName);
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<String> results = searchTask.getValue();

            if (rxNormResults != null) {
                rxNormResults.setDisable(false);
                rxNormResults.setPromptText("Resultats RxNorm");
            }

            if (results == null || results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "RxNorm", "Aucun medicament trouve.");
                return;
            }

            if (rxNormResults != null) {
                rxNormResults.getItems().setAll(results);
                rxNormResults.show();
            }
        });

        searchTask.setOnFailed(event -> {
            if (rxNormResults != null) {
                rxNormResults.setDisable(false);
                rxNormResults.setPromptText("Resultats RxNorm");
            }

            showAlert(Alert.AlertType.ERROR, "Erreur RxNorm", "Impossible de verifier le medicament.");
        });

        Thread thread = new Thread(searchTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image du produit");

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
    public void saveProduct() {
        try {
            String name = safe(nameField.getText());
            String description = safe(descriptionField.getText());
            String dosage = safe(dosageField.getText());
            String form = safe(formField.getText());
            String laboratory = safe(laboratoryField.getText());
            String therapeuticClass = safe(therapeuticClassField.getText());
            String category = safe(categoryField.getText());
            String priceText = safe(priceField.getText()).replace(",", ".");

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir le nom du produit.");
                return;
            }

            if (priceText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir le prix du produit.");
                return;
            }

            double price;

            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Prix invalide", "Veuillez saisir un prix valide. Exemple : 4.500");
                return;
            }

            int pharmacyId = getDefaultPharmacyId();

            if (pharmacyId == 0) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Erreur",
                        "Aucune pharmacie trouvée. Veuillez d'abord ajouter une pharmacie."
                );
                return;
            }

            Product product = new Product();

            product.setProductName(name);
            product.setDescription(description);
            product.setDosage(dosage);
            product.setForm(form);
            product.setLaboratory(laboratory);
            product.setTherapeuticClass(therapeuticClass);
            product.setPrice(price);
            product.setCategory(category);
            product.setImage(selectedImagePath);

            /*
             Ancienne logique gardée :
             Le produit garde pharmacyId en base.
             Mais l'utilisateur ne choisit plus la pharmacie dans l'interface.
             On utilise automatiquement une pharmacie existante pour éviter l'erreur FK.
            */
            product.setPharmacyId(pharmacyId);

            productServices.add(product);

            showAlertAndGo(
                    Alert.AlertType.INFORMATION,
                    "Succès",
                    "Produit ajouté avec succès.",
                    "/adminproducts.fxml"
            );

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le produit : " + e.getMessage());
        }
    }

    private int getDefaultPharmacyId() {
        try {
            List<Pharmacy> pharmacies = pharmacyServices.getAll();

            if (pharmacies == null || pharmacies.isEmpty()) {
                return 0;
            }

            return pharmacies.get(0).getPharmacyId();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @FXML
    public void goBackProducts() {
        MainFx.changeScene("/adminproducts.fxml");
    }

    @FXML
    public void goBack() {
        MainFx.changeScene("/adminproducts.fxml");
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
