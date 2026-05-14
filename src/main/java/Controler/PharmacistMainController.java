package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

public class PharmacistMainController implements Initializable {

    @FXML private AnchorPane contentContainer;
    @FXML private ImageView  logoView;
    @FXML private ImageView  profileImageView;
    @FXML private Label      userNameLabel;
    @FXML private Label      initialsLabel;
    @FXML private Circle     avatarCircle;

    private static PharmacistMainController instance;
    private final userServices userService = new userServices();

    public static PharmacistMainController getInstance() { return instance; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) logoView.setImage(logo);
        } catch (Exception ignored) {}
        loadUserInfo();
        // Dashboard is triggered by Main.showPharmacistSpace() after controller assignment
    }

    public void loadUserInfo() {
        try {
            int userId = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText(u.getFirstName() + " " + u.getLastName());

                    // Build initials fallback
                    String initials = buildInitials(u.getFirstName(), u.getLastName());
                    initialsLabel.setText(initials);

                    String photoBase64 = (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty())
                            ? u.getAvatarUrl() : u.getUserPhoto();
                    if (photoBase64 != null && photoBase64.startsWith("generated_"))
                        photoBase64 = u.getUserPhoto();

                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        try {
                            byte[] bytes = Base64.getDecoder().decode(
                                    photoBase64.trim().replaceAll("[^A-Za-z0-9+/=]", ""));
                            profileImageView.setImage(new Image(new ByteArrayInputStream(bytes)));
                            profileImageView.setVisible(true);
                            profileImageView.setManaged(true);
                        } catch (Exception e) {
                            showInitials();
                        }
                    } else {
                        showInitials();
                    }
                    break;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showInitials() {
        profileImageView.setVisible(false);
        profileImageView.setManaged(false);
    }

    private String buildInitials(String firstName, String lastName) {
        String f = (firstName != null && !firstName.isEmpty())
                ? String.valueOf(firstName.charAt(0)).toUpperCase() : "";
        String l = (lastName != null && !lastName.isEmpty())
                ? String.valueOf(lastName.charAt(0)).toUpperCase() : "";
        return f + l;
    }

    public void updateContent(javafx.scene.Parent content) {
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        }
    }

    @FXML private void goToDashboard() { Main.showPharmacistDashboard(); }
    @FXML private void goToStock()     { Main.showPharmacyStock(); }
    @FXML private void goToEvents()    { Main.showPharmacistEvents(); }
    @FXML private void goToProfile()   { Main.showUserProfile(); }
    @FXML private void logout()        { Main.showLoginPage(); }
}
