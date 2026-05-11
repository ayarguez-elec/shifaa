package Controler;

import entities.user;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import services.userServices;
import services.AvatarService;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UserProfileController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextArea addressArea;
    @FXML private ImageView profileImageView;
    @FXML private Label imageStatusLabel;

    @FXML private VBox proSection;
    @FXML private VBox specialtyBox;
    @FXML private VBox feeBox;
    @FXML private VBox pharmacyBox;
    @FXML private VBox licenceBox;
    
    @FXML private TextField specialtyField;
    @FXML private TextField feeField;
    @FXML private TextField pharmacyNameField;
    @FXML private TextField licenceField;

    @FXML private Button editButton;
    @FXML private VBox imageButtonsBox;
    @FXML private VBox passwordSection;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private HBox actionButtonsBox;

    @FXML private VBox avatarGalleryBox;
    @FXML private FlowPane avatarOptionsContainer;

    private user currentUser;
    private userServices userService = new userServices();
    private AvatarService avatarService = new AvatarService();
    private String base64Image = null;
    private String selectedAvatarUrl = null;

    @FXML
    public void initialize() {
        genderCombo.setItems(javafx.collections.FXCollections.observableArrayList("Homme", "Femme"));
        loadUserData(Main.getCurrentUserId());
    }

    public void loadUserData(int userId) {
        try {
            for (user u : userService.getAll()) {
                if (u.getUserId() == userId) {
                    currentUser = u;
                    break;
                }
            }

            if (currentUser != null) {
                firstNameField.setText(currentUser.getFirstName());
                lastNameField.setText(currentUser.getLastName());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhone());
                ageField.setText(String.valueOf(currentUser.getAge()));
                genderCombo.setValue(currentUser.getGender() == user.Gender.M ? "Homme" : "Femme");
                addressArea.setText(currentUser.getAddress());
                
                base64Image = currentUser.getUserPhoto();
                selectedAvatarUrl = currentUser.getAvatarUrl();
                loadProfileImage();
                setupRoleSpecificFields();
                toggleEditMode(false); // Mode lecture seule par défaut
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le profil");
        }
    }

    private void toggleEditMode(boolean enable) {
        // Toggle field editability
        firstNameField.setEditable(enable);
        lastNameField.setEditable(enable);
        emailField.setEditable(enable);
        phoneField.setEditable(enable);
        ageField.setEditable(enable);
        addressArea.setEditable(enable);
        specialtyField.setEditable(enable);
        feeField.setEditable(enable);
        pharmacyNameField.setEditable(enable);
        licenceField.setEditable(enable);

        // ComboBox interaction
        genderCombo.setMouseTransparent(!enable);

        String editStyle = "-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-text-fill: #4A5568;";
        String readStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #2D3748; -fx-opacity: 1;";

        // Apply styles
        firstNameField.setStyle(enable ? editStyle : readStyle);
        lastNameField.setStyle(enable ? editStyle : readStyle);
        emailField.setStyle(enable ? editStyle : readStyle);
        phoneField.setStyle(enable ? editStyle : readStyle);
        ageField.setStyle(enable ? editStyle : readStyle);
        addressArea.setStyle(enable ? editStyle : readStyle);
        specialtyField.setStyle(enable ? editStyle : readStyle);
        feeField.setStyle(enable ? editStyle : readStyle);
        pharmacyNameField.setStyle(enable ? editStyle : readStyle);
        licenceField.setStyle(enable ? editStyle : readStyle);
        genderCombo.setStyle(enable ? editStyle : readStyle);

        // Toggle visibility
        editButton.setVisible(!enable);
        editButton.setManaged(!enable);
        
        imageButtonsBox.setVisible(enable);
        imageButtonsBox.setManaged(enable);

        passwordSection.setVisible(enable);
        passwordSection.setManaged(enable);
        
        actionButtonsBox.setVisible(enable);
        actionButtonsBox.setManaged(enable);
        
        if (!enable) {
            newPasswordField.clear();
            confirmNewPasswordField.clear();
            avatarGalleryBox.setVisible(false);
            avatarGalleryBox.setManaged(false);
        }
    }

    @FXML
    private void startEditing() {
        toggleEditMode(true);
    }

    private void setupRoleSpecificFields() {
        user.Role role = currentUser.getRole();
        
        // Par défaut, cacher tout
        proSection.setVisible(false);
        proSection.setManaged(false);
        specialtyBox.setVisible(false);
        specialtyBox.setManaged(false);
        feeBox.setVisible(false);
        feeBox.setManaged(false);
        pharmacyBox.setVisible(false);
        pharmacyBox.setManaged(false);
        licenceBox.setVisible(false);
        licenceBox.setManaged(false);

        if (role == user.Role.doctor) {
            proSection.setVisible(true);
            proSection.setManaged(true);
            specialtyBox.setVisible(true);
            specialtyBox.setManaged(true);
            feeBox.setVisible(true);
            feeBox.setManaged(true);
            specialtyField.setText(currentUser.getSpecialty());
            feeField.setText(String.valueOf(currentUser.getConsultationFee()));
        } else if (role == user.Role.pharmacist) {
            proSection.setVisible(true);
            proSection.setManaged(true);
            pharmacyBox.setVisible(true);
            pharmacyBox.setManaged(true);
            licenceBox.setVisible(true);
            licenceBox.setManaged(true);
            pharmacyNameField.setText(currentUser.getPharmacyName());
            licenceField.setText(currentUser.getLicenceNumber());
        }
    }

    private void loadProfileImage() {
        String photoToLoad = (selectedAvatarUrl != null && !selectedAvatarUrl.isEmpty()) ? selectedAvatarUrl : base64Image;

        // Protection contre les anciennes données de test (contenant 'generated_')
        if (photoToLoad != null && photoToLoad.startsWith("generated_")) {
            photoToLoad = (base64Image != null && !base64Image.startsWith("generated_")) ? base64Image : null;
        }

        if (photoToLoad != null && !photoToLoad.trim().isEmpty()) {
            try {
                // Nettoyage rigoureux de la chaîne Base64
                String clean = photoToLoad.trim().replaceAll("[^A-Za-z0-9+/=]", "");
                byte[] bytes = Base64.getDecoder().decode(clean);
                profileImageView.setImage(new Image(new ByteArrayInputStream(bytes)));
                imageStatusLabel.setText(selectedAvatarUrl != null && !selectedAvatarUrl.isEmpty() ? "Avatar 3D chargé" : "Photo réelle chargée");
                return;
            } catch (Exception e) {
                System.err.println("Erreur décodage image: " + e.getMessage());
            }
        }
        profileImageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        imageStatusLabel.setText("Aucune image");
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            try {
                // Compression et redimensionnement pour éviter "Packet for query is too large"
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) {
                    showAlert("Erreur", "Format d'image non supporté");
                    return;
                }

                // Redimensionner si trop grand (max 400px)
                int targetWidth = 400;
                int targetHeight = (int) (originalImage.getHeight() * (400.0 / originalImage.getWidth()));
                
                if (originalImage.getWidth() <= targetWidth) {
                    targetWidth = originalImage.getWidth();
                    targetHeight = originalImage.getHeight();
                }

                // Utiliser TYPE_INT_RGB pour le format JPG (pas de transparence)
                BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                // Remplir le fond en blanc (au cas où l'image originale avait de la transparence)
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, targetWidth, targetHeight);
                g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                g.dispose();

                // Convertir en Base64 avec compression JPEG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos);
                byte[] bytes = baos.toByteArray();
                
                base64Image = Base64.getEncoder().encodeToString(bytes);
                
                // Prévisualisation immédiate
                profileImageView.setImage(new Image(new ByteArrayInputStream(bytes)));
                imageStatusLabel.setText("Image optimisée: " + (bytes.length/1024) + " KB");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de traiter l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void removeImage() {
        base64Image = "";
        selectedAvatarUrl = "";
        profileImageView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        imageStatusLabel.setText("Image supprimée");
    }

    @FXML
    private void generateAvatarOptions() {
        if (currentUser == null) {
            showAlert("Erreur", "Données utilisateur non chargées. Veuillez vérifier votre connexion base de données.");
            return;
        }

        avatarGalleryBox.setVisible(true);
        avatarGalleryBox.setManaged(true);
        avatarOptionsContainer.getChildren().clear();

        try {
            String seed = currentUser.getEmail() != null ? currentUser.getEmail() : "default_seed";
            java.util.List<String> options = avatarService.generateAvatarOptions(seed, 6);
            for (String base64 : options) {
                if (base64 == null || base64.isEmpty()) continue;
                
                ImageView optionView = new ImageView(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64))));
                optionView.setFitHeight(100);
                optionView.setFitWidth(100);
                optionView.setCursor(javafx.scene.Cursor.HAND);
                optionView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5); -fx-background-radius: 10;");
                
                optionView.setOnMouseClicked(e -> {
                    // On garde la vraie photo intacte (base64Image) pour le Face ID
                    // On n'enregistre l'avatar que dans selectedAvatarUrl pour l'affichage
                    selectedAvatarUrl = base64; 
                    profileImageView.setImage(optionView.getImage());
                    imageStatusLabel.setText("Avatar 3D sélectionné (L'avatar sera affiché, mais votre photo réelle reste active pour le Face ID)");
                });

                avatarOptionsContainer.getChildren().add(optionView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer les avatars : " + e.getMessage());
        }
    }
    @FXML
    private void saveProfile() {
        try {
            // Check password change
            String newPass = newPasswordField.getText();
            String confirmPass = confirmNewPasswordField.getText();
            if (newPass != null && !newPass.isEmpty()) {
                if (!newPass.equals(confirmPass)) {
                    showAlert("Erreur", "Les mots de passe ne correspondent pas.");
                    return;
                }
                currentUser.setPassword(newPass);
            }

            currentUser.setFirstName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setPhone(phoneField.getText());
            currentUser.setAge(Integer.parseInt(ageField.getText()));
            currentUser.setGender(genderCombo.getValue().equals("Homme") ? user.Gender.M : user.Gender.F);
            currentUser.setAddress(addressArea.getText());
            currentUser.setUserPhoto(base64Image);
            currentUser.setAvatarUrl(selectedAvatarUrl);

            if (currentUser.getRole() == user.Role.doctor) {
                currentUser.setSpecialty(specialtyField.getText());
                currentUser.setConsultationFee(Float.parseFloat(feeField.getText()));
            } else if (currentUser.getRole() == user.Role.pharmacist) {
                currentUser.setPharmacyName(pharmacyNameField.getText());
                currentUser.setLicenceNumber(licenceField.getText());
            }

            userService.update(currentUser);
            Main.refreshMainMenu();
            showAlert("Succès", "Profil mis à jour avec succès !");
            toggleEditMode(false);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les formats numériques (âge, tarifs)");
        } catch (SQLException e) {
            showAlert("Erreur", "Base de données: " + e.getMessage());
        }
    }

    @FXML
    private void cancelChanges() {
        loadUserData(currentUser.getUserId());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
