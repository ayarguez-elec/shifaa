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
    @FXML private TextField passwordVisible;
    @FXML private TextField confirmPasswordVisible;
    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;
    @FXML private TextField phoneField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Gender> genderCombo;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label errorLabel;
    @FXML private Label errorLabelSignUp;

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
    @FXML private Label documentStatus;
    @FXML private Label documentStatusPharma;
    @FXML private Label signatureStatus;
    private String base64Document = "";
    private String base64Signature = "";

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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
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

    @FXML private void chooseDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un document PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                byte[] fileContent = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                base64Document = Base64.getEncoder().encodeToString(fileContent);
                String fileName = selectedFile.getName();
                if (documentStatus != null) documentStatus.setText(fileName);
                if (documentStatusPharma != null) documentStatusPharma.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Erreur lors de la lecture du fichier");
            }
        }
    }

    @FXML private void chooseSignature() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de signature");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                BufferedImage img = ImageIO.read(selectedFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                base64Signature = Base64.getEncoder().encodeToString(baos.toByteArray());
                if (signatureStatus != null) signatureStatus.setText("✓ " + selectedFile.getName());
                signatureStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #48BB78;");
            } catch (IOException e) {
                e.printStackTrace();
                errorLabelSignUp.setText("Erreur lors de la lecture de la signature");
            }
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordField.setVisible(false);  passwordField.setManaged(false);
            passwordVisible.setVisible(true); passwordVisible.setManaged(true);
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false); passwordVisible.setManaged(false);
            passwordField.setVisible(true);    passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) {
            confirmPasswordVisible.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);  confirmPasswordField.setManaged(false);
            confirmPasswordVisible.setVisible(true);  confirmPasswordVisible.setManaged(true);
            confirmPasswordVisible.requestFocus();
            confirmPasswordVisible.positionCaret(confirmPasswordVisible.getText().length());
        } else {
            confirmPasswordField.setText(confirmPasswordVisible.getText());
            confirmPasswordVisible.setVisible(false); confirmPasswordVisible.setManaged(false);
            confirmPasswordField.setVisible(true);    confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
        }
    }

    @FXML
    private void handleSignUp() {
        errorLabel.setText("");
        errorLabelSignUp.setText("");

        // Synchroniser les champs visibles → masqués avant validation
        if (passwordShown) passwordField.setText(passwordVisible.getText());
        if (confirmPasswordShown) confirmPasswordField.setText(confirmPasswordVisible.getText());

        if (!captchaGenerator.validate(captchaInputField.getText())) {
            errorLabelSignUp.setText("Captcha incorrect");
            refreshCaptcha();
            return;
        }

        // --- VALIDATION DES CHAMPS OBLIGATOIRES GÉNÉRAUX ---
        if (firstNameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty() || roleCombo.getValue() == null) {
            errorLabelSignUp.setText("Veuillez remplir les champs obligatoires (*)");
            return;
        }

        Role selectedRole = roleCombo.getValue();

        // --- VALIDATION SPÉCIFIQUE (OBLIGATOIRE) ---
        if (selectedRole == Role.doctor) {
            if (specialtyField.getValue() == null || orderNumberField.getText().isEmpty() || consultationFeeField.getText().isEmpty()) {
                errorLabelSignUp.setText("Tous les champs Docteur sont obligatoires (*)");
                return;
            }
            if (base64Document.isEmpty()) {
                errorLabelSignUp.setText("Veuillez importer votre diplôme (PDF)");
                return;
            }
            if (base64Signature.isEmpty()) {
                errorLabelSignUp.setText("Veuillez importer votre image de signature (*)");
                return;
            }
        } else if (selectedRole == Role.pharmacist) {
            if (pharmacyNameField.getText().isEmpty() || licenceNumberField.getText().isEmpty()) {
                errorLabelSignUp.setText("Tous les champs Pharmacie sont obligatoires (*)");
                return;
            }
            if (base64Document.isEmpty()) {
                errorLabelSignUp.setText("Veuillez importer votre licence (PDF)");
                return;
            }
        } else if (selectedRole == Role.assistant) {
            if (doctorSelectionCombo.getValue() == null) {
                errorLabelSignUp.setText("Veuillez sélectionner un médecin référent (*)");
                return;
            }
        }

        try {
            pendingUser = new user();
            pendingUser.setFirstName(firstNameField.getText());
            pendingUser.setLastName(lastNameField.getText());
            pendingUser.setEmail(emailField.getText());
            pendingUser.setPassword(passwordField.getText());
            pendingUser.setRole(selectedRole);
            
            int age = 0;
            if (!ageField.getText().isEmpty()) {
                try {
                    age = Integer.parseInt(ageField.getText());
                } catch (NumberFormatException e) {
                    errorLabelSignUp.setText("L'âge doit être un nombre");
                    return;
                }
            }
            pendingUser.setAge(age);
            pendingUser.setGender(genderCombo.getValue());
            pendingUser.setUserPhoto(base64Image);
            pendingUser.setAddress(addressArea.getText());
            pendingUser.setPhone(phoneField.getText());
            
            // Gestion des champs spécifiques
            if (selectedRole == Role.doctor) {
                pendingUser.setSpecialty(specialtyField.getValue());
                pendingUser.setOrderNumber(orderNumberField.getText());
                pendingUser.setConsultationFee(Float.parseFloat(consultationFeeField.getText()));
                pendingUser.setImageSignature(base64Signature);
            } else if (selectedRole == Role.pharmacist) {
                pendingUser.setPharmacyName(pharmacyNameField.getText());
                pendingUser.setLicenceNumber(licenceNumberField.getText());
            } else if (selectedRole == Role.assistant) {
                // Associer au docteur sélectionné
                String docName = doctorSelectionCombo.getValue();
                user doc = allDoctors.stream()
                        .filter(d -> ("Dr. " + d.getFirstName() + " " + d.getLastName()).equals(docName))
                        .findFirst().orElse(null);
                if (doc != null) pendingUser.setDoctorId(doc.getUserId());
            }

            if (selectedRole == Role.doctor || selectedRole == Role.pharmacist) {
                pendingUser.setProfessionalDocument(base64Document);
            }

            // Les comptes professionnels sont désactivés par défaut (attente admin)
            // Sauf les patients (auto-approuvés)
            if (selectedRole == Role.patient) {
                pendingUser.setAccountStatus(AccountStatus.active);
            } else {
                pendingUser.setAccountStatus(AccountStatus.pending); // En attente de validation Admin
            }

            generatedVerificationCode = EmailService.generateVerificationCode();

            // Désactiver le bouton et afficher un message d'attente
            errorLabelSignUp.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #3182CE;");
            errorLabelSignUp.setText("⏳ Envoi du code en cours...");

            final String codeToSend = generatedVerificationCode;
            final String emailToSend = pendingUser.getEmail();

            // Envoi dans un thread séparé pour ne pas bloquer l'UI
            Thread emailThread = new Thread(() -> {
                boolean sent = EmailService.sendVerificationEmail(emailToSend, codeToSend);
                javafx.application.Platform.runLater(() -> {
                    errorLabelSignUp.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #FF5C5C;");
                    if (sent) {
                        signUpForm.setVisible(false);
                        signUpForm.setManaged(false);
                        verificationSection.setVisible(true);
                        verificationSection.setManaged(true);
                        verificationText.setText("Un code a été envoyé à : " + emailToSend);
                    } else {
                        errorLabelSignUp.setText("❌ Échec envoi email. Vérifiez votre connexion internet.");
                    }
                });
            });
            emailThread.setDaemon(true);
            emailThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabelSignUp.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleVerifyRegistration() {
        String input = verificationCodeField.getText().trim();
        if (input.equals(generatedVerificationCode)) {
            try {
                userService.add(pendingUser);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bienvenue");
                alert.setHeaderText(null);
                alert.setContentText("Compte créé avec succès ! En attente de validation admin.");
                alert.showAndWait();
                
                Main.showLoginPage();
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Erreur : " + e.getMessage());
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
