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

public class AjoutermedicalFile {

    @FXML private TextField searchPatient;
    @FXML private ListView<userDS> listPatients;
    @FXML private DatePicker creationdate;
    @FXML private DatePicker lastvisit;
    @FXML private ComboBox<String> patientstatus;
    @FXML private TextArea generalhistory;
    @FXML private TextArea antecedents;
    @FXML private TextArea allergies;
    @FXML private TextArea followup;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;

    private userservDS userService;
    private medicalFileServ medicalFileService;
    private ObservableList<userDS> patientsList;
    private userDS selectedPatient;
    
    private boolean isRecording = false;
    private Button activeMicButton = null;

    // Timer pour la recherche dynamique
    private javafx.animation.Timeline searchTimeline;

    public AjoutermedicalFile() {
        userService = new userservDS();
        medicalFileService = new medicalFileServ();
        patientsList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "stable", "en amélioration", "critique"
        );
        patientstatus.setItems(statuses);

        creationdate.setValue(LocalDate.now());

        // Personnaliser l'affichage du ListView
        setupListViewCellFactory();

        setupSearchListener();
        setupSelectionListener();
        setupValidationListeners();

        // Charger tous les patients au départ (optionnel)
        loadAllPatients();
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

    private void setupListViewCellFactory() {
        // Personnaliser comment chaque patient est affiché dans la liste
        listPatients.setCellFactory(listView -> new ListCell<userDS>() {
            @Override
            protected void updateItem(userDS patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(patient.getFullName());

                    // Vous pouvez ajouter une icône ou un style personnalisé
                    setStyle("-fx-padding: 8px; -fx-border-color: transparent transparent #e0e0e0 transparent;");
                }
            }
        });
    }

    private void setupSearchListener() {
        // Utiliser un délai pour éviter trop de requêtes pendant la frappe
        searchTimeline = new javafx.animation.Timeline();
        searchTimeline.setCycleCount(1);

        searchPatient.textProperty().addListener((obs, oldVal, newVal) -> {
            // Annuler la recherche précédente
            if (searchTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                searchTimeline.stop();
            }

            // Démarrer une nouvelle recherche après 300ms de pause
            searchTimeline.getKeyFrames().clear();
            searchTimeline.getKeyFrames().add(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(300),
                            event -> performSearch(newVal)
                    )
            );
            searchTimeline.play();
        });
    }

    private void performSearch(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<userDS> result = userService.searchByNameOrSurname(keyword);
            patientsList.setAll(result);
            listPatients.setItems(patientsList);
            listPatients.setVisible(!result.isEmpty());

            // Afficher un message si aucun résultat
            if (result.isEmpty()) {
                showAlert("Information", "Aucun patient trouvé pour: " + keyword);
                listPatients.setVisible(false);
            } else {
                System.out.println("✅ " + result.size() + " patient(s) trouvé(s)");
            }
        } else {
            // Si le champ est vide, afficher tous les patients ou cacher la liste
            if (searchPatient.getText() == null || searchPatient.getText().trim().isEmpty()) {
                loadAllPatients();
            } else {
                listPatients.getItems().clear();
                listPatients.setVisible(false);
            }
        }
    }

    private void loadAllPatients() {
        // Optionnel : charger tous les patients pour un aperçu initial
        try {
            List<userDS> allPatients = userService.getAll();
            if (!allPatients.isEmpty()) {
                patientsList.setAll(allPatients);
                listPatients.setItems(patientsList);
                listPatients.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement patients: " + e.getMessage());
        }
    }

    private void setupSelectionListener() {
        listPatients.setOnMouseClicked(event -> {
            selectedPatient = listPatients.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                searchPatient.setText(selectedPatient.getFullName());
                listPatients.setVisible(false);
                searchPatient.setStyle("-fx-border-color: green; -fx-border-width: 2px;");

                // Optionnel : afficher une confirmation
                System.out.println("✅ Patient sélectionné: " + selectedPatient.getFullName());
            }
        });

        // Quand le champ de recherche reçoit le focus, cacher le highlight vert
        searchPatient.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                searchPatient.setStyle(null);
            }
        });
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
                showAlert("Modèle Vocal Manquant", "Le dossier 'model' contenant le modèle Vosk (ex: français) est introuvable à la racine du projet.");
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
            case "searchPatient": searchPatient.appendText(formattedText); break;
            case "generalhistory": generalhistory.appendText(formattedText); break;
            case "antecedents": antecedents.appendText(formattedText); break;
            case "allergies": allergies.appendText(formattedText); break;
            case "followup": followup.appendText(formattedText); break;
        }
    }

    @FXML
    public void ajoutds() {
        if (!validateInputs()) {
            return;
        }

        try {
            medicalFile mf = createMedicalFile();
            medicalFileService.add(mf);

            showAlert("Succès", "Dossier médical ajouté pour " + selectedPatient.getFullName());
            
            // Redirection vers la liste
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GererPatients.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchPatient.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

        } catch (SQLException e) {
            showAlert("Erreur", "Base de données: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
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
            showAlert("Erreur", "Veuillez sélectionner un patient dans la liste");
            searchPatient.requestFocus();
            return false;
        }

        if (creationdate.getValue() == null) {
            showAlert("Erreur", "Date de création obligatoire");
            creationdate.requestFocus();
            return false;
        }

        if (patientstatus.getValue() == null) {
            showAlert("Erreur", "Sélectionnez un statut");
            patientstatus.requestFocus();
            return false;
        }

        if (generalhistory.getText() == null || generalhistory.getText().trim().isEmpty()) {
            showAlert("Erreur", "Historique général obligatoire");
            generalhistory.requestFocus();
            return false;
        }

        if (antecedents.getText() == null || antecedents.getText().trim().isEmpty()) {
            showAlert("Erreur", "Antécédents médicaux obligatoires");
            antecedents.requestFocus();
            return false;
        }

        if (allergies.getText() == null || allergies.getText().trim().isEmpty()) {
            showAlert("Erreur", "Allergies obligatoires");
            allergies.requestFocus();
            return false;
        }

        if (followup.getText() == null || followup.getText().trim().isEmpty()) {
            showAlert("Erreur", "Suivi médical obligatoire");
            followup.requestFocus();
            return false;
        }

        if (lastvisit.getValue() == null) {
            showAlert("Erreur", "Date de dernière visite obligatoire");
            lastvisit.requestFocus();
            return false;
        }

        return true;
    }

    private void setupValidationListeners() {
        searchPatient.textProperty().addListener((obs, oldVal, newVal) -> {
            if (selectedPatient != null && !selectedPatient.getFullName().equals(newVal)) {
                selectedPatient = null;
                searchPatient.setStyle(null);
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

    private medicalFile createMedicalFile() {
        medicalFile mf = new medicalFile();
        mf.setPatientId(selectedPatient.getUserId());
        mf.setCreationdate(creationdate.getValue());
        mf.setLastvisit(lastvisit.getValue());
        mf.setPatientstatus(patientstatus.getValue());
        mf.setGeneralhistory(generalhistory.getText());
        mf.setAntecedents(antecedents.getText());
        mf.setAllergies(allergies.getText());
        mf.setFollowup(followup.getText());
        return mf;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GererPatients.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Gestion des Patients");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de retourner: " + e.getMessage());
        }
    }

    @FXML
    public void allerAjouterDossier(ActionEvent event) {
        // Déjà sur cette interface, on peut rafraîchir ou ne rien faire
        clearFields();
    }

    private void clearFields() {
        searchPatient.clear();
        searchPatient.setStyle(null);
        clearValidationErrors();
        listPatients.getItems().clear();
        creationdate.setValue(LocalDate.now());
        lastvisit.setValue(null);
        patientstatus.setValue(null);
        generalhistory.clear();
        antecedents.clear();
        allergies.clear();
        followup.clear();
        selectedPatient = null;

        // Recharger tous les patients
        loadAllPatients();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Erreur") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
