package Controler;

import entities.user;
import entities.user.Role;
import entities.user.Gender;
import entities.user.AccountStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import services.EmailService;
import services.userServices;
import tests.Main;
import utils.CaptchaGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class SignUpController {

    @FXML private VBox signUpForm;
    @FXML private VBox verificationSection;
    @FXML private TextField verificationCodeField;
    @FXML private Label verificationText;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Gender> genderCombo;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label errorLabel;

    @FXML private ImageView profileImageView;
    @FXML private Label photoStatus;
    private String base64Image = "";

    @FXML private ImageView logoView;
    @FXML private ImageView captchaImageView;
    @FXML private TextField captchaInputField;
    private CaptchaGenerator captchaGenerator;

    @FXML private VBox doctorFields;
    @FXML private ComboBox<String> specialtyField;
    @FXML private TextField orderNumberField;
    @FXML private TextField consultationFeeField;

    @FXML private VBox pharmacyFields;
    @FXML private TextField pharmacyNameField;
    @FXML private TextField licenceNumberField;

    @FXML private VBox assistantFields;
    @FXML private ComboBox<String> doctorSelectionCombo;
    private java.util.List<user> allDoctors = new java.util.ArrayList<>();

    private userServices userService = new userServices();
    private String generatedVerificationCode;
    private user pendingUser;

    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList(Gender.values()));
        genderCombo.setValue(Gender.M);

        if (specialtyField != null) {
            specialtyField.setItems(FXCollections.observableArrayList(
                "Cardiologue", "Dermatologue", "Généraliste", "Gynécologue",
                "Pédiatre", "Neurologue", "Ophtalmologue", "Psychiatre", "Dentiste", "Orthopédiste"
            ));
        }

        roleCombo.setItems(FXCollections.observableArrayList(Role.patient, Role.doctor, Role.pharmacist, Role.assistant));
        roleCombo.setValue(Role.patient);
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateFieldsForRole(newVal));

        updateFieldsForRole(Role.patient);
        setDefaultProfileImage();
        captchaGenerator = new CaptchaGenerator();
        captchaImageView.setImage(captchaGenerator.getCaptchaImage());
    }

    @FXML private void refreshCaptcha() {
        captchaGenerator.generateCaptcha();
        captchaImageView.setImage(captchaGenerator.getCaptchaImage());
        captchaInputField.clear();
    }

    private void updateFieldsForRole(Role role) {
        if (doctorFields != null) { doctorFields.setVisible(false); doctorFields.setManaged(false); }
        if (pharmacyFields != null) { pharmacyFields.setVisible(false); pharmacyFields.setManaged(false); }
        if (assistantFields != null) { assistantFields.setVisible(false); assistantFields.setManaged(false); }

        if (role == Role.doctor && doctorFields != null) { doctorFields.setVisible(true); doctorFields.setManaged(true); }
        else if (role == Role.pharmacist && pharmacyFields != null) { pharmacyFields.setVisible(true); pharmacyFields.setManaged(true); }
        else if (role == Role.assistant && assistantFields != null) { assistantFields.setVisible(true); assistantFields.setManaged(true); loadDoctorsForAssistant(); }
    }

    private void loadDoctorsForAssistant() {
        try {
            List<user> allUsers = userService.getAll();
            allDoctors = allUsers.stream().filter(u -> u.getRole() == Role.doctor).collect(java.util.stream.Collectors.toList());
            javafx.collections.ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (user d : allDoctors) doctorNames.add("Dr. " + d.getFirstName() + " " + d.getLastName());
            if (doctorSelectionCombo != null) doctorSelectionCombo.setItems(doctorNames);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setDefaultProfileImage() {
        profileImageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png", 80, 80, true, true));
    }

    @FXML private void choosePhoto() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                profileImageView.setImage(new Image(selectedFile.toURI().toString(), 150, 150, true, true));
                BufferedImage bufferedImage = ImageIO.read(selectedFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                photoStatus.setText("Photo sélectionnée !");
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleSignUp() {
        // RESET ERROR
        errorLabel.setText("");

        if (!captchaGenerator.validate(captchaInputField.getText())) {
            errorLabel.setText("Captcha incorrect");
            refreshCaptcha();
            return;
        }

        if (firstNameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            errorLabel.setText("Veuillez remplir les champs obligatoires");
            return;
        }

        try {
            pendingUser = new user();
            pendingUser.setFirstName(firstNameField.getText());
            pendingUser.setLastName(lastNameField.getText());
            pendingUser.setEmail(emailField.getText());
            pendingUser.setPassword(passwordField.getText());
            pendingUser.setRole(roleCombo.getValue());
            pendingUser.setAge(Integer.parseInt(ageField.getText()));
            pendingUser.setGender(genderCombo.getValue());
            pendingUser.setAccountStatus(AccountStatus.active);
            pendingUser.setUserPhoto(base64Image);

            generatedVerificationCode = EmailService.generateVerificationCode();
            if (EmailService.sendVerificationEmail(pendingUser.getEmail(), generatedVerificationCode)) {
                // ICI ON CACHE LE FORMULAIRE ET ON AFFICHE LA SECTION DE VÉRIFICATION
                signUpForm.setVisible(false);
                signUpForm.setManaged(false);
                
                verificationSection.setVisible(true);
                verificationSection.setManaged(true);
                
                verificationText.setText("Un code a été envoyé à : " + pendingUser.getEmail());
            } else {
                errorLabel.setText("Erreur d'envoi d'email");
            }
        } catch (Exception e) {
            errorLabel.setText("Données invalides (Vérifiez l'âge)");
        }
    }

    @FXML
    private void handleVerifyRegistration() {
        String input = verificationCodeField.getText().trim();
        if (input.equals(generatedVerificationCode)) {
            try {
                userService.add(pendingUser);
                
                // Alert de succès (dernière alerte)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bienvenue");
                alert.setHeaderText(null);
                alert.setContentText("Compte créé avec succès !");
                alert.showAndWait();
                
                Main.showLoginPage();
            } catch (SQLException e) {
                errorLabel.setText("Erreur de sauvegarde");
            }
        } else {
            errorLabel.setText("Code incorrect");
        }
    }

    @FXML private void backToForm() {
        verificationSection.setVisible(false);
        verificationSection.setManaged(false);
        signUpForm.setVisible(true);
        signUpForm.setManaged(true);
    }

    @FXML private void goToLogin() { Main.showLoginPage(); }
}