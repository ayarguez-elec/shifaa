package controler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

    public class home extends Application {

        public static void main(String[] args) {
            launch(args);
        }

        @Override
        public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardPatient.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Throwable e) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter("error_log.txt")) {
                e.printStackTrace(pw);
            } catch (Exception ex) {}
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        }
    }

