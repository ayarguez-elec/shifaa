package Controler;

import entities.Participant;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.Participantserv;

import java.io.IOException;

public class Suppparticipant {

    @FXML private TableView<Participant>            tableView;
    @FXML private TableColumn<Participant, Integer> colId;
    @FXML private TableColumn<Participant, String>  colNom;
    @FXML private TableColumn<Participant, String>  colPrenom;
    @FXML private TableColumn<Participant, String>  colEmail;
    @FXML private TableColumn<Participant, String>  colTelephone;
    @FXML private Label                             lblMessage;

    private final Participantserv service = new Participantserv();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        chargerDonnees();
    }

    private void chargerDonnees() {
        tableView.setItems(FXCollections.observableArrayList(service.getAll()));
    }

    @FXML
    private void handleSupprimer() {
        Participant selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Veuillez sélectionner un participant.", "orange");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selected.getNom() + " " + selected.getPrenom() + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                service.supprimer(selected.getId());
                chargerDonnees();
                showMessage("✅ Participant supprimé.", "green");
            }
        });
    }

    @FXML
    private void handleRetour() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/GetAllparticipant.fxml"));
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Participants");
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }
}
