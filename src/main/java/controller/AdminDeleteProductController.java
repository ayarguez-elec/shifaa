package controller;

import entities.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.ProductServices;
import tests.MainFx;

import java.util.Optional;

public class AdminDeleteProductController {

    @FXML
    private TextField productIdField;

    @FXML
    private Label messageLabel;

    private static int selectedProductId = 0;

    public static void setSelectedProductId(int id) {
        selectedProductId = id;
    }

    @FXML
    public void initialize() {
        System.out.println("AdminDeleteProductController initialisé");

        if (selectedProductId != 0) {
            productIdField.setText(String.valueOf(selectedProductId));
            selectedProductId = 0;
        }
    }

    @FXML
    public void deleteProduct() {
        try {
            if (productIdField.getText().trim().isEmpty()) {
                messageLabel.setText("Veuillez entrer l'ID du produit.");
                showPopup("Erreur", "Veuillez entrer l'ID du produit à supprimer.", Alert.AlertType.ERROR);
                return;
            }

            int id = Integer.parseInt(productIdField.getText().trim());

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Voulez-vous vraiment supprimer ce produit ?");

            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Product product = new Product();
                product.setProductId(id);

                ProductServices service = new ProductServices();
                service.delete(product);

                messageLabel.setText("Produit supprimé avec succès.");
                showPopup("Succès", "Produit supprimé avec succès.", Alert.AlertType.INFORMATION);

                productIdField.clear();
            } else {
                messageLabel.setText("Suppression annulée.");
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("ID invalide.");
            showPopup("Erreur", "L'ID doit être un nombre.", Alert.AlertType.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur lors de la suppression.");
            showPopup("Erreur", "Impossible de supprimer le produit.", Alert.AlertType.ERROR);
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
        MainFx.changeScene("/adminproducts.fxml");
    }
}