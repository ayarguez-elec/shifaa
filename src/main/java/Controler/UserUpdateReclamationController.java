package Controler;

import entities.Attachment;
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
import java.util.ArrayList;
import java.util.List;

public class UserUpdateReclamationController {

    @FXML private TextField subjectField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private Label messageLabel;

    // ==================== ATTACHMENTS ====================
    @FXML private Button btnAddAttachment;
    @FXML private FlowPane attachmentsPreview;

    private final ReclamationServices reclamationServices = new ReclamationServices();
    private final List<File> newFilesToAdd = new ArrayList<>();
    private List<Attachment> existingAttachments = new ArrayList<>();

    private Reclamation reclamation;
    private boolean submitted = false;

    @FXML
    public void initialize() {
        priorityCombo.getItems().addAll("faible", "moyenne", "urgente");

        reclamation = tests.Main.selectedReclamation;

        if (reclamation != null) {
            subjectField.setText(reclamation.getSubject());
            descriptionField.setText(reclamation.getDescription());
            priorityCombo.setValue(reclamation.getPriority() != null ? reclamation.getPriority() : "moyenne");

            loadExistingAttachments();
        }

        if (btnAddAttachment != null) {
            btnAddAttachment.setOnAction(e -> chooseNewAttachments());
        }
    }

    private void loadExistingAttachments() {
        existingAttachments = reclamationServices.getAttachments(reclamation.getReclamationId());
        attachmentsPreview.getChildren().clear();

        for (Attachment att : existingAttachments) {
            addExistingAttachmentToPreview(att);
        }
    }

    private void addExistingAttachmentToPreview(Attachment att) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(6, 10, 6, 10));
        item.setStyle("-fx-background-color: #f0f4fa; -fx-background-radius: 8;");

        Label name = new Label("📎 " + att.getFileName());
        Button remove = new Button("🗑");
        remove.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        remove.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Supprimer ce fichier définitivement ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    reclamationServices.deleteAttachment(att.getId());
                    attachmentsPreview.getChildren().remove(item);
                    existingAttachments.remove(att);
                }
            });
        });

        item.getChildren().addAll(name, remove);
        attachmentsPreview.getChildren().add(item);
    }

    private void chooseNewAttachments() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ajouter de nouvelles pièces jointes");

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp");
        FileChooser.ExtensionFilter docFilter =
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt");
        FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, docFilter, allFilter);
        fileChooser.setInitialFileName(String.valueOf(true));   // ← Correct method

        List<File> files = fileChooser.showOpenMultipleDialog(
                (Stage) subjectField.getScene().getWindow());

        if (files != null) {
            for (File file : files) {
                if (file.length() > 15 * 1024 * 1024) { // 15MB
                    showWarning("Fichier trop volumineux",
                            file.getName() + " dépasse la limite de 15MB.");
                    continue;
                }
                if (!newFilesToAdd.contains(file)) {
                    newFilesToAdd.add(file);
                    addNewFileToPreview(file);
                }
            }
        }
    }

    private void addNewFileToPreview(File file) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(6, 10, 6, 10));
        item.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8;");

        Label name = new Label("📎 " + file.getName() + " (nouveau)");
        Button remove = new Button("✕");
        remove.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        remove.setOnAction(e -> {
            newFilesToAdd.remove(file);
            attachmentsPreview.getChildren().remove(item);
        });

        item.getChildren().addAll(name, remove);
        attachmentsPreview.getChildren().add(item);
    }

    @FXML
    private void modifierReclamation() {
        String subject = subjectField.getText().trim();
        String description = descriptionField.getText().trim();
        String priority = priorityCombo.getValue();

        if (subject.isEmpty() || subject.length() < 3) {
            showWarning("Sujet invalide", "Le sujet doit contenir au moins 3 caractères.");
            return;
        }
        if (description.isEmpty() || description.length() < 10) {
            showWarning("Description invalide", "La description doit contenir au moins 10 caractères.");
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
            System.err.println("Moderation error: " + e.getMessage());
        }

        // Update reclamation
        reclamation.setSubject(subject);
        reclamation.setDescription(description);
        reclamation.setPriority(priority);
        reclamationServices.update(reclamation);

        // Add new attachments
        if (!newFilesToAdd.isEmpty()) {
            reclamationServices.addAttachments(reclamation.getReclamationId(), newFilesToAdd);
        }

        submitted = true;
        showInfo("Succès", "Réclamation modifiée avec succès !");
        tests.Main.clearSelection();
        ((Stage) subjectField.getScene().getWindow()).close();
    }

    @FXML
    private void annuler() {
        tests.Main.clearSelection();
        ((Stage) subjectField.getScene().getWindow()).close();
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
