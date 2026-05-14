package Controler;

import entities.appointment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.appointmentServices;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;

public class UpdateAppointment {

    @FXML private TextField searchId, patientId, doctorId, appointmentTime;
    @FXML private DatePicker appointmentDate;
    @FXML private ComboBox<String> type, paymentMethod;
    @FXML private TextArea reason;
    @FXML private Label messageLabel;

    private final appointmentServices service = new appointmentServices();
    private appointment current;

    @FXML
    public void initialize() {
        type.getItems().addAll("Consultation", "Contrôle", "Urgence");
        paymentMethod.getItems().addAll("espèce", "carte bancaire");
    }

    @FXML
    void rechercherRdv() {
        try {
            int id = Integer.parseInt(searchId.getText().trim());
            current = service.getAppointmentById(id);
            if (current == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Aucun rendez-vous trouvé avec l'ID " + id);
            } else {
                patientId.setText(String.valueOf(current.getPatientId()));
                doctorId.setText(String.valueOf(current.getDoctorId()));
                appointmentDate.setValue(current.getAppointmentDate().toLocalDate());
                appointmentTime.setText(String.valueOf(current.getAppointmentTime()));
                type.setValue(current.getType());
                paymentMethod.setValue(current.getPaymentMethodAsString());
                reason.setText(current.getReason());
                messageLabel.setStyle("-fx-text-fill: #2D3748;");
                messageLabel.setText("Rendez-vous chargé. Modifiez puis cliquez sur Modifier.");
            }
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID invalide.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void modifierRdv() {
        if (current == null) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Veuillez d'abord rechercher un rendez-vous.");
            return;
        }
        try {
            current.setPatientId(Integer.parseInt(patientId.getText().trim()));
            current.setDoctorId(Integer.parseInt(doctorId.getText().trim()));
            current.setAppointmentDate(Date.valueOf(appointmentDate.getValue()));
            current.setAppointmentTime(Time.valueOf(appointmentTime.getText().trim()));
            current.setType(type.getValue());
            current.setPaymentMethodFromString(paymentMethod.getValue());
            current.setReason(reason.getText().trim());

            service.update(current);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Rendez-vous mis à jour avec succès !");
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID Patient/Médecin invalides.");
        } catch (IllegalArgumentException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Format heure invalide. Utilisez HH:mm:ss");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }
}
