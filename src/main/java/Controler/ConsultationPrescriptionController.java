package Controler;

import entities.Prescription;
import entities.userDS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import services.SmsService;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.util.List;
import services.userservDS;

public class ConsultationPrescriptionController {

    @FXML private Label lblPatientName;
    @FXML private Label lblDate;
    @FXML private Label lblDuration;
    @FXML private Label lblDetails;
    @FXML private Label lblPrescriptionId;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;

    private userservDS userService = new userservDS();
    private Prescription currentPrescription;
    private userDS currentPatient;

    @FXML
    public void initialize() {
        chargerProfilMedecin();
    }

    private void chargerProfilMedecin() {
        lblDoctorName.setText("Dr. Saif Gharbi");
        List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty()) {
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

    public void setPrescriptionData(Prescription p, userDS patient) {
        this.currentPrescription = p;
        this.currentPatient = patient;
        if (patient != null) {
            lblPatientName.setText(patient.getFullName());
        } else {
            lblPatientName.setText("Patient inconnu (ID: " + p.getPatientId() + ")");
        }
        
        lblDate.setText(p.getPrescriptionDate().toString());
        lblDuration.setText(p.getTreatmentDuration());
        lblDetails.setText(p.getPrescription());
        lblPrescriptionId.setText("Prescription #" + p.getPrescriptionId());
    }

    @FXML
    public void envoyerSMS() {
        if (currentPatient == null || currentPrescription == null) {
            showAlert("Erreur", "Données manquantes pour l'envoi du SMS.");
            return;
        }

        String messageBody = String.format(
            "Bonjour %s %s (ID: %d), votre traitement: %s. Durée: %s.",
            currentPatient.getFirstName(),
            currentPatient.getLastName(),
            currentPatient.getUserId(),
            currentPrescription.getPrescription(),
            currentPrescription.getTreatmentDuration()
        );

        try {
            SmsService.sendSms("+21651342604", messageBody);
            showAlert("Succès", "SMS envoyé avec succès au patient !");
        } catch (Exception e) {
            showAlert("Erreur Twilio", "Échec de l'envoi : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void retourner(javafx.event.Event event) {
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

    @FXML
    public void allerGererPatients(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GererPatients.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
