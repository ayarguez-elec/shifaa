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
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

public class PatientMainController implements Initializable {

    @FXML private AnchorPane contentContainer;
    @FXML private AnchorPane mainAnchor; // Le nouveau conteneur parent
    @FXML private ImageView logoView;
    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;

    private static PatientMainController instance;
    
    public static PatientMainController getInstance() {
        return instance;
    }

    private userServices userService = new userServices();
    private Parent activeChatbot = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            if (logo != null) logoView.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo non trouvé");
        }
        loadUserInfo();
        Main.showUserProfile();
    }

    public void loadUserInfo() {
        try {
            int userId = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    userNameLabel.setText(u.getFirstName() + " " + u.getLastName());
                    if (u.getUserPhoto() != null && !u.getUserPhoto().isEmpty()) {
                        try {
                            String cleanBase64 = u.getUserPhoto().trim().replaceAll("\\s+", "").replaceAll("\\n", "").replaceAll("\\r", "");
                            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                            Image image = new Image(new ByteArrayInputStream(imageBytes));
                            profileImageView.setImage(image);
                        } catch (Exception e) { setDefaultProfileImage(); }
                    } else { setDefaultProfileImage(); }
                    break;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setDefaultProfileImage() {
        try {
            Image defaultImage = new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png", 80, 80, true, true);
            if (defaultImage != null) profileImageView.setImage(defaultImage);
        } catch (Exception e) {}
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
                
                // Positionnement flottant en bas à droite
                mainAnchor.getChildren().add(activeChatbot);
                AnchorPane.setBottomAnchor(activeChatbot, 100.0); // Au-dessus du bouton
                AnchorPane.setRightAnchor(activeChatbot, 30.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void goToProfile() { Main.showUserProfile(); }
    @FXML private void goToDoctors() { Main.showPatientDoctors(); }
    @FXML private void goToPharmacies() { Main.showPatientPharmacies(); }
    @FXML private void goToMedicalFiles() { Main.showPatientMedicalFiles(); }
    @FXML private void goToProducts() { Main.showPatientProducts(); }
    @FXML
    private void goToEvents() {
        Main.showPatientEvents();
    }

    @FXML
    private void goToHealthStats() {
        Main.showPatientHealthStats();
    }

    @FXML
    private void goToPillTracker() {
        Main.showPatientPillTracker();
    }

    @FXML
    private void goToReclamation() {
        Main.showPatientReclamation();
    }
    @FXML private void logout() { Main.showLoginPage(); }
}