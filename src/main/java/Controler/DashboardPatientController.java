package Controler;

import entities.user;
import entities.userDS;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.SessionManager;

import java.io.IOException;

public class DashboardPatientController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblInitials;
    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = extractNameFromEmail(currentUser.getEmail());
            lblUserName.setText(userName);
            lblUserRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "");
            lblWelcome.setText("Bienvenue, " + userName + " !");

            String avatarPath = currentUser.getAvatarUrl();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                try {
                    if (!avatarPath.startsWith("/")) avatarPath = "/" + avatarPath;
                    Image img = new Image(getClass().getResourceAsStream(avatarPath));
                    imgAvatar.setImage(img);
                } catch (Exception e) {
                    imgAvatar.setVisible(false);
                    lblInitials.setVisible(true);
                    lblInitials.setText(getInitials(userName));
                }
            } else {
                imgAvatar.setVisible(false);
                lblInitials.setVisible(true);
                lblInitials.setText(getInitials(userName));
            }
        }
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) return "Utilisateur";
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String namePart = email.substring(0, atIndex);
            return namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase();
        }
        return email;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) if (!p.isEmpty()) sb.append(p.charAt(0));
        return sb.toString().toUpperCase();
    }

    public void setPatientConnecte(userDS patient) {
        // patient context from MonDossierController — stored for compatibility
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GetAllevent.fxml"));
            Stage stage = (Stage) lblUserName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
