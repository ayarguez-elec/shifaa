package Controler;

import entities.feedback;
import entities.feedback.TargetType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.AvatarService;
import services.feedbackServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;

public class PatientAddFeedbackController {

    @FXML private HBox starsBox;
    @FXML private TextArea commentArea;
    @FXML private CheckBox anonymeCheck;
    @FXML private Label titleLabel;

    // Avatar section (visible uniquement en mode anonyme)
    @FXML private VBox avatarSection;
    @FXML private FlowPane avatarContainer;
    @FXML private Label avatarStatusLabel;

    private feedbackServices feedbackService = new feedbackServices();
    private AvatarService avatarService = new AvatarService();
    private int selectedRating = 5;
    private String currentTargetType;
    private int currentTargetId;
    private String currentTargetName;
    private String selectedAvatarBase64 = null;

    @FXML
    public void initialize() {
        createStarButtons();

        // Afficher/masquer la galerie d'avatars selon la case anonyme
        anonymeCheck.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            avatarSection.setVisible(isNowSelected);
            avatarSection.setManaged(isNowSelected);
            if (isNowSelected && avatarContainer.getChildren().isEmpty()) {
                loadAvatarGallery();
            }
            if (!isNowSelected) {
                // Réinitialiser la sélection si on décoche
                selectedAvatarBase64 = null;
                avatarStatusLabel.setText("Aucun avatar sélectionné");
                // Retirer le style de sélection de tous les avatars
                avatarContainer.getChildren().forEach(node ->
                    node.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); -fx-cursor: hand;")
                );
            }
        });
    }

    public void initData(String targetType, int targetId, String targetName) {
        this.currentTargetType = targetType;
        this.currentTargetId = targetId;
        this.currentTargetName = targetName;
        titleLabel.setText("Ajouter un feedback pour " + targetName);
    }

    private void loadAvatarGallery() {
        avatarContainer.getChildren().clear();
        List<String> avatars = avatarService.generateAvatarOptions("feedback", 6);
        for (String base64 : avatars) {
            if (base64 == null || base64.isEmpty()) continue;

            ImageView iv = new ImageView(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64))));
            iv.setFitWidth(80);
            iv.setFitHeight(80);
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); -fx-cursor: hand;");

            iv.setOnMouseClicked(e -> {
                // Retirer la sélection des autres
                avatarContainer.getChildren().forEach(node ->
                    node.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); -fx-cursor: hand;")
                );
                // Mettre en surbrillance l'avatar sélectionné
                iv.setStyle("-fx-effect: dropshadow(gaussian, #FF5C5C, 15, 0.5, 0, 0); -fx-cursor: hand;");
                selectedAvatarBase64 = base64;
                avatarStatusLabel.setText("✓ Avatar sélectionné");
                avatarStatusLabel.setStyle("-fx-text-fill: #48BB78; -fx-font-size: 11; -fx-font-weight: bold;");
            });

            avatarContainer.getChildren().add(iv);
        }
    }

    private void createStarButtons() {
        starsBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button starBtn = new Button(i <= selectedRating ? "★" : "☆");
            starBtn.setStyle("-fx-font-size: 35; -fx-background-color: transparent; -fx-text-fill: #FBBF24; -fx-cursor: hand;");
            starBtn.setOnAction(e -> {
                selectedRating = rating;
                updateStars();
            });
            starsBox.getChildren().add(starBtn);
        }
    }

    private void updateStars() {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Button starBtn = (Button) starsBox.getChildren().get(i);
            starBtn.setText(i < selectedRating ? "★" : "☆");
        }
    }

    @FXML
    private void saveFeedback() {
        if (commentArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un commentaire");
            return;
        }

        // Si anonyme coché, un avatar doit être sélectionné
        if (anonymeCheck.isSelected() && selectedAvatarBase64 == null) {
            showAlert("Avatar requis", "Veuillez choisir un avatar pour votre feedback anonyme.");
            return;
        }

        TargetType type = currentTargetType.equals("doctor") ? TargetType.doctor : TargetType.pharmacy;

        try {
            feedback newFeedback = new feedback();
            newFeedback.setUserId(Main.getCurrentUserId());
            newFeedback.setTargetType(type);
            newFeedback.setTargetId(currentTargetId);
            newFeedback.setRating(selectedRating);
            newFeedback.setComment(commentArea.getText());
            newFeedback.setCreationDate(new Timestamp(System.currentTimeMillis()));
            newFeedback.setAnonyme(anonymeCheck.isSelected());
            // Sauvegarder l'avatar choisi (null si non anonyme)
            newFeedback.setAvatarUrl(anonymeCheck.isSelected() ? selectedAvatarBase64 : null);

            feedbackService.add(newFeedback);
            showAlert("Succès", "Feedback ajouté avec succès !");
            goBack();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout du feedback");
        }
    }

    @FXML
    private void goBack() {
        if (currentTargetType.equals("doctor")) {
            Main.showPatientFeedbackForDoctor(currentTargetId, currentTargetName);
        } else {
            Main.showPatientPharmacies();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
