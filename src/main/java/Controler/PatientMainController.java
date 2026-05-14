package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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

public class PatientMainController implements Initializable {

    @FXML private AnchorPane contentContainer;
    @FXML private AnchorPane mainAnchor;
    @FXML private ImageView  logoView;
    @FXML private ImageView  profileImageView;
    @FXML private Label      userNameLabel;
    @FXML private Label      initialsLabel;
    @FXML private Circle     avatarCircle;

    private static PatientMainController instance;

    public static PatientMainController getInstance() { return instance; }

    private final userServices userService = new userServices();
    private Parent activeChatbot = null;

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
    private void toggleChatbot() {
        if (activeChatbot != null) {
            mainAnchor.getChildren().remove(activeChatbot);
            activeChatbot = null;
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatbotView.fxml"));
                activeChatbot = loader.load();
                mainAnchor.getChildren().add(activeChatbot);
                AnchorPane.setBottomAnchor(activeChatbot, 100.0);
                AnchorPane.setRightAnchor(activeChatbot, 30.0);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML private void goToDashboard()           { Main.showPatientDashboard(); }
    @FXML private void goToProfile()             { Main.showUserProfile(); }
    @FXML private void goToDoctors()             { Main.showPatientDoctors(); }
    @FXML private void goToAppointments()        { Main.showPatientAppointments(); }
    @FXML private void goToRequestAppointment()  { Main.showPatientRequestAppointment(); }
    @FXML private void goToPharmacies()          { Main.showPatientPharmacies(); }
    @FXML private void goToMedicalFiles()        { Main.showPatientMedicalFiles(); }
    @FXML private void goToPrescriptions()       { Main.showPatientPrescriptions(); }
    @FXML private void goToProducts()            { Main.showPatientProducts(); }
    @FXML private void goToEvents()              { Main.showPatientEvents(); }
    @FXML private void goToHealthStats()         { Main.showPatientHealthStats(); }
    @FXML private void goToPillTracker()         { Main.showPatientPillTracker(); }
    @FXML private void goToReclamation()         { Main.showPatientReclamation(); }
    @FXML private void goToUserDashboard()       { Main.showUserDashboard(); }
    @FXML private void goToUserNotifications()   { Main.showUserNotifications(); }
    @FXML private void logout()                  { Main.showLoginPage(); }
}
