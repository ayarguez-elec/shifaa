package Controler;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserServ;
import utils.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private Label lblMessage;

    private UserServ userService;

    @FXML
    public void initialize() {
        userService = new UserServ();
    }

    @FXML
    private void handleLogin() {
        String email = tfEmail.getText().trim();
        String password = tfPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("⚠️ Veuillez remplir tous les champs.", "#DC2626");
            return;
        }

        // Check against database
        User user = userService.getByEmail(email);
        
        if (user != null && user.getPasswordHash().equals(password)) {
            // Note: In a real app, verify against hashed password using BCrypt.checkpw
            showMessage("✅ Connexion réussie !", "#16A34A");
            
            // Start Session
            SessionManager.getInstance().login(user);
            
            if ("Administrateur".equalsIgnoreCase(user.getRole())) {
                navigateTo("/GetAllevent.fxml");
            } else {
                navigateTo("/DashboardPatient.fxml");
            }
        } else {
            // Fallback for testing before DB is populated
            if (email.equals("admin") && password.equals("admin")) {
                User adminFallback = new User(0, "admin@shifaa.com", "admin", "Administrateur", "default_profile.png");
                SessionManager.getInstance().login(adminFallback);
                navigateTo("/GetAllevent.fxml");
            } else {
                showMessage("❌ Email ou mot de passe incorrect.", "#DC2626");
            }
        }
    }

    @FXML
    private void handleGoToSignUp() {
        navigateTo("/SignUp.fxml");
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
