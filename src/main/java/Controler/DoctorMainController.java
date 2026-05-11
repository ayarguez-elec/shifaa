package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

public class DoctorMainController implements Initializable {

    @FXML private AnchorPane contentContainer;
    @FXML private ImageView logoView;
    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;

    private static DoctorMainController instance;
    private userServices userService = new userServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) logoView.setImage(logo);
        } catch (Exception e) {}

        loadUserInfo();
        Main.showDoctorPatients();
    }

    public void loadUserInfo() {
        try {
            int userId = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText("Dr. " + u.getFirstName() + " " + u.getLastName());
                    String photoBase64 = (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) ? u.getAvatarUrl() : u.getUserPhoto();
                    
                    if (photoBase64 != null && photoBase64.startsWith("generated_")) {
                        photoBase64 = u.getUserPhoto();
                    }

                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        try {
                            String cleanBase64 = photoBase64.trim().replaceAll("[^A-Za-z0-9+/=]", "");
                            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                            profileImageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                        } catch (Exception e) {
                            setDefaultProfileImage();
                        }
                    } else {
                        setDefaultProfileImage();
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultProfileImage() {
        try {
            Image defaultImage = new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png", 80, 80, true, true);
            if (defaultImage != null) profileImageView.setImage(defaultImage);
        } catch (Exception e) {}
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

    @FXML private void goToPatients() { Main.showDoctorPatients(); }
    @FXML private void goToProfile() { Main.showUserProfile(); }
    @FXML private void goToFeedbacks() { Main.showDoctorFeedbacks(); }
    @FXML private void logout() { Main.showLoginPage(); }
}
