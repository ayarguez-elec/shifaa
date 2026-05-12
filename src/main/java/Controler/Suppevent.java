package Controler;

import entities.event;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.eventServ;

import java.io.IOException;

public class Suppevent {

    @FXML private TableView<event>            tableView;
    @FXML private TableColumn<event, Integer> colId;
    @FXML private TableColumn<event, String>  colTitre;
    @FXML private TableColumn<event, String>  colType;
    @FXML private TableColumn<event, String>  colDate;
    @FXML private TableColumn<event, String>  colLieu;
    @FXML private TableColumn<event, String>  colStatus;
    @FXML private Label                       lblMessage;

    private final eventServ service = new eventServ();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        chargerDonnees();
    }

    private void chargerDonnees() {
        tableView.setItems(FXCollections.observableArrayList(service.getAll()));
    }

    @FXML
    private void handleSupprimer() {
        event selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Veuillez sélectionner un événement.", "orange"); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Confirmer la suppression de « " + selected.getTitre() + " » ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                service.supprimer(selected.getId());
                chargerDonnees();
                showMessage("✅ Événement supprimé.", "green");
            }
        });
    }

    @FXML
    private void handleRetour() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/GetAllevent.fxml"));
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showMessage(String msg, String color) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    public void setEvent(event selected) {
        if (selected != null) tableView.getSelectionModel().select(selected);
    }
}
