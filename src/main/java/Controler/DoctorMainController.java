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

public class DoctorMainController implements Initializable {

    @FXML private AnchorPane  contentContainer;
    @FXML private ImageView   logoView;
    @FXML private ImageView   profileImageView;
    @FXML private Label       userNameLabel;
    @FXML private Label       initialsLabel;
    @FXML private Circle      avatarCircle;

    private static DoctorMainController instance;
    private final userServices userService = new userServices();

    public static DoctorMainController getInstance() { return instance; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) logoView.setImage(logo);
        } catch (Exception ignored) {}
        loadUserInfo();
    }

    public void loadUserInfo() {
        try {
            int userId = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText("Dr. " + u.getFirstName() + " " + u.getLastName());
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
        } catch (SQLException e) { e.printStackTrace(); }
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

    @FXML private void goToDashboard() { Main.showDoctorDashboard(); }
    @FXML private void goToStats()     { Main.showStatistiques(); }
    @FXML private void goToPatients()  { Main.showDoctorPatients(); }
    @FXML private void goToProfile()   { Main.showUserProfile(); }
    @FXML private void goToFeedbacks() { Main.showDoctorFeedbacks(); }
    @FXML private void goToEvents()    { Main.showDoctorEvents(); }
    @FXML private void logout()        { Main.showLoginPage(); }
}
