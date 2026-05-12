package controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import entities.Pharmacy;
import entities.Product;
import javafx.animation.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import services.PharmacyServices;
import services.ProductServices;
import tests.MainFx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UserProductDetailsController {

    @FXML private VBox pageContainer;
    @FXML private VBox mainCard;
    @FXML private VBox imageFrameBox;
    @FXML private VBox qrCard;

    @FXML private ImageView productImage;
    @FXML private ImageView qrImageView;

    @FXML private Label productNameLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;

    @FXML private Label dosageLabel;
    @FXML private Label categoryLabel;
    @FXML private Label formLabel;
    @FXML private Label laboratoryLabel;
    @FXML private Label therapeuticClassLabel;

    @FXML private Label adviceTitle;
    @FXML private Label qrInfoLabel;

    @FXML private Button topReturnButton;
    @FXML private Button securityButton;
    @FXML private VBox securityContent;

    private ProductServices productServices;
    private PharmacyServices pharmacyServices;

    private static Product selectedProduct;

    public static void setSelectedProduct(Product product) {
        selectedProduct = product;
    }

    @FXML
    public void initialize() {
        productServices = new ProductServices();
        pharmacyServices = new PharmacyServices();

        if (selectedProduct == null) {
            showEmptyData();
            setupVisualEffects();
            return;
        }

        Product productToShow = getProductWithTotalStock(selectedProduct);

        showProductDetails(productToShow);
        generateAvailabilityQrCode(productToShow);
        setupVisualEffects();
        startPriceCounter(productToShow.getPrice());
    }

    private Product getProductWithTotalStock(Product product) {
        try {
            List<Product> allProducts = productServices.getAll();
            int totalStock = 0;

            for (Product p : allProducts) {
                boolean sameName = normalizeText(p.getProductName())
                        .equals(normalizeText(product.getProductName()));

                boolean sameDosage = normalizeText(p.getDosage())
                        .equals(normalizeText(product.getDosage()));

                if (sameName && sameDosage) {
                    totalStock += p.getAvailableQuantity();
                }
            }

            product.setAvailableQuantity(totalStock);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return product;
    }

    private void showEmptyData() {
        productNameLabel.setText("Produit non sélectionné");
        priceLabel.setText("0,000 DT");
        descriptionLabel.setText("Aucune description");

        dosageLabel.setText("-");
        categoryLabel.setText("-");
        formLabel.setText("-");
        laboratoryLabel.setText("-");
        therapeuticClassLabel.setText("-");

        if (qrInfoLabel != null) {
            qrInfoLabel.setText("Aucun QR code disponible.");
        }
    }

    private void showProductDetails(Product product) {
        productNameLabel.setText(safe(product.getProductName()));
        priceLabel.setText("0,000 DT");

        descriptionLabel.setText(
                safe(product.getDescription()).isEmpty()
                        ? "Aucune description"
                        : safe(product.getDescription())
        );

        dosageLabel.setText(emptyToDash(product.getDosage()));
        categoryLabel.setText(emptyToDash(product.getCategory()));
        formLabel.setText(emptyToDash(product.getForm()));
        laboratoryLabel.setText(emptyToDash(product.getLaboratory()));
        therapeuticClassLabel.setText(emptyToDash(product.getTherapeuticClass()));

        loadProductImage(product);
    }

    private void loadProductImage(Product product) {
        try {
            String imagePath = product.getImage();

            if (imagePath == null || imagePath.trim().isEmpty()) {
                return;
            }

            File file = new File(imagePath);

            if (file.exists()) {
                productImage.setImage(new Image(file.toURI().toString()));
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement image produit : " + e.getMessage());
        }
    }

    private void setupVisualEffects() {
        startPageFade();
        startImageFloating();
        startAdviceGlow();
        setupImage3DEffect();
        setupButtonEffects();
        setupSecurityButtonEffects();
        setupQrCardEffects();
    }

    private void startPageFade() {
        if (mainCard == null) {
            return;
        }

        mainCard.setOpacity(0);
        mainCard.setTranslateY(16);

        FadeTransition fade = new FadeTransition(Duration.millis(350), mainCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(350), mainCard);
        translate.setFromY(16);
        translate.setToY(0);

        new ParallelTransition(fade, translate).play();
    }

    private void startImageFloating() {
        if (imageFrameBox == null) {
            return;
        }

        TranslateTransition floating = new TranslateTransition(Duration.seconds(3), imageFrameBox);
        floating.setFromY(-6);
        floating.setToY(6);
        floating.setAutoReverse(true);
        floating.setCycleCount(Animation.INDEFINITE);
        floating.play();
    }

    private void startAdviceGlow() {
        if (adviceTitle == null) {
            return;
        }

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#F59E0B", 0.28));
        glow.setRadius(4);
        glow.setSpread(0.08);

        adviceTitle.setEffect(glow);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 3)),
                new KeyFrame(Duration.seconds(1), new KeyValue(glow.radiusProperty(), 10)),
                new KeyFrame(Duration.seconds(2), new KeyValue(glow.radiusProperty(), 3))
        );

        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void setupImage3DEffect() {
        if (imageFrameBox == null) {
            return;
        }

        imageFrameBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getCamera() == null) {
                newScene.setCamera(new PerspectiveCamera());
            }
        });

        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

        imageFrameBox.getTransforms().addAll(rotateX, rotateY);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0F172A", 0.16));
        shadow.setRadius(16);
        shadow.setOffsetX(0);
        shadow.setOffsetY(5);
        imageFrameBox.setEffect(shadow);

        imageFrameBox.setOnMouseMoved(event -> {
            double width = imageFrameBox.getWidth();
            double height = imageFrameBox.getHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            double rotateYValue = ((event.getX() / width) - 0.5) * 8;
            double rotateXValue = -((event.getY() / height) - 0.5) * 8;

            rotateX.setAngle(rotateXValue);
            rotateY.setAngle(rotateYValue);

            shadow.setOffsetX(-rotateYValue);
            shadow.setOffsetY(Math.abs(rotateXValue) + 6);
        });

        imageFrameBox.setOnMouseExited(event -> {
            Timeline reset = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(rotateX.angleProperty(), 0),
                            new KeyValue(rotateY.angleProperty(), 0),
                            new KeyValue(shadow.offsetXProperty(), 0),
                            new KeyValue(shadow.offsetYProperty(), 5)
                    )
            );

            reset.play();
        });
    }

    private void setupButtonEffects() {
        setupScaleHover(topReturnButton, 1.03);
    }

    private void setupScaleHover(Button button, double scaleValue) {
        if (button == null) {
            return;
        }

        button.setOnMouseEntered(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(scaleValue);
            scale.setToY(scaleValue);
            scale.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#6366F1", 0.20));
            glow.setRadius(14);
            glow.setOffsetY(4);
            button.setEffect(glow);
        });

        button.setOnMouseExited(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();

            button.setEffect(null);
        });
    }

    private void setupSecurityButtonEffects() {
        if (securityButton == null) {
            return;
        }

        DropShadow warningGlow = new DropShadow();
        warningGlow.setColor(Color.web("#F59E0B", 0.25));
        warningGlow.setRadius(5);
        warningGlow.setSpread(0.08);

        securityButton.setEffect(warningGlow);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(warningGlow.radiusProperty(), 4)),
                new KeyFrame(Duration.seconds(1), new KeyValue(warningGlow.radiusProperty(), 12)),
                new KeyFrame(Duration.seconds(2), new KeyValue(warningGlow.radiusProperty(), 4))
        );

        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        securityButton.setOnMouseEntered(event -> {
            securityButton.setStyle(
                    "-fx-background-color: #1E293B;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );
        });

        securityButton.setOnMouseExited(event -> {
            securityButton.setStyle(
                    "-fx-background-color: #0F172A;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );
        });
    }

    private void setupQrCardEffects() {
        if (qrCard == null) {
            return;
        }

        qrCard.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), qrCard);
            translate.setToY(-2);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#6366F1", 0.16));
            glow.setRadius(22);
            glow.setOffsetY(8);
            qrCard.setEffect(glow);

            translate.play();
        });

        qrCard.setOnMouseExited(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), qrCard);
            translate.setToY(0);

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#0F172A", 0.05));
            shadow.setRadius(14);
            shadow.setOffsetY(4);
            qrCard.setEffect(shadow);

            translate.play();
        });
    }

    private void startPriceCounter(double targetPrice) {
        Timeline timeline = new Timeline();
        int frames = 50;

        for (int i = 0; i <= frames; i++) {
            double progress = (double) i / frames;
            double eased = 1 - Math.pow(1 - progress, 3);
            double value = targetPrice * eased;

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(1500 * progress),
                    event -> priceLabel.setText(formatPrice(value) + " DT")
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(event -> priceLabel.setText(formatPrice(targetPrice) + " DT"));
        timeline.play();
    }

    private void generateAvailabilityQrCode(Product productToShow) {
        try {
            if (qrImageView == null) {
                return;
            }

            List<Product> sameProducts = getSameProducts(productToShow);
            String qrText = buildQrText(productToShow, sameProducts);

            QRCodeWriter writer = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = writer.encode(
                    qrText,
                    BarcodeFormat.QR_CODE,
                    420,
                    420,
                    hints
            );

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            qrImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));

            if (qrInfoLabel != null) {
                qrInfoLabel.setText("QR Code généré pour : " + safe(productToShow.getProductName()));
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (qrInfoLabel != null) {
                qrInfoLabel.setText("Erreur lors de la génération du QR Code.");
            }
        }
    }

    private List<Product> getSameProducts(Product productToShow) {
        List<Product> result = new ArrayList<>();

        try {
            List<Product> allProducts = productServices.getAll();

            for (Product product : allProducts) {
                boolean sameName = normalizeText(product.getProductName())
                        .equals(normalizeText(productToShow.getProductName()));

                boolean sameDosage = normalizeText(product.getDosage())
                        .equals(normalizeText(productToShow.getDosage()));

                if (sameName && sameDosage) {
                    result.add(product);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String buildQrText(Product productToShow, List<Product> products) {
        StringBuilder builder = new StringBuilder();

        builder.append("━━━━━━━━━━━━━━━━━━━━\n");
        builder.append("💊 SHIFAA - FICHE MÉDICAMENT\n");
        builder.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        builder.append("📌 Produit : ").append(cleanForQr(productToShow.getProductName())).append("\n");
        builder.append("💰 Prix : ").append(formatPrice(productToShow.getPrice())).append(" DT\n");
        builder.append("📦 Quantité totale : ").append(productToShow.getAvailableQuantity()).append(" unités\n\n");

        builder.append("━━━━━━━━━━━━━━━━━━━━\n");
        builder.append("🏥 PHARMACIES DISPONIBLES\n");
        builder.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        if (products == null || products.isEmpty()) {
            builder.append("❌ Aucune pharmacie disponible pour ce médicament.\n\n");
            builder.append("━━━━━━━━━━━━━━━━━━━━\n");
            builder.append("Merci d'utiliser SHIFAA 💙");
            return builder.toString();
        }

        List<Pharmacy> pharmacies = new ArrayList<>();

        try {
            pharmacies = pharmacyServices.getAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int index = 1;

        for (Product product : products) {
            Pharmacy pharmacy = getPharmacyById(pharmacies, product.getPharmacyId());
            int quantity = product.getAvailableQuantity();

            String icon;
            String state;

            if (quantity <= 0) {
                icon = "❌";
                state = "🔴 Indisponible";
            } else if (quantity <= 9) {
                icon = "⚠️";
                state = "🟠 Stock faible";
            } else {
                icon = "✅";
                state = "🟢 Disponible";
            }

            builder.append(icon).append(" ").append(index).append(". ");

            if (pharmacy == null) {
                builder.append("Pharmacie non définie\n");
                builder.append("📍 Adresse : Non renseignée\n");
            } else {
                builder.append(cleanForQr(pharmacy.getPharmacyName())).append("\n");
                builder.append("📍 Adresse : ").append(cleanForQr(pharmacy.getAddress())).append("\n");

                if (pharmacy.getPhone() != null && !pharmacy.getPhone().trim().isEmpty()) {
                    builder.append("📞 Téléphone : ").append(cleanForQr(pharmacy.getPhone())).append("\n");
                }
            }

            builder.append("📦 Stock : ").append(quantity);
            builder.append(quantity <= 1 ? " unité\n" : " unités\n");

            builder.append("📊 État : ").append(state).append("\n");

            if (pharmacy != null && pharmacy.getAddress() != null && !pharmacy.getAddress().trim().isEmpty()) {
                builder.append("🧭 Pour le guidage : ouvrez Google Maps et cherchez :\n");
                builder.append("   ").append(cleanForQr(pharmacy.getPharmacyName())).append(" - ");
                builder.append(cleanForQr(pharmacy.getAddress())).append("\n");
            }

            builder.append("\n");
            index++;
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n");
        builder.append("Merci d'utiliser SHIFAA 💙");

        return builder.toString();
    }

    private Pharmacy getPharmacyById(List<Pharmacy> pharmacies, int pharmacyId) {
        for (Pharmacy pharmacy : pharmacies) {
            if (pharmacy.getPharmacyId() == pharmacyId) {
                return pharmacy;
            }
        }

        return null;
    }

    @FXML
    public void toggleSecurity() {
        if (securityContent == null || securityButton == null) {
            return;
        }

        boolean show = !securityContent.isVisible();

        securityContent.setVisible(show);
        securityContent.setManaged(show);

        if (show) {
            securityButton.setText("⚠ Sécurité médicament — cliquez pour réduire");
        } else {
            securityButton.setText("⚠ Sécurité médicament — cliquez pour afficher");
        }
    }

    private String formatPrice(double price) {
        return String.format("%.3f", price).replace(".", ",");
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
        return value == null ? "" : value.trim();
    }

    private String emptyToDash(String value) {
        return safe(value).isEmpty() ? "-" : safe(value);
    }

    private String cleanForQr(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Non renseigné";
        }

        return value.trim()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ç", "c")
                .replace("É", "E")
                .replace("È", "E")
                .replace("À", "A")
                .replace("Ç", "C");
    }

    @FXML
    public void goBackProducts() {
        MainFx.changeScene("/userproducts.fxml");
    }

    @FXML
    public void goDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }

    @FXML
    public void goProducts() {
        MainFx.changeScene("/userproducts.fxml");
    }

    @FXML
    public void goPharmacies() {
        MainFx.changeScene("/userpharmacies.fxml");
    }
}