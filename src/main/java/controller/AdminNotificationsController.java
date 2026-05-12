package controller;

import entities.Notification;
import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.EscalationService;
import services.NotificationService;
import services.ReclamationServices;
import services.UserService;
import tests.MainFx;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * AdminNotificationsController — Admin-only notifications
 *
 * FIXED:
 * - Filters out user-facing confirmation messages
 * - Only shows admin-relevant alerts (escalades, status changes, new reclamations)
 * - Properly detects manual escalation notifications
 */
public class AdminNotificationsController implements Initializable {

    @FXML private VBox      notifList;
    @FXML private Label     lblUnreadBadge;
    @FXML private ComboBox<String> cmbTrier;

    private final NotificationService notifSvc = new NotificationService();
    private final ReclamationServices recSvc   = new ReclamationServices();
    private final EscalationService   escSvc   = new EscalationService();
    private final UserService         userSvc  = new UserService();

    private final List<NotifItem> masterList = new ArrayList<>();

    // ── User-only notification types that should NEVER appear in admin view ──
    private static final Set<String> USER_ONLY_TYPES = new HashSet<>(Arrays.asList(
            "ESCALADE_ENVOYEE",      // "Votre demande d'escalade a bien été envoyée..."
            "ESCALADE_CONFIRMATION", // User confirmation messages
            "REPONSE_RECUE",         // User received a response
            "STATUT_CHANGE_USER"     // Status change notification to user
    ));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTrier.setItems(FXCollections.observableArrayList(
                "Plus récent (défaut)",
                "Plus urgent",
                "Escalades"
        ));
        cmbTrier.getSelectionModel().selectFirst();
        cmbTrier.setOnAction(e -> appliquerTri());

        loadNotifications();
        refreshBadge();
    }

    // ── LOAD ─────────────────────────────────────────────────────────────────

    private void loadNotifications() {
        masterList.clear();

        // ── 1. Open escalations from escalation table (most reliable source) ──
        List<Map<String, Object>> escalades = escSvc.getAllOpen();
        for (Map<String, Object> esc : escalades) {
            int    recId     = (Integer)   esc.get("reclamationId");
            String subject   = (String)    esc.get("subject");
            String reason    = (String)    esc.get("reason");
            Integer urgency  = (Integer)   esc.get("urgency"); // manual escalation urgency level
            Timestamp ts     = (Timestamp) esc.get("createdAt");
            Instant instant  = ts != null ? ts.toInstant() : Instant.now();

            String title = "Escalade manuelle : " + nvl(subject);
            String desc = "Raison : " + (reason != null && !reason.isBlank() ? reason : "Non spécifiée");
            if (urgency != null) {
                desc += " [Urgence : " + urgency + "/10]";
            }

            masterList.add(new NotifItem(
                    "ESCALADE_MANUELLE", "⚠", "#FAEEDA",
                    title, desc, instant, recId, 3
            ));
        }

        // ── 2. Persistent DB notifications (notification table) ───────────────
        try {
            List<Notification> dbNotifs = notifSvc.getByUser(1); // admin userId = 1
            for (Notification n : dbNotifs) {
                String type = nvl(n.getType());

                // SKIP user-only confirmation messages
                if (USER_ONLY_TYPES.contains(type)) continue;

                // Map various escalation types to unified handling
                String normalizedType = normalizeEscaladeType(type);

                String icon  = iconFor(normalizedType);
                String color = colorFor(normalizedType);
                int    prio  = prioFor(normalizedType);
                Instant instant = n.getCreatedAt() != null
                        ? n.getCreatedAt().toInstant() : Instant.EPOCH;

                // Build description based on type
                String desc = buildNotifDescription(n, normalizedType);

                masterList.add(new NotifItem(
                        normalizedType, icon, color,
                        n.getMessage(), desc, instant,
                        n.getReclamationId(), prio
                ));
            }
        } catch (Exception e) {
            System.out.println("⚠️ DB notifications load error: " + e.getMessage());
        }

        // ── 3. Auto-escalated reclamations (urgent + pending + >3 days + no response) ──
        List<Reclamation> autoEscalades = recSvc.getEscalatedReclamations(3);
        for (Reclamation r : autoEscalades) {
            long daysOld = ChronoUnit.DAYS.between(
                    r.getReclamDate().toLocalDate(),
                    java.time.LocalDate.now()
            );

            // Skip if already added from escalation table
            boolean alreadyAdded = masterList.stream()
                    .anyMatch(n -> n.reclamationId == r.getReclamationId()
                            && n.type.contains("ESCALADE"));
            if (alreadyAdded) continue;

            masterList.add(new NotifItem(
                    "ESCALADE_AUTO", "⚠", "#FAEEDA",
                    "Escalade auto : " + nvl(r.getSubject()),
                    "Urgente · " + daysOld + " j sans réponse · SLA dépassé",
                    r.getReclamDate() != null
                            ? r.getReclamDate().toLocalDate().atStartOfDay()
                            .toInstant(java.time.ZoneOffset.UTC)
                            : Instant.EPOCH,
                    r.getReclamationId(), 3
            ));
        }

        // ── 4. Recent reclamations (last 7 days) ──────────────────────────────
        List<Reclamation> recent = recSvc.getRecentReclamations(7);
        for (Reclamation r : recent) {
            int prio = r.getPriority() == null ? 1
                    : r.getPriority().equalsIgnoreCase("Urgente") ? 3
                    : r.getPriority().equalsIgnoreCase("Moyenne") ? 2 : 1;

            masterList.add(new NotifItem(
                    "NOUVELLE", "📥", "#E6F1FB",
                    "Nouvelle réclamation : " + nvl(r.getSubject()),
                    "Priorité : " + nvl(r.getPriority()) + "  ·  Statut : " + nvl(r.getStatusreclam()),
                    r.getReclamDate() != null
                            ? r.getReclamDate().toLocalDate().atStartOfDay()
                            .toInstant(java.time.ZoneOffset.UTC)
                            : Instant.EPOCH,
                    r.getReclamationId(), prio
            ));
        }

        sortBy("Plus récent (défaut)");
        afficherNotifications(masterList);
    }

    // ── TYPE NORMALIZATION ─────────────────────────────────────────────────

    /**
     * Normalizes various escalation notification types to a standard type.
     * This handles cases where the DB stores different type strings.
     */
    private String normalizeEscaladeType(String type) {
        if (type == null) return "GENERIC";
        String upper = type.toUpperCase();
        if (upper.contains("ESCALADE")) {
            if (upper.contains("MANUEL") || upper.contains("USER")) return "ESCALADE_MANUELLE";
            if (upper.contains("AUTO")) return "ESCALADE_AUTO";
            return "ESCALADE";
        }
        return type;
    }

    /**
     * Builds additional description for notification based on type.
     */
    private String buildNotifDescription(Notification n, String normalizedType) {
        switch (normalizedType) {
            case "ESCALADE_MANUELLE":
                return "Action requise : répondre à la réclamation";
            case "ESCALADE_AUTO":
                return "SLA dépassé — réclamation urgente sans réponse";
            case "REPONSE_AJOUTEE":
                return "Nouvelle réponse sur REC-" + n.getReclamationId();
            case "STATUT_CHANGE":
                return "Statut mis à jour sur REC-" + n.getReclamationId();
            case "RESOLUTION":
                return "Réclamation résolue";
            default:
                return "";
        }
    }

    // ── SORT ─────────────────────────────────────────────────────────────────

    private void appliquerTri() {
        String choice = cmbTrier.getSelectionModel().getSelectedItem();
        List<NotifItem> view = new ArrayList<>(masterList);

        switch (nvl(choice)) {
            case "Plus urgent":
                view.sort(Comparator
                        .comparingInt((NotifItem n) -> n.priority).reversed()
                        .thenComparing(Comparator.comparing((NotifItem n) -> n.instant).reversed()));
                break;

            case "Escalades":
                view.removeIf(n -> !n.type.contains("ESCALADE"));
                view.sort(Comparator.comparing((NotifItem n) -> n.instant).reversed());
                break;

            default:
                view.sort(Comparator.comparing((NotifItem n) -> n.instant).reversed());
                break;
        }

        afficherNotifications(view);
    }

    private void sortBy(String mode) {
        switch (mode) {
            case "Plus urgent":
                masterList.sort(Comparator
                        .comparingInt((NotifItem n) -> n.priority).reversed()
                        .thenComparing(Comparator.comparing((NotifItem n) -> n.instant).reversed()));
                break;
            default:
                masterList.sort(Comparator.comparing((NotifItem n) -> n.instant).reversed());
                break;
        }
    }

    // ── RENDER ───────────────────────────────────────────────────────────────

    private void afficherNotifications(List<NotifItem> items) {
        notifList.getChildren().clear();

        if (items.isEmpty()) {
            Label empty = new Label("🔔  Aucune notification pour le moment.");
            empty.setStyle("-fx-font-size:14px; -fx-text-fill:#9aabb5; -fx-padding:40;");
            notifList.getChildren().add(empty);
            return;
        }

        for (NotifItem item : items) {
            HBox row = buildRow(item);
            notifList.getChildren().add(row);
        }
    }

    private HBox buildRow(NotifItem item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(10, 8, 10, 8));
        row.setStyle("-fx-background-color:white;"
                + "-fx-background-radius:10;"
                + "-fx-border-color:#e8eef5;"
                + "-fx-border-radius:10;");

        // ── Icon circle ───────────────────────────────────────────────────
        Label icon = new Label(item.icon);
        icon.setPrefWidth(40);
        icon.setPrefHeight(40);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + item.bgColor + ";"
                + "-fx-background-radius:20;"
                + "-fx-font-size:18px;");

        // ── Body ──────────────────────────────────────────────────────────
        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label title = new Label(item.title);
        title.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0b1f44;");
        title.setWrapText(true);

        Label desc = new Label(item.description);
        desc.setStyle("-fx-font-size:12px; -fx-text-fill:#5d6f91;");
        desc.setWrapText(true);

        Label timeLabel = new Label(relativeTime(item.instant));
        timeLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

        body.getChildren().addAll(title, desc, timeLabel);

        // ── Right: "Répondre" button for escalades ONLY ───────────────────
        boolean isEscalade = item.type.contains("ESCALADE");

        if (isEscalade) {
            Button btnRepondre = new Button("Répondre →");
            btnRepondre.setStyle(
                    "-fx-background-color:white;"
                            + "-fx-border-color:#E24B4A;"
                            + "-fx-text-fill:#E24B4A;"
                            + "-fx-border-radius:6;"
                            + "-fx-background-radius:6;"
                            + "-fx-font-size:11px;"
                            + "-fx-font-weight:bold;"
                            + "-fx-cursor:hand;"
                            + "-fx-padding:5 12 5 12;");

            btnRepondre.setOnAction(e -> naviguerVersReclamation(item.reclamationId));

            HBox right = new HBox(btnRepondre);
            right.setAlignment(Pos.CENTER);
            row.getChildren().addAll(icon, body, right);

        } else {
            // Non-escalade rows: NO click handler, NO button
            row.getChildren().addAll(icon, body);
        }

        // Hover effect
        if (isEscalade) {
            row.setStyle(row.getStyle() + "-fx-cursor:hand;");
            row.setOnMouseEntered(e -> row.setStyle(
                    "-fx-background-color:#f4f8fc;"
                            + "-fx-background-radius:10;"
                            + "-fx-border-color:#e8eef5;"
                            + "-fx-border-radius:10;"
                            + "-fx-cursor:hand;"));
            row.setOnMouseExited(e -> row.setStyle(
                    "-fx-background-color:white;"
                            + "-fx-background-radius:10;"
                            + "-fx-border-color:#e8eef5;"
                            + "-fx-border-radius:10;"
                            + "-fx-cursor:hand;"));
        } else {
            row.setOnMouseEntered(e -> row.setStyle(
                    "-fx-background-color:#f4f8fc;"
                            + "-fx-background-radius:10;"
                            + "-fx-border-color:#e8eef5;"
                            + "-fx-border-radius:10;"));
            row.setOnMouseExited(e -> row.setStyle(
                    "-fx-background-color:white;"
                            + "-fx-background-radius:10;"
                            + "-fx-border-color:#e8eef5;"
                            + "-fx-border-radius:10;"));
        }

        return row;
    }

    // ── NAVIGATION ───────────────────────────────────────────────────────────

    private void naviguerVersReclamation(int reclamationId) {
        MainFx.selectedReclamationId = reclamationId;
        MainFx.changeScene("/adminreclamations.fxml");
    }

    // ── BADGE ────────────────────────────────────────────────────────────────

    private void refreshBadge() {
        try {
            int unread = notifSvc.countUnread(1);
            if (lblUnreadBadge != null) {
                lblUnreadBadge.setText(String.valueOf(unread));
                lblUnreadBadge.setVisible(unread > 0);
                lblUnreadBadge.setManaged(unread > 0);
            }
        } catch (Exception e) {
            System.err.println("Badge error: " + e.getMessage());
        }
    }

    // ── FXML HANDLERS ────────────────────────────────────────────────────────

    @FXML
    private void handleMarkAllRead() {
        try {
            notifSvc.markAllAsRead(1);
            loadNotifications();
            refreshBadge();
        } catch (Exception e) {
            System.out.println("Erreur markAllRead: " + e.getMessage());
        }
    }

    @FXML private void goToDashboard()    { MainFx.changeScene("/admindashboard.fxml"); }
    @FXML private void goToReclamations() { MainFx.changeScene("/adminreclamations.fxml"); }
    @FXML private void goToNotifications(){ loadNotifications(); refreshBadge(); }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String relativeTime(Instant instant) {
        if (instant == null || instant.equals(Instant.EPOCH)) return "";
        long minutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        if (minutes < 1)  return "À l'instant";
        if (minutes < 60) return minutes + " min";
        long hours = minutes / 60;
        if (hours < 24)   return hours + "h";
        long days = hours / 24;
        return days + " j";
    }

    private String iconFor(String type) {
        if (type == null) return "🔔";
        switch (type) {
            case "ESCALADE_AUTO":     return "⚠";
            case "ESCALADE_MANUELLE": return "⚠";
            case "ESCALADE":          return "⚠";
            case "REPONSE_AJOUTEE":   return "💬";
            case "STATUT_CHANGE":     return "🔄";
            case "RESOLUTION":        return "✅";
            default:                  return "🔔";
        }
    }

    private String colorFor(String type) {
        if (type == null) return "#E6F1FB";
        switch (type) {
            case "ESCALADE_AUTO":
            case "ESCALADE_MANUELLE":
            case "ESCALADE":          return "#FAEEDA";
            case "RESOLUTION":        return "#E8F5E9";
            case "REPONSE_AJOUTEE":   return "#E3F2FD";
            default:                  return "#E6F1FB";
        }
    }

    private int prioFor(String type) {
        if (type == null) return 1;
        switch (type) {
            case "ESCALADE_AUTO":
            case "ESCALADE_MANUELLE":
            case "ESCALADE":          return 3;
            case "STATUT_CHANGE":     return 2;
            default:                  return 1;
        }
    }

    private String nvl(String s) { return s != null ? s : "—"; }

    // ── Inner data class ─────────────────────────────────────────────────────

    private static class NotifItem {
        final String  type;
        final String  icon;
        final String  bgColor;
        final String  title;
        final String  description;
        final Instant instant;
        final int     reclamationId;
        final int     priority;

        NotifItem(String type, String icon, String bgColor,
                  String title, String description,
                  Instant instant, int reclamationId, int priority) {
            this.type          = type;
            this.icon          = icon;
            this.bgColor       = bgColor;
            this.title         = title;
            this.description   = description;
            this.instant       = instant;
            this.reclamationId = reclamationId;
            this.priority      = priority;
        }
    }
}