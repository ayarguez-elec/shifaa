package Controler;

import entities.ReclamationNotification;
import entities.Reclamation;
import entities.Reponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.ReclamationNotificationService;
import services.ReclamationServices;
import services.ReponseServices;
import tests.Main;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * UserDashboardController
 *
 * FIXES applied:
 *
 * 1. countByStatus() used .contains("resolu") which NEVER matches "Résolu"
 *    (accented é). Fixed with accent-normalised comparison using
 *    java.text.Normalizer — strips diacritics before comparing.
 *
 * 2. loadLastResponses() now shows BOTH admin responses AND status-change
 *    notifications (type STATUT_CHANGE / RESOLUTION) as a unified activity
 *    timeline, sorted newest first.
 *
 * 3. The recent reclamations TableView (ROW 4) has been removed entirely
 *    from both this controller and the FXML.
 */
public class UserDashboardController implements Initializable {

    private int getUserId() {
        return tests.Main.getCurrentUserId();
    }

    // ── Services ─────────────────────────────────────────────────────────────
    private final ReclamationServices reclamationSvc = new ReclamationServices();
    private final ReponseServices     reponseSvc     = new ReponseServices();
    private final ReclamationNotificationService notifSvc       = new ReclamationNotificationService();

    // ── KPI Labels ───────────────────────────────────────────────────────────
    @FXML private Label lblTotalReclamations;
    @FXML private Label lblTotalTrend;
    @FXML private Label lblEnAttente;
    @FXML private Label lblEnAttentePct;
    @FXML private Label lblResolues;
    @FXML private Label lblResoluesTrend;
    @FXML private Label lblNouvellesReponses;
    @FXML private Label lblReponsesInfo;
    @FXML private Label lblNewResponsesBadge;

    // ── Priority Labels ───────────────────────────────────────────────────────
    @FXML private Label lblUrgente;
    @FXML private Label lblMoyenne;
    @FXML private Label lblFaible;

    // ── Charts ────────────────────────────────────────────────────────────────
    @FXML private PieChart                 statusPieChart;
    @FXML private BarChart<String, Number> categoryBarChart;
    @FXML private CategoryAxis             categoryXAxis;
    @FXML private NumberAxis               categoryYAxis;

    // ── Dynamic containers ────────────────────────────────────────────────────
    @FXML private VBox pendingBox;
    @FXML private VBox responsesBox;

    @FXML private Button btnRefresh;

    // ── Data cache ────────────────────────────────────────────────────────────
    private List<Reclamation> myReclamations = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAll();
    }

    // ── LOAD ALL ─────────────────────────────────────────────────────────────
    private void loadAll() {
        myReclamations = reclamationSvc.getByUser(getUserId());

        runSafe(this::loadKpis,           "loadKpis");
        runSafe(this::loadPieChart,        "loadPieChart");
        runSafe(this::loadCategoryChart,   "loadCategoryChart");
        runSafe(this::loadPendingAlerts,   "loadPendingAlerts");
        runSafe(this::loadActivityFeed,    "loadActivityFeed");
    }

    private void runSafe(Runnable r, String name) {
        try { r.run(); }
        catch (Exception e) { System.out.println(name + " error: " + e.getMessage()); e.printStackTrace(); }
    }

    // ── KPI CARDS ────────────────────────────────────────────────────────────
    private void loadKpis() {
        long total     = myReclamations.size();
        long enAttente = countByStatus("en attente");
        long enCours   = countByStatus("en cours");
        long resolu    = countByStatus("resolu");       // FIX: accent-normalised
        long urgente   = countByPriority("urgente");
        long moyenne   = countByPriority("moyenne");
        long faible    = countByPriority("faible");

        // Count all responses for this user's reclamations
        int totalResponses = 0;
        for (Reclamation r : myReclamations) {
            totalResponses += reponseSvc.getByReclamation(r.getReclamationId()).size();
        }

        set(lblTotalReclamations, String.valueOf(total));
        set(lblTotalTrend, "Vos réclamations actives");
        set(lblEnAttente, String.valueOf(enAttente));
        set(lblEnAttentePct, total > 0
                ? ((int) Math.round(enAttente * 100.0 / total)) + "% du total" : "");
        set(lblResolues, String.valueOf(resolu));
        set(lblResoluesTrend, total > 0
                ? ((int) Math.round(resolu * 100.0 / total)) + "% résolu" : "");
        set(lblNouvellesReponses, String.valueOf(totalResponses));
        set(lblReponsesInfo, totalResponses + " réponse(s) au total");

        if (lblNewResponsesBadge != null) {
            lblNewResponsesBadge.setVisible(totalResponses > 0);
            lblNewResponsesBadge.setText("● " + totalResponses + " réponse(s)");
        }


        set(lblUrgente, String.valueOf(urgente));
        set(lblMoyenne, String.valueOf(moyenne));
        set(lblFaible,  String.valueOf(faible));
    }

    /**
     * FIX — accent-safe status comparison.
     *
     * Problem: "Résolu".toLowerCase() = "résolu" which does NOT contain "resolu".
     * Solution: strip diacritics with Normalizer before comparing.
     *   "résolu" → normalise → "resolu" ✓
     */
    private long countByStatus(String needle) {
        String normNeedle = normalise(needle);
        return myReclamations.stream()
                .filter(r -> r.getStatusreclam() != null
                        && normalise(r.getStatusreclam()).contains(normNeedle))
                .count();
    }

    private long countByPriority(String needle) {
        return myReclamations.stream()
                .filter(r -> r.getPriority() != null
                        && normalise(r.getPriority()).equalsIgnoreCase(normalise(needle)))
                .count();
    }

    /**
     * Strips accents/diacritics and lowercases for reliable string comparison.
     * "Résolu" → "resolu", "En Attente" → "en attente"
     */
    private String normalise(String s) {
        if (s == null) return "";
        String decomposed = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    // ── PIE CHART ────────────────────────────────────────────────────────────
    private void loadPieChart() {
        long enAttente = countByStatus("en attente");
        long enCours   = countByStatus("en cours");
        long resolu    = countByStatus("resolu");

        statusPieChart.getData().clear();
        statusPieChart.layout();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("En Attente", Math.max(enAttente, 0.01)),
                new PieChart.Data("En Cours",   Math.max(enCours,   0.01)),
                new PieChart.Data("Résolu",     Math.max(resolu,    0.01))
        );
        statusPieChart.setData(data);
        statusPieChart.setLegendVisible(true);
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setAnimated(false);
        statusPieChart.setClockwise(true);
        statusPieChart.setStartAngle(90);

        Platform.runLater(() -> {
            String[] colors = {"#378ADD", "#EF9F27", "#639922"};
            int i = 0;
            for (PieChart.Data d : statusPieChart.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color:" + colors[i % colors.length] + ";");
                }
                if (i == 0) d.setName("En Attente (" + enAttente + ")");
                else if (i == 1) d.setName("En Cours (" + enCours + ")");
                else if (i == 2) d.setName("Résolu (" + resolu + ")");
                i++;
            }
            statusPieChart.layout();
        });
    }

    // ── BAR CHART ────────────────────────────────────────────────────────────
    private void loadCategoryChart() {
        Map<String, Integer> counts = new HashMap<>();
        for (Reclamation r : myReclamations) {
            String cat = r.getCategory() == null ? "Autre" : r.getCategory();
            if (cat.equalsIgnoreCase("Général")) cat = "Autre";
            counts.merge(cat, 1, Integer::sum);
        }

        categoryBarChart.getData().clear();
        categoryBarChart.setAnimated(false);
        categoryXAxis.setTickLabelsVisible(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réclamations");

        List<String> categories = new ArrayList<>();
        String[] barColors = {"#378ADD", "#EF9F27", "#639922", "#BA7517", "#D4537E", "#534AB7"};
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            categories.add(entry.getKey());
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        categoryXAxis.setCategories(FXCollections.observableArrayList(categories));
        categoryBarChart.getData().clear();
        categoryBarChart.getData().add(series);

        Platform.runLater(() -> {
            int i = 0;
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill:" + barColors[i % barColors.length] + ";");
                }
                i++;
            }
        });
    }

    // ── PENDING ALERTS ────────────────────────────────────────────────────────
    private void loadPendingAlerts() {
        pendingBox.getChildren().clear();

        List<Reclamation> pending = new ArrayList<>();
        for (Reclamation r : myReclamations) {
            if (r.getStatusreclam() != null
                    && normalise(r.getStatusreclam()).contains("attente")) {
                pending.add(r);
            }
        }

        if (pending.isEmpty()) {
            Label ok = new Label("✅ Aucune réclamation en attente");
            ok.setStyle("-fx-text-fill:#3B6D11; -fx-font-size:13px; -fx-padding:8 0 8 0;");
            pendingBox.getChildren().add(ok);
            return;
        }
        for (Reclamation r : pending) {
            pendingBox.getChildren().add(buildPendingCard(r));
        }
    }

    private VBox buildPendingCard(Reclamation r) {
        VBox card = new VBox(4);
        long daysOld = 0;
        try {
            daysOld = ChronoUnit.DAYS.between(r.getReclamDate().toLocalDate(), LocalDate.now());
        } catch (Exception ignored) {}

        boolean isUrgent = normalise(r.getPriority()).equals("urgente");
        boolean isOld    = daysOld > 3;

        card.setStyle(
                "-fx-background-color:" + (isUrgent && isOld ? "#fff5f5" : "#fffbf0") + ";"
                        + "-fx-border-color:"   + (isUrgent && isOld ? "#E24B4A" : "#EF9F27") + ";"
                        + "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 12 10 12;"
        );

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label idLbl = new Label("REC-" + r.getReclamationId());
        idLbl.setStyle("-fx-font-weight:bold; -fx-font-size:12px; -fx-text-fill:#0b1f44;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean urg = normalise(r.getPriority()).equals("urgente");
        Label badge = new Label(r.getPriority());
        badge.setStyle(urg
                ? "-fx-background-color:#FCEBEB; -fx-text-fill:#A32D2D;"
                + "-fx-font-size:10px; -fx-font-weight:bold;"
                + "-fx-background-radius:10; -fx-padding:2 7 2 7;"
                : "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;"
                + "-fx-font-size:10px; -fx-font-weight:bold;"
                + "-fx-background-radius:10; -fx-padding:2 7 2 7;"
        );
        topRow.getChildren().addAll(idLbl, spacer, badge);

        Label subj = new Label(r.getSubject());
        subj.setStyle("-fx-font-size:13px; -fx-text-fill:#0b1f44;");
        subj.setWrapText(true);

        Label meta;
        if (isUrgent && isOld) {
            meta = new Label("⚠ Urgent · " + daysOld + " jours sans réponse · SLA dépassé");
            meta.setStyle("-fx-font-size:11px; -fx-text-fill:#A32D2D;");
        } else {
            meta = new Label("Depuis " + daysOld + " jours · " + r.getCategory());
            meta.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");
        }
        card.getChildren().addAll(topRow, subj, meta);
        return card;
    }

    // ── ACTIVITY FEED (responses + status changes) ───────────────────────────
    /**
     * FIX: replaced loadLastResponses() with a unified activity feed that shows:
     *   - Admin responses (from reponse table)
     *   - Status-change notifications (type STATUT_CHANGE / RESOLUTION)
     * Both sorted newest first.
     */
    private void loadActivityFeed() {
        responsesBox.getChildren().clear();

        List<ActivityItem> items = new ArrayList<>();

        // 1. Collect admin responses
        for (Reclamation r : myReclamations) {
            for (Reponse rep : reponseSvc.getByReclamation(r.getReclamationId())) {
                items.add(new ActivityItem(
                        "REPONSE",
                        "✉",
                        "#E6F1FB",
                        "REC-" + r.getReclamationId() + " : " + truncate(rep.getContent(), 80),
                        "Répondu le " + formatTs(rep.getResponseDate()) + " · Admin",
                        rep.getResponseDate() != null
                                ? rep.getResponseDate().toInstant() : java.time.Instant.EPOCH
                ));
            }
        }

        // 2. Collect status-change / resolution notifications for this user
        try {
            List<ReclamationNotification> notifs = notifSvc.getByUser(getUserId());
            for (ReclamationNotification n : notifs) {
                String type = n.getType() == null ? "" : n.getType();
                if (type.equals("STATUT_CHANGE") || type.equals("RESOLUTION")) {
                    String icon  = type.equals("RESOLUTION") ? "✅" : "🔄";
                    String color = type.equals("RESOLUTION") ? "#E8F5E9" : "#E6F1FB";
                    items.add(new ActivityItem(
                            type, icon, color,
                            n.getMessage(),
                            formatTs(n.getCreatedAt()),
                            n.getCreatedAt() != null
                                    ? n.getCreatedAt().toInstant() : java.time.Instant.EPOCH
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Notification feed error: " + e.getMessage());
        }

        // Sort newest first
        items.sort((a, b) -> b.instant.compareTo(a.instant));

        if (items.isEmpty()) {
            Label empty = new Label("📭 Aucune activité récente");
            empty.setStyle("-fx-text-fill:#5d6f91; -fx-font-size:13px; -fx-padding:8 0 8 0;");
            responsesBox.getChildren().add(empty);
            return;
        }

        int limit = Math.min(items.size(), 15);
        for (int i = 0; i < limit; i++) {
            responsesBox.getChildren().add(buildActivityRow(items.get(i)));
        }
    }

    private HBox buildActivityRow(ActivityItem item) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setStyle(
                "-fx-border-color:transparent transparent #e1e8f0 transparent;"
                        + "-fx-border-width:0 0 1 0;"
        );

        Label icon = new Label(item.icon);
        icon.setPrefWidth(32);
        icon.setPrefHeight(32);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle(
                "-fx-background-color:" + item.bgColor + ";"
                        + "-fx-background-radius:16; -fx-font-size:14px;"
        );

        VBox body = new VBox(2);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label msg = new Label(item.title);
        msg.setStyle("-fx-font-size:12px; -fx-text-fill:#0b1f44;");
        msg.setWrapText(true);

        Label sub = new Label(item.subtitle);
        sub.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

        body.getChildren().addAll(msg, sub);
        row.getChildren().addAll(icon, body);
        return row;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String formatTs(java.sql.Timestamp ts) {
        if (ts == null) return "—";
        try {
            return ts.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return ts.toString();
        }
    }

    @FXML
    private void refreshDashboard() {
        loadAll();
        System.out.println("[UserDashboard] refreshed");
    }

    @FXML private void ouvrirUserDashboard()    { tests.Main.changeScene("/userdashboard.fxml"); }
    @FXML private void ouvrirUserReclamations() { tests.Main.changeScene("/userreclamations.fxml"); }

    // ── Inner classes ─────────────────────────────────────────────────────────

    private static class ActivityItem {
        final String            type;
        final String            icon;
        final String            bgColor;
        final String            title;
        final String            subtitle;
        final java.time.Instant instant;

        ActivityItem(String type, String icon, String bgColor,
                     String title, String subtitle, java.time.Instant instant) {
            this.type    = type;
            this.icon    = icon;
            this.bgColor = bgColor;
            this.title   = title;
            this.subtitle = subtitle;
            this.instant = instant;
        }
    }
}
