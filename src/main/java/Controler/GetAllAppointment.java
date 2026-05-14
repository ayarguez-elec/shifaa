package Controler;

import entities.appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.appointmentServices;
import java.sql.SQLException;
import java.util.List;

public class GetAllAppointment {

    @FXML private ListView<String> listAppointments;
    @FXML private Label messageLabel;

    private final appointmentServices service = new appointmentServices();

    @FXML
    public void initialize() {
        loadRdv();
    }

    @FXML
    public void loadRdv() {
        try {
            List<appointment> all = service.getAll();
            ObservableList<String> items = FXCollections.observableArrayList();

            for (appointment a : all) {
                // ✅ Protection null sur paymentMethod
                String payment = (a.getPaymentMethod() != null)
                        ? a.getPaymentMethodAsString()
                        : "Non précisé";

                // ✅ Protection null sur date et time
                String date = (a.getAppointmentDate() != null)
                        ? a.getAppointmentDate().toString()
                        : "N/A";

                String time = (a.getAppointmentTime() != null)
                        ? a.getAppointmentTime().toString()
                        : "N/A";

                String line = String.format(
                        "RDV #%d  |  Date : %s  |  Heure : %s  |  Raison : %s  |  Type : %s  |  Paiement : %s  |  Patient ID : %d  |  Médecin ID : %d",
                        a.getAppointmentId(),
                        date,
                        time,
                        a.getReason(),
                        a.getType(),
                        payment,
                        a.getPatientId(),
                        a.getDoctorId()
                );
                items.add(line);
            }

            listAppointments.setItems(items);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText(all.size() + " rendez-vous chargés.");

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur DB : " + e.getMessage());
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
