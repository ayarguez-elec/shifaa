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
import services.userServices;
import tests.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class AdminEditUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Gender> genderCombo;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private ComboBox<AccountStatus> statusCombo;

    @FXML private ImageView profileImageView;
    @FXML private Label photoStatus;
    private String base64Image = "";

    @FXML private VBox doctorFields;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private TextField orderNumberField;
    @FXML private TextField consultationFeeField;

    @FXML private VBox pharmacyFields;
    @FXML private TextField pharmacyNameField;
    @FXML private TextField licenceNumberField;

    @FXML private VBox assistantFields;
    @FXML private ComboBox<String> doctorSelectionCombo;
    private java.util.List<user> allDoctors = new java.util.ArrayList<>();

    private userServices userService = new userServices();
    private user currentUser;

    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList(Gender.values()));
        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        
        specialtyCombo.setItems(FXCollections.observableArrayList(
            "Généraliste", "Cardiologue", "Dermatologue", "Pédiatre", 
            "Neurologue", "Ophtalmologue", "Dentiste", "Gynécologue", 
            "Orthopédiste", "Psychiatre", "Autre"
        ));
        statusCombo.setItems(FXCollections.observableArrayList(AccountStatus.values()));

        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldsForRole(newVal);
        });
    }

    public void setUser(user u) {
        this.currentUser = u;

        firstNameField.setText(u.getFirstName());
        lastNameField.setText(u.getLastName());
        emailField.setText(u.getEmail());
        passwordField.setText(u.getPassword());
        phoneField.setText(u.getPhone());
        ageField.setText(String.valueOf(u.getAge()));
        genderCombo.setValue(u.getGender());
        addressArea.setText(u.getAddress());
        roleCombo.setValue(u.getRole());
        statusCombo.setValue(u.getAccountStatus());

        loadUserPhoto();

        if (u.getRole() == Role.doctor) {
            specialtyCombo.setValue(u.getSpecialty());
            orderNumberField.setText(u.getOrderNumber());
            consultationFeeField.setText(String.valueOf(u.getConsultationFee()));
        } else if (u.getRole() == Role.pharmacist) {
            pharmacyNameField.setText(u.getPharmacyName());
            licenceNumberField.setText(u.getLicenceNumber());
        } else if (u.getRole() == Role.assistant) {
            loadDoctorsForAssistant();
            // Find doctor and select it
            for (int i = 0; i < allDoctors.size(); i++) {
                if (allDoctors.get(i).getUserId() == u.getDoctorId()) {
                    doctorSelectionCombo.getSelectionModel().select(i);
                    break;
                }
            }
        }

        updateFieldsForRole(u.getRole());
    }

    private void loadUserPhoto() {
        if (currentUser != null && currentUser.getUserPhoto() != null && !currentUser.getUserPhoto().isEmpty()) {
            try {
                String cleanBase64 = currentUser.getUserPhoto().trim()
                        .replaceAll("\\s+", "")
                        .replaceAll("\\n", "")
                        .replaceAll("\\r", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                profileImageView.setImage(image);
                base64Image = currentUser.getUserPhoto();
                photoStatus.setText("Photo chargée");
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/default-avatar.png"));
            if (defaultImage != null) {
                profileImageView.setImage(defaultImage);
            }
        } catch (Exception e) {}
    }

    private void updateFieldsForRole(Role role) {
        doctorFields.setVisible(false);
        doctorFields.setManaged(false);
        pharmacyFields.setVisible(false);
        pharmacyFields.setManaged(false);

        if (role == Role.doctor) {
            doctorFields.setVisible(true);
            doctorFields.setManaged(true);
        } else if (role == Role.pharmacist) {
            pharmacyFields.setVisible(true);
            pharmacyFields.setManaged(true);
        } else if (role == Role.assistant) {
            assistantFields.setVisible(true);
            assistantFields.setManaged(true);
            loadDoctorsForAssistant();
        }
    }

    private void loadDoctorsForAssistant() {
        try {
            List<user> allUsers = userService.getAll();
            allDoctors = allUsers.stream()
                    .filter(u -> u.getRole() != null && u.getRole().name().trim().equals("doctor"))
                    .collect(java.util.stream.Collectors.toList());
            
            javafx.collections.ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (user d : allDoctors) {
                String spec = (d.getSpecialty() != null && !d.getSpecialty().isEmpty()) ? " (" + d.getSpecialty() + ")" : "";
                doctorNames.add("Dr. " + d.getFirstName() + " " + d.getLastName() + spec);
            }
            
            if (doctorSelectionCombo != null) {
                doctorSelectionCombo.setItems(doctorNames);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void choosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Redimensionner l'image à 150x150 pour réduire la taille
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) {
                    photoStatus.setText("Format non supporté");
                    photoStatus.setStyle("-fx-text-fill: #FF5C5C;");
                    return;
                }

                // Redimensionner
                BufferedImage resizedImage = resizeImage(originalImage, 150, 150);

                // Compresser en JPG avec qualité 0.7
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Vérifier la taille (max 500KB)
                if (imageBytes.length > 500 * 1024) {
                    // Recompresser plus fort
                    baos.reset();
                    // Réduire encore la taille
                    BufferedImage smallerImage = resizeImage(originalImage, 100, 100);
                    ImageIO.write(smallerImage, "jpg", baos);
                    imageBytes = baos.toByteArray();
                }

                base64Image = Base64.getEncoder().encodeToString(imageBytes);

                // Afficher l'image redimensionnée
                Image displayImage = new Image(new ByteArrayInputStream(imageBytes));
                profileImageView.setImage(displayImage);

                photoStatus.setText("Photo modifiée (" + (imageBytes.length / 1024) + " KB)");
                photoStatus.setStyle("-fx-text-fill: #48BB78;");

            } catch (IOException e) {
                e.printStackTrace();
                photoStatus.setText("Erreur: " + e.getMessage());
                photoStatus.setStyle("-fx-text-fill: #FF5C5C;");
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    @FXML
    private void updateUser() {
        try {
            currentUser.setFirstName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setPassword(passwordField.getText());
            currentUser.setPhone(phoneField.getText());
            currentUser.setAge(ageField.getText().isEmpty() ? 0 : Integer.parseInt(ageField.getText()));
            currentUser.setGender(genderCombo.getValue());
            currentUser.setAddress(addressArea.getText());
            currentUser.setRole(roleCombo.getValue());
            currentUser.setAccountStatus(statusCombo.getValue());
            currentUser.setUserPhoto(base64Image);

            if (currentUser.getRole() == Role.doctor) {
                currentUser.setSpecialty(specialtyCombo.getValue() != null ? specialtyCombo.getValue() : "");
                currentUser.setOrderNumber(orderNumberField.getText());
                currentUser.setConsultationFee(consultationFeeField.getText().isEmpty() ? 0 : Float.parseFloat(consultationFeeField.getText()));
            } else if (currentUser.getRole() == Role.pharmacist) {
                currentUser.setPharmacyName(pharmacyNameField.getText());
                currentUser.setLicenceNumber(licenceNumberField.getText());
                currentUser.setDoctorId(0);
            } else if (currentUser.getRole() == Role.assistant) {
                currentUser.setPharmacyName("");
                currentUser.setLicenceNumber("");
                int selectedIndex = doctorSelectionCombo.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    currentUser.setDoctorId(allDoctors.get(selectedIndex).getUserId());
                }
            } else {
                currentUser.setDoctorId(0);
            }

            userService.update(currentUser);
            showAlert("Succès", "Utilisateur modifié !");
            goBack();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification");
        }
    }

    @FXML
    private void goBack() {
        Main.showAdminUtilisateurs();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}