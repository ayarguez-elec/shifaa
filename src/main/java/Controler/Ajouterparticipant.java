package Controler;

import entities.Participant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Participantserv;

import java.io.IOException;

public class Ajouterparticipant {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private Label     lblMessage;

    private final Participantserv service = new Participantserv();

    @FXML
    private void handleAjouter() {
        // ── Validation ─────────────────────────────────────────────────────────
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() ||
            tfEmail.getText().isEmpty() || tfTelephone.getText().isEmpty()) {
            showMessage("⚠️ Tous les champs sont obligatoires.", "red");
            return;
        }
        if (!tfEmail.getText().contains("@")) {
            showMessage("⚠️ Email invalide.", "red");
            return;
        }

        Participant p = new Participant(
            tfNom.getText().trim(),
            tfPrenom.getText().trim(),
            tfEmail.getText().trim(),
            tfTelephone.getText().trim()
        );

        service.ajouter(p);
        showMessage("✅ Participant ajouté avec succès !", "green");
        handleReset();
    }

    @FXML
    public void handleReset() {
        if (tfNom != null) tfNom.clear();
        if (tfPrenom != null) tfPrenom.clear();
        if (tfEmail != null) tfEmail.clear();
        if (tfTelephone != null) tfTelephone.clear();
        if (lblMessage != null) lblMessage.setText("");
    }

    @FXML
    private void handleVoirListe() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/GetAllparticipant.fxml"));
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Participants");
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }
}
