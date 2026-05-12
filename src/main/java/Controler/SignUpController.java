package Controler;

import entities.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.UserServ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SignUpController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMessage;
    @FXML private ImageView imgProfile;

    private UserServ userService;
    private String selectedImagePath = "default_profile.png"; // default

    @FXML
    public void initialize() {
        userService = new UserServ();
        cbRole.setItems(FXCollections.observableArrayList("Patient", "Médecin", "Pharmacien", "Administrateur"));
        cbRole.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) tfEmail.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                // Determine destination path inside resources
                String userHome = System.getProperty("user.dir");
                Path destDir = Paths.get(userHome, "src", "main", "resources", "images", "users");
                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }
                
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Path destFile = destDir.resolve(fileName);
                
                // Copy file
                Files.copy(file.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI and relative path
                Image image = new Image(file.toURI().toString());
                imgProfile.setImage(image);
                
                // Store the relative path for the database
                selectedImagePath = "images/users/" + fileName;
                
            } catch (IOException e) {
                showMessage("❌ Erreur lors du chargement de l'image.", "#DC2626");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSignUp() {
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email = tfEmail.getText().trim();
        String pwd = tfPassword.getText().trim();
        String role = cbRole.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            showMessage("⚠️ Veuillez remplir tous les champs obligatoires.", "#DC2626");
            return;
        }

        if (!pwd.equals(tfConfirmPassword.getText())) {
            showMessage("❌ Les mots de passe ne correspondent pas.", "#DC2626");
            return;
        }

        // Create new user
        User newUser = new User(email, pwd, role, selectedImagePath);
        userService.ajouter(newUser);

        showMessage("✅ Inscription réussie ! Redirection...", "#16A34A");
        
        // Wait 1 second before navigating to login
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> navigateTo("/Login.fxml"));
        pause.play();
    }

    @FXML
    private void handleGoToLogin() {
        navigateTo("/Login.fxml");
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
