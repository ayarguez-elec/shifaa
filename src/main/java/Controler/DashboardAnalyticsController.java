package Controler;

import entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AnalyticsServ;
import utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DashboardAnalyticsController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblInitials;

    // Stats Labels
    @FXML private Label lblTotalEvents;
    @FXML private Label lblTotalParticipants;
    @FXML private Label lblSaturation;
    @FXML private Label lblAvgPlaces;
    @FXML private Label lblWaitlist;

    // Charts
    @FXML private PieChart pieEventStatus;
    @FXML private BarChart<String, Number> barPopularEvents;
    @FXML private LineChart<String, Number> lineTemporal;
    @FXML private PieChart pieResStatus;

    private final AnalyticsServ analyticsServ = new AnalyticsServ();

    @FXML
    public void initialize() {
        loadUserProfile();
        refreshData();
    }

    private void refreshData() {
        // Load data in background to keep UI responsive
        new Thread(() -> {
            Map<String, Object> eventStats = analyticsServ.getEventStats();
            Map<String, Object> partStats = analyticsServ.getParticipantStats();
            Map<String, Object> seatStats = analyticsServ.getSeatStats();
            Map<String, Object> tempStats = analyticsServ.getTemporalStats();

            Platform.runLater(() -> {
                updateTopStats(eventStats, partStats, seatStats);
                updateEventStatusChart(eventStats);
                updatePopularEventsChart(eventStats);
                updateTemporalChart(tempStats);
                updateResStatusChart(partStats);
            });
        }).start();
    }

    private void updateTopStats(Map<String, Object> ev, Map<String, Object> part, Map<String, Object> seat) {
        if (lblTotalEvents      != null) lblTotalEvents.setText(String.valueOf(ev.getOrDefault("total_evenements", 0)));
        if (lblTotalParticipants!= null) lblTotalParticipants.setText(String.valueOf(part.getOrDefault("total_participants", 0)));
        if (lblSaturation       != null) lblSaturation.setText(String.valueOf(seat.getOrDefault("saturation_globale", "0%")));
        if (lblAvgPlaces        != null) lblAvgPlaces.setText(String.valueOf(part.getOrDefault("moyenne_places_par_resa", 0)));
        if (lblWaitlist         != null) lblWaitlist.setText(String.valueOf(ev.getOrDefault("total_en_liste_attente", 0)));
    }

    private void updateEventStatusChart(Map<String, Object> ev) {
        if (pieEventStatus == null) return;
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
            new PieChart.Data("Actifs (" + ev.get("actifs") + ")", (int) ev.get("actifs")),
            new PieChart.Data("Terminés (" + ev.get("termines") + ")", (int) ev.get("termines")),
            new PieChart.Data("Annulés (" + ev.get("annules") + ")", (int) ev.get("annules"))
        );
        pieEventStatus.setData(data);
    }

    private void updateResStatusChart(Map<String, Object> part) {
        if (pieResStatus == null) return;

        int confirme = (int) part.getOrDefault("count_confirme", 0);
        int refuse   = (int) part.getOrDefault("count_refuse",   0);
        int attente  = (int) part.getOrDefault("count_attente",  0);
        int annule   = (int) part.getOrDefault("count_annule",   0);

        // Show chart only when there is data to display
        if (confirme + refuse + attente + annule == 0) {
            pieResStatus.setData(FXCollections.observableArrayList(
                new PieChart.Data("Aucune réservation", 1)
            ));
            return;
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        if (confirme > 0) data.add(new PieChart.Data("Confirmées (" + confirme + ")", confirme));
        if (refuse   > 0) data.add(new PieChart.Data("Refusées ("  + refuse   + ")", refuse));
        if (attente  > 0) data.add(new PieChart.Data("En attente (" + attente + ")", attente));
        if (annule   > 0) data.add(new PieChart.Data("Annulées ("  + annule   + ")", annule));
        pieResStatus.setData(data);
    }

    private void updatePopularEventsChart(Map<String, Object> ev) {
        if (barPopularEvents == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Places réservées");

        List<Map<String, Object>> popular = (List<Map<String, Object>>) ev.get("plus_populaires");
        if (popular != null) {
            for (Map<String, Object> item : popular) {
                series.getData().add(new XYChart.Data<>((String) item.get("titre"), (Integer) item.get("places")));
            }
        }
        barPopularEvents.getData().clear();
        barPopularEvents.getData().add(series);
    }

    private void updateTemporalChart(Map<String, Object> temp) {
        if (lineTemporal == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        List<Map<String, Object>> daily = (List<Map<String, Object>>) temp.get("reservations_7_derniers_jours");
        if (daily != null) {
            for (Map<String, Object> item : daily) {
                series.getData().add(new XYChart.Data<>((String) item.get("date"), (Integer) item.get("count")));
            }
        }
        lineTemporal.getData().clear();
        lineTemporal.getData().add(series);
    }

    private void loadUserProfile() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && lblUserName != null) {
            String email = user.getEmail();
            String name = email.substring(0, email.indexOf('@'));
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            lblUserName.setText(name);
            if (lblUserRole != null) lblUserRole.setText(user.getRole());
            if (lblInitials != null) lblInitials.setText(name.substring(0, 1).toUpperCase());
        }
    }

    // Navigation
    @FXML public void handleNaviguerEvenements()   { naviguer("/GetAllevent.fxml"); }
    @FXML public void handleNaviguerParticipants() { naviguer("/GetAllparticipant.fxml"); }
    @FXML public void handleNaviguerReservations() { naviguer("/SmartBooking.fxml"); }
    @FXML public void handleLogout() {
        naviguer("/GetAllevent.fxml");
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) lblUserName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
