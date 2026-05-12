package controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import entities.Pharmacy;
import entities.Product;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.PharmacyServices;
import services.ProductServices;
import tests.MainFx;

import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AdminProductPharmaciesController {

    @FXML private Label productTitleLabel;
    @FXML private Label totalLabel;
    @FXML private FlowPane pharmacyContainer;
    @FXML private ImageView qrImageView;

    private ProductServices productServices;
    private PharmacyServices pharmacyServices;

    private List<Product> allProducts = new ArrayList<>();
    private List<Pharmacy> allPharmacies = new ArrayList<>();

    private static String selectedProductName = "";
    private static String selectedProductDosage = "";

    public static void setSelectedProduct(String productName, String dosage) {
        selectedProductName = productName == null ? "" : productName;
        selectedProductDosage = dosage == null ? "" : dosage;
    }

    @FXML
    public void initialize() {
        productServices = new ProductServices();
        pharmacyServices = new PharmacyServices();

        loadData();
        showPharmaciesForProduct();
    }

    private void loadData() {
        try {
            allProducts = productServices.getAll();
            allPharmacies = pharmacyServices.getAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPharmaciesForProduct() {
        pharmacyContainer.getChildren().clear();

        productTitleLabel.setText("Pharmacies disponibles pour : " + safe(selectedProductName));

        List<Product> productsForThisMedicine = new ArrayList<>();

        for (Product product : allProducts) {
            boolean sameName = normalizeText(product.getProductName()).equals(normalizeText(selectedProductName));
            boolean sameDosage = normalizeText(product.getDosage()).equals(normalizeText(selectedProductDosage));

            if (sameName && sameDosage) {
                productsForThisMedicine.add(product);
            }
        }

        generateQrCode(productsForThisMedicine);

        if (productsForThisMedicine.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setSpacing(8);
            emptyBox.setPrefWidth(800);
            emptyBox.setPrefHeight(250);

            Label title = new Label("Aucune pharmacie trouvée.");
            title.getStyleClass().add("medicine-title");

            Label text = new Label("Ce produit n’est associé à aucune pharmacie.");
            text.getStyleClass().add("admin-subtitle");

            emptyBox.getChildren().addAll(title, text);
            pharmacyContainer.getChildren().add(emptyBox);

            totalLabel.setText("Total : 0 pharmacie");
            return;
        }

        for (Product product : productsForThisMedicine) {
            Pharmacy pharmacy = getPharmacyById(product.getPharmacyId());
            pharmacyContainer.getChildren().add(createPharmacyCard(product, pharmacy));
        }

        totalLabel.setText("Total : " + productsForThisMedicine.size() + " pharmacies");
    }

    private VBox createPharmacyCard(Product product, Pharmacy pharmacy) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(310);
        card.getStyleClass().add("medicine-card");

        StackPane iconFrame = new StackPane();
        iconFrame.setPrefWidth(260);
        iconFrame.setPrefHeight(110);
        iconFrame.getStyleClass().add("medicine-image-frame");

        Label icon = new Label("🏥");
        icon.getStyleClass().add("medicine-placeholder-icon");
        iconFrame.getChildren().add(icon);

        Label pharmacyName = new Label(pharmacy == null ? "Pharmacie non définie" : safe(pharmacy.getPharmacyName()));
        pharmacyName.getStyleClass().add("medicine-title");
        pharmacyName.setWrapText(true);

        Label address = new Label("Adresse : " + (pharmacy == null ? "Adresse non définie" : safe(pharmacy.getAddress())));
        address.getStyleClass().add("medicine-description");
        address.setWrapText(true);

        Label phone = new Label("Téléphone : " + (pharmacy == null ? "Non renseigné" : safe(pharmacy.getPhone())));
        phone.getStyleClass().add("medicine-description");
        phone.setWrapText(true);

        Label quantity = new Label("Quantité : " + product.getAvailableQuantity());
        quantity.getStyleClass().add("medicine-info-value");

        Label status = new Label("État : " + safe(product.getAvailabilityStatus()));
        status.setMaxWidth(Double.MAX_VALUE);
        status.setAlignment(Pos.CENTER);
        status.getStyleClass().add(getProductStatusStyle(product));

        card.getChildren().addAll(
                iconFrame,
                pharmacyName,
                address,
                phone,
                quantity,
                status
        );

        return card;
    }

    private void generateQrCode(List<Product> productsForThisMedicine) {
        try {
            String qrContent = buildQrContent(productsForThisMedicine);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    260,
                    260,
                    hints
            );

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            qrImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildQrContent(List<Product> productsForThisMedicine) {
        StringBuilder builder = new StringBuilder();

        builder.append("SHIFAA - Disponibilite medicament\n\n");
        builder.append("Produit : ").append(safe(selectedProductName)).append("\n");
        builder.append("Dosage : ").append(safe(selectedProductDosage)).append("\n\n");
        builder.append("Scanner ce QR code pour voir la liste des pharmacies qui contiennent ce medicament.\n\n");
        builder.append("Pharmacies disponibles :\n");

        if (productsForThisMedicine == null || productsForThisMedicine.isEmpty()) {
            builder.append("- Aucune pharmacie disponible\n");
            return builder.toString();
        }

        for (Product product : productsForThisMedicine) {
            Pharmacy pharmacy = getPharmacyById(product.getPharmacyId());

            builder.append("- ");

            if (pharmacy == null) {
                builder.append("Pharmacie non definie");
            } else {
                builder.append(safe(pharmacy.getPharmacyName()));
            }

            builder.append(" | Quantite : ").append(product.getAvailableQuantity());
            builder.append(" | Etat : ").append(safe(product.getAvailabilityStatus()));

            if (pharmacy != null) {
                builder.append(" | Adresse : ").append(safe(pharmacy.getAddress()));
                builder.append(" | Tel : ").append(safe(pharmacy.getPhone()));
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    private Pharmacy getPharmacyById(int pharmacyId) {
        for (Pharmacy pharmacy : allPharmacies) {
            if (pharmacy.getPharmacyId() == pharmacyId) {
                return pharmacy;
            }
        }

        return null;
    }

    private String getProductStatusStyle(Product product) {
        String status = normalizeText(product.getAvailabilityStatus());

        if (product.getAvailableQuantity() <= 0
                || status.contains("rupture")
                || status.contains("indisponible")) {
            return "product-status-unavailable";
        }

        if (product.getAvailableQuantity() <= 10
                || status.contains("faible")) {
            return "product-status-low";
        }

        return "product-status-available";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String result = value.trim().toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Non renseigné" : value.trim();
    }

    @FXML
    public void goBack() {
        MainFx.changeScene("/adminproducts.fxml");
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
}