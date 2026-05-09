package Controler;

import entities.feedback;
import entities.feedback.TargetType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import services.feedbackServices;
import tests.Main;

import java.sql.SQLException;
import java.sql.Timestamp;

public class PatientEditFeedbackController {

    @FXML private HBox starsBox;
    @FXML private TextArea commentArea;
    @FXML private CheckBox anonymeCheck;
    @FXML private Label titleLabel;

    private feedbackServices feedbackService = new feedbackServices();
    private feedback currentFeedback;
    private int selectedRating;
    private String currentTargetType;
    private int currentTargetId;
    private String currentTargetName;

    @FXML
    public void initialize() {
    }

    public void initData(feedback fb, String targetType, int targetId, String targetName) {
        this.currentFeedback = fb;
        this.currentTargetType = targetType;
        this.currentTargetId = targetId;
        this.currentTargetName = targetName;
        this.selectedRating = fb.getRating();

        titleLabel.setText("Modifier le feedback pour " + targetName);
        commentArea.setText(fb.getComment());
        anonymeCheck.setSelected(fb.isAnonyme());

        createStarButtons();
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
    private void updateFeedback() {
        if (commentArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un commentaire");
            return;
        }

        try {
            currentFeedback.setRating(selectedRating);
            currentFeedback.setComment(commentArea.getText());
            currentFeedback.setAnonyme(anonymeCheck.isSelected());
            currentFeedback.setCreationDate(new Timestamp(System.currentTimeMillis()));

            feedbackService.update(currentFeedback);
            showAlert("Succès", "Feedback modifié avec succès !");

            goBack();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification du feedback");
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