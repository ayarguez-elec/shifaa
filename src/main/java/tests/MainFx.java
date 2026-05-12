package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.SessionManager;

import java.net.URL;

public class MainFx extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        primaryStage.setTitle("Shefee - Gestion Santé");
        primaryStage.setWidth(1536);
        primaryStage.setHeight(850);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);

        // Pour tester l'espace pharmacie
        changeScene("/userdashboard.fxml");

        // Après test, tu peux remettre :
        // changeScene("/userdashboard.fxml");

        primaryStage.show();
    }

    public static void changeScene(String fxmlPath) {
        try {
            URL fxmlUrl = MainFx.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                showError("FXML introuvable", "Le fichier est introuvable : " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);

            Scene scene = new Scene(root);

            URL cssUrl = MainFx.class.getResource("/style.css");

            if (cssUrl != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("CSS introuvable : /style.css");
            }

            primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showError(
                    "Erreur JavaFX",
                    "Erreur lors du chargement de la page : " + fxmlPath + "\n\n" + e.getMessage()
            );
        }
    }

    public static void goToUserSpace() {
        changeScene("/userdashboard.fxml");
    }

    public static void goToAdminSpace() {
        changeScene("/admindashboard.fxml");
    }

    public static void goToPharmacySpace(int pharmacyId) {
        SessionManager.setConnectedPharmacyId(pharmacyId);
        changeScene("/pharmacystock.fxml");
    }

    public static void redirectAfterLogin(String role, int pharmacyId) {
        if (role == null) {
            changeScene("/userdashboard.fxml");
            return;
        }

        String normalizedRole = role.trim().toLowerCase();

        switch (normalizedRole) {
            case "admin":
                changeScene("/admindashboard.fxml");
                break;

            case "pharmacie":
            case "pharmacy":
                SessionManager.setConnectedPharmacyId(pharmacyId);
                changeScene("/pharmacystock.fxml");
                break;

            case "user":
            case "utilisateur":
            default:
                changeScene("/userdashboard.fxml");
                break;
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}