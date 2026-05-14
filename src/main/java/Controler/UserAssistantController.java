package Controler;

import GUI.MapViewWindow;
import entities.Pharmacy;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.AssistantProximiteService;
import services.PharmacyRecommendationService.PharmacyRecommendation;
import tests.Main;

import java.util.List;

public class UserAssistantController {

    @FXML private TextField locationField;
    @FXML private FlowPane resultsBox;
    @FXML private Label infoLabel;

    private AssistantProximiteService assistantProximiteService;

    @FXML
    public void initialize() {
        assistantProximiteService = new AssistantProximiteService();
        infoLabel.setText("Saisissez votre emplacement pour afficher les pharmacies les plus proches.");
    }

    @FXML
    public void searchNearestPharmacies() {
        String location = locationField.getText();

        if (location == null || location.trim().isEmpty()) {
            infoLabel.setText("Veuillez saisir votre emplacement. Exemple : Ariana Soghra.");
            return;
        }

        resultsBox.getChildren().clear();
        infoLabel.setText("Recherche des pharmacies les plus proches...");

        Thread thread = new Thread(() -> {
            assistantProximiteService.rechercherPharmaciesProches(location);
            String userLocation = assistantProximiteService.getLastUserLocation();
            List<PharmacyRecommendation> recommendations = assistantProximiteService.getLastRecommendations();

            Platform.runLater(() -> {
                resultsBox.getChildren().clear();

                if (recommendations == null || recommendations.isEmpty()) {
                    infoLabel.setText("Aucune pharmacie proche trouvée pour : " + location);
                    return;
                }

                infoLabel.setText("Les pharmacies les plus proches de : " + userLocation);

                int rank = 1;

                for (PharmacyRecommendation recommendation : recommendations) {
                    resultsBox.getChildren().add(createRecommendationCard(rank, userLocation, recommendation));
                    rank++;
                }
            });
        });

        thread.setDaemon(true);
        thread.start();
    }

    private VBox createRecommendationCard(int rank, String userLocation, PharmacyRecommendation recommendation) {
        Pharmacy pharmacy = recommendation.getPharmacy();

        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(18));
        card.setPrefWidth(330);
        card.setMinHeight(230);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #dbe5f0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 4);"
        );

        Label title = new Label(rank + ". " + safe(pharmacy.getPharmacyName()));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0b1f44;");

        Label address = new Label("Adresse : " + safe(pharmacy.getAddress()));
        address.setWrapText(true);
        address.setStyle("-fx-text-fill: #53627c; -fx-font-size: 13px;");

        Label phone = new Label("Téléphone : " + safe(pharmacy.getPhone()));
        phone.setStyle("-fx-text-fill: #53627c; -fx-font-size: 13px;");

        Label status = new Label("Statut : " + safe(pharmacy.getPharmacyStatus()));
        status.setStyle(
                "-fx-background-color: #ecfdf5;" +
                        "-fx-text-fill: #166534;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 7 12 7 12;" +
                        "-fx-background-radius: 12;"
        );

        Button routeButton = new Button("Afficher l’itinéraire");
        routeButton.setPrefHeight(42);
        routeButton.setMaxWidth(Double.MAX_VALUE);
        routeButton.setStyle(
                "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 14;" +
                        "-fx-padding: 10 16 10 16;" +
                        "-fx-cursor: hand;"
        );

        routeButton.setOnAction(event -> MapViewWindow.showRouteInApp(
                userLocation,
                pharmacy.getPharmacyName(),
                pharmacy.getAddress()
        ));

        card.getChildren().addAll(
                title,
                address,
                phone,
                status,
                routeButton
        );

        return card;
    }

    @FXML
    public void clearAssistant() {
        locationField.clear();
        resultsBox.getChildren().clear();
        infoLabel.setText("Saisissez votre emplacement pour afficher les pharmacies les plus proches.");
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

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Non renseigné" : value.trim();
    }
}
