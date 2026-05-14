package Controler;

import entities.availability;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import services.availabilityServices;
import java.sql.SQLException;
import java.util.List;

public class GetAllAvailability {

    @FXML private TableView<availability> tableAvailability;
    @FXML private TableColumn<availability, Integer> colId;
    @FXML private TableColumn<availability, Integer> colDoctor;
    @FXML private TableColumn<availability, String>  colDate;
    @FXML private TableColumn<availability, String>  colTime;
    @FXML private TableColumn<availability, String>  colStatus;
    @FXML private Label messageLabel;

    private final ObservableList<availability> list = FXCollections.observableArrayList();
    private final availabilityServices service = new availabilityServices();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getAvailabilityId()).asObject());

        colDate.setCellValueFactory(d -> {
            // ✅ Protection null sur date
            java.sql.Date date = d.getValue().getDate();
            return new SimpleStringProperty(date != null ? date.toString() : "");
        });

        colTime.setCellValueFactory(d -> {
            // ✅ Protection null sur time
            java.sql.Time time = d.getValue().getTime();
            return new SimpleStringProperty(time != null ? time.toString() : "");
        });

        colStatus.setCellValueFactory(d -> {
            // ✅ Protection null sur statusRDV
            availability.StatusRDV status = d.getValue().getStatusRDV();
            return new SimpleStringProperty(status != null ? status.name() : "");
        });

        colDoctor.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getDoctorId()).asObject());

        tableAvailability.setItems(list);
        loadDisponibilites();
    }

    @FXML
    public void loadDisponibilites() {
        try {
            list.clear();
            List<availability> all = service.getAll();
            list.addAll(all);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText(all.size() + " disponibilités chargées.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur DB : " + e.getMessage());
        } catch (Exception e) {
            // ✅ Capture toute autre exception inattendue
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
