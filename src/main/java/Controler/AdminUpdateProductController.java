package Controler;

import entities.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.ProductServices;
import tests.Main;

import java.io.File;
import java.util.List;

public class AdminUpdateProductController {

    @FXML private TextField productIdField;

    @FXML private ImageView imagePreview;

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField dosageField;
    @FXML private TextField formField;
    @FXML private TextField laboratoryField;
    @FXML private TextField therapeuticClassField;
    @FXML private TextField priceField;
    @FXML private TextField categoryField;

    private ProductServices productServices;

    private static Product selectedProduct;
    private String selectedImagePath = "";

    public static void setSelectedProduct(Product product) {
        selectedProduct = product;
    }

    @FXML
    public void initialize() {
        productServices = new ProductServices();

        if (selectedProduct != null) {
            loadSelectedProduct();
        }
    }

    @FXML
    public void searchProduct() {
        if (productIdField == null) {
            loadSelectedProduct();
            return;
        }

        String searchText = safe(productIdField.getText());

        if (searchText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nom obligatoire", "Veuillez saisir le nom du produit.");
            return;
        }

        try {
            List<Product> results = productServices.searchByName(searchText);

            if (results.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Introuvable", "Aucun produit trouvé avec ce nom.");
                return;
            }

            // Prendre le premier résultat trouvé
            selectedProduct = results.get(0);
            loadSelectedProduct();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de chercher le produit.");
        }
    }

    @FXML
    public void loadProduct() {
        loadSelectedProduct();
    }

    private void loadSelectedProduct() {
        if (selectedProduct == null) {
            return;
        }

        if (productIdField != null) {
            productIdField.setText(String.valueOf(selectedProduct.getProductId()));
        }

        if (nameField != null) {
            nameField.setText(safe(selectedProduct.getProductName()));
        }

        if (descriptionField != null) {
            descriptionField.setText(safe(selectedProduct.getDescription()));
        }

        if (dosageField != null) {
            dosageField.setText(safe(selectedProduct.getDosage()));
        }

        if (formField != null) {
            formField.setText(safe(selectedProduct.getForm()));
        }

        if (laboratoryField != null) {
            laboratoryField.setText(safe(selectedProduct.getLaboratory()));
        }

        if (therapeuticClassField != null) {
            therapeuticClassField.setText(safe(selectedProduct.getTherapeuticClass()));
        }

        if (priceField != null) {
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
        }

        if (categoryField != null) {
            categoryField.setText(safe(selectedProduct.getCategory()));
        }

        selectedImagePath = safe(selectedProduct.getImage());
        loadImagePreview();
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
            System.out.println("Erreur chargement image : " + e.getMessage());
        }
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
    }

    @FXML
    public void updateProduct() {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun produit sélectionné.");
            return;
        }

        String name = nameField == null ? "" : safe(nameField.getText());
        String description = descriptionField == null ? "" : safe(descriptionField.getText());
        String dosage = dosageField == null ? "" : safe(dosageField.getText());
        String form = formField == null ? "" : safe(formField.getText());
        String laboratory = laboratoryField == null ? "" : safe(laboratoryField.getText());
        String therapeuticClass = therapeuticClassField == null ? "" : safe(therapeuticClassField.getText());
        String priceText = priceField == null ? "" : safe(priceField.getText()).replace(",", ".");
        String category = categoryField == null ? "" : safe(categoryField.getText());

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

        try {
            selectedProduct.setProductName(name);
            selectedProduct.setDescription(description);
            selectedProduct.setDosage(dosage);
            selectedProduct.setForm(form);
            selectedProduct.setLaboratory(laboratory);
            selectedProduct.setTherapeuticClass(therapeuticClass);
            selectedProduct.setPrice(price);
            selectedProduct.setCategory(category);
            selectedProduct.setImage(selectedImagePath);

            /*
             Important :
             On ne modifie plus ici :
             - quantité disponible
             - statut disponibilité
             - pharmacie
             - date mise à jour

             Ces informations seront gérées dans la partie stock du pharmacien.
            */

            productServices.update(selectedProduct);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit modifié avec succès.");
            Main.showAdminProducts();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le produit : " + e.getMessage());
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
    public void goBackProducts() {
        Main.showAdminProducts();
    }

    @FXML
    public void cancelUpdate() {
        Main.showAdminProducts();
    }

    @FXML
    public void goBack() {
        Main.showAdminProducts();
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
