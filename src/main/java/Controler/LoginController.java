package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.EmailService;
import services.userServices;
import tests.Main;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeBtn;
    @FXML private Label errorLabel;

    private boolean passwordShown = false;
    private userServices userService = new userServices();

    @FXML
    private void handleLogin() {
        // Lire le mot de passe depuis le champ actif
        String email = emailField.getText().trim();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorLabel.setText("Format d'email invalide");
            return;
        }

        try {
            user authenticatedUser = authenticate(email, password);

            if (authenticatedUser != null) {
                // --- VÉRIFICATION DU STATUT DU COMPTE ---
                if (authenticatedUser.getAccountStatus() == user.AccountStatus.pending) {
                    errorLabel.setText("Votre compte est en attente de validation par l'admin.");
                    return;
                } else if (authenticatedUser.getAccountStatus() == user.AccountStatus.blocked) {
                    errorLabel.setText("Votre compte est bloqué. Contactez le support.");
                    return;
                }

                // Initialiser la session
                utils.SessionManager.getInstance().login(authenticatedUser);

                String role = authenticatedUser.getRole().name();
                Main.redirectToSpace(authenticatedUser.getUserId(), role);
            } else {
                errorLabel.setText("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de connexion à la base de données");
        }
    }

    private user authenticate(String email, String password) throws SQLException {
        for (user u : userService.getAll()) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    @FXML
    private void goToSignUp() {
        Main.showSignUpPage();
    }

    @FXML
    private void handleGoogleLogin() {
        processOAuthLogin("Google");
    }

    private void processOAuthLogin(String provider) {
        errorLabel.setText("Ouverture de " + provider + "...");
        services.OAuthService.openOAuthWindow(provider, oAuthUser -> {
            try {
                user existingUser = null;
                for (user u : userService.getAll()) {
                    if (u.getEmail().equals(oAuthUser.getEmail())) {
                        existingUser = u;
                        break;
                    }
                }

                if (existingUser != null) {
                    utils.SessionManager.getInstance().login(existingUser);
                    Main.redirectToSpace(existingUser.getUserId(), existingUser.getRole().name());
                } else {
                    oAuthUser.setPassword("oauth_" + System.currentTimeMillis());
                    userService.add(oAuthUser);
                    for (user u : userService.getAll()) {
                        if (u.getEmail().equals(oAuthUser.getEmail())) {
                            utils.SessionManager.getInstance().login(u);
                            Main.redirectToSpace(u.getUserId(), u.getRole().name());
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                errorLabel.setText("Erreur base de données (OAuth)");
            }
        }, errorMessage -> {
            errorLabel.setText(errorMessage);
        });
    }

    @FXML
    private void handleFaceIdLogin() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorLabel.setText("Saisissez votre email pour le Face ID");
            return;
        }

        errorLabel.setText("Préparation de la webcam...");
        services.FaceIdService faceIdService = new services.FaceIdService();
        faceIdService.scanAndLogin(email, new services.FaceIdService.FaceIdCallback() {
            @Override
            public void onSuccess(user user) {
                utils.SessionManager.getInstance().login(user);
                Main.redirectToSpace(user.getUserId(), user.getRole().name());
            }

            @Override
            public void onFailure(String message) {
                errorLabel.setText(message);
            }
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            // Copier le texte masqué → champ visible
            passwordVisible.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            eyeBtn.setText("🙈");
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            // Copier le texte visible → champ masqué
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            eyeBtn.setText("👁");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void handleForgotPassword() {
        Main.showForgotPasswordPage();
    }
}
