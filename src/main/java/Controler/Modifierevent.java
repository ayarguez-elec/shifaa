package Controler;

import entities.event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.eventServ;
import utils.RoleGuard;

import java.io.IOException;

public class Modifierevent {

    @FXML private TextField        tfTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private TextArea         taDescription;
    @FXML private DatePicker       dpDate;
    @FXML private TextField        tfTime;
    @FXML private TextField        tfLieu;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField        tfCapacite;
    @FXML private TextField        tfOrganisateur;
    @FXML private Label            lblMessage;

    private final eventServ service = new eventServ();
    private event eventAModifier;

    @FXML
    public void initialize() {
        // [SECURITY] Layer 2 — controller guard: redirect non-admin after scene is ready
        Platform.runLater(() -> {
            if (RoleGuard.denyIfNotAdmin()) {
                tests.Main.showPatientEvents();
            }
        });

        cbType.getItems().addAll("Conférence", "Formation", "Séminaire", "Atelier");
        cbStatus.getItems().addAll("Planifié", "En cours", "Terminé", "Annulé");
    }

    public void setEvent(event e) {
        this.eventAModifier = e;
        tfTitre.setText(e.getTitre());
        cbType.setValue(e.getType());
        taDescription.setText(e.getDescription());
        dpDate.setValue(e.getDate());
        tfTime.setText(e.getTime());
        tfLieu.setText(e.getLieu());
        cbStatus.setValue(e.getStatus());
        if (tfCapacite != null) tfCapacite.setText(String.valueOf(e.getCapaciteMax()));
        if (tfOrganisateur != null) tfOrganisateur.setText("—");
    }

    @FXML
    private void handleModifier() {
        // [SECURITY] Layer 2 — action-level guard
        if (RoleGuard.denyIfNotAdmin()) return;
        if (tfTitre.getText().isEmpty() || cbType.getValue() == null
                || dpDate.getValue() == null || tfLieu.getText().isEmpty()) {
            showMessage("⚠️ Champs obligatoires manquants.", "#DC2626");
            return;
        }

        eventAModifier.setTitre(tfTitre.getText().trim());
        eventAModifier.setType(cbType.getValue());
        eventAModifier.setDescription(taDescription.getText().trim());
        eventAModifier.setDate(dpDate.getValue());
        eventAModifier.setTime(tfTime.getText().trim());
        eventAModifier.setLieu(tfLieu.getText().trim());
        eventAModifier.setStatus(cbStatus.getValue());
        
        try {
            int cap = Integer.parseInt(tfCapacite.getText().trim());
            eventAModifier.setCapaciteMax(cap);
        } catch (NumberFormatException ex) {
            // keep old value or set to 0
        }

        service.modifier(eventAModifier);
        showMessage("✅ Événement modifié avec succès !", "#16A34A");
        handleRetour();
    }

    @FXML
    public void handleRetour() {
        tests.Main.showAdminEvents();
    }

    private void showMessage(String msg, String color) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
    }
}
