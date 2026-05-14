package Controler;

import entities.Reclamation;
import entities.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ReclamationServices;
import services.ReponseServices;
import services.userServices;
import tests.Main;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * FIXED AdminReclamationController
 * - "Répondre" button now opens modal dialog with proper error handling
 * - Added "Supprimer la réponse" button when response exists
 * - Added scroll-to-reclamation support from notifications
 */
public class AdminReclamationController implements Initializable {

    // ── FXML nodes ─────────────────────────────────────────────────────────
    @FXML private TextField   searchField;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private VBox        cardsContainer;
    @FXML private Label       lblTotal;

    // ── Services ───────────────────────────────────────────────────────────
    private final ReclamationServices reclamationSvc = new ReclamationServices();
    private final ReponseServices     reponseSvc     = new ReponseServices();
    private final userServices         userSvc        = new userServices();

    private List<Reclamation> allReclamations;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboStatut.getItems().addAll(
                "Tous les statuts", "En attente", "En cours", "Résolu"
        );
        comboStatut.getSelectionModel().selectFirst();

        comboPriorite.getItems().addAll(
                "Toutes les priorités", "Urgente", "Moyenne", "Faible"
        );
        comboPriorite.getSelectionModel().selectFirst();

        loadAll();
    }

    // ── LOAD & BUILD ─────────────────────────────────────────────────────

    private void loadAll() {
        allReclamations = reclamationSvc.getAll();
        buildCards(allReclamations);

        // Handle navigation from notifications with target reclamation
        if (tests.Main.selectedReclamationId != 0) {
            final int targetId = tests.Main.selectedReclamationId;
            tests.Main.selectedReclamationId = 0; // clear immediately
            javafx.application.Platform.runLater(() -> scrollToReclamation(targetId));
        }
    }

    private void buildCards(List<Reclamation> list) {
        cardsContainer.getChildren().clear();
        lblTotal.setText("Total : " + list.size() + " réclamation(s)");

        if (list.isEmpty()) {
            Label empty = new Label("Aucune réclamation trouvée.");
            empty.setStyle("-fx-text-fill:#5d6f91; -fx-font-size:14px; -fx-padding:30;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (Reclamation r : list) {
            VBox card = buildOneCard(r);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox buildOneCard(Reclamation r) {
        List<Reponse> reponses = reponseSvc.getByReclamation(r.getReclamationId());
        Reponse existingReponse = reponses.isEmpty() ? null : reponses.get(0);

        VBox card = new VBox(0);
        card.setStyle(buildCardStyle(r, existingReponse));
        card.setUserData(r.getReclamationId()); // ← store ID for scroll-to feature

        // ── HEADER ────────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.TOP_LEFT);
        header.setPadding(new Insets(14, 18, 10, 18));

        String initials = userSvc.getInitials(r.getUserId());
        Label avatar = buildAvatar(initials, r.getUserId());

        VBox meta = new VBox(2);
        HBox.setHgrow(meta, Priority.ALWAYS);

        String userName = userSvc.getUserName(r.getUserId());
        Label lblUser = new Label(userName);
        lblUser.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0b1f44;");

        Label lblIdDate = new Label(
                "REC #" + r.getReclamationId() + "  ·  " + r.getReclamDate()
        );
        lblIdDate.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

        meta.getChildren().addAll(lblUser, lblIdDate);

        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER_RIGHT);
        badges.getChildren().addAll(
                buildStatusBadge(r.getStatusreclam()),
                buildPriorityBadge(r.getPriority()),
                buildCategoryBadge(r.getCategory()),
                buildEscaladeBadge(r)
        );

        header.getChildren().addAll(avatar, meta, badges);

        // ── SUBJECT & DESCRIPTION ───────────────────────────────────────
        Label lblSubject = new Label(r.getSubject());
        lblSubject.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0b1f44; -fx-padding:0 18 4 18;");
        lblSubject.setWrapText(true);

        Label lblDesc = new Label(r.getDescription());
        lblDesc.setStyle("-fx-font-size:13px; -fx-text-fill:#5d6f91; -fx-padding:0 18 12 18;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(900);

        Separator divider = new Separator();
        divider.setPadding(new Insets(0, 18, 0, 18));

        // ── RESPONSE ZONE ─────────────────────────────────────────────────
        VBox responseZone = buildResponseZone(r, existingReponse, userName);

        card.getChildren().addAll(header, lblSubject, lblDesc, divider, responseZone);
        return card;
    }

    /**
     * Builds the bottom zone of the card with status dropdown + response actions.
     */
    private VBox buildResponseZone(Reclamation r, Reponse rep, String userName) {
        VBox zone = new VBox(8);
        zone.setPadding(new Insets(12, 18, 14, 18));
        zone.setStyle("-fx-background-color:#f9fbff; -fx-background-radius:0 0 16 16;");

        // ── Status selector row ─────────────────────────────────────────
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#5d6f91;");

        ComboBox<String> statusCombo = new ComboBox<String>();
        statusCombo.getItems().addAll("En attente", "En cours", "Résolu");
        statusCombo.setValue(r.getStatusreclam());
        statusCombo.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#dce5f0;" +
                        "-fx-border-radius:6;" +
                        "-fx-background-radius:6;" +
                        "-fx-font-size:12px;" +
                        "-fx-pref-width:130;"
        );

        // Save button (appears when status changes)
        Button btnSaveStatus = new Button("✓");
        btnSaveStatus.setStyle(
                "-fx-background-color:#378ADD;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:11px;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:4 10 4 10;" +
                        "-fx-cursor:hand;"
        );
        btnSaveStatus.setVisible(false);
        btnSaveStatus.setManaged(false);

        // Automatic status update on change
        statusCombo.setOnAction(e -> {
            String newStatus = statusCombo.getValue();
            if (newStatus != null && !newStatus.equals(r.getStatusreclam())) {
                reclamationSvc.updateStatus(r.getReclamationId(), newStatus);
                r.setStatusreclam(newStatus); // update local object
                
                // Show small feedback and refresh
                System.out.println("✅ Status updated to " + newStatus);
                loadAll(); 
            }
        });

        statusRow.getChildren().addAll(statusLabel, statusCombo);

        // ── Response zone label ─────────────────────────────────────────
        HBox zoneLabel = new HBox(6);
        zoneLabel.setAlignment(Pos.CENTER_LEFT);
        Label iconReply = new Label("↩ Réponse admin");
        iconReply.setStyle(
                "-fx-font-size:11px; -fx-font-weight:bold;" +
                        "-fx-text-fill:#9aabb5; -fx-letter-spacing:0.05em;"
        );
        zoneLabel.getChildren().add(iconReply);

        if (rep != null) {
            // ── Response EXISTS ──────────────────────────────────────
            Label lblContent = new Label(rep.getContent());
            lblContent.setStyle("-fx-font-size:13px; -fx-text-fill:#0b1f44; -fx-wrap-text:true;");
            lblContent.setWrapText(true);

            Label lblMeta = new Label(
                    "Répondu le " + rep.getResponseDate() +
                            "  ·  Admin  ·  Response #" + rep.getResponseId()
            );
            lblMeta.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

            // Buttons row: Modifier + Supprimer
            HBox btnRow = new HBox(10);
            btnRow.setAlignment(Pos.CENTER_LEFT);

            Button btnModifier = new Button("✏ Modifier la réponse");
            btnModifier.setStyle(
                    "-fx-background-color:white;" +
                            "-fx-border-color:#dce5f0;" +
                            "-fx-border-radius:8;" +
                            "-fx-background-radius:8;" +
                            "-fx-font-size:12px;" +
                            "-fx-padding:7 14 7 14;" +
                            "-fx-cursor:hand;" +
                            "-fx-text-fill:#0b1f44;"
            );
            btnModifier.setOnAction(e -> ouvrirModifierReponse(rep, r, userName));

            Button btnSupprimer = new Button("🗑 Supprimer");
            btnSupprimer.setStyle(
                    "-fx-background-color:#FCEBEB;" +
                            "-fx-text-fill:#A32D2D;" +
                            "-fx-font-size:12px;" +
                            "-fx-background-radius:8;" +
                            "-fx-padding:7 14 7 14;" +
                            "-fx-cursor:hand;" +
                            "-fx-font-weight:bold;"
            );
            btnSupprimer.setOnAction(e -> supprimerReponse(rep, r));

            btnRow.getChildren().addAll(btnModifier, btnSupprimer);

            zone.getChildren().addAll(statusRow, zoneLabel, lblContent, lblMeta, btnRow);

        } else {
            // ── No response yet ────────────────────────────────────────
            Label lblNoReponse = new Label("Aucune réponse envoyée pour le moment.");
            lblNoReponse.setStyle(
                    "-fx-font-size:13px; -fx-text-fill:#9aabb5; -fx-font-style:italic;"
            );

            Button btnRepondre = new Button("➤ Répondre");
            btnRepondre.setStyle(
                    "-fx-background-color:#378ADD;" +
                            "-fx-text-fill:white;" +
                            "-fx-font-size:13px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-background-radius:8;" +
                            "-fx-padding:8 18 8 18;" +
                            "-fx-cursor:hand;"
            );
            btnRepondre.setOnAction(e -> ouvrirAjouterReponse(r, userName));

            zone.getChildren().addAll(statusRow, zoneLabel, lblNoReponse, btnRepondre);
        }

        return zone;
    }

    // ── SCROLL TO RECLAMATION (from notifications) ───────────────────────────

    /**
     * Scrolls to and highlights the card matching the given reclamation ID.
     * Called when navigating from AdminNotificationsController.
     */
    private void scrollToReclamation(int targetId) {
        for (javafx.scene.Node node : cardsContainer.getChildren()) {
            if (node instanceof VBox card) {
                Integer cardId = (Integer) card.getUserData();
                if (cardId != null && cardId == targetId) {
                    // Highlight with blue border + glow
                    String highlightStyle =
                            "-fx-background-color:white;" +
                                    "-fx-background-radius:16;" +
                                    "-fx-border-radius:16;" +
                                    "-fx-border-color:#378ADD;" +
                                    "-fx-border-width:3;" +
                                    "-fx-effect:dropshadow(gaussian,rgba(55,138,221,0.4),20,0,0,4);";

                    card.setStyle(highlightStyle);

                    // Scroll into view
                    ScrollPane scrollPane = findScrollPane(card);
                    if (scrollPane != null) {
                        double contentHeight = cardsContainer.getBoundsInParent().getHeight();
                        double cardY = card.getBoundsInParent().getMinY();
                        double viewportHeight = scrollPane.getViewportBounds().getHeight();

                        double vValue = 0;
                        if (contentHeight > viewportHeight) {
                            vValue = cardY / (contentHeight - viewportHeight);
                            vValue = Math.max(0, Math.min(1, vValue));
                        }
                        scrollPane.setVvalue(vValue);
                    }

                    // Remove highlight after 3 seconds
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                    pause.setOnFinished(e -> {
                        // Rebuild card normally by reloading
                        loadAll();
                    });
                    pause.play();

                    break;
                }
            }
        }
    }

    /**
     * Walks up the scene graph to find the parent ScrollPane.
     */
    private ScrollPane findScrollPane(javafx.scene.Node node) {
        javafx.scene.Node parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                return (ScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    // ── DIALOG: AJOUTER RÉPONSE ───────────────────────────────────────────

    /**
     * FIXED: Opens add response modal with proper URL validation
     */
    private void ouvrirAjouterReponse(Reclamation r, String userName) {
        try {
            URL fxmlUrl = getClass().getResource("/adminajouterreponse.fxml");

            if (fxmlUrl == null) {
                showError("Fichier adminajouterreponse.fxml introuvable.\n" +
                        "Vérifiez que le fichier existe dans src/main/resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminAjouterReponseController ctrl = loader.getController();
            if (ctrl == null) {
                showError("Erreur: le contrôleur du dialog n'a pas été initialisé.");
                return;
            }

            ctrl.initData(r, userName);

            Stage dialog = new Stage();
            dialog.setTitle("Répondre à la réclamation");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            // Refresh after dialog closes
            loadAll();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire de réponse:\n" + e.getMessage());
        }
    }

    // ── DIALOG: MODIFIER RÉPONSE ──────────────────────────────────────────

    private void ouvrirModifierReponse(Reponse rep, Reclamation r, String userName) {
        try {
            URL fxmlUrl = getClass().getResource("/adminmodifierreponse.fxml");

            if (fxmlUrl == null) {
                showError("Fichier adminmodifierreponse.fxml introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminModifierReponseController ctrl = loader.getController();
            if (ctrl == null) {
                showError("Erreur: le contrôleur du dialog n'a pas été initialisé.");
                return;
            }

            ctrl.initData(rep, r, userName);

            Stage dialog = new Stage();
            dialog.setTitle("Modifier la réponse");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            loadAll();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire de modification:\n" + e.getMessage());
        }
    }

    // ── NEW: DELETE RESPONSE ──────────────────────────────────────────────

    /**
     * Deletes a response after confirmation dialog.
     * Updates reclamation status back to "En attente" after deletion.
     */
    private void supprimerReponse(Reponse rep, Reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la réponse ?");
        confirm.setContentText(
                "Cette action est irréversible.\n" +
                        "La réponse #" + rep.getResponseId() + " sera supprimée " +
                        "et le statut de la réclamation reviendra à \"En attente\"."
        );

        ButtonType btnOui = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnNon);

        confirm.showAndWait().ifPresent(result -> {
            if (result == btnOui) {
                try {
                    // Delete the response
                    reponseSvc.delete(rep.getResponseId());

                    // Update reclamation status back to "En attente"
                    reclamationSvc.updateStatus(r.getReclamationId(), "En attente");

                    // Refresh cards
                    loadAll();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression:\n" + e.getMessage());
                }
            }
        });
    }

    // ── FILTERS ───────────────────────────────────────────────────────────

    @FXML
    private void applyFilters() {
        if (allReclamations == null) return;

        String search   = searchField.getText().toLowerCase().trim();
        String statut   = comboStatut.getValue();
        String priorite = comboPriorite.getValue();

        List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> {
                    boolean matchSearch = search.isEmpty()
                            || r.getSubject().toLowerCase().contains(search)
                            || r.getDescription().toLowerCase().contains(search)
                            || String.valueOf(r.getUserId()).contains(search)
                            || userSvc.getUserName(r.getUserId()).toLowerCase().contains(search);

                    boolean matchStatut = statut == null
                            || statut.equals("Tous les statuts")
                            || r.getStatusreclam().equalsIgnoreCase(statut);

                    boolean matchPriorite = priorite == null
                            || priorite.equals("Toutes les priorités")
                            || r.getPriority().equalsIgnoreCase(priorite);

                    return matchSearch && matchStatut && matchPriorite;
                })
                .collect(Collectors.toList());

        buildCards(filtered);
    }

    @FXML
    private void reinitialiserFiltres() {
        searchField.clear();
        comboStatut.getSelectionModel().selectFirst();
        comboPriorite.getSelectionModel().selectFirst();
        buildCards(allReclamations);
    }

    // ── BADGE BUILDERS ────────────────────────────────────────────────────

    private Label buildAvatar(String initials, int userId) {
        Label av = new Label(initials);
        av.setPrefWidth(38);
        av.setPrefHeight(38);
        av.setMinWidth(38);
        av.setMinHeight(38);
        av.setAlignment(Pos.CENTER);
        av.setStyle(
                "-fx-background-color:" + avatarColor(userId) + ";" +
                        "-fx-background-radius:19;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:" + avatarTextColor(userId) + ";"
        );
        return av;
    }

    private String avatarColor(int userId) {
        String[] colors = {"#E6F1FB","#EAF3DE","#FAEEDA","#FCEBEB","#EEEDFE","#E1F5EE"};
        return colors[userId % colors.length];
    }

    private String avatarTextColor(int userId) {
        String[] colors = {"#185FA5","#3B6D11","#854F0B","#A32D2D","#534AB7","#0F6E56"};
        return colors[userId % colors.length];
    }

    private Label buildStatusBadge(String status) {
        Label b = new Label(status == null ? "—" : status);
        if (status == null) { b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;"); return b; }
        String s = status.toLowerCase();
        if (s.equals("en attente")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;");
        } else if (s.equals("en cours")) {
            b.setStyle(badgeBase() + "-fx-background-color:#E6F1FB; -fx-text-fill:#185FA5;");
        } else if (s.equals("résolu") || s.equals("resolved")) {
            b.setStyle(badgeBase() + "-fx-background-color:#EAF3DE; -fx-text-fill:#3B6D11;");
        } else {
            b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;");
        }
        return b;
    }

    private Label buildPriorityBadge(String priority) {
        Label b = new Label(priority == null ? "—" : priority);
        if (priority == null) { b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;"); return b; }
        String p = priority.toLowerCase();
        if (p.equals("urgente")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FCEBEB; -fx-text-fill:#A32D2D;");
        } else if (p.equals("moyenne")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;");
        } else if (p.equals("faible")) {
            b.setStyle(badgeBase() + "-fx-background-color:#EAF3DE; -fx-text-fill:#3B6D11;");
        } else {
            b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;");
        }
        return b;
    }

    private Label buildCategoryBadge(String category) {
        Label b = new Label(category == null ? "Autre" : category);
        b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#4c6290;");
        return b;
    }

    private Label buildEscaladeBadge(Reclamation r) {
        boolean isEscalated = false;
        try {
            long daysOld = ChronoUnit.DAYS.between(
                    r.getReclamDate().toLocalDate(), LocalDate.now()
            );
            List<Reponse> reps = reponseSvc.getByReclamation(r.getReclamationId());
            isEscalated = "urgente".equalsIgnoreCase(r.getPriority())
                    && "en attente".equalsIgnoreCase(r.getStatusreclam())
                    && reps.isEmpty()
                    && daysOld > 3;
        } catch (Exception ignored) {}

        Label b = new Label(isEscalated ? "⚠ Escaladée" : "");
        if (isEscalated) {
            b.setStyle(
                    badgeBase() +
                            "-fx-background-color:#FCEBEB;" +
                            "-fx-text-fill:#A32D2D;" +
                            "-fx-border-color:#E24B4A;" +
                            "-fx-border-radius:20;" +
                            "-fx-border-width:1;"
            );
        }
        b.setVisible(isEscalated);
        b.setManaged(isEscalated);
        return b;
    }

    private String badgeBase() {
        return "-fx-font-size:11px; -fx-font-weight:bold;" +
                "-fx-background-radius:20; -fx-padding:3 10 3 10;";
    }

    private String buildCardStyle(Reclamation r, Reponse rep) {
        String base =
                "-fx-background-color:white;" +
                        "-fx-background-radius:16;" +
                        "-fx-border-radius:16;" +
                        "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.08),10,0,0,3);";

        try {
            long daysOld = ChronoUnit.DAYS.between(
                    r.getReclamDate().toLocalDate(), LocalDate.now()
            );
            if ("urgente".equalsIgnoreCase(r.getPriority())
                    && "en attente".equalsIgnoreCase(r.getStatusreclam())
                    && rep == null
                    && daysOld > 3) {
                return base + "-fx-border-color:#E24B4A; -fx-border-width:0 0 0 4;";
            }
        } catch (Exception ignored) {}

        if (rep != null) {
            return base + "-fx-border-color:#639922; -fx-border-width:0 0 0 4;";
        }

        return base + "-fx-border-color:#EF9F27; -fx-border-width:0 0 0 4;";
    }

    // ── UTILITY ───────────────────────────────────────────────────────────

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.setTitle("Erreur");
        alert.showAndWait();
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────

    @FXML private void ouvrirAdminDashboard()    { tests.Main.changeScene("/admindashboard.fxml"); }
    @FXML private void ouvrirAdminReclamations() { tests.Main.changeScene("/adminreclamations.fxml"); }
    @FXML
    private void goToNotifications() {
        tests.Main.changeScene("/adminnotifications.fxml");
    }
}
