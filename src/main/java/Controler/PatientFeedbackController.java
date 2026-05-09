package Controler;

import entities.feedback;
import entities.feedback.TargetType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.SVGPath;
import services.feedbackServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class PatientFeedbackController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> ratingCombo;
    @FXML private Label statusLabel;
    @FXML private FlowPane cardsContainer;
    @FXML private Button addButton;

    private feedbackServices feedbackService = new feedbackServices();
    private ObservableList<feedback> feedbackList = FXCollections.observableArrayList();
    private String currentTargetType;
    private int currentTargetId;
    private String currentTargetName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortCombo.setItems(FXCollections.observableArrayList(
            "Plus récents", "Plus anciens"
        ));
        sortCombo.setValue("Plus récents");

        if (ratingCombo != null) {
            ratingCombo.setItems(FXCollections.observableArrayList(
                "Toutes les notes", "⭐⭐⭐⭐⭐ (5)", "⭐⭐⭐⭐ (4)", "⭐⭐⭐ (3)", "⭐⭐ (2)", "⭐ (1)"
            ));
            ratingCombo.setValue("Toutes les notes");
            ratingCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            applyFilters();
        });

        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    public void loadFeedbacks(String targetType, int targetId, String targetName) {
        this.currentTargetType = targetType;
        this.currentTargetId = targetId;
        this.currentTargetName = targetName;

        titleLabel.setText("⭐ Feedbacks pour " + targetName);
        
        // Cacher le bouton ajouter si c'est pour soi-même
        if (addButton != null) {
            addButton.setVisible(Main.getCurrentUserId() != currentTargetId);
            addButton.setManaged(Main.getCurrentUserId() != currentTargetId);
        }
        
        refreshFeedbacks();
    }

    private void refreshFeedbacks() {
        try {
            List<feedback> allFeedbacks = feedbackService.getAll();
            feedbackList.clear();

            TargetType type = currentTargetType.equals("doctor") ? TargetType.doctor : TargetType.pharmacy;

            for (feedback fb : allFeedbacks) {
                if (fb.getTargetType() == type && fb.getTargetId() == currentTargetId) {
                    feedbackList.add(fb);
                }
            }

            displayCards(feedbackList);
            statusLabel.setText("Total: " + feedbackList.size() + " feedbacks");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de chargement: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String sortOrder = sortCombo.getValue();
        String ratingFilter = (ratingCombo != null) ? ratingCombo.getValue() : "Toutes les notes";

        // Extraire la note numérique sélectionnée (ex: "⭐⭐⭐⭐⭐ (5)" -> 5)
        int selectedRating = -1; // -1 = toutes
        if (ratingFilter != null && !ratingFilter.equals("Toutes les notes")) {
            try {
                // Le chiffre est entre parenthèses à la fin
                String numPart = ratingFilter.replaceAll(".*\\((\\d)\\)", "$1");
                selectedRating = Integer.parseInt(numPart);
            } catch (Exception ignored) {}
        }
        final int filterRating = selectedRating;

        ObservableList<feedback> filtered = FXCollections.observableArrayList();
        for (feedback fb : feedbackList) {
            boolean matchesSearch = fb.getComment().toLowerCase().contains(searchText);
            boolean matchesRating = (filterRating == -1) || (fb.getRating() == filterRating);
            if (matchesSearch && matchesRating) {
                filtered.add(fb);
            }
        }

        if ("Plus récents".equals(sortOrder)) {
            filtered.sort((f1, f2) -> f2.getCreationDate().compareTo(f1.getCreationDate()));
        } else {
            filtered.sort((f1, f2) -> f1.getCreationDate().compareTo(f2.getCreationDate()));
        }

        displayCards(filtered);
        statusLabel.setText("Affichage: " + filtered.size() + " / " + feedbackList.size());
    }

    private void displayCards(ObservableList<feedback> feedbacks) {
        cardsContainer.getChildren().clear();

        if (feedbacks.isEmpty()) {
            Label emptyLabel = new Label("Aucun feedback pour cet établissement");
            emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 16;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (feedback fb : feedbacks) {
            StackPane card = createFeedbackCard(fb);
            cardsContainer.getChildren().add(card);
        }
    }

    private StackPane createFeedbackCard(feedback fb) {
        StackPane cardRoot = new StackPane();
        cardRoot.setPrefWidth(280);
        cardRoot.setMaxWidth(280);

        // Fond principal blanc arrondi
        VBox cardBody = new VBox(10);
        cardBody.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-border-color: #E2E8F0; -fx-border-radius: 20;");
        cardBody.setPadding(new Insets(60, 20, 20, 20));
        StackPane.setMargin(cardBody, new Insets(20, 0, 0, 0));

        // Ruban bleu (Header)
        VBox ribbon = new VBox(2);
        ribbon.setStyle("-fx-background-color: #3B5998; -fx-background-radius: 15 0 20 0; -fx-padding: 8 15;");
        ribbon.setMaxWidth(200);
        ribbon.setMaxHeight(45);
        
        String authorName = "Utilisateur Anonyme";
        if (!fb.isAnonyme()) {
            authorName = getAuthorName(fb.getUserId());
        }
        Label nameLabel = new Label(authorName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        Label dateLabel = new Label(formatDate(fb.getCreationDate()));
        dateLabel.setStyle("-fx-text-fill: #CBD5E0; -fx-font-size: 10;");
        ribbon.getChildren().addAll(nameLabel, dateLabel);

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitHeight(80);
        avatar.setFitWidth(80);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(40, 40, 40);
        avatar.setClip(clip);
        loadAuthorPhoto(avatar, fb.getUserId());
        
        StackPane avatarContainer = new StackPane(avatar);
        avatarContainer.setStyle("-fx-background-color: white; -fx-background-radius: 45; -fx-padding: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        avatarContainer.setMaxSize(86, 86);

        // Etoiles
        Label starsLabel = new Label(getStars(fb.getRating()));
        starsLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #F6AD55;");

        // Commentaire
        HBox commentBox = new HBox(15);
        Region accentLine = new Region();
        accentLine.setPrefWidth(3);
        accentLine.setStyle("-fx-background-color: #3B5998; -fx-background-radius: 2;");
        
        Label commentLabel = new Label(fb.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #4A5568; -fx-font-style: italic;");
        commentLabel.setPrefHeight(80);
        commentLabel.setAlignment(Pos.TOP_LEFT);
        commentBox.getChildren().addAll(accentLine, commentLabel);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (fb.getUserId() == Main.getCurrentUserId()) {
            Button editBtn = new Button();
            SVGPath editIcon = new SVGPath();
            editIcon.setContent("M 3 17.25 V 21 h 3.75 L 17.81 9.94 l -3.75 -3.75 L 3 17.25 z M 20.71 7.04 c 0.39 -0.39 0.39 -1.02 0 -1.41 l -2.34 -2.34 c -0.39 -0.39 -1.02 -0.39 -1.41 0 l -1.83 1.83 l 3.75 3.75 l 1.83 -1.83 z");
            editIcon.setFill(javafx.scene.paint.Color.web("#2B6CB0"));
            editBtn.setGraphic(editIcon);
            editBtn.setStyle("-fx-background-color: #BEE3F8; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;");
            editBtn.setOnAction(e -> Main.showEditFeedback(fb, currentTargetType, currentTargetId, currentTargetName));

            Button deleteBtn = new Button();
            SVGPath trashIcon = new SVGPath();
            trashIcon.setContent("M 6 19 c 0 1.1 0.9 2 2 2 h 8 c 1.1 0 2 -0.9 2 -2 V 7 H 6 v 12 z M 19 4 h -3.5 l -1 -1 h -5 l -1 1 H 5 v 2 h 14 V 4 z");
            trashIcon.setFill(javafx.scene.paint.Color.web("#C53030"));
            deleteBtn.setGraphic(trashIcon);
            deleteBtn.setStyle("-fx-background-color: #FED7D7; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deleteFeedback(fb));
            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        cardBody.getChildren().addAll(starsLabel, commentBox, actions);

        cardRoot.getChildren().addAll(cardBody, ribbon, avatarContainer);
        StackPane.setAlignment(ribbon, Pos.TOP_LEFT);
        StackPane.setAlignment(avatarContainer, Pos.TOP_RIGHT);
        StackPane.setMargin(avatarContainer, new Insets(0, 20, 0, 0));

        return cardRoot;
    }

    private void loadAuthorPhoto(ImageView imageView, int userId) {
        try {
            services.userServices userService = new services.userServices();
            entities.user author = null;
            for (entities.user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    author = u;
                    break;
                }
            }
            if (author != null && author.getUserPhoto() != null && !author.getUserPhoto().isEmpty()) {
                byte[] bytes = java.util.Base64.getDecoder().decode(author.getUserPhoto().trim().replaceAll("\\s+", ""));
                imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
            } else {
                imageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        }
    }

    private String getAuthorName(int userId) {
        try {
            services.userServices userService = new services.userServices();
            for (entities.user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    return u.getFirstName() + " " + u.getLastName();
                }
            }
        } catch (Exception e) {}
        return "Patient";
    }

    private void deleteFeedback(feedback fb) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le feedback");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce feedback ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                feedbackService.delete(fb);
                showAlert("Succès", "Feedback supprimé avec succès !");
                refreshFeedbacks();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        if (ratingCombo != null) ratingCombo.setValue("Toutes les notes");
        sortCombo.setValue("Plus récents");
        applyFilters();
    }

    @FXML
    private void showAddFeedbackDialog() {
        Main.showAddFeedback(currentTargetType, currentTargetId, currentTargetName);
    }

    @FXML
    private void goBack() {
        String role = Main.getCurrentUserRole();
        if (role.equals("doctor")) {
            Main.showDoctorPatients();
        } else {
            if (currentTargetType.equals("doctor")) {
                Main.showPatientDoctors();
            } else {
                Main.showPatientPharmacies();
            }
        }
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) stars.append("★");
        for (int i = rating; i < 5; i++) stars.append("☆");
        return stars.toString();
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "Date inconnue";
        return timestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}