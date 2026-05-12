package tests;

import entities.Reclamation;
import entities.Reponse;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import services.EscalationService;
import java.net.URL;

public class MainFx extends Application {

    private static Stage primaryStage;

    public static Reclamation selectedReclamation = null;
    public static Reponse selectedReponse = null;
    public static int selectedReclamationId = 0;

    // Notification: userId qui doit voir la notif
    public static int notificationUserId = 0;
    public static String notificationMessage = "";

    @Override
    public void start(Stage stage) {
        // Run auto-escalation check at startup
// Any urgent reclamation with no response after 3 days gets escalated
        javafx.application.Platform.runLater(new Runnable() {
            public void run() {
                try {
                    EscalationService esc = new EscalationService();
                    int escalated = esc.resolveEscalation(3); // SLA = 3 days
                    if (escalated > 0) {
                        System.out.println("[SHIFAA] Auto-escalated " + escalated + " reclamation(s).");
                    }
                } catch (Exception e) {
                    System.err.println("[SHIFAA] Auto-escalation error: " + e.getMessage());
                }
            }
        });
        primaryStage = stage;
        primaryStage.setTitle("Shifaa");
        primaryStage.setResizable(true);

        changeScene("/admindashboard.fxml");

        // On maximise APRES avoir chargé la scène
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    public static void changeScene(String fxmlPath) {
        try {
            URL fxmlUrl = MainFx.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                showError("FXML introuvable", "Le fichier est introuvable : " + fxmlPath);
                return;
            }

            boolean wasMaximized = primaryStage.isMaximized();

            // Désactiver maximize temporairement pour éviter le conflit de taille
            if (wasMaximized) {
                primaryStage.setMaximized(false);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Créer la scène sans dimensions fixes — le Stage gère la taille
            Scene scene = new Scene(root);

            URL cssUrl = MainFx.class.getResource("/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);

            // Remettre maximize si c'était le cas
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur JavaFX", e.getMessage());
        }
    }

    public static void clearSelection() {
        selectedReclamation = null;
        selectedReponse = null;
        selectedReclamationId = 0;
    }

    public static void setNotification(int userId, String message) {
        notificationUserId = userId;
        notificationMessage = message;
    }
    public static void clearNotification() {
        notificationUserId = 0;
        notificationMessage = "";
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}