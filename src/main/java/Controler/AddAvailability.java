package Controler;

import entities.availability;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.availabilityServices;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;

public class AddAvailability {

    @FXML private TextField doctorId, time;
    @FXML private DatePicker date;
    @FXML private ComboBox<String> statusRDV;
    @FXML private Label messageLabel;

    private final availabilityServices service = new availabilityServices();

    @FXML
    public void initialize() {
        statusRDV.getItems().addAll("libre", "occupe"); // ✅ "occupe" sans accent (correspond à l'enum)
    }

    @FXML
    void ajouterDisponibilite() {
        try {
            if (doctorId.getText().isEmpty() || date.getValue() == null
                    || time.getText().isEmpty() || statusRDV.getValue() == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Veuillez remplir tous les champs.");
                return;
            }

            availability a = new availability();
            a.setDoctorId(Integer.parseInt(doctorId.getText().trim()));
            a.setDate(Date.valueOf(date.getValue()));
            a.setTime(Time.valueOf(time.getText().trim()));

            // ✅ CORRECTION : convertir le String en enum via la méthode helper
            a.setStatusRDVFromString(statusRDV.getValue());

            service.add(a);
            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Disponibilite ajoutee avec succes !");
            clearFields();

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID Medecin doit etre un nombre.");
        } catch (IllegalArgumentException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Format heure invalide. Utilisez HH:mm:ss");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur base de donnees : " + e.getMessage());
        }
    }

    @FXML
    void annuler() {
        clearFields();
        messageLabel.setText("");
    }

    private void clearFields() {
        doctorId.clear();
        date.setValue(null);
        time.clear();
        statusRDV.setValue(null);
    }
}
