package Controler;

import entities.Participant;
import entities.event;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.EmailService;
import services.Participantserv;
import services.RecommendedEvent;
import services.RecommendationService;
import utils.SessionManager;
import entities.user;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecommendationsController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private ComboBox<Participant> cbParticipant;
    @FXML private FlowPane             cardsPane;
    @FXML private Label                lblStatTotal;
    @FXML private Label                lblStatCtr;
    @FXML private Label                lblStatConv;
    @FXML private Label                lblStatTopCat;
    @FXML private Label                lblInfo;
    @FXML private Button               btnEnvoyerEmail;
    @FXML private Button               btnModePersonal;
    @FXML private Button               btnModeTrend;
    @FXML private Button               btnModePopular;
    @FXML private ImageView            imgAvatar;
    @FXML private Label                lblInitials;
    @FXML private Label                lblUserName;
    @FXML private Label                lblUserRole;

    private final RecommendationService recoServ  = new RecommendationService();
    private final Participantserv        partServ  = new Participantserv();
    private final EmailService           emailServ = new EmailService();

    private String  currentMode = "trending";
    private List<RecommendedEvent> lastPersonalRecs = null;

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadUserProfile();
        chargerParticipants();
        chargerStats();
        handleModeTrend(); // default view: trending
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String name = extractName(currentUser.getEmail());
        if (lblUserName != null) lblUserName.setText(name);
        if (lblUserRole != null) lblUserRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "");
        if (imgAvatar != null) imgAvatar.setVisible(false);
        if (lblInitials != null) { lblInitials.setVisible(true); lblInitials.setText(initials(name)); }
    }

    private void chargerParticipants() {
        List<Participant> list = partServ.getAll();
        cbParticipant.setItems(FXCollections.observableArrayList(list));
        cbParticipant.setConverter(new javafx.util.StringConverter<Participant>() {
            @Override public String toString(Participant p) {
                if (p == null) return "";
                return p.getPrenom() + " " + p.getNom() + "  [" + p.getEmail() + "]";
            }
            @Override public Participant fromString(String s) { return null; }
        });
    }

    private void chargerStats() {
        new Thread(() -> {
            Map<String, Object> stats = recoServ.getStatistics();
            Platform.runLater(() -> {
                lblStatTotal.setText(String.valueOf(stats.getOrDefault("totalRecommendations", 0)));
                lblStatCtr.setText(String.valueOf(stats.getOrDefault("clickRate", "0%")));
                lblStatConv.setText(String.valueOf(stats.getOrDefault("conversionRate", "0%")));
                Object cat = stats.get("topRecommendedCategory");
                lblStatTopCat.setText(cat != null && !"N/A".equals(cat.toString())
                    ? shorten(cat.toString(), 14) : "—");
            });
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODE HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleParticipantChange() {
        if ("personal".equals(currentMode)) handleModePersonal();
    }

    @FXML
    public void handleModePersonal() {
        setActiveMode("personal");
        Participant p = cbParticipant.getValue();
        if (p == null) {
            showPlaceholder("👤 Sélectionnez un participant dans la liste pour voir ses recommandations personnalisées.");
            return;
        }
        lblInfo.setText("Recommandations personnalisées pour " + p.getPrenom() + " " + p.getNom());
        cardsPane.getChildren().clear();
        Label loading = new Label("⏳ Calcul en cours...");
        loading.setStyle("-fx-font-size:14px; -fx-text-fill:#9333ea; -fx-padding:40;");
        cardsPane.getChildren().add(loading);

        new Thread(() -> {
            List<RecommendedEvent> recs = recoServ.getPersonalized(p.getId(), 12);
            lastPersonalRecs = recs;
            Platform.runLater(() -> {
                cardsPane.getChildren().clear();
                if (recs.isEmpty()) {
                    showPlaceholder("📭 Aucune recommandation disponible. L'historique de réservations est vide.");
                    return;
                }
                int rank = 1;
                for (RecommendedEvent r : recs) cardsPane.getChildren().add(buildRecoCard(r, rank++));
                chargerStats();
            });
        }).start();
    }

    @FXML
    public void handleModeTrend() {
        setActiveMode("trending");
        lblInfo.setText("Événements avec le plus de réservations récentes.");
        cardsPane.getChildren().clear();
        new Thread(() -> {
            List<Map<String, Object>> list = recoServ.getTrending(12);
            Platform.runLater(() -> {
                cardsPane.getChildren().clear();
                if (list.isEmpty()) { showPlaceholder("📭 Aucun événement tendance pour l'instant."); return; }
                int rank = 1;
                for (Map<String, Object> m : list) cardsPane.getChildren().add(buildTrendCard(m, rank++));
            });
        }).start();
    }

    @FXML
    public void handleModePopular() {
        setActiveMode("popular");
        lblInfo.setText("Événements avec le plus de participants confirmés.");
        cardsPane.getChildren().clear();
        new Thread(() -> {
            List<Map<String, Object>> list = recoServ.getPopular(12);
            Platform.runLater(() -> {
                cardsPane.getChildren().clear();
                if (list.isEmpty()) { showPlaceholder("📭 Aucun événement populaire pour l'instant."); return; }
                int rank = 1;
                for (Map<String, Object> m : list) cardsPane.getChildren().add(buildPopularCard(m, rank++));
            });
        }).start();
    }

    @FXML
    public void handleActualiser() {
        chargerStats();
        switch (currentMode) {
            case "personal": handleModePersonal();  break;
            case "trending": handleModeTrend();     break;
            case "popular":  handleModePopular();   break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EMAIL NOTIFICATION
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleEnvoyerEmail() {
        Participant p = cbParticipant.getValue();
        if (p == null) {
            alert("Sélectionnez d'abord un participant.");
            return;
        }
        if (p.getEmail() == null || p.getEmail().isBlank()) {
            alert("Ce participant n'a pas d'adresse email.");
            return;
        }

        List<RecommendedEvent> recs = lastPersonalRecs;
        if (recs == null || recs.isEmpty()) {
            // Generate on the spot
            recs = recoServ.getPersonalized(p.getId(), 8);
            lastPersonalRecs = recs;
        }
        if (recs.isEmpty()) {
            alert("Aucune recommandation à envoyer pour ce participant.");
            return;
        }

        final List<RecommendedEvent> finalRecs = recs;
        new Thread(() -> {
            boolean ok = emailServ.sendRecommendations(p.getEmail(),
                p.getPrenom() + " " + p.getNom(), finalRecs);
            Platform.runLater(() -> {
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION,
                        "✅ Recommandations envoyées à " + p.getEmail(), ButtonType.OK);
                    a.setHeaderText("Email envoyé avec succès");
                    a.showAndWait();
                } else {
                    alert("❌ Échec de l'envoi. Vérifiez la configuration SMTP.");
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARD BUILDERS
    // ══════════════════════════════════════════════════════════════════════════

    private VBox buildRecoCard(RecommendedEvent r, int rank) {
        event ev    = r.getEvent();
        String type = ev.getType() != null ? ev.getType() : "Événement";
        String[] tc = typeColors(type);

        // Banner
        StackPane banner = new StackPane();
        banner.setPrefHeight(130);
        banner.setStyle(String.format(
            "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
            "-fx-background-radius:12 12 0 0;", tc[0], tc[1]));

        // Rank badge
        Label rankBadge = new Label("#" + rank);
        rankBadge.setStyle("-fx-background-color:rgba(0,0,0,0.3); -fx-text-fill:white; " +
            "-fx-background-radius:12; -fx-padding:3 8; -fx-font-size:11px; -fx-font-weight:bold;");
        StackPane.setAlignment(rankBadge, Pos.TOP_LEFT);
        StackPane.setMargin(rankBadge, new Insets(10, 0, 0, 10));

        // Score badge
        String scoreColor = r.getScore() >= 70 ? "#16a34a" : r.getScore() >= 40 ? "#d97706" : "#2563eb";
        Label scoreBadge = new Label(r.getScore() + " pts");
        scoreBadge.setStyle("-fx-background-color:" + scoreColor + "; -fx-text-fill:white; " +
            "-fx-background-radius:12; -fx-padding:3 8; -fx-font-size:11px; -fx-font-weight:bold;");
        StackPane.setAlignment(scoreBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(scoreBadge, new Insets(10, 10, 0, 0));

        // Center emoji
        Label emoji = new Label(typeEmoji(type));
        emoji.setStyle("-fx-font-size:40px; -fx-opacity:0.75;");
        StackPane.setAlignment(emoji, Pos.CENTER);
        StackPane.setMargin(emoji, new Insets(0, 0, 18, 0));

        // Bottom type label
        Label typeLabel = new Label(type.toUpperCase());
        typeLabel.setStyle("-fx-font-size:10px; -fx-font-weight:bold; " +
            "-fx-text-fill:rgba(255,255,255,0.85);");
        StackPane.setAlignment(typeLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeLabel, new Insets(0, 0, 10, 10));

        banner.getChildren().addAll(rankBadge, scoreBadge, emoji, typeLabel);

        // Body
        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 14, 12, 14));
        body.setStyle("-fx-background-color:#ffffff;");

        Label title = new Label(ev.getTitre());
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2332; -fx-wrap-text:true;");
        title.setMaxWidth(250);
        title.setWrapText(true);

        VBox meta = new VBox(3);
        if (ev.getDate() != null) meta.getChildren().add(metaRow("📅", ev.getDate().toString()));
        if (ev.getLieu() != null) meta.getChildren().add(metaRow("📍", ev.getLieu()));

        // Score bar
        HBox scoreRow = new HBox(8);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        Label scoreIcon = new Label("🎯");
        scoreIcon.setStyle("-fx-font-size:11px;");
        ProgressBar pb = new ProgressBar(r.getScore() / 100.0);
        pb.setPrefWidth(130);
        pb.setMaxHeight(5);
        pb.setStyle("-fx-accent:" + scoreColor + ";");
        Label scoreVal = new Label(r.getScore() + "/100");
        scoreVal.setStyle("-fx-font-size:11px; -fx-text-fill:#4a5568; -fx-font-weight:bold;");
        scoreRow.getChildren().addAll(scoreIcon, pb, scoreVal);

        // Reason
        Label reason = new Label("💡 " + r.getReason());
        reason.setStyle("-fx-font-size:11px; -fx-text-fill:#6b46c1; -fx-wrap-text:true; -fx-font-style:italic;");
        reason.setWrapText(true);
        reason.setMaxWidth(250);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#f0f4f0;");

        // Buttons
        HBox btns = new HBox(8);
        Button btnRes = styledBtn("🎟 Réserver", "#9333ea", "white");
        Button btnIgn = styledBtn("✕ Ignorer", "#e2e8f0", "#4a5568");
        btnIgn.setStyle(btnIgn.getStyle() + "-fx-font-size:11.5px;");
        btnIgn.setOnAction(e -> cardsPane.getChildren().remove(
            (javafx.scene.Node) btnIgn.getScene().lookup("#card_" + rank)));
        btns.getChildren().addAll(btnRes, btnIgn);

        body.getChildren().addAll(title, meta, scoreRow, sep, reason, btns);

        VBox card = wrapCard(banner, body, tc[2]);
        return card;
    }

    private VBox buildTrendCard(Map<String, Object> m, int rank) {
        String type  = str(m, "type");
        String[] tc  = typeColors(type);
        int    trend = num(m, "recentReservations");
        int    avail = num(m, "availableSeats");

        StackPane banner = colorBanner(tc, rank, "🔥", type, trend + " rés.");

        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 14, 12, 14));
        body.setStyle("-fx-background-color:#ffffff;");

        Label title = new Label(str(m, "title"));
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2332; -fx-wrap-text:true;");
        title.setMaxWidth(250); title.setWrapText(true);

        VBox meta = new VBox(3);
        meta.getChildren().add(metaRow("📅", str(m, "date")));
        meta.getChildren().add(metaRow("📍", str(m, "lieu")));
        meta.getChildren().add(metaRow("🔥", trend + " réservations récentes"));

        Label avLabel = new Label(avail > 0 ? "✅ " + avail + " places dispo" : "⚠ Presque complet");
        avLabel.setStyle("-fx-font-size:11.5px; -fx-font-weight:bold; -fx-text-fill:" +
            (avail > 5 ? "#16a34a" : "#d97706") + ";");

        body.getChildren().addAll(title, meta, new Separator(), avLabel);
        return wrapCard(banner, body, tc[2]);
    }

    private VBox buildPopularCard(Map<String, Object> m, int rank) {
        String type     = str(m, "type");
        String[] tc     = typeColors(type);
        int    conf     = num(m, "confirmedParticipants");
        String fillRate = str(m, "fillRate");
        int    popScore = num(m, "popularityScore");

        StackPane banner = colorBanner(tc, rank, "⭐", type, "Score " + popScore);

        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 14, 12, 14));
        body.setStyle("-fx-background-color:#ffffff;");

        Label title = new Label(str(m, "title"));
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2332; -fx-wrap-text:true;");
        title.setMaxWidth(250); title.setWrapText(true);

        VBox meta = new VBox(3);
        meta.getChildren().add(metaRow("📅", str(m, "date")));
        meta.getChildren().add(metaRow("👥", conf + " participants confirmés"));
        meta.getChildren().add(metaRow("📊", "Taux remplissage : " + fillRate));

        // Popularity bar
        double fill = 0;
        try { fill = Double.parseDouble(fillRate.replace("%", "")) / 100.0; } catch (Exception ignored) {}
        ProgressBar pb = new ProgressBar(Math.min(fill, 1.0));
        pb.setPrefWidth(Double.MAX_VALUE);
        pb.setMaxHeight(6);
        pb.setStyle("-fx-accent:#9333ea;");

        body.getChildren().addAll(title, meta, pb);
        return wrapCard(banner, body, tc[2]);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private StackPane colorBanner(String[] tc, int rank, String icon, String type, String badge) {
        StackPane banner = new StackPane();
        banner.setPrefHeight(130);
        banner.setStyle(String.format(
            "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
            "-fx-background-radius:12 12 0 0;", tc[0], tc[1]));

        Label rankL = new Label("#" + rank);
        rankL.setStyle("-fx-background-color:rgba(0,0,0,0.3); -fx-text-fill:white;" +
            "-fx-background-radius:12; -fx-padding:3 8; -fx-font-size:11px; -fx-font-weight:bold;");
        StackPane.setAlignment(rankL, Pos.TOP_LEFT);
        StackPane.setMargin(rankL, new Insets(10, 0, 0, 10));

        Label badgeL = new Label(icon + " " + badge);
        badgeL.setStyle("-fx-background-color:rgba(0,0,0,0.25); -fx-text-fill:white;" +
            "-fx-background-radius:12; -fx-padding:3 8; -fx-font-size:11px;");
        StackPane.setAlignment(badgeL, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeL, new Insets(10, 10, 0, 0));

        Label emojiL = new Label(typeEmoji(type));
        emojiL.setStyle("-fx-font-size:40px; -fx-opacity:0.75;");
        StackPane.setAlignment(emojiL, Pos.CENTER);
        StackPane.setMargin(emojiL, new Insets(0, 0, 18, 0));

        Label typeL = new Label(type.toUpperCase());
        typeL.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:rgba(255,255,255,0.85);");
        StackPane.setAlignment(typeL, Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeL, new Insets(0, 0, 10, 10));

        banner.getChildren().addAll(rankL, badgeL, emojiL, typeL);
        return banner;
    }

    private VBox wrapCard(StackPane banner, VBox body, String borderColor) {
        VBox card = new VBox();
        card.setPrefWidth(275);
        card.setMaxWidth(275);
        card.setStyle(
            "-fx-background-color:#ffffff;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + borderColor + ";" +
            "-fx-border-width:0 0 0 4;" +
            "-fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.09),8,0,0,2);");
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
            .replace("rgba(0,0,0,0.09),8,0,0,2","rgba(0,0,0,0.18),16,0,0,4")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
            .replace("rgba(0,0,0,0.18),16,0,0,4","rgba(0,0,0,0.09),8,0,0,2")));
        card.getChildren().addAll(banner, body);
        return card;
    }

    private HBox metaRow(String icon, String text) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label i = new Label(icon); i.setStyle("-fx-font-size:11px;");
        Label t = new Label(text); t.setStyle("-fx-font-size:11.5px; -fx-text-fill:#4a5568;");
        t.setMaxWidth(210); t.setWrapText(false);
        row.getChildren().addAll(i, t);
        return row;
    }

    private Button styledBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle(String.format(
            "-fx-background-color:%s; -fx-text-fill:%s;" +
            "-fx-background-radius:7; -fx-font-size:12px;" +
            "-fx-padding:6 12; -fx-cursor:hand; -fx-font-weight:bold;", bg, fg));
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private void setActiveMode(String mode) {
        currentMode = mode;
        String active   = "-fx-background-color:#9333ea; -fx-text-fill:white; " +
                          "-fx-background-radius:20; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:12.5px;";
        String inactive = "-fx-background-color:#e2e8f0; -fx-text-fill:#4a5568; " +
                          "-fx-background-radius:20; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:12.5px;";
        btnModePersonal.setStyle("personal".equals(mode) ? active : inactive);
        btnModeTrend   .setStyle("trending".equals(mode) ? active : inactive);
        btnModePopular .setStyle("popular" .equals(mode) ? active : inactive);
        btnEnvoyerEmail.setDisable(false);
    }

    private void showPlaceholder(String msg) {
        cardsPane.getChildren().clear();
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill:#a0aab8; -fx-font-size:14px; -fx-padding:40;");
        cardsPane.getChildren().add(lbl);
    }

    private String[] typeColors(String type) {
        if (type == null) return new String[]{"#1a2332","#2d3748","#e2e8f0"};
        switch (type.toUpperCase()
            .replace("É","E").replace("È","E").replace("Ê","E")
            .replace("À","A").replace("Ç","C")) {
            case "DON DE SANG":         return new String[]{"#7f1d1d","#dc2626","#dc2626"};
            case "VACCINATION":         return new String[]{"#14532d","#16a34a","#16a34a"};
            case "CONFERENCE MEDICALE": return new String[]{"#1e3a5f","#2563eb","#2563eb"};
            case "CONSULTATION":        return new String[]{"#312e81","#7c3aed","#7c3aed"};
            case "SENSIBILISATION":     return new String[]{"#92400e","#d97706","#d97706"};
            case "ATELIER SANTE":       return new String[]{"#134e4a","#0d9488","#0d9488"};
            case "DEPISTAGE":           return new String[]{"#1e3a5f","#0284c7","#0284c7"};
            case "PREMIERS SECOURS":    return new String[]{"#7c2d12","#ea580c","#ea580c"};
            default:                    return new String[]{"#1a2332","#9333ea","#9333ea"};
        }
    }

    private String typeEmoji(String type) {
        if (type == null) return "🏥";
        switch (type.toUpperCase()
            .replace("É","E").replace("È","E").replace("Ê","E")
            .replace("À","A").replace("Ç","C")) {
            case "DON DE SANG":     return "🩸";
            case "VACCINATION":     return "💉";
            case "CONSULTATION":    return "🩺";
            case "SENSIBILISATION": return "❤";
            case "ATELIER SANTE":   return "🥗";
            case "DEPISTAGE":       return "🔬";
            case "PREMIERS SECOURS":return "🚑";
            default:                return "🏥";
        }
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString() : "";
    }
    private int num(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v != null ? v.toString() : "0"); } catch (Exception e) { return 0; }
    }
    private String shorten(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
    private String extractName(String email) {
        if (email == null) return "Utilisateur";
        int at = email.indexOf('@');
        String n = at > 0 ? email.substring(0, at) : email;
        return n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase();
    }
    private String initials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] p = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : p) if (!s.isEmpty()) sb.append(s.charAt(0));
        return sb.toString().toUpperCase();
    }
    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    @FXML public void handleNaviguerEvenements()   { tests.Main.showAdminEvents(); }
    @FXML public void handleNaviguerParticipants() { tests.Main.showAdminEventParticipants(); }
    @FXML public void handleNaviguerReservations() { tests.Main.showAdminEventReservations(); }
    @FXML public void handleNaviguerAnalytiques()  { tests.Main.showAdminEventAnalytics(); }
    @FXML public void handleLogout()               { tests.Main.showLoginPage(); }
}
