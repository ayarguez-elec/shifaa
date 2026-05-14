package Controler;

import entities.Reclamation;
import entities.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import services.ReponseServices;

import java.net.URL;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for adminajouterreponse.fxml
 *
 * fx:id mapping (must match FXML exactly):
 *   lblAvatar      → avatar circle letter
 *   lblUserName    → user full name
 *   lblRecInfo     → "REC #X · date · Priorité · Statut"
 *   lblRecSubject  → subject text
 *   taContent      → TextArea for the response
 *   lblMessage     → feedback label after save
 *
 * Buttons use onAction in FXML:
 *   #enregistrerReponse
 *   #annuler
 */
public class AdminAjouterReponseController implements Initializable {

    // ── FXML fields — names must match fx:id in adminajouterreponse.fxml ──────
    @FXML private Label   lblAvatar;
    @FXML private Label   lblUserName;
    @FXML private Label   lblRecInfo;
    @FXML private Label   lblRecSubject;
    @FXML private TextArea taContent;
    @FXML private Label   lblMessage;

    // We grab any button in the scene to call getScene().getWindow() on close.
    // Using taContent works too — any node that is always present.

    private Reclamation reclamation;
    private final ReponseServices reponseSvc = new ReponseServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Nothing needed — data injected via initData()
    }

    /**
     * Called by AdminDashboardController / AdminNotificationsController
     * AFTER the FXMLLoader has loaded the FXML, to inject context data.
     */
    public void initData(Reclamation rec, String userName) {
        this.reclamation = rec;

        // Avatar: first letter of user name
        String name = (userName != null && !userName.isBlank()) ? userName : "?";
        if (lblAvatar != null) {
            lblAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        if (lblUserName != null) {
            lblUserName.setText(name);
        }

        if (lblRecInfo != null) {
            String dateStr = rec.getReclamDate() != null
                    ? rec.getReclamDate().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "—";
            lblRecInfo.setText(
                    "REC #" + rec.getReclamationId()
                            + "  ·  " + dateStr
                            + "  ·  " + nvl(rec.getPriority())
                            + "  ·  " + nvl(rec.getStatusreclam())
            );
        }

        if (lblRecSubject != null) {
            lblRecSubject.setText(nvl(rec.getSubject()));
        }

        if (lblMessage != null) {
            lblMessage.setText("");
        }
    }

    // ── ACTIONS ───────────────────────────────────────────────────────────────

    @FXML
    private void enregistrerReponse() {
        // Guard: controller must have been initialised with a reclamation
        if (reclamation == null) {
            setMessage("❌ Erreur interne : réclamation non chargée.", false);
            return;
        }

        String content = taContent.getText();
        if (content == null || content.trim().isEmpty()) {
            setMessage("⚠ Veuillez saisir une réponse avant d'enregistrer.", false);
            return;
        }

        Reponse reponse = new Reponse();
        reponse.setContent(content.trim());
        reponse.setResponseDate(new Timestamp(System.currentTimeMillis()));
        reponse.setAdminId(tests.Main.getCurrentUserId()); 
        reponse.setReclamationId(reclamation.getReclamationId());

        try {
            reponseSvc.add(reponse);
            setMessage("✅ Réponse enregistrée avec succès !", true);

            // Close the dialog after a brief visual confirmation
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.8));
            pause.setOnFinished(ev -> fermerDialog());
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            setMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void annuler() {
        fermerDialog();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void fermerDialog() {
        // Use taContent (always present) to walk up to the Stage
        Stage stage = (Stage) taContent.getScene().getWindow();
        stage.close();
    }

    private void setMessage(String text, boolean success) {
        if (lblMessage == null) return;
        lblMessage.setText(text);
        lblMessage.setStyle(success
                ? "-fx-text-fill:#3B6D11; -fx-font-size:12px;"
                : "-fx-text-fill:#A32D2D; -fx-font-size:12px;");
    }

    private String nvl(String s) {
        return s != null ? s : "—";
    }
}
