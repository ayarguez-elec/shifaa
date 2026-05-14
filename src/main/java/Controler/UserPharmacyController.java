package Controler;

import entities.Pharmacy;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.PharmacyServices;
import tests.Main;
import utils.PharmacyOpeningUtils;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.text.Normalizer;
import java.util.*;

public class UserPharmacyController {

    @FXML private FlowPane pharmacyContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private ComboBox<String> cityCombo;
    @FXML private Label totalLabel;
    @FXML private Label pageLabel;

    private PharmacyServices pharmacyServices;

    private List<Pharmacy> pharmacies = new ArrayList<>();
    private List<Pharmacy> displayedPharmacies = new ArrayList<>();

    private int currentPage = 0;
    private final int itemsPerPage = 6;

    @FXML
    public void initialize() {
        pharmacyServices = new PharmacyServices();

        setupCombos();
        loadPharmacies();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filterPharmacies());
        }

        if (serviceCombo != null) {
            serviceCombo.valueProperty().addListener((obs, oldValue, newValue) -> filterPharmacies());
        }

        if (cityCombo != null) {
            cityCombo.valueProperty().addListener((obs, oldValue, newValue) -> filterPharmacies());
        }
    }

    private void setupCombos() {
        if (serviceCombo != null) {
            serviceCombo.setItems(FXCollections.observableArrayList(
                    "Tous les services",
                    "Ouverte maintenant",
                    "Fermée maintenant",
                    "Service : Jour",
                    "Service : Nuit"
            ));
            serviceCombo.setValue("Tous les services");
        }

        if (cityCombo != null) {
            cityCombo.setItems(FXCollections.observableArrayList(getAllTunisianCities()));
            cityCombo.setValue("Toutes les villes");
            cityCombo.setVisibleRowCount(8);
        }
    }

    private void loadPharmacies() {
        try {
            pharmacies = pharmacyServices.getAll();
            displayedPharmacies = new ArrayList<>(pharmacies);

            loadCities();

            currentPage = 0;
            showCurrentPage();

        } catch (Exception e) {
            e.printStackTrace();

            if (totalLabel != null) {
                totalLabel.setText("Erreur : impossible de charger les pharmacies.");
            }
        }
    }

    private void loadCities() {
        if (cityCombo == null) {
            return;
        }

        Set<String> finalCities = new TreeSet<>();
        finalCities.addAll(getAllTunisianCitiesWithoutDefault());

        for (Pharmacy pharmacy : pharmacies) {
            String city = extractCity(pharmacy.getAddress());

            if (!city.isEmpty() && !isCountry(city)) {
                finalCities.add(city);
            }
        }

        List<String> cities = new ArrayList<>();
        cities.add("Toutes les villes");
        cities.addAll(finalCities);

        cityCombo.setItems(FXCollections.observableArrayList(cities));
        cityCombo.setValue("Toutes les villes");
        cityCombo.setVisibleRowCount(8);
    }

    private List<String> getAllTunisianCities() {
        List<String> cities = new ArrayList<>();
        cities.add("Toutes les villes");
        cities.addAll(getAllTunisianCitiesWithoutDefault());
        return cities;
    }

    private List<String> getAllTunisianCitiesWithoutDefault() {
        return Arrays.asList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef",
                "Mahdia", "La Manouba", "Médenine", "Monastir", "Nabeul",
                "Sfax", "Sidi Bouzid", "Siliana", "Sousse", "Tataouine",
                "Tozeur", "Tunis", "Zaghouan"
        );
    }

    private void filterPharmacies() {
        currentPage = 0;

        String keyword = searchField == null ? "" : normalizeText(searchField.getText());
        String selectedService = serviceCombo == null ? "Tous les services" : serviceCombo.getValue();
        String selectedCity = cityCombo == null ? "Toutes les villes" : cityCombo.getValue();

        List<Pharmacy> filtered = new ArrayList<>();

        for (Pharmacy pharmacy : pharmacies) {
            PharmacyOpeningUtils.PharmacyStatus status =
                    PharmacyOpeningUtils.calculateStatus(pharmacy.getOpeningHours());

            String name = normalizeText(pharmacy.getPharmacyName());
            String address = normalizeText(pharmacy.getAddress());
            String phone = normalizeText(pharmacy.getPhone());
            String openingHours = normalizeText(pharmacy.getOpeningHours());
            String pharmacyStatus = normalizeText(pharmacy.getPharmacyStatus());
            String city = normalizeText(extractCity(pharmacy.getAddress()));

            boolean matchKeyword = keyword.isEmpty()
                    || name.contains(keyword)
                    || address.contains(keyword)
                    || phone.contains(keyword)
                    || openingHours.contains(keyword)
                    || pharmacyStatus.contains(keyword)
                    || city.contains(keyword);

            boolean matchCity = selectedCity == null
                    || selectedCity.equals("Toutes les villes")
                    || city.equals(normalizeText(selectedCity))
                    || address.contains(normalizeText(selectedCity));

            boolean matchService = selectedService == null
                    || selectedService.equals("Tous les services")
                    || normalizeText(status.getMainStatus()).equals(normalizeText(selectedService))
                    || normalizeText(status.getServiceType()).equals(normalizeText(selectedService));

            if (matchKeyword && matchCity && matchService) {
                filtered.add(pharmacy);
            }
        }

        displayedPharmacies = filtered;
        showCurrentPage();
    }

    private void showCurrentPage() {
        if (pharmacyContainer == null) {
            return;
        }

        pharmacyContainer.getChildren().clear();

        if (displayedPharmacies == null || displayedPharmacies.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setSpacing(8);
            emptyBox.setPrefWidth(900);
            emptyBox.setPrefHeight(250);

            Label title = new Label("Aucune pharmacie trouvée.");
            title.getStyleClass().add("medicine-title");

            Label text = new Label("Essayez un autre filtre.");
            text.getStyleClass().add("admin-subtitle");

            emptyBox.getChildren().addAll(title, text);
            pharmacyContainer.getChildren().add(emptyBox);

            if (totalLabel != null) {
                totalLabel.setText("Total : 0 pharmacie");
            }

            if (pageLabel != null) {
                pageLabel.setText("Page 0 / 0");
            }

            return;
        }

        int totalItems = displayedPharmacies.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        if (currentPage < 0) {
            currentPage = 0;
        }

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        int index = 0;

        for (Pharmacy pharmacy : displayedPharmacies.subList(start, end)) {
            VBox card = createPharmacyCard(pharmacy);
            pharmacyContainer.getChildren().add(card);
            playCardEntrance(card, index);
            index++;
        }

        if (totalLabel != null) {
            totalLabel.setText("Total : " + totalItems + " pharmacies");
        }

        if (pageLabel != null) {
            pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        }
    }

    private VBox createPharmacyCard(Pharmacy pharmacy) {
        PharmacyOpeningUtils.PharmacyStatus status =
                PharmacyOpeningUtils.calculateStatus(pharmacy.getOpeningHours());

        VBox card = new VBox();
        card.setSpacing(11);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(455);

        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 20;"
        );

        DropShadow normalShadow = new DropShadow();
        normalShadow.setColor(Color.web("#000000", 0.06));
        normalShadow.setRadius(24);
        normalShadow.setOffsetY(8);
        card.setEffect(normalShadow);

        StackPane imageFrame = createImageFrame(pharmacy);

        Label name = new Label(safe(pharmacy.getPharmacyName()));
        name.setWrapText(true);
        name.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;"
        );

        Label address = new Label(safe(pharmacy.getAddress()));
        address.setWrapText(true);
        address.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 14px;" +
                        "-fx-line-spacing: 5;"
        );

        Label phoneTitle = new Label("📞 Téléphone");
        phoneTitle.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label phone = new Label(safe(pharmacy.getPhone()));
        phone.setWrapText(true);
        phone.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px;");

        Label hoursTitle = new Label("🕐 Horaires");
        hoursTitle.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label hours = new Label(safe(pharmacy.getOpeningHours()));
        hours.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px;");

        VBox timeBlock = createModernTimeBlock(status);

        Button mapButton = new Button("Voir sur carte");
        mapButton.setMaxWidth(Double.MAX_VALUE);
        mapButton.setPrefHeight(42);
        mapButton.setStyle(
                "-fx-background-color: #EFF6FF;" +
                        "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        mapButton.setOnAction(event -> openMap(pharmacy));

        setupMapButtonHover(mapButton);

        card.getChildren().addAll(
                imageFrame,
                name,
                address,
                phoneTitle,
                phone,
                hoursTitle,
                hours,
                createSeparator(),
                timeBlock,
                createSeparator(),
                mapButton
        );

        setupCardHover(card);

        return card;
    }

    private StackPane createImageFrame(Pharmacy pharmacy) {
        StackPane imageFrame = new StackPane();
        imageFrame.setPrefWidth(260);
        imageFrame.setPrefHeight(120);
        imageFrame.setStyle(
                "-fx-background-color: #EFF6FF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #DBEAFE;" +
                        "-fx-border-radius: 16;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(105);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (loadPharmacyImage(pharmacy, imageView)) {
            imageFrame.getChildren().add(imageView);
        } else {
            Label icon = new Label("⚕");
            icon.setStyle("-fx-font-size: 34px; -fx-text-fill: #94A3B8;");
            imageFrame.getChildren().add(icon);
        }

        return imageFrame;
    }

    private boolean loadPharmacyImage(Pharmacy pharmacy, ImageView imageView) {
        try {
            String imagePath = pharmacy.getImage();

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement image pharmacie : " + e.getMessage());
        }

        return false;
    }

    private VBox createModernTimeBlock(PharmacyOpeningUtils.PharmacyStatus status) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.setAlignment(Pos.CENTER);

        boolean isOpen = status.isOpen();

        String serviceIcon = isOpen ? "🌤️ " : "🌙 ";
        String statusIcon = isOpen ? "✅ " : "❌ ";

        String bgColor = isOpen ? "#D1FAE5" : "#FEE2E2";
        String textColor = isOpen ? "#10B981" : "#EF4444";

        Label serviceBadge = createModernBadge(
                serviceIcon + status.getServiceType(),
                bgColor,
                textColor
        );

        Label statusBadge = createModernBadge(
                statusIcon + status.getMainStatus(),
                bgColor,
                textColor
        );

        if (isOpen) {
            startOpenPulse(statusBadge);
        } else {
            addClosedShadow(statusBadge);
        }

        Label detailLabel = new Label("🕐 " + status.getDetailStatus());
        detailLabel.setAlignment(Pos.CENTER);
        detailLabel.setMaxWidth(Double.MAX_VALUE);
        detailLabel.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-style: italic;" +
                        "-fx-padding: 4 0 0 0;"
        );

        box.getChildren().addAll(serviceBadge, statusBadge, detailLabel);

        return box;
    }

    private Label createModernBadge(String text, String backgroundColor, String textColor) {
        Label label = new Label(text);
        label.setMinWidth(200);
        label.setMaxWidth(220);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 10 24 10 24;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0F172A", 0.04));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        label.setEffect(shadow);

        return label;
    }

    private Region createSeparator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxHeight(1);
        line.setStyle("-fx-background-color: #F1F5F9;");
        VBox.setMargin(line, new Insets(3, 0, 3, 0));
        return line;
    }

    private void startOpenPulse(Label label) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#10B981", 0.15));
        glow.setRadius(5);
        glow.setSpread(0.12);

        label.setEffect(glow);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 4),
                        new KeyValue(glow.spreadProperty(), 0.08)
                ),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(glow.radiusProperty(), 14),
                        new KeyValue(glow.spreadProperty(), 0.28)
                ),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(glow.radiusProperty(), 4),
                        new KeyValue(glow.spreadProperty(), 0.08)
                )
        );

        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void addClosedShadow(Label label) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0F172A", 0.04));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        label.setEffect(shadow);
    }

    private void setupCardHover(VBox card) {
        card.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
            translate.setToY(-4);

            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#000000", 0.10));
            hoverShadow.setRadius(32);
            hoverShadow.setOffsetY(12);
            card.setEffect(hoverShadow);

            translate.play();
        });

        card.setOnMouseExited(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
            translate.setToY(0);

            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.web("#000000", 0.06));
            normalShadow.setRadius(24);
            normalShadow.setOffsetY(8);
            card.setEffect(normalShadow);

            translate.play();
        });
    }

    private void setupMapButtonHover(Button button) {
        button.setOnMouseEntered(event -> {
            button.setStyle(
                    "-fx-background-color: #DBEAFE;" +
                            "-fx-text-fill: #0F172A;" +
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
                    "-fx-background-color: #EFF6FF;" +
                            "-fx-text-fill: #0F172A;" +
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

    private void playCardEntrance(Node node, int index) {
        node.setOpacity(0);
        node.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 80));

        TranslateTransition translate = new TranslateTransition(Duration.millis(350), node);
        translate.setFromY(20);
        translate.setToY(0);
        translate.setDelay(Duration.millis(index * 80));

        new ParallelTransition(fade, translate).play();
    }

    private void openMap(Pharmacy pharmacy) {
        try {
            String query = safe(pharmacy.getPharmacyName()) + " " + safe(pharmacy.getAddress());
            String encoded = query.trim().replace(" ", "+");
            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la carte.");
        }
    }

    private String extractCity(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String cleanAddress = address.trim();
        String[] parts = cleanAddress.split(",");

        if (parts.length >= 2) {
            String last = parts[parts.length - 1].trim();
            String beforeLast = parts[parts.length - 2].trim();

            if (isCountry(last)) {
                return beforeLast;
            }

            return last;
        }

        return cleanAddress;
    }

    private boolean isCountry(String value) {
        String normalized = normalizeText(value);
        return normalized.equals("tunisie")
                || normalized.equals("tunisia")
                || normalized.equals("tunisien");
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

    @FXML
    public void resetFilters() {
        if (searchField != null) {
            searchField.clear();
        }

        if (serviceCombo != null) {
            serviceCombo.setValue("Tous les services");
        }

        if (cityCombo != null) {
            cityCombo.setValue("Toutes les villes");
        }

        displayedPharmacies = new ArrayList<>(pharmacies);
        currentPage = 0;
        showCurrentPage();
    }

    @FXML
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) displayedPharmacies.size() / itemsPerPage);

        if (currentPage < totalPages - 1) {
            currentPage++;
            showCurrentPage();
        }
    }

    @FXML
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            showCurrentPage();
        }
    }

    @FXML
    public void goDashboard() {
        Main.showPatientDashboard();
    }

    @FXML
    public void goProducts() {
        Main.showPatientProducts();
    }

    @FXML
    public void goPharmacies() {
        Main.showPatientPharmacies();
    }

    @FXML
    public void goAssistant() {
        Main.showPatientAssistant();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
