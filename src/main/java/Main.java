import api.ApiServer;
import entities.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseInitializer;
import utils.SessionManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ── Auto-créer tables/colonnes manquantes ────────────────────────────
        DatabaseInitializer.init();

        // ── Démarrer l'API REST en arrière-plan ─────────────────────────────
        ApiServer.launchAsync();

        // ── Session admin par défaut (login désactivé) ───────────────────────
        SessionManager.getInstance().login(
            new User(1, "admin@shifaa.tn", "", "ADMIN", null));

        // ── Point d'entrée JavaFX ────────────────────────────────────────────
        Parent root = FXMLLoader.load(getClass().getResource("/GetAllevent.fxml"));
        primaryStage.setTitle("🏥 SHIFAA — Gestion Événements Santé");
        primaryStage.setScene(new Scene(root, 1100, 700));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
