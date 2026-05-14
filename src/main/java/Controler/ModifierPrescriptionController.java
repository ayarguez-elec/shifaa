package Controler;

import entities.Prescription;
import entities.medicalFile;
import entities.userDS;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Prescriptionserv;
import services.medicalFileServ;
import services.userservDS;
import services.VoskService;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierPrescriptionController {

    @FXML private ComboBox<userDS> comboPatient;
    @FXML private DatePicker datePrescription;
    @FXML private TextField txtDuree;
    @FXML private TextArea txtPrescription;

    private Prescriptionserv prescriptionService;
    private userservDS userService;
    private medicalFileServ medicalFileService;
    private Prescription prescriptionEnCours;
    private boolean isRecording = false;

    public ModifierPrescriptionController() {
        prescriptionService = new Prescriptionserv();
        userService = new userservDS();
        medicalFileService = new medicalFileServ();
    }

    @FXML
    public void initialize() {
        // Charger la liste des patients
        List<userDS> patients = userService.getAll();
        comboPatient.setItems(FXCollections.observableArrayList(patients));
        
        // Custom display pour le ComboBox
        comboPatient.setCellFactory(lv -> new ListCell<userDS>() {
            @Override
            protected void updateItem(userDS item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getFullName());
            }
        });
        comboPatient.setButtonCell(new ListCell<userDS>() {
            @Override
            protected void updateItem(userDS item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getFullName());
            }
        });
        setupValidationListeners();
    }

    @FXML
    public void handleVoiceInput(ActionEvent event) {
        Button micButton = (Button) event.getSource();
        String targetId = (String) micButton.getUserData();
        
        if (isRecording) {
            VoskService.getInstance().stopListening();
            micButton.setStyle("-fx-background-color: #EDF2F7; -fx-background-radius: 10;");
            isRecording = false;
        } else {
            if (!VoskService.getInstance().isReady()) {
                showAlert("Modèle Vocal Manquant", "Le dossier 'model' contenant le modèle Vosk est introuvable à la racine.");
                return;
            }
            
            isRecording = true;
            micButton.setStyle("-fx-background-color: #FF5C5C; -fx-background-radius: 10; -fx-text-fill: white;");
            
            VoskService.getInstance().startListening(text -> {
                Platform.runLater(() -> {
                    appendToField(targetId, text);
                });
            });
        }
    }

    private void appendToField(String id, String text) {
        if (text == null || text.trim().isEmpty()) return;
        String formattedText = text + " ";
        if ("txtDuree".equals(id)) {
            txtDuree.appendText(formattedText);
        } else if ("txtPrescription".equals(id)) {
            txtPrescription.appendText(formattedText);
        }
    }

    public void prechargerDonnees(Prescription p) {
        this.prescriptionEnCours = p;
        datePrescription.setValue(p.getPrescriptionDate());
        txtDuree.setText(p.getTreatmentDuration());
        txtPrescription.setText(p.getPrescription());
        
        // Sélectionner le bon patient
        for (userDS patient : comboPatient.getItems()) {
            if (patient.getUserId() == p.getPatientId()) {
                comboPatient.setValue(patient);
                break;
            }
        }
    }

    @FXML
    public void enregistrerModifications(ActionEvent event) {
        userDS selectedPatient = comboPatient.getValue();
        LocalDate date = datePrescription.getValue();
        String duree = txtDuree.getText();
        String detail = txtPrescription.getText();

        if (!validateInputs(selectedPatient, date, duree, detail)) {
            return;
        }

        prescriptionEnCours.setPrescriptionDate(date);
        prescriptionEnCours.setPrescription(detail);
        prescriptionEnCours.setTreatmentDuration(duree);
        prescriptionEnCours.setPatientId(selectedPatient.getUserId());

        try {
            // Mettre à jour l'idfile si le patient a changé
            List<medicalFile> dossiers = medicalFileService.getByPatientId(selectedPatient.getUserId());
            if (dossiers.isEmpty()) {
                showAlert("Erreur", "Le patient sélectionné n'a pas de dossier médical.");
                return;
            }
            prescriptionEnCours.setIdfile(dossiers.get(0).getIdfile());

            prescriptionService.update(prescriptionEnCours);
            showAlert("Succès", "La prescription a été modifiée avec succès !");
            retourner(event);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs(userDS selectedPatient, LocalDate date, String duree, String detail) {
        clearValidationErrors();
        boolean valid = true;
        Node firstInvalidField = null;

        if (selectedPatient == null) {
            markInvalid(comboPatient);
            firstInvalidField = comboPatient;
            valid = false;
        }
        if (date == null) {
            markInvalid(datePrescription);
            if (firstInvalidField == null) firstInvalidField = datePrescription;
            valid = false;
        }
        if (duree == null || duree.trim().isEmpty()) {
            markInvalid(txtDuree);
            if (firstInvalidField == null) firstInvalidField = txtDuree;
            valid = false;
        }
        if (detail == null || detail.trim().isEmpty()) {
            markInvalid(txtPrescription);
            if (firstInvalidField == null) firstInvalidField = txtPrescription;
            valid = false;
        }

        if (!valid) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            firstInvalidField.requestFocus();
        }

        return valid;
    }

    private void setupValidationListeners() {
        comboPatient.valueProperty().addListener((obs, oldVal, newVal) -> clearInvalid(comboPatient));
        datePrescription.valueProperty().addListener((obs, oldVal, newVal) -> clearInvalid(datePrescription));
        txtDuree.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(txtDuree));
        txtPrescription.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(txtPrescription));
    }

    private void markInvalid(Node field) {
        if (!field.getStyleClass().contains("error-field")) {
            field.getStyleClass().add("error-field");
        }
    }

    private void clearInvalid(Node field) {
        field.getStyleClass().remove("error-field");
    }

    private void clearValidationErrors() {
        clearInvalid(comboPatient);
        clearInvalid(datePrescription);
        clearInvalid(txtDuree);
        clearInvalid(txtPrescription);
    }

    @FXML
    public void retourner(javafx.event.Event event) {
        if (isRecording) {
            VoskService.getInstance().stopListening();
        }
        tests.Main.showDoctorDashboard();
    }

    @FXML
    public void allerGererPatients(javafx.event.Event event) {
        if (isRecording) {
            VoskService.getInstance().stopListening();
        }
        // Redirection vers la liste du portail docteur
        tests.Main.showDoctorPatients();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
