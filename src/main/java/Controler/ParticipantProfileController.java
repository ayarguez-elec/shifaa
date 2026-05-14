package Controler;

import entities.Participant;
import entities.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.LoyaltyService;
import services.ParticipantStatsServ;
import services.ReservationServ;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Vue profil détaillée d'un participant :
 * ■ Score fidélité + badge + priorité
 * ■ Statistiques comportementales
 * ■ Historique réservations
 * ■ Notifications
 */
public class ParticipantProfileController {

    @FXML private Label lblNom;
    @FXML private Label lblEmail;
    @FXML private Label lblTel;
    @FXML private Label lblAvatar;

    // Fidélité
    @FXML private Label       lblBadge;
    @FXML private Label       lblPriority;
    @FXML private Label       lblScore;
    @FXML private ProgressBar pbScore;
    @FXML private Label       lblProchainBadge;

    // Stats
    @FXML private Label lblTotalResa;
    @FXML private Label lblConfirme;
    @FXML private Label lblAnnule;
    @FXML private Label lblPresence;
    @FXML private Label lblTauxPresence;

    // Listes
    @FXML private VBox reservationsPane;
    @FXML private VBox notificationsPane;

    private Participant participant;
    private final LoyaltyService        loyaltyServ = new LoyaltyService();
    private final ParticipantStatsServ  statsServ   = new ParticipantStatsServ();
    private final ReservationServ       resServ     = new ReservationServ();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Entry point — called from GetAllparticipant ───────────────────────────
    public void setParticipant(Participant p) {
        this.participant = p;
        loadAll();
    }

    private void loadAll() {
        // Enrich with live loyalty data
        loyaltyServ.enrich(participant);
        fillHeader();
        fillLoyalty();

        // Load heavy data in background
        new Thread(() -> {
            Map<String, Object> stats = statsServ.getProfileStats(participant.getId());
            List<Reservation>   resas = resServ.getByParticipant(participant.getId());
            List<Map<String, Object>> notifs = statsServ.getNotifications(participant.getId());
            Platform.runLater(() -> {
                fillStats(stats);
                fillReservations(resas);
                fillNotifications(notifs);
            });
        }, "Profile-Loader").start();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private void fillHeader() {
        String fullName = participant.getNom() + " " + participant.getPrenom();
        if (lblNom    != null) lblNom.setText(fullName);
        if (lblEmail  != null) lblEmail.setText(participant.getEmail());
        if (lblTel    != null) lblTel.setText(participant.getTelephone() != null ? participant.getTelephone() : "—");
        if (lblAvatar != null) {
            lblAvatar.setText(participant.getNom().substring(0, 1).toUpperCase());
            String avatarStyle = "-fx-background-color:" + getAvatarColor() + "; " +
                "-fx-background-radius:40; -fx-text-fill:white; -fx-font-weight:bold; " +
                "-fx-font-size:22px; -fx-min-width:60; -fx-min-height:60; -fx-alignment:center;";
            lblAvatar.setStyle(avatarStyle);
        }
    }

    // ── Loyalty section ───────────────────────────────────────────────────────
    private void fillLoyalty() {
        int score = participant.getLoyaltyPoints();
        Map<String, Object> progress = loyaltyServ.getBadgeProgress(participant.getId());

        if (lblBadge != null) {
            lblBadge.setText(participant.getBadgeEmoji() + "  " + participant.getBadge().name());
            lblBadge.setStyle("-fx-text-fill:" + participant.getBadgeColor() +
                              "; -fx-font-size:16px; -fx-font-weight:bold;");
        }
        if (lblPriority != null) {
            lblPriority.setText(participant.getPriorityEmoji() + "  " + participant.getPriorityLevel().name());
            lblPriority.setStyle("-fx-text-fill:" + participant.getPriorityColor() +
                                 "; -fx-font-weight:bold; -fx-font-size:13px;");
        }
        if (lblScore != null) lblScore.setText(score + " pts");

        if (pbScore != null) {
            int pct = (int) progress.getOrDefault("progression_pct", 0);
            pbScore.setProgress(Math.min(1.0, pct / 100.0));
            String color = score >= 150 ? "#d97706" : score >= 50 ? "#6b7280" : "#b45309";
            pbScore.setStyle("-fx-accent:" + color + ";");
        }
        if (lblProchainBadge != null) {
            String next = (String) progress.getOrDefault("prochain_badge", "MAX");
            int manque  = (int)    progress.getOrDefault("pts_manquants",   0);
            lblProchainBadge.setText("MAX".equals(next)
                ? "🏆 Badge maximum atteint !"
                : manque + " pts pour " + next);
        }
    }

    // ── Stats section ─────────────────────────────────────────────────────────
    private void fillStats(Map<String, Object> stats) {
        setLbl(lblTotalResa,   stats, "total_reservations");
        setLbl(lblConfirme,    stats, "nb_confirmees");
        setLbl(lblAnnule,      stats, "nb_annulees");
        setLbl(lblPresence,    stats, "nb_presences_reelles");
        setLbl(lblTauxPresence,stats, "taux_presence_reelle");
    }

    private void setLbl(Label lbl, Map<String, Object> m, String key) {
        if (lbl != null) lbl.setText(String.valueOf(m.getOrDefault(key, "0")));
    }

    // ── Reservation history ───────────────────────────────────────────────────
    private void fillReservations(List<Reservation> resas) {
        if (reservationsPane == null) return;
        reservationsPane.getChildren().clear();

        if (resas.isEmpty()) {
            reservationsPane.getChildren().add(emptyLabel("📭 Aucune réservation"));
            return;
        }

        for (Reservation r : resas) {
            reservationsPane.getChildren().add(buildResaRow(r));
        }
    }

    private HBox buildResaRow(Reservation r) {
        String[] sc = getStatusStyle(r.getStatut());
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8; " +
                     "-fx-border-color:" + sc[2] + "; -fx-border-width:0 0 0 4; -fx-border-radius:8; " +
                     "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");

        Label badge = new Label(sc[0]);
        badge.setStyle("-fx-background-color:" + sc[1] + "; -fx-text-fill:" + sc[2] +
                       "; -fx-background-radius:14; -fx-padding:3 9 3 9; -fx-font-size:11px; -fx-font-weight:bold;");
        badge.setMinWidth(115);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label lEvent = new Label("Événement #" + r.getEventId() + "   |   🎟 " + r.getNombrePlaces() + " place(s)");
        lEvent.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#1a2332;");
        Label lDate = new Label("🕐 " + (r.getDateDemande() != null ? r.getDateDemande().format(FMT) : "—"));
        lDate.setStyle("-fx-font-size:11px; -fx-text-fill:#718096;");
        info.getChildren().addAll(lEvent, lDate);

        if (r.getMotifRefus() != null && !r.getMotifRefus().isEmpty()) {
            Label lMotif = new Label("ℹ " + r.getMotifRefus());
            lMotif.setStyle("-fx-font-size:11px; -fx-text-fill:#c53030; -fx-wrap-text:true;");
            lMotif.setWrapText(true);
            info.getChildren().add(lMotif);
        }

        row.getChildren().addAll(badge, info);
        return row;
    }

    // ── Notifications history ─────────────────────────────────────────────────
    private void fillNotifications(List<Map<String, Object>> notifs) {
        if (notificationsPane == null) return;
        notificationsPane.getChildren().clear();

        if (notifs.isEmpty()) {
            notificationsPane.getChildren().add(emptyLabel("🔔 Aucune notification"));
            return;
        }

        for (Map<String, Object> n : notifs) {
            notificationsPane.getChildren().add(buildNotifRow(n));
        }
    }

    private HBox buildNotifRow(Map<String, Object> n) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        boolean read = Boolean.TRUE.equals(n.get("is_read"));
        row.setStyle("-fx-background-color:" + (read ? "#f8fafc" : "#eff6ff") +
                     "; -fx-background-radius:8; -fx-border-color:#e2e8f0; " +
                     "-fx-border-width:1; -fx-border-radius:8;");

        String type = String.valueOf(n.get("type"));
        Label icon = new Label(notifIcon(type));
        icon.setStyle("-fx-font-size:18px;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label sujet = new Label(String.valueOf(n.get("sujet")));
        sujet.setStyle("-fx-font-size:12px; -fx-font-weight:" + (read ? "normal" : "bold") +
                       "; -fx-text-fill:#1a2332;");
        Label date = new Label("📅 " + n.get("sent_at"));
        date.setStyle("-fx-font-size:10px; -fx-text-fill:#718096;");
        info.getChildren().addAll(sujet, date);

        row.getChildren().addAll(icon, info);
        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String[] getStatusStyle(Reservation.Statut s) {
        switch (s) {
            case CONFIRME:        return new String[]{"✓ CONFIRMÉ",      "#d4edda", "#16a34a"};
            case LISTE_D_ATTENTE: return new String[]{"⏳ ATTENTE",       "#fef3c7", "#d97706"};
            case CHECKED_IN:      return new String[]{"🎯 PRÉSENT",       "#dbeafe", "#1d4ed8"};
            case REFUSE:          return new String[]{"✕ REFUSÉ",         "#fdeaea", "#c53030"};
            case ANNULE:          return new String[]{"↩ ANNULÉ",         "#f3f4f6", "#6b7280"};
            default:              return new String[]{"◷ EN ATTENTE",     "#fef9c3", "#b45309"};
        }
    }

    private String notifIcon(String type) {
        switch (type) {
            case "CONFIRMATION":   return "✅";
            case "REFUS":          return "❌";
            case "WAITLIST_PROMO": return "🎉";
            case "REMINDER":       return "⏰";
            default:               return "🔔";
        }
    }

    private String getAvatarColor() {
        switch (participant.getPriorityLevel()) {
            case VIP:    return "#7c3aed";
            case HIGH:   return "#dc2626";
            case MEDIUM: return "#d97706";
            default:     return "#1e3a5f";
        }
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#a0aec0; -fx-font-size:13px; -fx-padding:20;");
        return l;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    public void handleRetour() {
        tests.Main.showAdminEventParticipants();
    }
}
