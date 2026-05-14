package Controler;

import entities.Pharmacy;
import entities.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import services.PharmacyServices;
import services.PharmacyStockService;
import tests.Main;

import java.util.List;
import java.util.Optional;

public class PharmacyAddStockController {

    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label messageLabel;

    @FXML private ComboBox<Pharmacy> pharmacyCombo;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> statusCombo;

    @FXML private Button saveButton;

    private PharmacyServices pharmacyServices;
    private PharmacyStockService pharmacyStockService;

    private static Product selectedProduct;

    public static void setSelectedProduct(Product product) {
        selectedProduct = product;
    }

    @FXML
    public void initialize() {
        pharmacyServices = new PharmacyServices();
        pharmacyStockService = new PharmacyStockService();

        setupStatusCombo();
        loadPharmacies();

        if (selectedProduct == null) {
            loadAddMode();
        } else {
            loadUpdateMode();
        }
    }

    private void setupStatusCombo() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Disponible",
                "Stock faible",
                "Rupture"
        ));

        statusCombo.setValue("Disponible");
    }

    private void loadPharmacies() {
        try {
            List<Pharmacy> pharmacies = pharmacyServices.getAll();
            pharmacyCombo.setItems(FXCollections.observableArrayList(pharmacies));

            pharmacyCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Pharmacy pharmacy, boolean empty) {
                    super.updateItem(pharmacy, empty);
                    setText(empty || pharmacy == null ? null : pharmacy.getPharmacyName());
                }
            });

            pharmacyCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Pharmacy pharmacy, boolean empty) {
                    super.updateItem(pharmacy, empty);
                    setText(empty || pharmacy == null ? "Choisir une pharmacie" : pharmacy.getPharmacyName());
                }
            });

            pharmacyCombo.setOnAction(event -> {
                Pharmacy selected = pharmacyCombo.getValue();

                if (selected != null && selectedProduct == null) {
                    loadProductsForStock();
                }
            });

            if (!pharmacies.isEmpty()) {
                pharmacyCombo.setValue(pharmacies.get(0));

                if (selectedProduct == null) {
                    loadProductsForStock();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les pharmacies.");
        }
    }

    private void loadProductsForStock() {
        try {
            List<Product> products = pharmacyStockService.getAllProductsForStock();
            productCombo.setItems(FXCollections.observableArrayList(products));

            productCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Product product, boolean empty) {
                    super.updateItem(product, empty);
                    setText(empty || product == null ? null : formatProductName(product));
                }
            });

            productCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Product product, boolean empty) {
                    super.updateItem(product, empty);
                    setText(empty || product == null ? "Choisir un produit" : formatProductName(product));
                }
            });

            if (!products.isEmpty()) {
                productCombo.setValue(products.get(0));
                fillProductStockFields(products.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les produits.");
        }
    }

    private void loadAddMode() {
        pageTitleLabel.setText("Ajouter stock");
        pageSubtitleLabel.setText("Choisissez la pharmacie, le produit, la quantite et le statut.");
        saveButton.setText("Enregistrer");

        pharmacyCombo.setDisable(false);
        productCombo.setDisable(false);

        productCombo.setOnAction(event -> {
            Product product = productCombo.getValue();

            if (product != null) {
                fillProductStockFields(product);
            }
        });

        messageLabel.setText("");
    }

    private void loadUpdateMode() {
        pageTitleLabel.setText("Modifier le stock");
        pageSubtitleLabel.setText("Modifiez la quantite et le statut du medicament.");
        saveButton.setText("Enregistrer");

        selectPharmacyById(selectedProduct.getPharmacyId());

        pharmacyCombo.setDisable(true);
        productCombo.setDisable(true);

        productCombo.setItems(FXCollections.observableArrayList(selectedProduct));
        productCombo.setValue(selectedProduct);

        productCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty || product == null ? null : formatProductName(product));
            }
        });

        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty || product == null ? "Produit" : formatProductName(product));
            }
        });

        fillProductStockFields(selectedProduct);
        messageLabel.setText("");
    }

    @FXML
    public void saveStock() {
        Pharmacy pharmacy = pharmacyCombo.getValue();
        Product product = selectedProduct == null ? productCombo.getValue() : selectedProduct;

        if (pharmacy == null) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez choisir une pharmacie.");
            return;
        }

        if (product == null) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez choisir un produit.");
            return;
        }

        String quantityText = quantityField.getText() == null ? "" : quantityField.getText().trim();
        String status = statusCombo.getValue() == null ? "Disponible" : statusCombo.getValue();

        if (quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Veuillez saisir la quantite.");
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Quantite invalide", "Veuillez saisir un nombre valide.");
            return;
        }

        if (quantity < 0) {
            showAlert(Alert.AlertType.WARNING, "Quantite invalide", "La quantite ne peut pas etre negative.");
            return;
        }

        pharmacyStockService.updateProductStock(
                product.getProductId(),
                pharmacy.getPharmacyId(),
                quantity,
                status
        );

        showSuccessAndGoBack("Stock enregistre avec succes.");
    }

    @FXML
    public void clearForm() {
        if (selectedProduct == null) {
            Product product = productCombo.getValue();

            if (product != null) {
                fillProductStockFields(product);
            } else {
                quantityField.clear();
                statusCombo.setValue("Disponible");
            }
        } else {
            loadUpdateMode();
        }
    }

    @FXML
    public void goBackStock() {
        selectedProduct = null;
        Main.showPharmacyStock();
    }

    @FXML
    public void goStock() {
        selectedProduct = null;
        Main.showPharmacyStock();
    }

    private void fillProductStockFields(Product product) {
        quantityField.setText(String.valueOf(product.getAvailableQuantity()));

        String status = product.getAvailabilityStatus();
        statusCombo.setValue(status == null || status.trim().isEmpty() ? "Disponible" : status);
    }

    private void selectPharmacyById(int pharmacyId) {
        if (pharmacyCombo == null || pharmacyCombo.getItems() == null) {
            return;
        }

        for (Pharmacy pharmacy : pharmacyCombo.getItems()) {
            if (pharmacy.getPharmacyId() == pharmacyId) {
                pharmacyCombo.setValue(pharmacy);
                return;
            }
        }
    }

    private String formatProductName(Product product) {
        String dosage = safe(product.getDosage());

        if (dosage.isEmpty()) {
            return safe(product.getProductName());
        }

        return safe(product.getProductName()) + " - " + dosage;
    }

    private void showSuccessAndGoBack(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText("Succes");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            selectedProduct = null;
            Main.showPharmacyStock();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
