package controller;

import entities.Notification;
import entities.Reclamation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EscalationService;
import services.NotificationService;
import services.ReclamationServices;
import services.UserService;
import tests.MainFx;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class AdminDashboardController implements Initializable {

    private final ReclamationServices svc     = new ReclamationServices();
    private final UserService         userSvc = new UserService();
    private final NotificationService notifSvc = new NotificationService();
    private final EscalationService   escSvc  = new EscalationService();

    // ── KPI Labels ────────────────────────────────────────────────────────────
    @FXML private Label lblTotalReclamations;
    @FXML private Label lblTotalTrend;
    @FXML private Label lblAvgResponseTime;
    @FXML private Label lblAvgTrend;
    @FXML private Label lblEnAttente;
    @FXML private Label lblEnAttentePct;
    @FXML private Label lblUrgentesNonRep;
    @FXML private Label lblEscaladesCount;
    @FXML private Label lblEscaladesBadge;

    // ── Priority Labels ───────────────────────────────────────────────────────
    @FXML private Label lblUrgente;
    @FXML private Label lblMoyenne;
    @FXML private Label lblFaible;

    // ── Suggest Labels ────────────────────────────────────────────────────────
    @FXML private Label lblSuggestKeywords;
    @FXML private Label lblSuggestCategory;

    // ── Sidebar notification badge ────────────────────────────────────────────
    @FXML private Label lblNotifBadge;

    // ── Charts ────────────────────────────────────────────────────────────────
    @FXML private PieChart                    statusPieChart;
    @FXML private LineChart<String, Number>   trendLineChart;
    @FXML private CategoryAxis                trendXAxis;
    @FXML private NumberAxis                  trendYAxis;

    // ── Dynamic VBox containers ───────────────────────────────────────────────
    @FXML private VBox categoriesBox;
    @FXML private VBox escaladeBox;
    @FXML private VBox notifBox;

    // ── Recent Table ──────────────────────────────────────────────────────────
    @FXML private TableView<Reclamation>          recentTable;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String>  colSubject;
    @FXML private TableColumn<Reclamation, String>  colCategory;
    @FXML private TableColumn<Reclamation, String>  colPriority;
    @FXML private TableColumn<Reclamation, String>  colStatus;
    @FXML private TableColumn<Reclamation, String>  colDate;

    @FXML private Button btnRefresh;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try { setupTable(); } catch (Exception e) { System.out.println("setupTable error: " + e.getMessage()); }
        loadAll();
        refreshNotifBadge();
    }

    // ── NOTIFICATION BADGE ────────────────────────────────────────────────────
    private void refreshNotifBadge() {
        try {
            int unread = notifSvc.countUnread(1);
            if (lblNotifBadge != null) {
                if (unread > 0) {
                    lblNotifBadge.setText(String.valueOf(unread));
                    lblNotifBadge.setVisible(true);
                    lblNotifBadge.setManaged(true);
                } else {
                    lblNotifBadge.setVisible(false);
                    lblNotifBadge.setManaged(false);
                }
            }
        } catch (Exception e) {
            System.err.println("[AdminDashboard] notif badge error: " + e.getMessage());
        }
    }

    // ── TABLE SETUP ───────────────────────────────────────────────────────────
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("reclamationId"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusreclam"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("reclamDate"));

        colPriority.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if (val.equalsIgnoreCase("urgente"))       setStyle("-fx-text-fill:#A32D2D; -fx-font-weight:bold;");
                else if (val.equalsIgnoreCase("moyenne"))  setStyle("-fx-text-fill:#854F0B; -fx-font-weight:bold;");
                else                                       setStyle("-fx-text-fill:#3B6D11; -fx-font-weight:bold;");
            }
        });

        colStatus.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                String l = val.toLowerCase();
                if      (l.contains("attente")) setStyle("-fx-text-fill:#854F0B;");
                else if (l.contains("cours"))   setStyle("-fx-text-fill:#185FA5;");
                else if (l.contains("solu"))    setStyle("-fx-text-fill:#3B6D11;");
                else                            setStyle("-fx-text-fill:#A32D2D; -fx-font-weight:bold;");
            }
        });

        recentTable.setPlaceholder(new Label("Aucune reclamation recente"));
    }

    // ── LOAD ALL ──────────────────────────────────────────────────────────────
    private void loadAll() {
        try { loadKpis();          } catch (Exception e) { System.out.println("loadKpis: "          + e.getMessage()); }
        try { loadPieChart();      } catch (Exception e) { System.out.println("loadPieChart: "      + e.getMessage()); }
        try { loadTrendChart();    } catch (Exception e) { System.out.println("loadTrendChart: "    + e.getMessage()); }
        try { loadCategories();    } catch (Exception e) { System.out.println("loadCategories: "    + e.getMessage()); }
        try { loadEscalades();     } catch (Exception e) { System.out.println("loadEscalades: "     + e.getMessage()); }
        try { loadNotifications(); } catch (Exception e) { System.out.println("loadNotifications: " + e.getMessage()); }
        try { loadRecentTable();   } catch (Exception e) { System.out.println("loadRecentTable: "   + e.getMessage()); }
        try { loadAutoSuggest();   } catch (Exception e) { System.out.println("loadAutoSuggest: "   + e.getMessage()); }
    }

    // ── KPI CARDS ─────────────────────────────────────────────────────────────
    // ── KPI CARDS ─────────────────────────────────────────────────────────────
    private void loadKpis() {
        try {
            long total = svc.getTotalReclamations();
            set(lblTotalReclamations, String.valueOf(total));
            set(lblTotalTrend, "Total actif dans le systeme");
        } catch (Exception e) {
            set(lblTotalReclamations, "0");
            set(lblTotalTrend, "N/A");
        }

        try {
            double avg = svc.getAverageResponseTime();
            set(lblAvgResponseTime, String.format("%.1f j", avg));
            set(lblAvgTrend, avg > 2 ? "Au-dessus de la moyenne" : "Bon delai");
        } catch (Exception e) {
            set(lblAvgResponseTime, "0.0 j");
            set(lblAvgTrend, "N/A");
        }

        try {
            long enAttente = svc.getReclamationsByStatus("en attente");
            set(lblEnAttente, String.valueOf(enAttente));
            long total = svc.getTotalReclamations();
            set(lblEnAttentePct, total > 0 ? ((int) Math.round(enAttente * 100.0 / total)) + "% du total" : "");
        } catch (Exception e) {
            set(lblEnAttente, "0");
            set(lblEnAttentePct, "");
        }

        try {
            long urgNonRep = svc.getUrgentesNonRepondues();
            set(lblUrgentesNonRep, String.valueOf(urgNonRep));
            if (lblUrgentesNonRep != null && urgNonRep > 0)
                lblUrgentesNonRep.setStyle("-fx-text-fill:#E24B4A; -fx-font-size:28px; -fx-font-weight:bold;");
        } catch (Exception e) {
            set(lblUrgentesNonRep, "0");
        }

        try {
            // Count escalations from ALL sources:
            // 1. Auto-escalated reclamations (urgent + pending + >3 days + no response)
            // 2. Manual escalations from escalation table
            // 3. Escalation notifications (fallback if table is empty)

            Set<Integer> escalatedIds = new HashSet<>();

            // Source 1: Auto-escalated
            List<Reclamation> autoList = svc.getEscalatedReclamations(3);
            for (Reclamation r : autoList) escalatedIds.add(r.getReclamationId());

            // Source 2: Manual escalations from escalation table
            List<Map<String, Object>> manualList = escSvc.getAllOpen();
            for (Map<String, Object> m : manualList) {
                Integer recId = (Integer) m.get("reclamationId");
                if (recId != null) escalatedIds.add(recId);
            }

            // Source 3: Escalation notifications (fallback — checks notification table)
            try {
                List<Notification> notifs = notifSvc.getByUser(1);
                for (Notification n : notifs) {
                    String type = n.getType();
                    if (type != null && type.toUpperCase().contains("ESCALADE")) {
                        int recId = n.getReclamationId();
                        if (recId > 0) escalatedIds.add(recId);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Escalade notification fallback failed: " + e.getMessage());
            }

            int escCount = escalatedIds.size();
            set(lblEscaladesCount, "dont " + escCount + " escaladee(s)");
            if (lblEscaladesBadge != null) {
                if (escCount > 0) {
                    lblEscaladesBadge.setText("● " + escCount + " escaladee(s)");
                    lblEscaladesBadge.setVisible(true);
                    lblEscaladesBadge.setManaged(true);
                } else {
                    lblEscaladesBadge.setVisible(false);
                    lblEscaladesBadge.setManaged(false);
                }
            }
        } catch (Exception e) {
            set(lblEscaladesCount, "dont 0 escaladee(s)");
            if (lblEscaladesBadge != null) { lblEscaladesBadge.setVisible(false); lblEscaladesBadge.setManaged(false); }
        }

        try {
            long urgente = svc.getReclamationsByPriority("urgente");
            long moyenne = svc.getReclamationsByPriority("moyenne");
            long faible  = svc.getReclamationsByPriority("faible");
            set(lblUrgente, String.valueOf(urgente));
            set(lblMoyenne, String.valueOf(moyenne));
            set(lblFaible,  String.valueOf(faible));
        } catch (Exception e) {
            set(lblUrgente, "0"); set(lblMoyenne, "0"); set(lblFaible, "0");
        }
    }

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    // ── PIE CHART ─────────────────────────────────────────────────────────────
    private void loadPieChart() {
        long enAttente = svc.getReclamationsByStatus("en attente");
        long enCours   = svc.getReclamationsByStatus("en cours");
        long resolu    = svc.getReclamationsByStatus("resolu");

        statusPieChart.getData().clear();
        statusPieChart.layout();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("En Attente", Math.max(enAttente, 0.01)),
                new PieChart.Data("En Cours",   Math.max(enCours,   0.01)),
                new PieChart.Data("Resolu",     Math.max(resolu,    0.01))
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
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                if      (i == 0) d.setName("En Attente (" + enAttente + ")");
                else if (i == 1) d.setName("En Cours ("   + enCours   + ")");
                else if (i == 2) d.setName("Resolu ("     + resolu    + ")");
                i++;
            }
            statusPieChart.layout();
        });
    }

    // ── LINE CHART (FIX: integer Y-axis) ─────────────────────────────────────
    private void loadTrendChart() {
        Map<String, Integer> trend = svc.getDailyTrend(7);

        // Find max value across all 7 days
        int maxVal = trend.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int upperBound = Math.max(maxVal, 1); // at least 1 so the axis isn't flat

        // Force integer-only ticks on Y axis
        trendYAxis.setAutoRanging(false);
        trendYAxis.setLowerBound(0);
        trendYAxis.setUpperBound(upperBound);
        trendYAxis.setTickUnit(1);          // step of 1 → only whole numbers
        trendYAxis.setMinorTickCount(0);    // no fractional minor ticks
        trendYAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override public String toString(Number n)   { return String.valueOf(n.intValue()); }
            @Override public Number fromString(String s) { return Integer.parseInt(s); }
        });

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouvelles reclamations");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            String dayKey = LocalDate.now().minusDays(i).toString();
            String dayLbl = LocalDate.now().minusDays(i).format(fmt);
            series.getData().add(
                    new XYChart.Data<>(dayLbl, trend.getOrDefault(dayKey, 0))
            );
        }

        trendLineChart.getData().clear();
        trendLineChart.getData().add(series);
        trendLineChart.setAnimated(false);
        trendLineChart.setCreateSymbols(true);

        Platform.runLater(() -> {
            if (!trendLineChart.getData().isEmpty()) {
                XYChart.Series<String, Number> s = trendLineChart.getData().get(0);
                if (s.getNode() != null)
                    s.getNode().setStyle("-fx-stroke:#378ADD; -fx-stroke-width:2.5px;");
            }
        });
    }

    // ── CATEGORIES PROGRESS BARS ──────────────────────────────────────────────
    private void loadCategories() {
        categoriesBox.getChildren().clear();
        List<String> cats = svc.getMostCommonCategories(5);

        long max = 1;
        List<String[]> parsed = new ArrayList<>();
        for (String c : cats) {
            int pOpen  = c.lastIndexOf('(');
            int pClose = c.lastIndexOf(')');
            if (pOpen > 0 && pClose > pOpen) {
                String name  = c.substring(0, pOpen).trim();
                int    count = Integer.parseInt(c.substring(pOpen + 1, pClose).trim());
                if (name.equalsIgnoreCase("Général")) name = "Autre";

                boolean found = false;
                for (String[] existing : parsed) {
                    if (existing[0].equalsIgnoreCase(name)) {
                        existing[1] = String.valueOf(Integer.parseInt(existing[1]) + count);
                        found = true;
                        break;
                    }
                }
                if (!found) parsed.add(new String[]{name, String.valueOf(count)});

                for (String[] entry : parsed) {
                    int ec = Integer.parseInt(entry[1]);
                    if (ec > max) max = ec;
                }
            }
        }

        String[]   barColors = {"#378ADD", "#EF9F27", "#639922", "#BA7517", "#D4537E"};
        final long maxFinal  = max;
        int idx = 0;

        for (String[] entry : parsed) {
            String name  = entry[0];
            int    count = Integer.parseInt(entry[1]);
            double pct   = (double) count / maxFinal;

            Label lName = new Label(name);
            lName.setPrefWidth(100);
            lName.setStyle("-fx-font-size:12px; -fx-text-fill:#5d6f91;");

            StackPane barBg = new StackPane();
            barBg.setPrefHeight(8);
            barBg.setStyle("-fx-background-color:#f0f4fa; -fx-background-radius:4;");
            HBox.setHgrow(barBg, Priority.ALWAYS);

            HBox barFill = new HBox();
            barFill.setPrefHeight(8);
            barFill.setMaxHeight(8);
            barFill.setPrefWidth(pct * 180);
            barFill.setStyle("-fx-background-color:" + barColors[idx % barColors.length] + "; -fx-background-radius:4;");
            StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
            barBg.getChildren().add(barFill);

            Label lCount = new Label(String.valueOf(count));
            lCount.setPrefWidth(28);
            lCount.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0b1f44;");

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(lName, barBg, lCount);
            categoriesBox.getChildren().add(row);
            idx++;
        }
    }

    // ── ESCALADE ALERTS (FIX: merges auto + manual escalations) ──────────────
    // ── ESCALADE ALERTS (FIXED: detects all escalation sources) ──────────────
    private void loadEscalades() {
        escaladeBox.getChildren().clear();

        Set<Integer> shownIds = new HashSet<>();
        List<javafx.scene.Node> cards = new ArrayList<>();

        // ── Source 1: Auto-escalated reclamations ─────────────────────────
        List<Reclamation> autoList;
        try {
            autoList = svc.getEscalatedReclamations(3);
        } catch (Exception e) {
            System.out.println("⚠️ loadEscalades auto failed: " + e.getMessage());
            autoList = new ArrayList<>();
        }

        for (Reclamation r : autoList) {
            shownIds.add(r.getReclamationId());
            long daysOld = ChronoUnit.DAYS.between(r.getReclamDate().toLocalDate(), LocalDate.now());
            cards.add(buildEscaladeCard(
                    "REC-" + r.getReclamationId(),
                    r.getSubject(),
                    "Urgente · " + daysOld + " j sans réponse · SLA dépassé",
                    "AUTO",
                    r,
                    null
            ));
        }

        // ── Source 2: Manual escalations from escalation table ──────────
        List<Map<String, Object>> manualList;
        try {
            manualList = escSvc.getAllOpen();
        } catch (Exception e) {
            System.out.println("⚠️ loadEscalades manual failed: " + e.getMessage());
            manualList = new ArrayList<>();
        }

        for (Map<String, Object> esc : manualList) {
            int recId = (Integer) esc.get("reclamationId");
            if (shownIds.contains(recId)) continue;
            shownIds.add(recId);

            String subject = (String) esc.get("subject");
            String reason  = (String) esc.get("reason");
            Integer urgency = (Integer) esc.get("urgency");

            String meta = "Escalade manuelle";
            if (reason != null && !reason.isBlank()) meta += " · Raison : " + reason;
            if (urgency != null) meta += " [Urgence : " + urgency + "/10]";

            cards.add(buildEscaladeCard(
                    "REC-" + recId,
                    subject != null ? subject : "—",
                    meta,
                    "MANUELLE",
                    null,
                    recId
            ));
        }

        // ── Source 3: Escalation notifications (fallback) ───────────────
        // This catches cases where escalation was sent but not stored in escalation table
        try {
            List<Notification> notifs = notifSvc.getByUser(1);
            for (Notification n : notifs) {
                String type = n.getType();
                if (type == null) continue;

                String upperType = type.toUpperCase();
                if (!upperType.contains("ESCALADE")) continue;

                int recId = n.getReclamationId();
                if (recId <= 0 || shownIds.contains(recId)) continue;

                shownIds.add(recId);

                // Parse notification message to extract info
                String msg = n.getMessage();
                String subject = "Inconnu";
                String reason = "";
                Integer urgency = null;

                // Try to extract from message: "Escalade manuelle demandée pour 'X' — Raison : Y [Urgence : Z/10]"
                if (msg != null) {
                    int pourIdx = msg.indexOf("pour \"");
                    if (pourIdx > 0) {
                        int endQuote = msg.indexOf("\"", pourIdx + 6);
                        if (endQuote > 0) subject = msg.substring(pourIdx + 6, endQuote);
                    }

                    int raisonIdx = msg.indexOf("Raison :");
                    if (raisonIdx > 0) {
                        int bracketIdx = msg.indexOf("[", raisonIdx);
                        if (bracketIdx > 0) reason = msg.substring(raisonIdx + 8, bracketIdx).trim();
                        else reason = msg.substring(raisonIdx + 8).trim();
                    }

                    int urgIdx = msg.indexOf("Urgence :");
                    if (urgIdx > 0) {
                        int slashIdx = msg.indexOf("/", urgIdx);
                        if (slashIdx > 0) {
                            try {
                                urgency = Integer.parseInt(msg.substring(urgIdx + 9, slashIdx).trim());
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                String meta = "Escalade manuelle (notification)";
                if (!reason.isEmpty()) meta += " · Raison : " + reason;
                if (urgency != null) meta += " [Urgence : " + urgency + "/10]";

                cards.add(buildEscaladeCard(
                        "REC-" + recId,
                        subject,
                        meta,
                        "MANUELLE",
                        null,
                        recId
                ));
            }
        } catch (Exception e) {
            System.out.println("⚠️ loadEscalades notification fallback failed: " + e.getMessage());
        }

        // ── Render ────────────────────────────────────────────────────────
        if (cards.isEmpty()) {
            Label ok = new Label("✅ Aucune escalade active");
            ok.setStyle("-fx-text-fill:#3B6D11; -fx-font-size:13px; -fx-padding:8 0 8 0;");
            escaladeBox.getChildren().add(ok);
            if (lblEscaladesBadge != null) { lblEscaladesBadge.setVisible(false); lblEscaladesBadge.setManaged(false); }
            return;
        }

        escaladeBox.getChildren().addAll(cards);

        if (lblEscaladesBadge != null) {
            lblEscaladesBadge.setText("● " + cards.size() + " escaladée(s)");
            lblEscaladesBadge.setVisible(true);
            lblEscaladesBadge.setManaged(true);
        }
    }

    /**
     * Builds one escalade card for either an auto or manual escalation.
     * Pass reclamation object for auto escalations, reclamationId integer for manual ones.
     */
    private VBox buildEscaladeCard(String idLabel, String subject, String metaText,
                                   String badgeLabel, Reclamation recObj, Integer recIdFallback) {
        VBox card = new VBox(4);
        card.setStyle(
                "-fx-background-color:#fff5f5;"
                        + "-fx-border-color:#E24B4A;"
                        + "-fx-border-radius:8;"
                        + "-fx-background-radius:8;"
                        + "-fx-padding:10 12 10 12;"
        );

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill:#E24B4A; -fx-font-size:10px;");

        Label idLbl = new Label(idLabel);
        idLbl.setStyle("-fx-font-weight:bold; -fx-font-size:12px; -fx-text-fill:#A32D2D;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("ESCALADÉE · " + badgeLabel);
        badge.setStyle(
                "-fx-background-color:#FCEBEB;"
                        + "-fx-text-fill:#A32D2D;"
                        + "-fx-font-size:10px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-background-radius:10;"
                        + "-fx-padding:2 7 2 7;"
        );
        topRow.getChildren().addAll(dot, idLbl, spacer, badge);

        Label subj = new Label(subject);
        subj.setStyle("-fx-font-size:13px; -fx-text-fill:#0b1f44;");
        subj.setWrapText(true);

        Label meta = new Label(metaText);
        meta.setStyle("-fx-font-size:11px; -fx-text-fill:#A32D2D;");
        meta.setWrapText(true);

        Button btn = new Button("Répondre →");
        btn.setStyle(
                "-fx-background-color:white;"
                        + "-fx-border-color:#dce5f0;"
                        + "-fx-border-radius:6;"
                        + "-fx-background-radius:6;"
                        + "-fx-font-size:11px;"
                        + "-fx-cursor:hand;"
                        + "-fx-padding:4 10 4 10;"
        );

        if (recObj != null) {
            btn.setOnAction(e -> ouvrirAjouterReponse(recObj));
        } else {
            final int rid = recIdFallback;
            btn.setOnAction(e -> {
                Reclamation r = svc.getById(rid);
                if (r != null) ouvrirAjouterReponse(r);
            });
        }

        card.getChildren().addAll(topRow, subj, meta, btn);
        return card;
    }

    // ── MODAL DIALOG: AJOUTER REPONSE ─────────────────────────────────────────
    private void ouvrirAjouterReponse(Reclamation r) {
        try {
            URL fxmlUrl = getClass().getResource("/adminajouterreponse.fxml");
            if (fxmlUrl == null) { showError("Fichier adminajouterreponse.fxml introuvable."); return; }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminAjouterReponseController ctrl = loader.getController();
            if (ctrl == null) { showError("Erreur: controleur non initialise."); return; }

            ctrl.initData(r, userSvc.getUserName(r.getUserId()));

            Stage dialog = new Stage();
            dialog.setTitle("Repondre a REC-" + r.getReclamationId());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            loadAll();
            refreshNotifBadge();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire:\n" + e.getMessage());
        }
    }

    // ── NOTIFICATIONS PANEL ───────────────────────────────────────────────────
    private void loadNotifications() {
        notifBox.getChildren().clear();

        List<Reclamation> escalades = svc.getEscalatedReclamations(3);
        List<Reclamation> recent    = svc.getRecentReclamations(1);

        List<String[]> notifs = new ArrayList<>();

        for (Reclamation r : escalades) {
            long d = ChronoUnit.DAYS.between(r.getReclamDate().toLocalDate(), LocalDate.now());
            notifs.add(new String[]{
                    "⚠", "#FAEEDA",
                    "REC-" + r.getReclamationId() + " escaladee auto (SLA +" + d + "j)",
                    "Systeme · Reclamation urgente"
            });
        }
        for (Reclamation r : recent) {
            notifs.add(new String[]{
                    "📥", "#E6F1FB",
                    "Nouvelle reclamation: " + r.getSubject(),
                    "Aujourd'hui · userId " + r.getUserId()
            });
        }

        if (notifs.isEmpty()) {
            Label empty = new Label("Aucune notification recente");
            empty.setStyle("-fx-text-fill:#5d6f91; -fx-font-size:13px; -fx-padding:8 0 8 0;");
            notifBox.getChildren().add(empty);
            return;
        }

        for (String[] n : notifs) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(8, 0, 8, 0));
            row.setStyle("-fx-border-color:transparent transparent #e1e8f0 transparent; -fx-border-width:0 0 1 0;");

            Label icon = new Label(n[0]);
            icon.setPrefWidth(30);
            icon.setPrefHeight(30);
            icon.setAlignment(Pos.CENTER);
            icon.setStyle("-fx-background-color:" + n[1] + "; -fx-background-radius:15; -fx-font-size:14px;");

            VBox body = new VBox(2);
            HBox.setHgrow(body, Priority.ALWAYS);

            Label msg = new Label(n[2]);
            msg.setStyle("-fx-font-size:12px; -fx-text-fill:#0b1f44;");
            msg.setWrapText(true);

            Label time = new Label(n[3]);
            time.setStyle("-fx-font-size:11px; -fx-text-fill:#9aabb5;");

            body.getChildren().addAll(msg, time);
            row.getChildren().addAll(icon, body);
            notifBox.getChildren().add(row);
        }
    }

    // ── RECENT TABLE ──────────────────────────────────────────────────────────
    private void loadRecentTable() {
        List<Reclamation> recent = svc.getRecentReclamations(7);
        recentTable.setItems(FXCollections.observableArrayList(recent));
    }

    // ── AUTO-SUGGEST ──────────────────────────────────────────────────────────
    private void loadAutoSuggest() {
        List<Reclamation> recent = svc.getRecentReclamations(7);
        if (recent.isEmpty()) return;

        Reclamation last = recent.get(0);
        String keywords  = extractKeywords(last.getSubject() + " " + last.getDescription());
        String category  = svc.suggestCategory(last.getSubject(), last.getDescription());

        set(lblSuggestKeywords, "Mots-cles: " + keywords);
        set(lblSuggestCategory, "→ Categorie suggeree: " + category);
    }

    private String extractKeywords(String text) {
        if (text == null || text.isEmpty()) return "—";
        String[] words = text.toLowerCase().split("\\s+");
        List<String> stop = Arrays.asList(
                "je", "le", "la", "les", "de", "du", "un", "une", "en", "et", "est",
                "pas", "sur", "pour", "avec", "mon", "ma", "mes", "que", "qui", "ce"
        );
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String w : words) {
            if (w.length() > 3 && !stop.contains(w) && count < 4) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(w);
                count++;
            }
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    // ── UTILITY ───────────────────────────────────────────────────────────────
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.setTitle("Erreur");
        alert.showAndWait();
    }

    // ── REFRESH ───────────────────────────────────────────────────────────────
    @FXML
    private void refreshDashboard() {
        loadAll();
        refreshNotifBadge();
        System.out.println("Dashboard refreshed");
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────────
    @FXML private void ouvrirAdminDashboard()    { MainFx.changeScene("/admindashboard.fxml");    }
    @FXML private void ouvrirAdminReclamations() { MainFx.changeScene("/adminreclamations.fxml"); }
    @FXML private void goToNotifications()       { MainFx.changeScene("/adminnotifications.fxml"); }
}