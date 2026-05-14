package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class AdminMainController {

    @FXML private AnchorPane contentContainer;
    @FXML private ImageView  logoView;
    @FXML private ImageView  profileImageView;
    @FXML private Label      userNameLabel;
    @FXML private Label      initialsLabel;
    @FXML private Circle     avatarCircle;
    @FXML private Button     pendingBadgeBtn;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) logoView.setImage(logo);
        } catch (Exception ignored) {}
        loadUserInfo();
        updatePendingBadge();
    }

    public void updatePendingBadge() {
        try {
            userServices svc = new userServices();
            long pending = svc.getAll().stream()
                    .filter(u -> u.getAccountStatus() == user.AccountStatus.pending)
                    .count();
            if (pendingBadgeBtn != null) {
                pendingBadgeBtn.setText("⏳  En attente (" + pending + ")");
                if (pending > 0) {
                    pendingBadgeBtn.setStyle("-fx-text-fill: #ED8936; -fx-font-weight: bold;");
                } else {
                    pendingBadgeBtn.setStyle("-fx-text-fill: rgba(255,255,255,0.60);");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadUserInfo() {
        try {
            userServices svc = new userServices();
            int userId = Main.getCurrentUserId();
            for (user u : svc.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText(u.getFirstName() + " " + u.getLastName());
                    initialsLabel.setText(buildInitials(u.getFirstName(), u.getLastName()));

                    String photoBase64 = resolvePhoto(u);
                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        try {
                            byte[] bytes = Base64.getDecoder().decode(
                                    photoBase64.trim().replaceAll("[^A-Za-z0-9+/=]", ""));
                            profileImageView.setImage(new Image(new ByteArrayInputStream(bytes)));
                            profileImageView.setVisible(true);
                            profileImageView.setManaged(true);
                        } catch (Exception e) { showInitials(); }
                    } else { showInitials(); }
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInitials() {
        profileImageView.setVisible(false);
        profileImageView.setManaged(false);
    }

    private String resolvePhoto(user u) {
        String p = (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty())
                ? u.getAvatarUrl() : u.getUserPhoto();
        if (p != null && p.startsWith("generated_")) p = u.getUserPhoto();
        return p;
    }

    private String buildInitials(String first, String last) {
        String f = (first != null && !first.isEmpty()) ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = (last  != null && !last.isEmpty())  ? String.valueOf(last.charAt(0)).toUpperCase()  : "";
        return f + l;
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

    @FXML private void goToDashboard()               { Main.showAdminDashboard(); }
    @FXML private void goToUtilisateurs()            { Main.showAdminUtilisateurs(); }
    @FXML private void goToPendingUsers()            { Main.showAdminPendingUsers(); }
    @FXML private void goToProfile()                 { Main.showUserProfile(); }
    @FXML private void goToAvis()                    { Main.showAdminAvis(); }
    @FXML private void goToEvents()                  { Main.showAdminEvents(); }
    @FXML private void goToEventAnalytics()          { Main.showAdminEventAnalytics(); }
    @FXML private void goToEventParticipants()       { Main.showAdminEventParticipants(); }
    @FXML private void goToEventReservations()       { Main.showAdminEventReservations(); }
    @FXML private void goToEventRecommendations()    { Main.showAdminEventRecommendations(); }
    @FXML private void goToReclamations()            { Main.showAdminReclamations(); }
    @FXML private void goToAdminDashboardReclamation(){ Main.showAdminDashboardReclamations(); }
    @FXML private void goToAdminNotifications()      { Main.showAdminNotifications(); }
    @FXML private void goToPharmacies()              { Main.showAdminPharmacies(); }
    @FXML private void goToProducts()                { Main.showAdminProducts(); }
    @FXML private void logout()                      { Main.showLoginPage(); }
}
