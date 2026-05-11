package Controler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import tests.Main;

public class AdminMainController {

    @FXML
    private AnchorPane contentContainer;

    @FXML
    private ImageView logoView;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label userNameLabel;

    @FXML
    private Button pendingBadgeBtn;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) {
                logoView.setImage(logo);
            }
        } catch (Exception e) {
            System.out.println("Logo non trouvé");
        }

        loadUserInfo();
        updatePendingBadge();
        Main.showAdminDashboard();
    }

    public void updatePendingBadge() {
        try {
            services.userServices userService = new services.userServices();
            long pendingCount = userService.getAll().stream()
                    .filter(u -> u.getAccountStatus() == entities.user.AccountStatus.pending)
                    .count();
            if (pendingBadgeBtn != null) {
                if (pendingCount > 0) {
                    pendingBadgeBtn.setText("⏳ En attente (" + pendingCount + ")");
                    pendingBadgeBtn.setStyle("-fx-text-fill: white; -fx-background-color: #ED8936; -fx-font-weight: bold;");
                } else {
                    pendingBadgeBtn.setText("⏳ En attente (0)");
                    pendingBadgeBtn.setStyle("-fx-text-fill: #718096; -fx-font-weight: normal;");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadUserInfo() {
        try {
            services.userServices userService = new services.userServices();
            int userId = Main.getCurrentUserId();
            for (entities.user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText(u.getFirstName() + " " + u.getLastName());
                    // Charger la photo (Priorité Avatar, sinon Photo Réelle)
                    String photoBase64 = (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) ? u.getAvatarUrl() : u.getUserPhoto();

                    // Protection legacy
                    if (photoBase64 != null && photoBase64.startsWith("generated_")) {
                        photoBase64 = u.getUserPhoto();
                    }

                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        try {
                            String cleanBase64 = photoBase64.trim().replaceAll("[^A-Za-z0-9+/=]", "");
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
                            Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                            profileImageView.setImage(image);
                        } catch (Exception ex) {
                            setDefaultImage();
                        }
                    } else {
                        setDefaultImage();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/default-avatar.png"));
            if (defaultImage != null) {
                profileImageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            // Image par défaut
        }
    }

    public void updateContent(Parent content) {
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        }
    }

    @FXML
    private void goToUtilisateurs() {
        Main.showAdminUtilisateurs();
    }

    @FXML
    private void goToDashboard() {
        Main.showAdminDashboard();
    }

    @FXML
    private void goToPendingUsers() {
        Main.showAdminPendingUsers();
    }

    @FXML
    private void goToProfile() {
        Main.showUserProfile();
    }

    @FXML
    private void goToAvis() {
        Main.showAdminAvis();
    }

    @FXML
    private void goToEvents() {
        Main.showAdminEvents();
    }

    @FXML
    private void goToDossiers() {
        Main.showAdminDossiers();
    }

    @FXML
    private void goToReclamations() {
        Main.showAdminReclamations();
    }

    @FXML
    private void logout() {
        Main.showLoginPage();
    }
}