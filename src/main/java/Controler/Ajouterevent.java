package Controler;

import entities.event;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.eventServ;

import java.io.IOException;

public class Ajouterevent {

    @FXML private TextField        tfTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private TextArea         taDescription;
    @FXML private DatePicker       dpDate;
    @FXML private TextField        tfTime;
    @FXML private TextField        tfLieu;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField        tfCapacite;
    @FXML private Label            lblMessage;

    private final eventServ service = new eventServ();

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "Don de sang",
                "Vaccination",
                "Conférence médicale",
                "Consultation",
                "Sensibilisation",
                "Atelier santé",
                "Dépistage",
                "Premiers secours",
                "Conférence",
                "Séminaire",
                "Atelier"));
        cbStatus.setItems(FXCollections.observableArrayList(
                "Planifié", "En cours", "Terminé", "Annulé"));
        cbType.getSelectionModel().selectFirst();
        cbStatus.getSelectionModel().selectFirst();

        // Capacité par défaut = 50
        if (tfCapacite != null) tfCapacite.setText("50");
    }

    @FXML
    public void handleAjouter() {
        if (!valider()) return;

        int capacite = 0;
        try {
            capacite = Integer.parseInt(tfCapacite.getText().trim());
        } catch (NumberFormatException ex) {
            // default to 0 if invalid, but we should ideally validate
        }

        event e = new event(
                tfTitre.getText().trim(),
                cbType.getValue(),
                taDescription.getText().trim(),
                dpDate.getValue(),
                tfTime.getText().trim(),
                tfLieu.getText().trim(),
                cbStatus.getValue(),
                null,
                capacite
        );

        service.ajouter(e);
        afficherMessage("✅ Événement ajouté avec succès !", "#16A34A");
        naviguer("/GetAllevent.fxml");
    }

    @FXML
    public void handleVoirListe() {
        naviguer("/GetAllevent.fxml");
    }

    @FXML
    public void handleReset() {
        if (tfTitre != null) tfTitre.clear();
        if (cbType != null) cbType.getSelectionModel().selectFirst();
        if (taDescription != null) taDescription.clear();
        if (dpDate != null) dpDate.setValue(null);
        if (tfTime != null) tfTime.clear();
        if (tfLieu != null) tfLieu.clear();
        if (cbStatus != null) cbStatus.getSelectionModel().selectFirst();
        if (tfCapacite != null) tfCapacite.clear();
        if (lblMessage != null) lblMessage.setText("");
    }

    private boolean valider() {
        if (tfTitre.getText().trim().isEmpty()) {
            afficherMessage("❌ Le titre est obligatoire.", "#DC2626"); return false;
        }
        if (dpDate.getValue() == null) {
            afficherMessage("❌ La date est obligatoire.", "#DC2626"); return false;
        }
        if (tfLieu.getText().trim().isEmpty()) {
            afficherMessage("❌ Le lieu est obligatoire.", "#DC2626"); return false;
        }
        try {
            int cap = Integer.parseInt(tfCapacite.getText().trim());
            if (cap <= 0) {
                afficherMessage("❌ La capacité doit être positive.", "#DC2626"); return false;
            }
        } catch (NumberFormatException ex) {
            afficherMessage("❌ La capacité doit être un nombre.", "#DC2626"); return false;
        }
        return true;
    }

    private void afficherMessage(String msg, String color) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}