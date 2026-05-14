package Controler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.medicalFileServ;
import services.userservDS;
import entities.userDS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StatistiquesController {

    @FXML private LineChart<String, Number> lineChartActivite;
    @FXML private PieChart pieChartStatus;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label lblTotalDossiers;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblNouveauxCeMois;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;

    private medicalFileServ medicalFileService = new medicalFileServ();
    private userservDS userService = new userservDS();

    @FXML
    public void initialize() {
        chargerStats();
        chargerProfilMedecin();
    }

    private void chargerStats() {
        Map<String, Integer> stats = medicalFileService.getStatsByMonth();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux Dossiers");

        int total = 0;
        int ceMois = 0;
        String currentMonth = java.time.LocalDate.now().getMonth().getDisplayName(
            java.time.format.TextStyle.FULL, java.util.Locale.FRENCH
        );
        // Capitalize first letter
        currentMonth = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1);

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            total += entry.getValue();
            if (entry.getKey().equalsIgnoreCase(currentMonth)) {
                ceMois = entry.getValue();
            }
        }

        lineChartActivite.getData().clear();
        lineChartActivite.getData().add(series);
        chargerStatsStatus();
        
        lblTotalDossiers.setText(String.valueOf(total));
        lblTotalPatients.setText(String.valueOf(userService.getAll().size()));
        lblNouveauxCeMois.setText(String.valueOf(ceMois));
    }

    private void chargerStatsStatus() {
        Map<String, Integer> statsStatus = medicalFileService.getStatsByStatus();
        int totalStatus = statsStatus.values().stream().mapToInt(Integer::intValue).sum();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : statsStatus.entrySet()) {
            double percent = totalStatus == 0 ? 0 : (entry.getValue() * 100.0 / totalStatus);
            String label = String.format("%s %.0f%%", entry.getKey(), percent);
            pieData.add(new PieChart.Data(label, entry.getValue()));
        }

        pieChartStatus.setData(pieData);
    }

    private void chargerProfilMedecin() {
        if (lblDoctorName != null) lblDoctorName.setText("Dr. Saif Gharbi");
        List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty() && imgDoctor != null) {
                    try {
                        File file = new File(photoPath);
                        if (file.exists()) {
                            imgDoctor.setImage(new Image(file.toURI().toString()));
                        } else if (getClass().getResource("/" + photoPath) != null) {
                            imgDoctor.setImage(new Image(getClass().getResourceAsStream("/" + photoPath)));
                        }
                    } catch (Exception e) {}
                }
                break;
            }
        }
    }

    @FXML
    public void allerGererPatients(javafx.event.Event event) {
        // Redirection vers la liste du portail docteur
        tests.Main.showDoctorPatients();
    }

    @FXML
    public void allerPrescriptions(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
