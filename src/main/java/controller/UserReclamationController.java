package controller;

import entities.Reclamation;
import entities.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EscalationService;
import services.ReclamationServices;
import services.ReponseServices;
import tests.MainFx;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserReclamationController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPriorite;
    @FXML private Label totalLabel;
    @FXML private Label messageLabel;
    @FXML private Label notificationBanner;

    private final ReclamationServices reclamationSvc = new ReclamationServices();
    private final ReponseServices     reponseSvc     = new ReponseServices();
    private final EscalationService   escalationSvc  = new EscalationService();

    private final int currentUserId = 1;

    private List<Reclamation> allReclamations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterStatut.getItems().addAll("Tous les statuts", "En attente", "En cours", "Résolu");
        filterStatut.setValue("Tous les statuts");

        filterPriorite.getItems().addAll("Toutes les priorités", "faible", "moyenne", "urgente");
        filterPriorite.setValue("Toutes les priorités");

        if (MainFx.notificationUserId == currentUserId && !MainFx.notificationMessage.isEmpty()) {
            if (notificationBanner != null) {
                notificationBanner.setText("🔔 " + MainFx.notificationMessage);
                notificationBanner.setVisible(true);
                notificationBanner.setManaged(true);
            }
            MainFx.clearNotification();
        } else {
            if (notificationBanner != null) {
                notificationBanner.setVisible(false);
                notificationBanner.setManaged(false);
            }
        }

        loadAll();
    }

    private void loadAll() {
        allReclamations = reclamationSvc.getByUser(currentUserId);
        applyFilters();
    }

    @FXML
    private void applyFilters() {
        if (allReclamations == null) { return; }

        String keyword  = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statut   = filterStatut.getValue();
        String priorite = filterPriorite.getValue();

        List<Reclamation> filtered = new ArrayList<Reclamation>();
        for (Reclamation r : allReclamations) {
            boolean matchSearch = keyword.isEmpty()
                    || r.getSubject().toLowerCase().contains(keyword)
                    || r.getDescription().toLowerCase().contains(keyword)
                    || r.getStatusreclam().toLowerCase().contains(keyword)
                    || r.getPriority().toLowerCase().contains(keyword);

            boolean matchStatut = statut == null || statut.equals("Tous les statuts")
                    || r.getStatusreclam().equalsIgnoreCase(statut);

            boolean matchPriorite = priorite == null || priorite.equals("Toutes les priorités")
                    || r.getPriority().equalsIgnoreCase(priorite);

            if (matchSearch && matchStatut && matchPriorite) {
                filtered.add(r);
            }
        }

        buildCards(filtered);
    }

    @FXML
    private void reinitialiserFiltres() {
        searchField.clear();
        filterStatut.setValue("Tous les statuts");
        filterPriorite.setValue("Toutes les priorités");
        buildCards(allReclamations);
    }

    private void buildCards(List<Reclamation> reclamations) {
        cardsContainer.getChildren().clear();
        if (totalLabel != null) {
            totalLabel.setText("Total : " + reclamations.size() + " réclamations");
        }
        if (messageLabel != null) {
            messageLabel.setText(reclamations.isEmpty() ? "Aucune réclamation trouvée." : "");
        }
        for (Reclamation r : reclamations) {
            cardsContainer.getChildren().add(buildCard(r));
        }
    }

    private VBox buildCard(Reclamation r) {
        List<Reponse> reponses      = reponseSvc.getByReclamation(r.getReclamationId());
        Reponse existingReponse     = reponses.isEmpty() ? null : reponses.get(0);
        boolean alreadyEscalated   = escalationSvc.hasOpenEscalation(r.getReclamationId());
        boolean isResolved         = "Résolu".equalsIgnoreCase(r.getStatusreclam())
                || "resolved".equalsIgnoreCase(r.getStatusreclam());

        VBox card = new VBox(0);
        card.setPrefWidth(340);
        card.setStyle(buildCardStyle(r, existingReponse, alreadyEscalated));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));

        Label date = new Label("📅 " + r.getReclamDate());
        date.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = buildStatusBadge(r.getStatusreclam());
        header.getChildren().addAll(date, spacer, statusBadge);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setPadding(new Insets(0, 16, 6, 16));

        Label type = new Label("📁 Réclamation");
        type.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

        Label priorityBadge = buildPriorityBadge(r.getPriority());

        if (alreadyEscalated) {
            Label escBadge = new Label("🚨 Escalade");
            escBadge.setStyle(
                    "-fx-background-color:#FCEBEB; -fx-text-fill:#A32D2D;"
                            + "-fx-font-size:10px; -fx-font-weight:bold;"
                            + "-fx-background-radius:10; -fx-padding:2 7 2 7;"
            );
            metaRow.getChildren().addAll(type, priorityBadge, escBadge);
        } else {
            metaRow.getChildren().addAll(type, priorityBadge);
        }

        Label subject = new Label(r.getSubject());
        subject.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0b1f44;");
        subject.setPadding(new Insets(0, 16, 4, 16));
        subject.setWrapText(true);

        Label description = new Label(r.getDescription());
        description.setStyle("-fx-font-size:13px; -fx-text-fill:#5d6f91;");
        description.setPadding(new Insets(0, 16, 12, 16));
        description.setWrapText(true);
        description.setMaxWidth(300);

        Separator divider = new Separator();
        divider.setPadding(new Insets(0, 16, 0, 16));

        VBox responseZone = buildResponseZone(r, existingReponse, alreadyEscalated);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(12, 16, 14, 16));

        Button btnModifier = new Button("✏ Modifier");
        btnModifier.setStyle(
                "-fx-background-color:white;"
                        + "-fx-border-color:#dce5f0;"
                        + "-fx-border-radius:8;"
                        + "-fx-background-radius:8;"
                        + "-fx-font-size:12px;"
                        + "-fx-padding:6 14 6 14;"
                        + "-fx-cursor:hand;"
                        + "-fx-text-fill:#0b1f44;"
        );
        btnModifier.setOnAction(e -> ouvrirModifier(r));

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color:#FCEBEB;"
                        + "-fx-text-fill:#A32D2D;"
                        + "-fx-font-size:12px;"
                        + "-fx-background-radius:8;"
                        + "-fx-padding:6 14 6 14;"
                        + "-fx-cursor:hand;"
                        + "-fx-font-weight:bold;"
        );
        btnSupprimer.setOnAction(e -> supprimerReclamation(r));

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        if (!alreadyEscalated && !isResolved) {
            Button btnEscalade = new Button("⚠ Demander escalade");
            btnEscalade.setStyle(
                    "-fx-background-color:#FAEEDA;"
                            + "-fx-text-fill:#854F0B;"
                            + "-fx-font-size:12px;"
                            + "-fx-background-radius:8;"
                            + "-fx-padding:6 14 6 14;"
                            + "-fx-cursor:hand;"
                            + "-fx-font-weight:bold;"
            );
            btnEscalade.setOnAction(e -> openEscaladeDialog(r));
            actions.getChildren().add(btnEscalade);
        }

        card.getChildren().addAll(header, metaRow, subject, description, divider, responseZone, actions);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FIXED: "Ajouter" now opens as a small modal popup (like escalation)
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    private void ouvrirAjouterReclamation() {
        openDialog("/useraddreclamation.fxml", "Ajouter une Réclamation", null);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FIXED: "Modifier" now opens as a small modal popup (like escalation)
    // ═══════════════════════════════════════════════════════════════════════
    private void ouvrirModifier(Reclamation reclamation) {
        MainFx.selectedReclamation = reclamation;
        openDialog("/userupdatereclamation.fxml", "Modifier une Réclamation", reclamation);
    }

    /**
     * Generic helper to open any user dialog as a centered modal popup.
     * After the dialog closes, the cards are refreshed.
     */
    private void openDialog(String fxmlPath, String title, Reclamation reclamation) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showInfo("Fichier " + fxmlPath + " introuvable dans resources/");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(cardsContainer.getScene().getWindow());
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();

            // Refresh cards after dialog closes
            loadAll();

        } catch (Exception e) {
            System.err.println("[UserReclamation] openDialog error (" + fxmlPath + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEscaladeDialog(Reclamation r) {
        try {
            URL fxmlUrl = getClass().getResource("/userescalade.fxml");
            if (fxmlUrl == null) {
                showInfo("Fichier userescalade.fxml introuvable dans resources/");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            UserEscaladeController ctrl = loader.getController();
            ctrl.setData(r, currentUserId);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(cardsContainer.getScene().getWindow());
            dialog.setTitle("Demande d'escalade — REC-" + r.getReclamationId());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();

            if (ctrl.isSubmitted()) {
                loadAll();
                showInfo("Votre demande d'escalade a été envoyée à l'administrateur.");
            }
        } catch (Exception e) {
            System.err.println("[UserReclamation] openEscaladeDialog error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox buildResponseZone(Reclamation r, Reponse rep, boolean alreadyEscalated) {
        VBox zone = new VBox(8);
        zone.setPadding(new Insets(10, 16, 10, 16));
        zone.setStyle("-fx-background-color:#f9fbff; -fx-background-radius:0 0 16 16;");

        Label zoneLabel = new Label("↩ Réponse admin");
        zoneLabel.setStyle(
                "-fx-font-size:11px; -fx-font-weight:bold;"
                        + "-fx-text-fill:#9aabb5; -fx-letter-spacing:0.05em;"
        );

        if (rep != null) {
            Label lblContent = new Label(rep.getContent());
            lblContent.setStyle("-fx-font-size:13px; -fx-text-fill:#0b1f44;");
            lblContent.setWrapText(true);

            Label lblMeta = new Label("Répondu le " + rep.getResponseDate() + " · Admin");
            lblMeta.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

            zone.getChildren().addAll(zoneLabel, lblContent, lblMeta);
        } else {
            Label lblNoReponse = new Label("Aucune réponse pour le moment.");
            lblNoReponse.setStyle("-fx-font-size:13px; -fx-text-fill:#9aabb5; -fx-font-style:italic;");

            if (alreadyEscalated) {
                Label lblEscalade = new Label("🚨 Escalade en cours — l'admin a été notifié.");
                lblEscalade.setStyle("-fx-font-size:11px; -fx-text-fill:#A32D2D; -fx-font-weight:bold;");
                zone.getChildren().addAll(zoneLabel, lblNoReponse, lblEscalade);
            } else {
                boolean autoEscCond = false;
                try {
                    long daysOld = ChronoUnit.DAYS.between(r.getReclamDate().toLocalDate(), LocalDate.now());
                    autoEscCond = "urgente".equalsIgnoreCase(r.getPriority())
                            && "en attente".equalsIgnoreCase(r.getStatusreclam())
                            && daysOld > 3;
                } catch (Exception ignored) {}

                if (autoEscCond) {
                    Label lblEscalade = new Label("⚠ Cette réclamation est escaladée (SLA dépassé)");
                    lblEscalade.setStyle("-fx-font-size:11px; -fx-text-fill:#A32D2D; -fx-font-weight:bold;");
                    zone.getChildren().addAll(zoneLabel, lblNoReponse, lblEscalade);
                } else {
                    zone.getChildren().addAll(zoneLabel, lblNoReponse);
                }
            }
        }
        return zone;
    }

    private Label buildStatusBadge(String status) {
        Label b = new Label(status == null ? "—" : status);
        if (status == null) {
            b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;");
            return b;
        }
        String s = status.toLowerCase();
        if (s.contains("attente")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;");
        } else if (s.contains("cours")) {
            b.setStyle(badgeBase() + "-fx-background-color:#E6F1FB; -fx-text-fill:#185FA5;");
        } else if (s.contains("solu")) {
            b.setStyle(badgeBase() + "-fx-background-color:#EAF3DE; -fx-text-fill:#3B6D11;");
        } else {
            b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;");
        }
        return b;
    }

    private Label buildPriorityBadge(String priority) {
        Label b = new Label(priority == null ? "—" : priority);
        if (priority == null) {
            b.setStyle(badgeBase() + "-fx-background-color:#f0f4fa; -fx-text-fill:#5d6f91;");
            return b;
        }
        String p = priority.toLowerCase();
        if (p.equals("urgente")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FCEBEB; -fx-text-fill:#A32D2D;");
        } else if (p.equals("moyenne")) {
            b.setStyle(badgeBase() + "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;");
        } else {
            b.setStyle(badgeBase() + "-fx-background-color:#EAF3DE; -fx-text-fill:#3B6D11;");
        }
        return b;
    }

    private String badgeBase() {
        return "-fx-font-size:11px; -fx-font-weight:bold;"
                + "-fx-background-radius:20; -fx-padding:3 10 3 10;";
    }

    private String buildCardStyle(Reclamation r, Reponse rep, boolean alreadyEscalated) {
        String base =
                "-fx-background-color:white;"
                        + "-fx-background-radius:16;"
                        + "-fx-border-radius:16;"
                        + "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.08),10,0,0,3);";

        if (alreadyEscalated) {
            return base + "-fx-border-color:#E24B4A; -fx-border-width:0 0 0 4;";
        }
        try {
            long daysOld = ChronoUnit.DAYS.between(r.getReclamDate().toLocalDate(), LocalDate.now());
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

    private void supprimerReclamation(Reclamation reclamation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la réclamation ?");
        confirm.setContentText(
                "REC-" + reclamation.getReclamationId() + " : " + reclamation.getSubject()
                        + "\nCette action est irréversible."
        );

        ButtonType btnOui = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler",   ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnNon);

        confirm.showAndWait().ifPresent(result -> {
            if (result == btnOui) {
                try {
                    reclamationSvc.delete(reclamation.getReclamationId());
                    loadAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            "Erreur lors de la suppression:\n" + e.getMessage(), ButtonType.OK);
                    err.showAndWait();
                }
            }
        });
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void ouvrirUserDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }

    @FXML
    private void ouvrirUserReclamations() {
        MainFx.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void goToNotifications() {
        MainFx.changeScene("/usernotifications.fxml");
    }
}