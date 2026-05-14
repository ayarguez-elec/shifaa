package Controler;

import entities.Pharmacy;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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

import java.io.File;
import java.text.Normalizer;
import java.util.*;

public class AdminPharmacyController {

    @FXML private FlowPane pharmacyContainer;
    @FXML private TextField searchField;
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

        setupCityCombo();
        loadPharmacies();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filterPharmacies());
        }

        if (cityCombo != null) {
            cityCombo.valueProperty().addListener((obs, oldValue, newValue) -> filterPharmacies());
        }
    }

    private void setupCityCombo() {
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
        String selectedCity = cityCombo == null ? "Toutes les villes" : cityCombo.getValue();

        List<Pharmacy> filtered = new ArrayList<>();

        for (Pharmacy pharmacy : pharmacies) {
            String name = normalizeText(pharmacy.getPharmacyName());
            String address = normalizeText(pharmacy.getAddress());
            String phone = normalizeText(pharmacy.getPhone());
            String email = normalizeText(pharmacy.getEmail());
            String hours = normalizeText(pharmacy.getOpeningHours());
            String status = normalizeText(pharmacy.getPharmacyStatus());
            String city = normalizeText(extractCity(pharmacy.getAddress()));

            boolean matchKeyword = keyword.isEmpty()
                    || name.contains(keyword)
                    || address.contains(keyword)
                    || phone.contains(keyword)
                    || email.contains(keyword)
                    || hours.contains(keyword)
                    || status.contains(keyword)
                    || city.contains(keyword);

            boolean matchCity = selectedCity == null
                    || selectedCity.equals("Toutes les villes")
                    || city.equals(normalizeText(selectedCity))
                    || address.contains(normalizeText(selectedCity));

            if (matchKeyword && matchCity) {
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

        pharmacyContainer.setHgap(24);
        pharmacyContainer.setVgap(24);
        pharmacyContainer.setPadding(new Insets(20, 0, 95, 0));

        if (displayedPharmacies == null || displayedPharmacies.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setSpacing(8);
            emptyBox.setPrefWidth(900);
            emptyBox.setPrefHeight(250);

            Label title = new Label("Aucune pharmacie trouvée.");
            title.setStyle(
                    "-fx-text-fill: #0F172A;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );

            Label text = new Label("Essayez un autre filtre.");
            text.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");

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
        VBox card = new VBox();
        card.setSpacing(14);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(390);
        card.setAlignment(Pos.TOP_LEFT);

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
        name.setMaxWidth(Double.MAX_VALUE);
        name.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;"
        );

        Label phoneTitle = new Label("Téléphone");
        phoneTitle.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label phone = new Label(safe(pharmacy.getPhone()));
        phone.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px;");

        Label hoursTitle = new Label("Horaires");
        hoursTitle.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label hours = new Label(safe(pharmacy.getOpeningHours()));
        hours.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px;");

        Label typeTitle = new Label("Type");
        typeTitle.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label type = new Label(safe(pharmacy.getPharmacyStatus()));
        type.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Modifier");
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setPrefHeight(42);
        editButton.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        editButton.setOnAction(event -> openUpdatePharmacy(pharmacy));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setPrefHeight(42);
        deleteButton.setStyle(
                "-fx-background-color: #FEE2E2;" +
                        "-fx-text-fill: #B91C1C;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(event -> deletePharmacy(pharmacy));

        HBox actionButtons = new HBox();
        actionButtons.setSpacing(8);
        HBox.setHgrow(editButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        actionButtons.getChildren().addAll(editButton, deleteButton);

        setupButtonHover(editButton);
        setupButtonHover(deleteButton);
        setupCardHover(card);

        card.getChildren().addAll(
                imageFrame,
                name,
                phoneTitle,
                phone,
                hoursTitle,
                hours,
                typeTitle,
                type,
                spacer,
                actionButtons
        );

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

    private void setupButtonHover(Button button) {
        button.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(-2);
            translate.play();
        });

        button.setOnMouseExited(event -> {
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

    private void openUpdatePharmacy(Pharmacy pharmacy) {
        if (pharmacy == null) {
            return;
        }

        AdminUpdatePharmacyController.setSelectedPharmacy(pharmacy);
        Main.showAdminUpdatePharmacy();
    }

    private void deletePharmacy(Pharmacy pharmacy) {
        if (pharmacy == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la pharmacie");
        confirm.setContentText("Voulez-vous vraiment supprimer cette pharmacie ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                pharmacyServices.delete(pharmacy);
                loadPharmacies();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Pharmacie supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la pharmacie.");
            }
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
    public void goAddPharmacy() {
        Main.showAdminAddPharmacy();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
