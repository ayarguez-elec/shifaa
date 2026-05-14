package Controler;

import entities.feedback;
import entities.user;
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
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminFeedbackController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> targetTypeFilter;
    @FXML private ComboBox<String> ratingFilter;
    @FXML private Label statusLabel;
    @FXML private FlowPane cardsContainer;
    @FXML private HBox paginationBox;

    private feedbackServices feedbackService = new feedbackServices();
    private userServices userService = new userServices();
    private ObservableList<feedback> allFeedbacks = FXCollections.observableArrayList();
    private List<user> allUsers;

    private int itemsPerPage = 6;
    private int currentPage = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        targetTypeFilter.setItems(FXCollections.observableArrayList("Toutes les cibles", "Médecin", "Pharmacie"));
        targetTypeFilter.setValue("Toutes les cibles");

        ratingFilter.setItems(FXCollections.observableArrayList("Toutes les notes", "5 ⭐", "4 ⭐", "3 ⭐", "2 ⭐", "1 ⭐"));
        ratingFilter.setValue("Toutes les notes");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterFeedbacks(); });
        targetTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterFeedbacks(); });
        ratingFilter.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterFeedbacks(); });

        loadData();
    }

    private void loadData() {
        try {
            allUsers = userService.getAll();
            allFeedbacks.setAll(feedbackService.getAll());
            filterFeedbacks();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de chargement");
        }
    }

    @FXML
    private void filterFeedbacks() {
        String query = searchField.getText().toLowerCase();
        String targetType = targetTypeFilter.getValue();
        String rating = ratingFilter.getValue();

        List<feedback> filtered = allFeedbacks.stream()
            .filter(fb -> {
                String comment = fb.getComment().toLowerCase();
                String author = getAuthorName(fb.getUserId()).toLowerCase();
                return comment.contains(query) || author.contains(query);
            })
            .filter(fb -> {
                if (targetType.equals("Toutes les cibles")) return true;
                if (targetType.equals("Médecin")) return fb.getTargetType() == feedback.TargetType.doctor;
                if (targetType.equals("Pharmacie")) return fb.getTargetType() == feedback.TargetType.pharmacy;
                return true;
            })
            .filter(fb -> {
                if (rating.equals("Toutes les notes")) return true;
                int r = Integer.parseInt(rating.substring(0, 1));
                return fb.getRating() == r;
            })
            .collect(Collectors.toList());

        displayFeedbacks(filtered);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        targetTypeFilter.setValue("Toutes les cibles");
        ratingFilter.setValue("Toutes les notes");
        currentPage = 1;
        filterFeedbacks();
    }

    private void displayFeedbacks(List<feedback> list) {
        cardsContainer.getChildren().clear();
        int total = list.size();
        statusLabel.setText("Total: " + total + " avis");

        if (list.isEmpty()) {
            cardsContainer.getChildren().add(new Label("Aucun avis trouvé"));
            paginationBox.getChildren().clear();
            return;
        }

        int totalPages = (int) Math.ceil((double) total / itemsPerPage);
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);

        for (int i = start; i < end; i++) {
            cardsContainer.getChildren().add(createFeedbackCard(list.get(i)));
        }

        updatePagination(totalPages);
    }

    private StackPane createFeedbackCard(feedback fb) {
        StackPane cardRoot = new StackPane();
        cardRoot.setPrefWidth(280);
        cardRoot.setMaxWidth(280);

        // Fond principal blanc arrondi
        VBox cardBody = new VBox(10);
        cardBody.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-border-color: #E2E8F0; -fx-border-radius: 20;");
        cardBody.setPadding(new Insets(60, 20, 20, 20)); // Padding haut pour laisser place au ruban
        StackPane.setMargin(cardBody, new Insets(20, 0, 0, 0));

        // Ruban bleu (Header)
        VBox ribbon = new VBox(2);
        ribbon.setStyle("-fx-background-color: #3B5998; -fx-background-radius: 15 0 20 0; -fx-padding: 8 15;");
        ribbon.setMaxWidth(200);
        ribbon.setMaxHeight(45);
        Label nameLabel = new Label(fb.isAnonyme() ? "Utilisateur Anonyme" : getAuthorName(fb.getUserId()));
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        String targetName = getTargetName(fb.getTargetType(), fb.getTargetId());
        Label designationLabel = new Label("Pour: " + targetName);
        designationLabel.setStyle("-fx-text-fill: #CBD5E0; -fx-font-size: 10;");
        ribbon.getChildren().addAll(nameLabel, designationLabel);

        // Avatar (Pop-out)
        ImageView avatar = new ImageView();
        avatar.setFitHeight(80);
        avatar.setFitWidth(80);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(40, 40, 40);
        avatar.setClip(clip);
        loadAuthorPhoto(avatar, fb);
        
        StackPane avatarContainer = new StackPane(avatar);
        avatarContainer.setStyle("-fx-background-color: white; -fx-background-radius: 45; -fx-padding: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        avatarContainer.setMaxSize(86, 86);

        // Etoiles
        Label starsLabel = new Label(getStars(fb.getRating()));
        starsLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #F6AD55;");

        // Commentaire avec ligne d'accent
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

        // Footer avec date et bouton supprimer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label dateLabel = new Label(fb.getCreationDate().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 10;");
        
        Button deleteBtn = new Button();
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M 6 19 c 0 1.1 0.9 2 2 2 h 8 c 1.1 0 2 -0.9 2 -2 V 7 H 6 v 12 z M 19 4 h -3.5 l -1 -1 h -5 l -1 1 H 5 v 2 h 14 V 4 z");
        trashIcon.setFill(javafx.scene.paint.Color.web("#C53030"));
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setStyle("-fx-background-color: #FED7D7; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteFeedback(fb));
        footer.getChildren().addAll(dateLabel, deleteBtn);

        cardBody.getChildren().addAll(starsLabel, commentBox, footer);

        // Positionnement des éléments dans le StackPane
        cardRoot.getChildren().addAll(cardBody, ribbon, avatarContainer);
        StackPane.setAlignment(ribbon, Pos.TOP_LEFT);
        StackPane.setAlignment(avatarContainer, Pos.TOP_RIGHT);
        StackPane.setMargin(avatarContainer, new Insets(0, 20, 0, 0));

        return cardRoot;
    }

    private void loadAuthorPhoto(ImageView imageView, feedback fb) {
        // Si le feedback est anonyme et qu'un avatar a été choisi, l'afficher
        if (fb.isAnonyme() && fb.getAvatarUrl() != null && !fb.getAvatarUrl().isEmpty()) {
            try {
                byte[] bytes = java.util.Base64.getDecoder().decode(fb.getAvatarUrl().trim().replaceAll("\\s+", ""));
                imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                return;
            } catch (Exception e) {
                // fallback ci-dessous
            }
        }

        // Feedback non anonyme : afficher la photo de profil de l'auteur
        if (!fb.isAnonyme()) {
            user author = allUsers.stream().filter(u -> u.getUserId() == fb.getUserId()).findFirst().orElse(null);
            if (author != null && author.getUserPhoto() != null && !author.getUserPhoto().isEmpty()) {
                try {
                    byte[] bytes = java.util.Base64.getDecoder().decode(author.getUserPhoto().trim().replaceAll("\\s+", ""));
                    imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                    return;
                } catch (Exception e) {
                    // fallback ci-dessous
                }
            }
        }

        // Fallback : avatar par défaut
        setDefaultAvatar(imageView);
    }

    private void setDefaultAvatar(ImageView imageView) {
        imageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
    }

    private void deleteFeedback(feedback fb) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cet avis définitivement ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                feedbackService.delete(fb);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getAuthorName(int userId) {
        return allUsers.stream()
            .filter(u -> u.getUserId() == userId)
            .map(u -> u.getFirstName() + " " + u.getLastName())
            .findFirst()
            .orElse("Utilisateur inconnu");
    }

    private String getTargetName(feedback.TargetType type, int targetId) {
        return allUsers.stream()
            .filter(u -> u.getUserId() == targetId)
            .map(u -> (type == feedback.TargetType.doctor ? "Dr. " : "") + u.getFirstName() + " " + u.getLastName())
            .findFirst()
            .orElse("Cible inconnue");
    }

    private String getStars(int r) {
        return "★".repeat(r) + "☆".repeat(5 - r);
    }

    private void updatePagination(int totalPages) {
        paginationBox.getChildren().clear();
        if (totalPages <= 1) return;

        for (int i = 1; i <= totalPages; i++) {
            Button btn = new Button(String.valueOf(i));
            btn.getStyleClass().add("pagination-button");
            if (i == currentPage) btn.getStyleClass().add("pagination-button-active");
            int p = i;
            btn.setOnAction(e -> { currentPage = p; filterFeedbacks(); });
            paginationBox.getChildren().add(btn);
        }
    }
}
