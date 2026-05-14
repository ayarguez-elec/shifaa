package Controler;

import entities.Reclamation;
import entities.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import services.ReponseServices;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * STEP 5b — Create this NEW file at:
 * src/main/java/controller/AdminModifierReponseController.java
 *
 * This replaces AdminUpdateReponseController for the new modal flow.
 *
 * HOW IT WORKS:
 * 1. AdminReclamationController opens this dialog
 * 2. initData(reponse, reclamation, userName) pre-fills all fields
 * 3. Admin edits the textarea and saves
 * 4. Dialog closes → parent refreshes cards
 *
 * NOTE: The old AdminUpdateReponseController.java can be DELETED
 * (or kept if used elsewhere). This new controller handles everything.
 */
public class AdminModifierReponseController implements Initializable {

    // ── FXML nodes ─────────────────────────────────────────────────────────
    @FXML private Label    lblResponseMeta;
    @FXML private Label    lblAvatar;
    @FXML private Label    lblUserName;
    @FXML private Label    lblRecInfo;
    @FXML private Label    lblRecSubject;
    @FXML private TextArea taContent;
    @FXML private Label    lblMessage;

    // ── Services ───────────────────────────────────────────────────────────
    private final ReponseServices reponseSvc = new ReponseServices();

    // ── Data ───────────────────────────────────────────────────────────────
    private Reponse     currentReponse;
    private Reclamation currentReclamation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populated by initData()
    }

    /**
     * Called by AdminReclamationController right after loading this dialog.
     *
     * @param rep      the existing response to edit
     * @param r        the reclamation this response belongs to
     * @param userName the display name of the user who filed the reclamation
     */
    public void initData(Reponse rep, Reclamation r, String userName) {
        this.currentReponse     = rep;
        this.currentReclamation = r;

        // Subtitle: Response ID + original response date
        lblResponseMeta.setText(
                "Response #" + rep.getResponseId() +
                        "  ·  Répondu le " + rep.getResponseDate()
        );

        // Avatar initials
        String initials = buildInitials(userName);
        lblAvatar.setText(initials);
        lblAvatar.setStyle(
                "-fx-background-color:" + avatarBg(r.getUserId()) + ";" +
                        "-fx-text-fill:"        + avatarFg(r.getUserId()) + ";" +
                        "-fx-background-radius:20;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-pref-width:40;" +
                        "-fx-pref-height:40;" +
                        "-fx-min-width:40;" +
                        "-fx-min-height:40;" +
                        "-fx-alignment:CENTER;"
        );

        lblUserName.setText(userName);

        lblRecInfo.setText(
                "REC #" + r.getReclamationId() +
                        "  ·  " + r.getReclamDate() +
                        "  ·  " + r.getPriority() +
                        "  ·  " + r.getStatusreclam()
        );

        lblRecSubject.setText("\"" + r.getSubject() + "\"");

        // Pre-fill the textarea with existing response content
        taContent.setText(rep.getContent());
    }

    // ── SAVE MODIFICATION ─────────────────────────────────────────────────

    @FXML
    private void enregistrerModification() {
        String content = taContent.getText().trim();
        if (content.isEmpty()) {
            showMessage("⚠ Le contenu de la réponse ne peut pas être vide.", false);
            return;
        }

        // Update the response object
        currentReponse.setContent(content);
        currentReponse.setResponseDate(Timestamp.valueOf(LocalDateTime.now()));
        // Save to DB
        reponseSvc.update(currentReponse);

        showMessage("✅ Réponse modifiée avec succès !", true);

        // Close the dialog
        closeDialog();
    }

    // ── CANCEL ────────────────────────────────────────────────────────────

    @FXML
    private void annuler() {
        closeDialog();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private void closeDialog() {
        Stage stage = (Stage) taContent.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String msg, boolean success) {
        lblMessage.setText(msg);
        lblMessage.setStyle(
                success
                        ? "-fx-text-fill:#3B6D11; -fx-font-size:13px;"
                        : "-fx-text-fill:#A32D2D; -fx-font-size:13px;"
        );
    }

    private String buildInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].length() >= 2
                    ? parts[0].substring(0, 2).toUpperCase()
                    : parts[0].toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String avatarBg(int userId) {
        String[] c = {"#E6F1FB","#EAF3DE","#FAEEDA","#FCEBEB","#EEEDFE","#E1F5EE"};
        return c[userId % c.length];
    }

    private String avatarFg(int userId) {
        String[] c = {"#185FA5","#3B6D11","#854F0B","#A32D2D","#534AB7","#0F6E56"};
        return c[userId % c.length];
    }
}
