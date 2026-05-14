package Controler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import tests.Main;

public class LandingController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox servicesSection;
    @FXML private HBox contactSection;

    @FXML private TextField contactName;
    @FXML private TextField contactEmail;
    @FXML private TextField contactSubject;
    @FXML private TextArea contactMessage;
    @FXML private Label contactStatusLabel;

    @FXML
    private void scrollToServices() {
        smoothScrollTo(servicesSection.getBoundsInParent().getMinY());
    }

    @FXML
    private void scrollToContact() {
        smoothScrollTo(contactSection.getBoundsInParent().getMinY());
    }

    private void smoothScrollTo(double targetY) {
        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double maxScroll = contentHeight - viewportHeight;
        if (maxScroll <= 0) return;
        
        double scrollValue = targetY / maxScroll;
        if (scrollValue > 1.0) scrollValue = 1.0;
        if (scrollValue < 0.0) scrollValue = 0.0;
        
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), scrollValue, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.millis(600), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    @FXML
    private void goToLogin() {
        Main.showLoginPage();
    }

    @FXML
    private void goToSignUp() {
        Main.showSignUpPage();
    }

    @FXML
    private void handleContactSubmit() {
        if (contactName.getText().isEmpty() || contactEmail.getText().isEmpty() || contactMessage.getText().isEmpty()) {
            contactStatusLabel.setText("Veuillez remplir les champs obligatoires.");
            contactStatusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: bold;");
            contactStatusLabel.setVisible(true);
            return;
        }

        contactStatusLabel.setText("Envoi en cours...");
        contactStatusLabel.setStyle("-fx-text-fill: #2D3748; -fx-font-weight: bold;");
        contactStatusLabel.setVisible(true);

        // Envoyer le mail dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            boolean success = services.EmailService.sendContactMessage(
                contactEmail.getText(), 
                contactName.getText(), 
                contactSubject.getText(), 
                contactMessage.getText()
            );

            javafx.application.Platform.runLater(() -> {
                if (success) {
                    contactStatusLabel.setText("✅ Votre message a été envoyé avec succès !");
                    contactStatusLabel.setStyle("-fx-text-fill: #38A169; -fx-font-weight: bold;");
                    
                    // Vider les champs
                    contactName.clear();
                    contactEmail.clear();
                    contactSubject.clear();
                    contactMessage.clear();
                } else {
                    contactStatusLabel.setText("❌ Erreur lors de l'envoi. Veuillez réessayer.");
                    contactStatusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: bold;");
                }

                // Masquer le message après 3 secondes
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> contactStatusLabel.setVisible(false));
                delay.play();
            });
        }).start();
    }
}
