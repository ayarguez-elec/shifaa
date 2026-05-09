package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import services.EmailService;
import services.userServices;
import tests.Main;

import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private VBox step1Email;
    @FXML private VBox step2Code;
    @FXML private VBox step3Password;
    
    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private Label mainTitle;
    @FXML private Label subTitle;
    @FXML private Label errorLabel;

    private userServices userService = new userServices();
    private String generatedCode;
    private user targetUser;

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorLabel.setText("Veuillez saisir votre email");
            return;
        }

        try {
            for (user u : userService.getAll()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    targetUser = u;
                    break;
                }
            }

            if (targetUser != null) {
                generatedCode = EmailService.generateVerificationCode();
                if (EmailService.sendResetPasswordEmail(email, generatedCode)) {
                    showStep2();
                } else {
                    errorLabel.setText("Erreur lors de l'envoi de l'e-mail");
                }
            } else {
                errorLabel.setText("Aucun compte trouvé avec cet e-mail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur base de données");
        }
    }

    @FXML
    private void handleVerifyCode() {
        String code = codeField.getText().trim();
        if (code.equals(generatedCode)) {
            showStep3();
        } else {
            errorLabel.setText("Code de vérification incorrect");
        }
    }

    @FXML
    private void handleChangePassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (pass.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (!pass.equals(confirm)) {
            errorLabel.setText("Les mots de passe ne correspondent pas");
            return;
        }

        try {
            targetUser.setPassword(pass);
            userService.update(targetUser);
            
            mainTitle.setText("Succès !");
            subTitle.setText("Votre mot de passe a été modifié.");
            step3Password.setVisible(false);
            errorLabel.setText("");
            
            // Redirection après 2 secondes ou via bouton
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la mise à jour");
        }
    }

    private void showStep2() {
        step1Email.setVisible(false);
        step2Code.setVisible(true);
        mainTitle.setText("Vérification");
        subTitle.setText("Entrez le code envoyé à " + targetUser.getEmail());
        errorLabel.setText("");
    }

    private void showStep3() {
        step2Code.setVisible(false);
        step3Password.setVisible(true);
        mainTitle.setText("Sécurité");
        subTitle.setText("Choisissez un nouveau mot de passe robuste");
        errorLabel.setText("");
    }

    @FXML
    private void backToLogin() {
        Main.showLoginPage();
    }
}
