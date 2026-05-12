package controler;

import entities.appointment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.appointmentServices;
import java.sql.SQLException;

public class DeleteAppointment {

    @FXML private TextField appointmentIdField;
    @FXML private TextField previewPatient, previewDoctor, previewDate, previewTime, previewType, previewReason;
    @FXML private Label messageLabel;

    private final appointmentServices service = new appointmentServices();
    private appointment current;

    @FXML
    void rechercherRdv() {
        try {
            int id = Integer.parseInt(appointmentIdField.getText().trim());
            current = service.getAppointmentById(id);
            if (current == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Aucun rendez-vous trouvé avec l'ID " + id);
                clearPreview();
            } else {
                previewPatient.setText(String.valueOf(current.getPatientId()));
                previewDoctor.setText(String.valueOf(current.getDoctorId()));
                previewDate.setText(String.valueOf(current.getAppointmentDate()));
                previewTime.setText(String.valueOf(current.getAppointmentTime()));
                previewType.setText(current.getType());
                previewReason.setText(current.getReason());
                messageLabel.setStyle("-fx-text-fill: #2D3748;");
                messageLabel.setText("Rendez-vous trouvé. Cliquez sur Supprimer pour confirmer.");
            }
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Veuillez entrer un ID valide.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void supprimerRdv() {
        if (current == null) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Veuillez d'abord rechercher un rendez-vous.");
            return;
        }
        try {
            service.delete(current);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Rendez-vous #" + current.getAppointmentId() + " supprimé avec succès !");
            clearPreview();
            appointmentIdField.clear();
            current = null;
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur suppression : " + e.getMessage());
        }
    }

    private void clearPreview() {
        previewPatient.clear(); previewDoctor.clear();
        previewDate.clear(); previewTime.clear();
        previewType.clear(); previewReason.clear();
    }
}