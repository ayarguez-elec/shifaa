package tests;

import entities.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseInitializer;
import utils.SessionManager;

import java.net.URL;

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Auto-créer les tables/colonnes manquantes au démarrage
        DatabaseInitializer.init();

        // Session admin par défaut (login désactivé)
        SessionManager.getInstance().login(
            new User(1, "admin@shifaa.tn", "", "ADMIN", null));

        try {
            URL fxmlLocation = getClass().getResource("/GetAllevent.fxml");

            if (fxmlLocation == null) {
                System.err.println("ERREUR : Fichier FXML non trouvé !");
                System.err.println("Vérifiez que src/main/resources est marqué 'Resources Root'");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 700);

            primaryStage.setTitle("SHIFAA – Gestion Santé");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}