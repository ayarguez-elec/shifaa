package controller;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.GeminiModerationService;
import services.ReclamationServices;
import services.ModerationResult;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserAddReclamationController {

    @FXML private TextField subjectField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private Label messageLabel;

    // ==================== ATTACHMENTS ====================
    @FXML private Button btnAddAttachment;
    @FXML private FlowPane attachmentsPreview;

    private final ReclamationServices reclamationServices = new ReclamationServices();
    private final List<File> selectedFiles = new ArrayList<>();

    private final int currentUserId = 1;
    private boolean submitted = false;

    @FXML
    public void initialize() {
        priorityCombo.getItems().addAll("faible", "moyenne", "urgente");
        priorityCombo.setValue("moyenne");

        if (btnAddAttachment != null) {
            btnAddAttachment.setOnAction(e -> chooseAttachments());
        }
    }

    private void chooseAttachments() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des fichiers (images ou documents)");

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp");

        FileChooser.ExtensionFilter docFilter =
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt");

        FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, docFilter, allFilter);

        // ✅ Correct method name
        fileChooser.setInitialFileName(String.valueOf(true));

        List<File> files = fileChooser.showOpenMultipleDialog(
                (Stage) subjectField.getScene().getWindow());

        if (files != null) {
            for (File file : files) {
                if (file.length() > 15 * 1024 * 1024) { // 15MB limit
                    showWarning("Fichier trop volumineux",
                            file.getName() + " dépasse la limite de 15MB.");
                    continue;
                }
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    addFileToPreview(file);
                }
            }
        }
    }

    private void addFileToPreview(File file) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(6, 10, 6, 10));
        item.setStyle("-fx-background-color: #f0f4fa; -fx-background-radius: 8;");

        Label nameLabel = new Label("📎 " + file.getName());
        nameLabel.setStyle("-fx-font-size: 12px;");

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand;");

        removeBtn.setOnAction(e -> {
            selectedFiles.remove(file);
            attachmentsPreview.getChildren().remove(item);
        });

        item.getChildren().addAll(nameLabel, removeBtn);
        attachmentsPreview.getChildren().add(item);
    }

    @FXML
    private void ajouterReclamation() {
        String subject = subjectField.getText().trim();
        String description = descriptionField.getText().trim();
        String priority = priorityCombo.getValue();

        if (subject.isEmpty() || subject.length() < 3) {
            showWarning("Sujet invalide", "Le sujet doit contenir au moins 3 caractères.");
            subjectField.requestFocus();
            return;
        }
        if (description.isEmpty() || description.length() < 10) {
            showWarning("Description invalide", "La description doit contenir au moins 10 caractères.");
            descriptionField.requestFocus();
            return;
        }
        if (priority == null || priority.isEmpty()) {
            showWarning("Priorité requise", "Veuillez choisir une priorité.");
            return;
        }

        // Moderation
        try {
            GeminiModerationService moderation = new GeminiModerationService();
            ModerationResult result = moderation.checkReclamation(subject, description);
            if (result.isHarmful()) {
                showWarning("Contenu inapproprié détecté", result.explanation());
                return;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Moderation error (continuing): " + e.getMessage());
        }

        // Create & Save Reclamation
        Reclamation reclamation = new Reclamation(
                subject, description, Date.valueOf(LocalDate.now()),
                "En attente", priority, currentUserId);

        reclamationServices.add(reclamation);

        // Save Attachments
        if (!selectedFiles.isEmpty()) {
            reclamationServices.addAttachments(reclamation.getReclamationId(), selectedFiles);
        }

        submitted = true;
        showInfo("Succès", "Réclamation ajoutée avec succès !");

        Stage stage = (Stage) subjectField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) subjectField.getScene().getWindow();
        stage.close();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSubmitted() {
        return submitted;
    }
}