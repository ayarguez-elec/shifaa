package controler;

import entities.appointment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.appointmentServices;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;

public class AddAppointment {

    @FXML private TextField patientId, doctorId, appointmentTime;
    @FXML private DatePicker appointmentDate;
    @FXML private ComboBox<String> type, paymentMethod;
    @FXML private TextArea reason;
    @FXML private Label messageLabel;

    private final appointmentServices service = new appointmentServices();

    @FXML
    public void initialize() {
        type.getItems().addAll("Consultation", "Contrôle", "Urgence");
        paymentMethod.getItems().addAll("espèce", "carte bancaire");
    }

    @FXML
    void ajouterRdv() {
        try {
            if (patientId.getText().isEmpty() || doctorId.getText().isEmpty()
                    || appointmentDate.getValue() == null
                    || appointmentTime.getText().isEmpty()
                    || type.getValue() == null
                    || paymentMethod.getValue() == null
                    || reason.getText().isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Veuillez remplir tous les champs.");
                return;
            }

            appointment a = new appointment();
            a.setPatientId(Integer.parseInt(patientId.getText().trim()));
            a.setDoctorId(Integer.parseInt(doctorId.getText().trim()));
            a.setAppointmentDate(Date.valueOf(appointmentDate.getValue()));
            a.setAppointmentTime(Time.valueOf(appointmentTime.getText().trim()));
            a.setType(type.getValue());
            a.setPaymentMethodFromString(paymentMethod.getValue());
            a.setReason(reason.getText().trim());

            service.add(a);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Rendez-vous ajouté avec succès !");
            clearFields();

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID Patient et ID Médecin doivent être des nombres.");
        } catch (IllegalArgumentException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Format heure invalide. Utilisez HH:mm:ss");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur base de données : " + e.getMessage());
        }
    }

    @FXML
    void annuler() {
        clearFields();
        messageLabel.setText("");
    }

    private void clearFields() {
        patientId.clear();
        doctorId.clear();
        appointmentDate.setValue(null);
        appointmentTime.clear();
        type.setValue(null);
        paymentMethod.setValue(null);
        reason.clear();
    }
}