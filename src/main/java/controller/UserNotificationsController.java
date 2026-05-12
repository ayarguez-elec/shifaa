package controller;

import entities.Notification;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.NotificationService;
import tests.MainFx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UserNotificationsController
 * Bound to: usernotifications.fxml
 *
 * Shows all notifications for the currently logged-in user.
 * Hardcoded userId = 2 until session management is implemented.
 */
public class UserNotificationsController {

    // ── Sidebar navigation ──────────────────────────────────────
    @FXML private Button btnDashboard;
    @FXML private Button btnMesReclamations;
    @FXML private Button btnNotifications;

    // ── Header ───────────────────────────────────────────────────
    @FXML private Label lblUnreadBadge;
    @FXML private Button btnMarkAllRead;

    // ── Content ──────────────────────────────────────────────────
    @FXML private ScrollPane scrollPane;
    @FXML private VBox notifList;
    @FXML private Label lblEmpty;

    // !! Replace with real session userId when session management is added
    private static final int USER_ID = 2;

    private NotificationService notifSvc = new NotificationService();

    @FXML
    public void initialize() {
        if (btnNotifications != null) {
            btnNotifications.getStyleClass().add("sidebar-button-active");
        }
        loadNotifications();
    }

    // ────────────────────────────────────────────────────────────
    //  LOAD
    // ────────────────────────────────────────────────────────────

    private void loadNotifications() {
        if (notifList == null) { return; }
        notifList.getChildren().clear();

        List<Notification> list = notifSvc.getByUser(USER_ID);

        long unread = 0;
        for (Notification n : list) {
            if (!n.isRead()) { unread++; }
        }

        if (lblUnreadBadge != null) {
            if (unread > 0) {
                lblUnreadBadge.setText(String.valueOf(unread));
                lblUnreadBadge.setVisible(true);
                lblUnreadBadge.setManaged(true);
            } else {
                lblUnreadBadge.setVisible(false);
                lblUnreadBadge.setManaged(false);
            }
        }

        if (list.isEmpty()) {
            if (lblEmpty != null) { lblEmpty.setVisible(true); lblEmpty.setManaged(true); }
            return;
        }
        if (lblEmpty != null) { lblEmpty.setVisible(false); lblEmpty.setManaged(false); }

        for (Notification n : list) {
            notifList.getChildren().add(buildCard(n));
        }
    }

    // ────────────────────────────────────────────────────────────
    //  CARD BUILDER
    // ────────────────────────────────────────────────────────────

    private HBox buildCard(Notification n) {
        HBox card = new HBox(14);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(
                "-fx-background-color: " + (n.isRead() ? "white" : "#EEF5FF") + ";"
                        + "-fx-border-color: " + (n.isRead() ? "#e1e8f0" : "#378ADD") + ";"
                        + "-fx-border-width: 0 0 0 4;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-radius: 12;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );
        card.setMaxWidth(Double.MAX_VALUE);

        // Icon circle
        Label icon = new Label(n.getIcon());
        icon.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-min-width: 44px; -fx-max-width: 44px;"
                        + "-fx-min-height: 44px; -fx-max-height: 44px;"
                        + "-fx-alignment: center;"
                        + "-fx-background-color: " + iconBg(n.getType()) + ";"
                        + "-fx-background-radius: 22;"
        );

        // Text
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox topRow = new HBox();
        Label typeLabel = new Label(n.getTypeLabel());
        typeLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold;"
                        + "-fx-text-fill: " + typeColor(n.getType()) + ";"
                        + "-fx-background-color: " + iconBg(n.getType()) + ";"
                        + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label time = new Label(formatTime(n.getCreatedAt()));
        time.setStyle("-fx-font-size: 11px; -fx-text-fill: #9aabb5;");
        topRow.getChildren().addAll(typeLabel, spacer, time);

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: " + (n.isRead() ? "#5d6f91" : "#0b1f44") + ";"
        );

        Label recRef = new Label("Réclamation #" + n.getReclamationId());
        recRef.setStyle("-fx-font-size: 11px; -fx-text-fill: #9aabb5;");

        textBox.getChildren().addAll(topRow, msg, recRef);

        // Mark read action
        VBox actions = new VBox();
        actions.setStyle("-fx-alignment: center;");
        if (!n.isRead()) {
            Button markBtn = new Button("✓");
            markBtn.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-text-fill: #378ADD; -fx-font-size: 16px; -fx-cursor: hand;"
            );
            markBtn.setOnAction(e -> {
                notifSvc.markAsRead(n.getNotificationId());
                loadNotifications();
            });
            markBtn.setTooltip(new javafx.scene.control.Tooltip("Marquer comme lu"));
            actions.getChildren().add(markBtn);
        }

        card.getChildren().addAll(icon, textBox, actions);
        return card;
    }

    // ────────────────────────────────────────────────────────────
    //  ACTIONS
    // ────────────────────────────────────────────────────────────

    @FXML
    private void handleMarkAllRead() {
        notifSvc.markAllAsRead(USER_ID);
        loadNotifications();
    }

    @FXML
    private void goToDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }

    @FXML
    private void goToMesReclamations() {
        MainFx.changeScene("/userreclamations.fxml");
    }

    @FXML
    private void goToNotifications() {
        // Already here
    }

    // ────────────────────────────────────────────────────────────
    //  HELPERS
    // ────────────────────────────────────────────────────────────

    private String formatTime(java.sql.Timestamp ts) {
        if (ts == null) { return ""; }
        LocalDateTime ldt = ts.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(ldt, now).toMinutes();
        if (minutes < 1)  { return "À l'instant"; }
        if (minutes < 60) { return minutes + " min"; }
        long hours = minutes / 60;
        if (hours < 24)   { return hours + "h"; }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return ldt.format(fmt);
    }

    private String iconBg(String type) {
        if ("REPONSE_AJOUTEE".equals(type))    { return "#E6F1FB"; }
        if ("STATUT_CHANGE".equals(type))       { return "#EAF3DE"; }
        if ("ESCALADE_AUTO".equals(type))       { return "#FCEBEB"; }
        if ("ESCALADE_MANUELLE".equals(type))   { return "#FAEEDA"; }
        if ("RESOLUTION".equals(type))          { return "#EAF3DE"; }
        return "#f4f8fc";
    }

    private String typeColor(String type) {
        if ("REPONSE_AJOUTEE".equals(type))    { return "#185FA5"; }
        if ("STATUT_CHANGE".equals(type))       { return "#3B6D11"; }
        if ("ESCALADE_AUTO".equals(type))       { return "#A32D2D"; }
        if ("ESCALADE_MANUELLE".equals(type))   { return "#854F0B"; }
        if ("RESOLUTION".equals(type))          { return "#3B6D11"; }
        return "#5d6f91";
    }
}