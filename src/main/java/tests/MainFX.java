package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/PatientDashboard.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root);

            stage.setTitle("Gestion des Patients");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            System.out.println("❌ Erreur chargement FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}