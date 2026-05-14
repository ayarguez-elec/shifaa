package Controler;

import entities.userDS;
import entities.medicalFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import services.userservDS;
import services.medicalFileServ;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.application.Platform;
import services.VoskService;
import java.io.IOException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifiermedicalFile {

    // ================= FXML COMPONENTS =================
    @FXML private TextField searchPatient;
    @FXML private ListView<userDS> listPatients;
    @FXML private Label lblPatientName;
    @FXML private TextField idfile;
    @FXML private TextField patientId;
    @FXML private DatePicker creationdate;
    @FXML private DatePicker lastvisit;
    @FXML private ComboBox<String> patientstatus;
    @FXML private TextArea generalhistory;
    @FXML private TextArea antecedents;
    @FXML private TextArea allergies;
    @FXML private TextArea followup;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;
    @FXML private Label lblIdFile;
    @FXML private Label lblPatientId;

    // ================= SERVICES (non-statiques) =================
    private userservDS userService;        // ✅ Non-static
    private medicalFileServ medicalFileService;  // ✅ Non-static

    // ================= DATA =================
    private ObservableList<userDS> patientsList;
    private userDS selectedPatient;
    private medicalFile currentMedicalFile;
    
    private boolean isRecording = false;
    private Button activeMicButton = null;

    // ✅ Constructeur - initialisation des services
    public ModifiermedicalFile() {
        userService = new userservDS();
        medicalFileService = new medicalFileServ();  // ✅ Création de l'instance
        patientsList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configurer le ComboBox
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "stable", "en amélioration", "critique"
        );
        patientstatus.setItems(statuses);

        setupSearchListener();
        setupSelectionListener();
        setupValidationListeners();
        chargerProfilMedecin();
    }

    private void chargerProfilMedecin() {
        if (lblDoctorName != null) lblDoctorName.setText("Dr. Saif Gharbi");
        List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty() && imgDoctor != null) {
                    try {
                        java.io.File file = new java.io.File(photoPath);
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

    private void setupSearchListener() {
        searchPatient.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                List<userDS> result = userService.searchByNameOrSurname(newVal);
                patientsList.setAll(result);
                listPatients.setItems(patientsList);
                listPatients.setVisible(true);
            } else {
                listPatients.getItems().clear();
                listPatients.setVisible(false);
            }
        });
    }

    private void setupSelectionListener() {
        listPatients.setOnMouseClicked(event -> {
            selectedPatient = listPatients.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                searchPatient.setText(selectedPatient.getFullName());
                listPatients.setVisible(false);
                listPatients.getItems().clear();
                chargerDossierMedical(selectedPatient.getUserId());
            }
        });
    }

    private void chargerDossierMedical(int patientId) {
        try {
            List<medicalFile> dossiers = medicalFileService.getByPatientId(patientId);

            if (dossiers != null && !dossiers.isEmpty()) {
                currentMedicalFile = dossiers.get(0);
                remplirChamps(currentMedicalFile);
            } else {
                showAlert("Information", "Aucun dossier trouvé");
                clearFields();
                this.patientId.setText(String.valueOf(patientId));
            }
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void remplirChamps(medicalFile mf) {
        idfile.setText(String.valueOf(mf.getIdfile()));
        patientId.setText(String.valueOf(mf.getPatientId()));
        if (lblIdFile != null) lblIdFile.setText(String.valueOf(mf.getIdfile()));
        if (lblPatientId != null) lblPatientId.setText(String.valueOf(mf.getPatientId()));
        creationdate.setValue(mf.getCreationdate());
        lastvisit.setValue(mf.getLastvisit());
        generalhistory.setText(mf.getGeneralhistory());
        antecedents.setText(mf.getAntecedents());
        allergies.setText(mf.getAllergies());
        followup.setText(mf.getFollowup());

        if (mf.getPatientstatus() != null) {
            patientstatus.setValue(mf.getPatientstatus());
        }
    }

    /**
     * Méthode publique pour pré-remplir le formulaire depuis la vue consultation.
     */
    public void prechargerDossier(medicalFile mf, userDS patient) {
        currentMedicalFile = mf;
        selectedPatient = patient;
        if (searchPatient != null && patient != null) {
            searchPatient.setText(patient.getFullName());
        }
        if (lblPatientName != null && patient != null) {
            lblPatientName.setText(patient.getFullName());
        }
        remplirChamps(mf);
    }

    @FXML
    public void handleVoiceInput(ActionEvent event) {
        Button micButton = (Button) event.getSource();
        String targetId = (String) micButton.getUserData();
        
        if (isRecording) {
            VoskService.getInstance().stopListening();
            micButton.setStyle("-fx-background-color: #EDF2F7; -fx-background-radius: 10;");
            isRecording = false;
            activeMicButton = null;
        } else {
            if (!VoskService.getInstance().isReady()) {
                showAlert("Modèle Vocal Manquant", "Le dossier 'model' contenant le modèle Vosk est introuvable à la racine.");
                return;
            }
            
            isRecording = true;
            activeMicButton = micButton;
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
        switch (id) {
            case "generalhistory": generalhistory.appendText(formattedText); break;
            case "antecedents": antecedents.appendText(formattedText); break;
            case "allergies": allergies.appendText(formattedText); break;
            case "followup": followup.appendText(formattedText); break;
        }
    }

    @FXML
    public void modifierDossier() {
        if (currentMedicalFile == null) {
            showAlert("Erreur", "Aucun dossier chargé");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            // Mettre à jour l'objet
            currentMedicalFile.setCreationdate(creationdate.getValue());
            currentMedicalFile.setLastvisit(lastvisit.getValue());
            currentMedicalFile.setPatientstatus(patientstatus.getValue());
            currentMedicalFile.setGeneralhistory(generalhistory.getText());
            currentMedicalFile.setAntecedents(antecedents.getText());
            currentMedicalFile.setAllergies(allergies.getText());
            currentMedicalFile.setFollowup(followup.getText());

            // ✅ Appel correct sur l'instance (non-statique)
            medicalFileService.update(currentMedicalFile);

            showAlert("Succès", "Dossier médical modifié avec succès !");
            
            // Redirection vers la liste du portail docteur
            tests.Main.showDoctorPatients();

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la redirection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        clearValidationErrors();
        boolean valid = true;
        Node firstInvalidField = null;

        if (selectedPatient == null) {
            markInvalid(searchPatient);
            firstInvalidField = searchPatient;
            valid = false;
        }
        if (creationdate.getValue() == null) {
            markInvalid(creationdate);
            if (firstInvalidField == null) firstInvalidField = creationdate;
            valid = false;
        }
        if (lastvisit.getValue() == null) {
            markInvalid(lastvisit);
            if (firstInvalidField == null) firstInvalidField = lastvisit;
            valid = false;
        }
        if (patientstatus.getValue() == null) {
            markInvalid(patientstatus);
            if (firstInvalidField == null) firstInvalidField = patientstatus;
            valid = false;
        }
        if (isBlank(generalhistory)) {
            markInvalid(generalhistory);
            if (firstInvalidField == null) firstInvalidField = generalhistory;
            valid = false;
        }
        if (isBlank(antecedents)) {
            markInvalid(antecedents);
            if (firstInvalidField == null) firstInvalidField = antecedents;
            valid = false;
        }
        if (isBlank(allergies)) {
            markInvalid(allergies);
            if (firstInvalidField == null) firstInvalidField = allergies;
            valid = false;
        }
        if (isBlank(followup)) {
            markInvalid(followup);
            if (firstInvalidField == null) firstInvalidField = followup;
            valid = false;
        }

        if (!valid) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            firstInvalidField.requestFocus();
            return false;
        }
        if (selectedPatient == null) {
            showAlert("Erreur", "Veuillez sélectionner un patient");
            return false;
        }
        if (creationdate.getValue() == null) {
            showAlert("Erreur", "Date de création obligatoire");
            return false;
        }
        if (patientstatus.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un statut");
            return false;
        }
        return true;
    }

    private void setupValidationListeners() {
        searchPatient.textProperty().addListener((obs, oldVal, newVal) -> {
            if (selectedPatient != null && !selectedPatient.getFullName().equals(newVal)) {
                selectedPatient = null;
            }
            clearInvalid(searchPatient);
        });
        creationdate.valueProperty().addListener((obs, oldVal, newVal) -> clearInvalid(creationdate));
        lastvisit.valueProperty().addListener((obs, oldVal, newVal) -> clearInvalid(lastvisit));
        patientstatus.valueProperty().addListener((obs, oldVal, newVal) -> clearInvalid(patientstatus));
        generalhistory.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(generalhistory));
        antecedents.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(antecedents));
        allergies.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(allergies));
        followup.textProperty().addListener((obs, oldVal, newVal) -> clearInvalid(followup));
    }

    private boolean isBlank(TextInputControl field) {
        return field.getText() == null || field.getText().trim().isEmpty();
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
        clearInvalid(searchPatient);
        clearInvalid(creationdate);
        clearInvalid(lastvisit);
        clearInvalid(patientstatus);
        clearInvalid(generalhistory);
        clearInvalid(antecedents);
        clearInvalid(allergies);
        clearInvalid(followup);
    }

    @FXML
    public void retour(ActionEvent event) {
        allerGererPatients(event);
    }

    @FXML
    public void allerGererPatients(ActionEvent event) {
        if (isRecording) {
            VoskService.getInstance().stopListening();
        }
        // Redirection vers la liste du portail docteur
        tests.Main.showDoctorPatients();
    }

    @FXML
    public void allerAjouterDossier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutermedicalFile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Ajouter dossier médical");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void clearFields() {
        clearValidationErrors();
        idfile.clear();
        patientId.clear();
        creationdate.setValue(null);
        lastvisit.setValue(null);
        patientstatus.setValue(null);
        generalhistory.clear();
        antecedents.clear();
        allergies.clear();
        followup.clear();
        currentMedicalFile = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void prechargerPatient(userDS patient) {
        this.selectedPatient = patient;
        if (lblPatientName != null) {
            lblPatientName.setText(patient.getFullName());
        }
        chargerDossierMedical(patient.getUserId());
    }
}
