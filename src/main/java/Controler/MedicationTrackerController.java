package Controler;

import entities.MedicationReminder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import services.MedicationService;
import tests.Main;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicationTrackerController {

    @FXML private TextField medNameField;
    @FXML private TextField dosageField;
    @FXML private TextField timeField;
    @FXML private VBox pillContainer;

    private MedicationService medService = new MedicationService();
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        loadMedications();
        
        // Rafraichir toutes les minutes pour verifier les retards
        autoRefresh = new Timeline(new KeyFrame(Duration.minutes(1), e -> loadMedications()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void loadMedications() {
        try {
            int userId = Main.getCurrentUserId();
            List<MedicationReminder> list = medService.getByUserId(userId);
            pillContainer.getChildren().clear();
            for (MedicationReminder m : list) {
                pillContainer.getChildren().add(createMedCard(m));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddMedication() {
        if (medNameField.getText().isEmpty() || timeField.getText().isEmpty()) return;
        try {
            MedicationReminder m = new MedicationReminder(
                Main.getCurrentUserId(),
                medNameField.getText(),
                dosageField.getText(),
                timeField.getText(),
                false
            );
            medService.add(m);
            medNameField.clear(); dosageField.clear(); timeField.clear();
            loadMedications();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox createMedCard(MedicationReminder m) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(15));
        
        // --- LOGIQUE TEMPS RÉEL ---
        boolean isDelayed = false;
        try {
            LocalTime now = LocalTime.now();
            LocalTime medTime = LocalTime.parse(m.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
            if (now.isAfter(medTime) && !m.isTaken()) {
                isDelayed = true;
            }
        } catch (Exception e) { /* Format heure invalide */ }

        // --- STYLE DYNAMIQUE ---
        String bg = "white";
        String border = "#E2E8F0";
        String statusText = "Prévu à " + m.getTime();
        String statusColor = "#64748B";

        if (m.isTaken()) {
            bg = "#F0FDF4";
            border = "#10B981";
            statusText = "✓ Pris à l'heure";
            statusColor = "#10B981";
        } else if (isDelayed) {
            bg = "#FEF2F2"; // Rouge très clair
            border = "#EF4444"; // Rouge
            statusText = "⚠️ RETARD / OUBLI (Prévu à " + m.getTime() + ")";
            statusColor = "#EF4444";
        }

        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12; -fx-border-color: " + border + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        VBox info = new VBox(5);
        Label name = new Label(m.getMedName().toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #1E293B;");
        
        Label detail = new Label(m.getDosage());
        detail.setStyle("-fx-font-size: 13; -fx-text-fill: #475569;");
        
        Label status = new Label(statusText);
        status.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
        
        info.getChildren().addAll(name, detail, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        // --- ACTIONS ---
        Button actionBtn = new Button(m.isTaken() ? "Annuler" : "Marquer comme pris");
        if (m.isTaken()) {
            actionBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        } else if (isDelayed) {
            actionBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            actionBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3B82F6; -fx-border-radius: 20; -fx-text-fill: #3B82F6; -fx-cursor: hand;");
        }
        
        actionBtn.setOnAction(e -> {
            try {
                medService.updateStatus(m.getId(), !m.isTaken());
                loadMedications();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            try {
                medService.delete(m.getId());
                loadMedications();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        card.getChildren().addAll(info, actionBtn, delBtn);
        return card;
    }
}
