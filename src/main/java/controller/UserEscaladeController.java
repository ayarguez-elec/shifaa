package controller;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import services.EscalationService;
import services.NotificationService;

/**
 * UserEscaladeController
 * Bound to: userescalade.fxml (modal dialog)
 *
 * FIXED:
 * - Uses existing EscalationService.add() method (3 params)
 * - Uses existing NotificationService.triggerEscaladeManuelle() method
 * - Stores urgency in reason text for backward compatibility
 * - Properly separates admin alert from user confirmation
 */
public class UserEscaladeController {

    @FXML private Label    lblSubject;
    @FXML private Label    lblMeta;
    @FXML private TextArea txtReason;
    @FXML private Slider   sliderUrgence;   // 0–10 integer slider
    @FXML private Label    lblUrgenceValue; // shows the current integer value
    @FXML private Label    lblError;
    @FXML private Button   btnSubmit;
    @FXML private Button   btnCancel;

    private Reclamation     reclamation;
    private int             ownerUserId;
    private boolean         submitted = false;

    private final EscalationService escalationSvc = new EscalationService();

    // ── CALLED BY PARENT BEFORE showAndWait() ────────────────────────────────

    public void setData(Reclamation r, int userId) {
        this.reclamation = r;
        this.ownerUserId = userId;

        if (lblSubject != null) {
            lblSubject.setText(r.getSubject());
        }
        if (lblMeta != null) {
            lblMeta.setText(
                    "REC #" + r.getReclamationId()
                            + "  ·  Priorité : " + r.getPriority()
                            + "  ·  Date : " + r.getReclamDate()
            );
        }
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        // Configure slider: integers 0–10, default = 5
        if (sliderUrgence != null) {
            sliderUrgence.setMin(0);
            sliderUrgence.setMax(10);
            sliderUrgence.setValue(5);
            sliderUrgence.setMajorTickUnit(1);
            sliderUrgence.setMinorTickCount(0);
            sliderUrgence.setSnapToTicks(true);
            sliderUrgence.setShowTickMarks(true);
            sliderUrgence.setShowTickLabels(true);
            sliderUrgence.setBlockIncrement(1);

            updateUrgenceLabel((int) sliderUrgence.getValue());
            sliderUrgence.valueProperty().addListener((obs, oldVal, newVal) -> {
                int intVal = (int) Math.round(newVal.doubleValue());
                sliderUrgence.setValue(intVal);
                updateUrgenceLabel(intVal);
            });
        }
    }

    private void updateUrgenceLabel(int value) {
        if (lblUrgenceValue == null) return;
        String label = value + "/10";
        if (value >= 8) {
            label += "  🔴 Critique";
            lblUrgenceValue.setStyle("-fx-text-fill:#A32D2D; -fx-font-weight:bold;");
        } else if (value >= 5) {
            label += "  🟠 Élevé";
            lblUrgenceValue.setStyle("-fx-text-fill:#854F0B; -fx-font-weight:bold;");
        } else {
            label += "  🟡 Modéré";
            lblUrgenceValue.setStyle("-fx-text-fill:#5d6f91; -fx-font-weight:bold;");
        }
        lblUrgenceValue.setText(label);
    }

    public boolean isSubmitted() { return submitted; }

    // ── ACTIONS ──────────────────────────────────────────────────────────────

    @FXML
    private void handleSubmit() {
        String reason = txtReason.getText();

        if (reason == null || reason.trim().isEmpty()) {
            showError("Veuillez saisir une raison.");
            return;
        }
        if (reason.trim().length() < 10) {
            showError("La raison doit contenir au moins 10 caractères.");
            return;
        }
        if (escalationSvc.hasOpenEscalation(reclamation.getReclamationId())) {
            showError("Une demande d'escalade est déjà en cours pour cette réclamation.");
            return;
        }

        // Get urgency level from slider (default 5 if slider absent)
        int urgenceLevel = 5;
        if (sliderUrgence != null) {
            urgenceLevel = (int) Math.round(sliderUrgence.getValue());
        }

        // Build the reason string WITH urgency embedded (for backward compatibility)
        String fullReason = reason.trim() + " [Urgence : " + urgenceLevel + "/10]";

        // ── Persist escalation using EXISTING method signature ──────────
        // escalationSvc.add(reclamationId, userId, reason)
        escalationSvc.add(reclamation.getReclamationId(), ownerUserId, fullReason);

        // ── Notify admin using EXISTING method ───────────────────────────
        // NotificationService.triggerEscaladeManuelle(recId, userId, subject, fullReason)
        NotificationService.triggerEscaladeManuelle(
                reclamation.getReclamationId(),
                ownerUserId,
                reclamation.getSubject(),
                fullReason
        );

        submitted = true;
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    private void close() {
        Stage stage = null;
        if (btnCancel != null && btnCancel.getScene() != null) {
            stage = (Stage) btnCancel.getScene().getWindow();
        } else if (btnSubmit != null && btnSubmit.getScene() != null) {
            stage = (Stage) btnSubmit.getScene().getWindow();
        }
        if (stage != null) stage.close();
    }
}