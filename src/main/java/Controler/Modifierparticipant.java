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

public class Modifierparticipant {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private Label     lblMessage;

    private final Participantserv service = new Participantserv();
    private Participant participantAModifier;

    /**
     * Appelé depuis GetAllparticipant pour pré-remplir les champs.
     */
    public void setParticipant(Participant p) {
        this.participantAModifier = p;
        tfNom.setText(p.getNom());
        tfPrenom.setText(p.getPrenom());
        tfEmail.setText(p.getEmail());
        tfTelephone.setText(p.getTelephone());
    }

    @FXML
    private void handleModifier() {
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() ||
            tfEmail.getText().isEmpty() || tfTelephone.getText().isEmpty()) {
            showMessage("⚠️ Tous les champs sont obligatoires.", "red");
            return;
        }
        if (!tfEmail.getText().contains("@")) {
            showMessage("⚠️ Email invalide.", "red");
            return;
        }

        participantAModifier.setNom(tfNom.getText().trim());
        participantAModifier.setPrenom(tfPrenom.getText().trim());
        participantAModifier.setEmail(tfEmail.getText().trim());
        participantAModifier.setTelephone(tfTelephone.getText().trim());

        service.modifier(participantAModifier);
        showMessage("✅ Participant modifié avec succès !", "green");
    }

    @FXML
    private void handleRetour() {
        tests.Main.showAdminEventParticipants();
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }
}
