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

public class AdminAddUserController {

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

    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList(Gender.values()));
        genderCombo.setValue(Gender.M);
        
        specialtyCombo.setItems(FXCollections.observableArrayList(
            "Généraliste", "Cardiologue", "Dermatologue", "Pédiatre", 
            "Neurologue", "Ophtalmologue", "Dentiste", "Gynécologue", 
            "Orthopédiste", "Psychiatre", "Autre"
        ));

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.setValue(Role.patient);
        statusCombo.setItems(FXCollections.observableArrayList(AccountStatus.values()));
        statusCombo.setValue(AccountStatus.active);

        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldsForRole(newVal);
        });

        updateFieldsForRole(Role.patient);
        setDefaultImage();
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
                if (!doctorNames.isEmpty() && doctorSelectionCombo.getSelectionModel().isEmpty()) {
                    doctorSelectionCombo.getSelectionModel().selectFirst();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) {
                    photoStatus.setText("Format non supporté");
                    photoStatus.setStyle("-fx-text-fill: #FF5C5C;");
                    return;
                }

                // Redimensionner à 150x150
                BufferedImage resizedImage = resizeImage(originalImage, 150, 150);

                // Convertir en JPG compressé
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                base64Image = Base64.getEncoder().encodeToString(imageBytes);

                Image displayImage = new Image(new ByteArrayInputStream(imageBytes));
                profileImageView.setImage(displayImage);

                photoStatus.setText("Photo sélectionnée (" + (imageBytes.length / 1024) + " KB)");
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
    private void saveUser() {
        try {
            user newUser = new user();
            newUser.setFirstName(firstNameField.getText());
            newUser.setLastName(lastNameField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setPassword(passwordField.getText());
            newUser.setPhone(phoneField.getText());
            newUser.setAge(ageField.getText().isEmpty() ? 0 : Integer.parseInt(ageField.getText()));
            newUser.setGender(genderCombo.getValue());
            newUser.setAddress(addressArea.getText());
            newUser.setRole(roleCombo.getValue());
            newUser.setAccountStatus(statusCombo.getValue());
            newUser.setUserPhoto(base64Image);

            if (newUser.getRole() == Role.doctor) {
                newUser.setSpecialty(specialtyCombo.getValue() != null ? specialtyCombo.getValue() : "");
                newUser.setOrderNumber(orderNumberField.getText());
                newUser.setConsultationFee(consultationFeeField.getText().isEmpty() ? 0 : Float.parseFloat(consultationFeeField.getText()));
                newUser.setPharmacyName("");
                newUser.setLicenceNumber("");
            } else if (newUser.getRole() == Role.pharmacist) {
                newUser.setSpecialty("");
                newUser.setOrderNumber("");
                newUser.setConsultationFee(0);
                newUser.setPharmacyName(pharmacyNameField.getText());
                newUser.setLicenceNumber(licenceNumberField.getText());
                newUser.setDoctorId(0);
            } else if (newUser.getRole() == Role.assistant) {
                newUser.setSpecialty("");
                newUser.setOrderNumber("");
                newUser.setConsultationFee(0);
                newUser.setPharmacyName("");
                newUser.setLicenceNumber("");
                int selectedIndex = doctorSelectionCombo.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    newUser.setDoctorId(allDoctors.get(selectedIndex).getUserId());
                }
            } else {
                newUser.setSpecialty("");
                newUser.setOrderNumber("");
                newUser.setConsultationFee(0);
                newUser.setPharmacyName("");
                newUser.setLicenceNumber("");
                newUser.setDoctorId(0);
            }

            userService.add(newUser);
            showAlert("Succès", "Utilisateur ajouté !");
            goBack();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout");
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
