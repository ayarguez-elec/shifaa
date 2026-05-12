package controler;

import entities.availability;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import services.availabilityServices;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class UpdateAvailability implements Initializable {

    @FXML private TextField searchId;
    @FXML private TextField doctorId;
    @FXML private DatePicker date;
    @FXML private TextField time;
    @FXML private ComboBox<String> statusRDV;
    @FXML private Label messageLabel;

    private final availabilityServices service = new availabilityServices();
    private availability current;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ Remplir le ComboBox avec les valeurs de l'enum StatusRDV
        statusRDV.getItems().addAll("libre", "occupe");
    }

    @FXML
    void rechercherDisponibilite() {
        try {
            int id = Integer.parseInt(searchId.getText().trim());

            // ✅ CORRECTION : getAvailabilityById() au lieu de getById()
            current = service.getAvailabilityById(id);

            if (current == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Aucune disponibilité trouvée avec l'ID " + id);
                clearFields();
            } else {
                // ✅ Remplir les champs avec les données existantes
                doctorId.setText(String.valueOf(current.getDoctorId()));

                // ✅ CORRECTION : Date SQL → LocalDate pour DatePicker
                date.setValue(current.getDate().toLocalDate());

                // ✅ Time SQL → String HH:mm:ss
                time.setText(current.getTime().toString());

                // ✅ CORRECTION : getStatusRDVAsString() car statusRDV est un enum
                statusRDV.setValue(current.getStatusRDVAsString());

                messageLabel.setStyle("-fx-text-fill: #2D3748;");
                messageLabel.setText("Disponibilité trouvée. Modifiez puis cliquez sur Modifier.");
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
    void modifierDisponibilite() {
        if (current == null) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Veuillez d'abord rechercher une disponibilité.");
            return;
        }

        try {
            // ✅ Validation doctorId
            int docId = Integer.parseInt(doctorId.getText().trim());

            // ✅ CORRECTION : LocalDate → java.sql.Date
            LocalDate localDate = date.getValue();
            if (localDate == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Veuillez sélectionner une date.");
                return;
            }
            Date sqlDate = Date.valueOf(localDate);

            // ✅ Validation et parsing de l'heure HH:mm:ss
            String timeText = time.getText().trim();
            if (!timeText.matches("\\d{2}:\\d{2}:\\d{2}")) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Format heure invalide. Utilisez HH:mm:ss");
                return;
            }
            Time sqlTime = Time.valueOf(timeText);

            // ✅ Validation statut
            String selectedStatus = statusRDV.getValue();
            if (selectedStatus == null) {
                messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
                messageLabel.setText("Veuillez sélectionner un statut.");
                return;
            }

            // ✅ Mettre à jour l'objet current
            current.setDoctorId(docId);
            current.setDate(sqlDate);
            current.setTime(sqlTime);
            current.setStatusRDVFromString(selectedStatus);

            service.update(current);

            messageLabel.setStyle("-fx-text-fill: #38A169;");
            messageLabel.setText("Disponibilité #" + current.getAvailabilityId() + " modifiée avec succès !");

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("ID Médecin invalide.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #FF5C5C;");
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    private void clearFields() {
        doctorId.clear();
        date.setValue(null);
        time.clear();
        statusRDV.setValue(null);
    }
}