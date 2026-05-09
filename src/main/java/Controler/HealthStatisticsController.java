package Controler;

import entities.HealthLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import services.HealthLogService;
import tests.Main;

import java.sql.SQLException;
import java.util.List;

public class HealthStatisticsController {

    @FXML private LineChart<String, Number> severityChart;
    @FXML private PieChart typePieChart;
    @FXML private VBox historyContainer;
    
    @FXML private Label totalLogsLabel;
    @FXML private Label avgSeverityLabel;
    @FXML private Label lastActivityLabel;
    @FXML private Label alertsCountLabel;

    private HealthLogService healthService = new HealthLogService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            int userId = Main.getCurrentUserId();
            List<HealthLog> logs = healthService.getByUserId(userId);
            
            if (logs.isEmpty()) {
                totalLogsLabel.setText("0");
                avgSeverityLabel.setText("0.0");
                lastActivityLabel.setText("Aucune donnée");
                return;
            }

            // 1. SUMMARY TILES
            totalLogsLabel.setText(String.valueOf(logs.size()));
            double avg = logs.stream().filter(l -> l.getType().equals("Symptom")).mapToInt(HealthLog::getSeverity).average().orElse(0.0);
            avgSeverityLabel.setText(String.format("%.1f", avg));
            lastActivityLabel.setText(logs.get(0).getDate().toString().substring(0, 10));
            
            long emergencyCount = logs.stream().filter(l -> l.getSeverity() >= 10).count();
            alertsCountLabel.setText(String.valueOf(emergencyCount));

            // 2. MODERN CARDS HISTORY
            historyContainer.getChildren().clear();
            for (HealthLog log : logs) {
                historyContainer.getChildren().add(createLogCard(log));
            }

            // 3. LINE CHART
            severityChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int count = 0;
            for (int i = logs.size() - 1; i >= 0 && count < 8; i--) {
                HealthLog log = logs.get(i);
                if (log.getType().equals("Symptom")) {
                    series.getData().add(new XYChart.Data<>(log.getDate().toString().substring(5, 10), log.getSeverity()));
                    count++;
                }
            }
            severityChart.getData().add(series);

            // 4. PIE CHART
            long routine = logs.stream().filter(l -> l.getType().equals("Routine")).count();
            long symptom = logs.stream().filter(l -> l.getType().equals("Symptom")).count();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Suivis (" + routine + ")", routine),
                new PieChart.Data("Alertes (" + symptom + ")", symptom)
            );
            typePieChart.setData(pieData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createLogCard(HealthLog log) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(15));
        
        // Couleur selon sévérité
        String color = "#10B981"; // Vert (Routine)
        if (log.getType().equals("Symptom")) {
            if (log.getSeverity() >= 7) color = "#EF4444"; // Rouge (Urgent)
            else if (log.getSeverity() >= 4) color = "#F59E0B"; // Orange (Modéré)
            else color = "#3B82F6"; // Bleu (Faible)
        }

        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2); -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");

        VBox info = new VBox(5);
        Label typeLbl = new Label(log.getType().toUpperCase());
        typeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 10;");
        
        Label resLbl = new Label(log.getResult());
        resLbl.setStyle("-fx-font-size: 14; -fx-text-fill: #1E293B; -fx-font-weight: bold;");
        
        Label dateLbl = new Label(log.getDate().toString().substring(0, 16));
        dateLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11;");
        
        info.getChildren().addAll(typeLbl, resLbl, dateLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label sevLbl = new Label(log.getSeverity() + "/10");
        sevLbl.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #475569;");

        card.getChildren().addAll(info, sevLbl);
        return card;
    }
}
