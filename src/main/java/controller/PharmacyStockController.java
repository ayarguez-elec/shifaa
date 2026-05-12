package controller;

import entities.Pharmacy;
import entities.Product;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.OpenFdaRecallService;
import services.PharmacyServices;
import services.PharmacyStockService;
import tests.MainFx;

import java.io.File;
import java.util.List;

public class PharmacyStockController {

    @FXML private ComboBox<Pharmacy> pharmacyCombo;
    @FXML private javafx.scene.layout.FlowPane stockContainer;
    @FXML private Label sectionTitleLabel;

    private PharmacyServices pharmacyServices;
    private PharmacyStockService pharmacyStockService;
    private OpenFdaRecallService openFdaRecallService;

    @FXML
    public void initialize() {
        pharmacyServices = new PharmacyServices();
        pharmacyStockService = new PharmacyStockService();
        openFdaRecallService = new OpenFdaRecallService();

        loadPharmacies();
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

                if (selected != null) {
                    loadProductsByPharmacy(selected);
                }
            });

            if (!pharmacies.isEmpty()) {
                pharmacyCombo.setValue(pharmacies.get(0));
                loadProductsByPharmacy(pharmacies.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les pharmacies.");
        }
    }

    private void loadProductsByPharmacy(Pharmacy pharmacy) {
        stockContainer.getChildren().clear();

        if (sectionTitleLabel != null) {
            sectionTitleLabel.setText("Produits de : " + safe(pharmacy.getPharmacyName()));
        }

        try {
            List<Product> products = pharmacyStockService.getProductsByPharmacy(pharmacy.getPharmacyId());

            if (products.isEmpty()) {
                stockContainer.getChildren().add(createEmptyState(pharmacy));
                return;
            }

            int index = 0;

            for (Product product : products) {
                VBox card = createProductCard(product);
                stockContainer.getChildren().add(card);
                playCardEntrance(card, index);
                index++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les produits.");
        }
    }

    private VBox createEmptyState(Pharmacy pharmacy) {
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPrefWidth(940);
        emptyBox.setPrefHeight(260);
        emptyBox.setPadding(new Insets(28));
        emptyBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 18;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0F172A", 0.06));
        shadow.setRadius(18);
        shadow.setOffsetY(5);
        emptyBox.setEffect(shadow);

        Label title = new Label("Aucun produit lie a cette pharmacie");
        title.setStyle(
                "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        Label text = new Label("Utilisez le bouton + Ajouter stock pour associer un medicament a "
                + safe(pharmacy.getPharmacyName()) + ".");
        text.setWrapText(true);
        text.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 14px;"
        );

        emptyBox.getChildren().addAll(title, text);
        return emptyBox;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(11);
        card.setPadding(new Insets(16));
        card.setPrefWidth(295);
        card.setMinWidth(295);
        card.setMaxWidth(295);
        card.setMinHeight(445);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 18;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0F172A", 0.07));
        shadow.setRadius(18);
        shadow.setOffsetY(5);
        card.setEffect(shadow);

        StackPane imageFrame = createImageFrame(product);
        Region separatorTop = createSeparator();

        Label name = new Label(safe(product.getProductName()));
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;"
        );

        HBox priceAndStatus = new HBox(10);
        priceAndStatus.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(formatPrice(product.getPrice()) + " DT");
        price.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #10B981;"
        );

        Region priceSpacer = new Region();
        HBox.setHgrow(priceSpacer, Priority.ALWAYS);

        Label status = createStatusBadge(product.getAvailabilityStatus());
        priceAndStatus.getChildren().addAll(price, priceSpacer, status);

        HBox quantityRow = createInfoRow("Quantite disponible", String.valueOf(product.getAvailableQuantity()));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region separatorBottom = createSeparator();

        Button editButton = new Button("Modifier stock");
        editButton.setPrefHeight(44);
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );

        editButton.setOnAction(event -> {
            PharmacyAddStockController.setSelectedProduct(product);
            MainFx.changeScene("/pharmacyaddstock.fxml");
        });

        Button safetyButton = new Button("Verifier securite");
        safetyButton.setPrefHeight(42);
        safetyButton.setMaxWidth(Double.MAX_VALUE);
        safetyButton.setStyle(
                "-fx-background-color: #FEE2E2;" +
                        "-fx-text-fill: #EF4444;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );

        safetyButton.setOnAction(event -> verifyProductSafety(product, safetyButton));

        setupButtonHover(editButton);
        setupSafetyButtonHover(safetyButton);
        setupCardHover(card);

        card.getChildren().addAll(
                imageFrame,
                separatorTop,
                name,
                priceAndStatus,
                quantityRow,
                spacer,
                separatorBottom,
                editButton,
                safetyButton
        );

        return card;
    }

    private StackPane createImageFrame(Product product) {
        StackPane frame = new StackPane();
        frame.setPrefWidth(263);
        frame.setMinHeight(140);
        frame.setPrefHeight(140);
        frame.setMaxHeight(140);
        frame.setAlignment(Pos.CENTER);
        frame.setStyle(
                "-fx-background-color: #EFF6FF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #DBEAFE;" +
                        "-fx-border-radius: 16;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(118);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (loadProductImage(product.getImage(), imageView)) {
            frame.getChildren().add(imageView);
            setupImageHover(imageView);
        } else {
            Label icon = new Label("RX");
            icon.setStyle(
                    "-fx-font-size: 30px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #94A3B8;"
            );
            frame.getChildren().add(icon);
        }

        return frame;
    }

    private HBox createInfoRow(String key, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 12;"
        );

        Label keyLabel = new Label(key);
        keyLabel.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        row.getChildren().addAll(keyLabel, spacer, valueLabel);
        return row;
    }

    private Region createSeparator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxHeight(1);
        line.setStyle("-fx-background-color: #F1F5F9;");
        VBox.setMargin(line, new Insets(2, 0, 2, 0));
        return line;
    }

    private boolean loadProductImage(String imagePath, ImageView imageView) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur image produit : " + e.getMessage());
        }

        return false;
    }

    private Label createStatusBadge(String statusText) {
        String status = safe(statusText);

        if (status.isEmpty()) {
            status = "Rupture";
        }

        String bg = "#FEE2E2";
        String text = "#EF4444";

        if (status.equalsIgnoreCase("Disponible")) {
            bg = "#D1FAE5";
            text = "#10B981";
        } else if (status.equalsIgnoreCase("Stock faible")) {
            bg = "#FEF3C7";
            text = "#F59E0B";
        }

        Label badge = new Label(status);
        badge.setAlignment(Pos.CENTER);
        badge.setMinHeight(30);
        badge.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + text + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 7 12 7 12;"
        );

        return badge;
    }

    @FXML
    public void goAddStock() {
        PharmacyAddStockController.setSelectedProduct(null);
        MainFx.changeScene("/pharmacyaddstock.fxml");
    }

    @FXML
    public void refreshStock() {
        Pharmacy selected = pharmacyCombo.getValue();

        if (selected != null) {
            loadProductsByPharmacy(selected);
        }
    }

    @FXML
    public void goStock() {
        MainFx.changeScene("/pharmacystock.fxml");
    }

    private void verifyProductSafety(Product product, Button sourceButton) {
        if (product == null || safe(product.getProductName()).isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Nom du medicament introuvable.");
            return;
        }

        String productName = safe(product.getProductName());

        if (sourceButton != null) {
            sourceButton.setDisable(true);
            sourceButton.setText("Verification...");
        }

        Task<List<String>> recallTask = new Task<>() {
            @Override
            protected List<String> call() {
                return openFdaRecallService.searchDrugRecalls(productName);
            }
        };

        recallTask.setOnSucceeded(event -> {
            if (sourceButton != null) {
                sourceButton.setDisable(false);
                sourceButton.setText("Verifier securite");
            }

            List<String> recalls = recallTask.getValue();

            if (recalls == null || recalls.isEmpty()) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Securite medicament",
                        "Aucun rappel trouve pour ce medicament"
                );
                return;
            }

            showRecallDialog(productName, recalls);
        });

        recallTask.setOnFailed(event -> {
            if (sourceButton != null) {
                sourceButton.setDisable(false);
                sourceButton.setText("Verifier securite");
            }

            showAlert(
                    Alert.AlertType.ERROR,
                    "Erreur openFDA",
                    "Impossible de verifier les rappels de ce medicament."
            );
        });

        Thread thread = new Thread(recallTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showRecallDialog(String productName, List<String> recalls) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Securite medicament");
        alert.setHeaderText("Rappels openFDA : " + productName);

        TextArea textArea = new TextArea(formatRecallResults(recalls));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(680);
        textArea.setPrefHeight(360);
        textArea.setStyle(
                "-fx-control-inner-background: #F8FAFC;" +
                        "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 13px;"
        );

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private String formatRecallResults(List<String> recalls) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < recalls.size(); i++) {
            builder.append("Rappel ").append(i + 1).append("\n");
            builder.append(recalls.get(i));

            if (i < recalls.size() - 1) {
                builder.append("\n\n------------------------------\n\n");
            }
        }

        return builder.toString();
    }

    private void setupCardHover(VBox card) {
        card.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(250), card);
            translate.setToY(-4);

            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#0F172A", 0.11));
            hoverShadow.setRadius(24);
            hoverShadow.setOffsetY(8);
            card.setEffect(hoverShadow);

            translate.play();
        });

        card.setOnMouseExited(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(250), card);
            translate.setToY(0);

            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.web("#0F172A", 0.07));
            normalShadow.setRadius(18);
            normalShadow.setOffsetY(5);
            card.setEffect(normalShadow);

            translate.play();
        });
    }

    private void setupImageHover(ImageView imageView) {
        imageView.setOnMouseEntered(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(250), imageView);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
        });

        imageView.setOnMouseExited(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(250), imageView);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    private void setupButtonHover(Button button) {
        button.setOnMouseEntered(event -> {
            button.setStyle(
                    "-fx-background-color: #1E293B;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(-2);
            translate.play();
        });

        button.setOnMouseExited(event -> {
            button.setStyle(
                    "-fx-background-color: #0F172A;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(0);
            translate.play();
        });
    }

    private void setupSafetyButtonHover(Button button) {
        button.setOnMouseEntered(event -> {
            button.setStyle(
                    "-fx-background-color: #FECACA;" +
                            "-fx-text-fill: #B91C1C;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(-2);
            translate.play();
        });

        button.setOnMouseExited(event -> {
            button.setStyle(
                    "-fx-background-color: #FEE2E2;" +
                            "-fx-text-fill: #EF4444;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(0);
            translate.play();
        });
    }

    private void playCardEntrance(Node node, int index) {
        node.setOpacity(0);
        node.setTranslateY(18);

        FadeTransition fade = new FadeTransition(Duration.millis(330), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 65L));

        TranslateTransition translate = new TranslateTransition(Duration.millis(330), node);
        translate.setFromY(18);
        translate.setToY(0);
        translate.setDelay(Duration.millis(index * 65L));

        new ParallelTransition(fade, translate).play();
    }

    private String formatPrice(double price) {
        return String.format("%.3f", price).replace(".", ",");
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
