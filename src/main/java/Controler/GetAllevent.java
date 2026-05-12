package Controler;

import entities.event;
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
import services.eventServ;
import utils.SessionManager;
import entities.User;

import utils.EventImageGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GetAllevent {

    // ── FXML fields ────────────────────────────────────────────────────────────
    // NOTE: tableView fields kept as null-safe for backward compat
    // but are no longer in FXML — cards replace them
    @FXML private FlowPane     cardsPane;
    @FXML private TextField    tfRecherche;
    @FXML private ComboBox<String> cbFiltreStatut;
    @FXML private Label        lblTotal;
    @FXML private Label        statTotal;
    @FXML private Label        statActifs;
    @FXML private Label        statPlanifies;
    @FXML private Label        statTermines;
    
    // Profile Sidebar
    @FXML private ImageView    imgAvatar;
    @FXML private Label        lblUserName;
    @FXML private Label        lblUserRole;
    @FXML private Label        lblInitials;

    private eventServ   service;
    private List<event> allEvents;

    // ── Colors per type ────────────────────────────────────────────────────────
    private static final String[][] TYPE_COLORS = {
        {"CONFERENCE",  "#1e3a5f", "#2563eb", "#dbeafe", "#1e40af"},
        {"SEMINAIRE",   "#14532d", "#16a34a", "#dcfce7", "#14532d"},
        {"WORKSHOP",    "#78350f", "#d97706", "#fef9c3", "#78350f"},
        {"COMPETITION", "#3b0764", "#9333ea", "#f3e8ff", "#6b21a8"},
    };

    // ── Init ───────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (cbFiltreStatut != null) {
            cbFiltreStatut.setItems(FXCollections.observableArrayList(
                    "Tous", "Planifié", "En cours", "Terminé", "Annulé"));
            cbFiltreStatut.getSelectionModel().selectFirst();
        }
        service = new eventServ();
        chargerDonnees();
        loadUserProfile();
    }

    private void loadUserProfile() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String userName = extractNameFromEmail(user.getEmail());
            lblUserName.setText(userName);
            lblUserRole.setText(user.getRole());
            
            if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                try {
                    // Try to load from resources/images
                    String path = user.getProfileImagePath();
                    if (!path.startsWith("/")) path = "/" + path;
                    Image img = new Image(getClass().getResourceAsStream(path));
                    imgAvatar.setImage(img);
                } catch (Exception e) {
                    // Fallback to initials if image not found
                    imgAvatar.setVisible(false);
                    lblInitials.setVisible(true);
                    lblInitials.setText(getInitials(userName));
                }
            } else {
                imgAvatar.setVisible(false);
                lblInitials.setVisible(true);
                lblInitials.setText(getInitials(userName));
            }
        }
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) return "Utilisateur";
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String namePart = email.substring(0, atIndex);
            // Just capitalize the first letter for simplicity
            return namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase();
        }
        return email;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) if (!p.isEmpty()) sb.append(p.charAt(0));
        return sb.toString().toUpperCase();
    }

    // ── Load data + update stats ───────────────────────────────────────────────
    private void chargerDonnees() {
        allEvents = service.getAll();

        long total     = allEvents.size();
        long actifs    = allEvents.stream().filter(e -> "En cours".equalsIgnoreCase(e.getStatus())).count();
        long planifies = allEvents.stream().filter(e -> "Planifié".equalsIgnoreCase(e.getStatus())).count();
        long termines  = allEvents.stream().filter(e -> "Terminé".equalsIgnoreCase(e.getStatus())).count();

        if (statTotal    != null) statTotal.setText(String.valueOf(total));
        if (statActifs   != null) statActifs.setText(String.valueOf(actifs));
        if (statPlanifies!= null) statPlanifies.setText(String.valueOf(planifies));
        if (statTermines != null) statTermines.setText(String.valueOf(termines));

        // Global filling rate statistic
        int totalCap = allEvents.stream().mapToInt(event::getCapaciteMax).sum();
        int totalParticipants = allEvents.stream().mapToInt(e -> service.getParticipantCount(e.getId())).sum();
        if (totalCap > 0) {
            double fillingRate = (double) totalParticipants / totalCap * 100;
            if (lblTotal != null) {
                lblTotal.setText(String.format("Total : %d événements | Remplissage global : %.1f%%", total, fillingRate));
            }
        } else if (lblTotal != null) {
            lblTotal.setText("Total : " + total + " événement(s)");
        }

        afficherCartes(allEvents);
    }

    // ── Render cards into FlowPane ─────────────────────────────────────────────
    private void afficherCartes(List<event> list) {
        if (cardsPane == null) return;
        cardsPane.getChildren().clear();

        // Filling rate already handled in chargerDonnees

        if (list.isEmpty()) {
            Label empty = new Label("📭  Aucun événement trouvé.");
            empty.setStyle("-fx-text-fill:#a0aab8; -fx-font-size:15px; -fx-padding:40;");
            cardsPane.getChildren().add(empty);
            return;
        }

        for (event ev : list) {
            cardsPane.getChildren().add(buildCard(ev));
        }
    }

    // ── Build one event card ───────────────────────────────────────────────────
    private VBox buildCard(event ev) {

        // ── Determine type colors ──
        String[] tc = getTypeColors(ev.getType());
        String darkColor  = tc[0]; // banner dark
        String midColor   = tc[1]; // banner mid
        String badgeBg    = tc[2]; // type badge bg
        String badgeFg    = tc[3]; // type badge text

        // ── Status badge ──
        String[] sc = getStatusColors(ev.getStatus());
        String statusBg  = sc[0];
        String statusFg  = sc[1];
        String statusIcon= sc[2];

        // ══ BANNER (colored illustration area) ══
        StackPane banner = new StackPane();
        banner.setPrefHeight(140);
        banner.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom right, %s, %s); " +
            "-fx-background-radius: 12 12 0 0;", darkColor, midColor));

        // Banner title text
        VBox bannerText = new VBox(4);
        bannerText.setAlignment(Pos.BOTTOM_LEFT);
        bannerText.setPadding(new Insets(0, 0, 16, 16));
        Label bannerTitle = new Label(ev.getType().toUpperCase());
        bannerTitle.setStyle("-fx-font-size:13px; -fx-font-weight:bold; " +
                             "-fx-text-fill:rgba(255,255,255,0.9);");
        Label bannerSub = new Label(getTypeSubtitle(ev.getType()));
        bannerSub.setStyle("-fx-font-size:9px; -fx-text-fill:rgba(255,255,255,0.55);");
        bannerText.getChildren().addAll(bannerTitle, bannerSub);

        // Decorative circles on banner
        Label circle1 = new Label();
        circle1.setMinSize(80, 80);
        circle1.setMaxSize(80, 80);
        circle1.setStyle("-fx-background-color:rgba(255,255,255,0.06); -fx-background-radius:40;");
        StackPane.setAlignment(circle1, Pos.TOP_RIGHT);
        StackPane.setMargin(circle1, new Insets(-20, -20, 0, 0));

        Label circle2 = new Label();
        circle2.setMinSize(50, 50);
        circle2.setMaxSize(50, 50);
        circle2.setStyle("-fx-background-color:rgba(255,255,255,0.04); -fx-background-radius:25;");
        StackPane.setAlignment(circle2, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(circle2, new Insets(0, 10, 10, 0));

        // ── Banner content: PNG image or emoji fallback ──
        String genImgPath = EventImageGenerator.getImagePath(ev.getType());
        boolean imageLoaded = false;
        if (genImgPath != null) {
            try {
                Image img = new Image(new File(genImgPath).toURI().toString());
                if (!img.isError()) {
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(290);
                    iv.setFitHeight(140);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    banner.getChildren().add(iv);
                    imageLoaded = true;
                }
            } catch (Exception ignored) {}
        }
        if (!imageLoaded) {
            String emoji = getCategoryEmoji(ev.getType(), ev.getImagePath());
            Label emojiIcon = new Label(emoji);
            emojiIcon.setStyle("-fx-font-size:46px; -fx-opacity:0.80;");
            StackPane.setAlignment(emojiIcon, Pos.CENTER);
            StackPane.setMargin(emojiIcon, new Insets(0, 0, 20, 0));
            banner.getChildren().addAll(circle1, circle2, emojiIcon, bannerText);
        }

        // Type badge (top-left overlay)
        Label typeBadge = new Label(ev.getType());
        typeBadge.setStyle(String.format(
            "-fx-background-color:%s; -fx-text-fill:%s; " +
            "-fx-background-radius:20; -fx-padding:3 10 3 10; " +
            "-fx-font-size:11.5px; -fx-font-weight:bold;", badgeBg, badgeFg));
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        StackPane.setMargin(typeBadge, new Insets(12, 0, 0, 12));
        banner.getChildren().add(typeBadge);

        // Status badge (top-right overlay)
        Label statusBadge = new Label(statusIcon + " " + ev.getStatus());
        statusBadge.setStyle(String.format(
            "-fx-background-color:%s; -fx-text-fill:%s; " +
            "-fx-background-radius:20; -fx-padding:3 10 3 10; " +
            "-fx-font-size:11.5px; -fx-font-weight:bold;", statusBg, statusFg));
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(12, 12, 0, 0));
        banner.getChildren().add(statusBadge);

        // ══ CARD BODY ══
        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));
        body.setStyle("-fx-background-color:#ffffff;");

        // Title
        Label title = new Label(ev.getTitre());
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a2332; " +
                       "-fx-wrap-text:true;");
        title.setMaxWidth(260);
        title.setWrapText(true);

        // Meta info
        VBox meta = new VBox(5);
        if (ev.getDate() != null) {
            String dateStr = ev.getDate().toString();
            String timeStr = (ev.getTime() != null && !ev.getTime().isEmpty())
                           ? " · " + ev.getTime() : "";
            meta.getChildren().add(metaChip("📅", dateStr + timeStr));
        }
        if (ev.getLieu() != null && !ev.getLieu().isEmpty()) {
            meta.getChildren().add(metaChip("📍", ev.getLieu()));
        }

        // Registration status (Coordinateur Feature)
        int count = service.getParticipantCount(ev.getId());
        int max   = ev.getCapaciteMax();
        String capStr = (max > 0) ? count + " / " + max : count + " inscrits";
        String capColor = (max > 0 && count >= max) ? "#e05a5a" : "#4a5568";
        HBox capChip = metaChip("👥", capStr);
        ((Label)capChip.getChildren().get(1)).setStyle("-fx-font-size:12px; -fx-text-fill:" + capColor + "; -fx-font-weight:bold;");
        meta.getChildren().add(capChip);

        // Simple progress bar
        if (max > 0) {
            ProgressBar pb = new ProgressBar((double) count / max);
            pb.setPrefWidth(260);
            pb.setMaxHeight(6);
            pb.getStyleClass().add("event-progress");
            if (count >= max) pb.setStyle("-fx-accent: #e05a5a;");
            else if (count >= max * 0.8) pb.setStyle("-fx-accent: #f6ad55;");
            else pb.setStyle("-fx-accent: #48bb78;");
            meta.getChildren().add(pb);
        }

        // Separator line
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#f0f4f0;");

        // Description
        Label desc = new Label(ev.getDescription() != null ? ev.getDescription() : "");
        desc.setStyle("-fx-font-size:12px; -fx-text-fill:#718096; -fx-wrap-text:true;");
        desc.setWrapText(true);
        desc.setMaxWidth(260);
        // Clamp to 2 lines visually
        desc.setMaxHeight(38);

        // ══ ACTION BUTTONS ══
        // Row 1: Modifier + Supprimer
        HBox btnRow1 = new HBox(8);
        Button btnModifier   = styledBtn("✏  Modifier",   "#2d3748", "#ffffff");
        Button btnSupprimer  = styledBtn("🗑  Supprimer",  "#e05a5a", "#ffffff");
        btnModifier.setOnAction(e  -> doModifier(ev));
        btnSupprimer.setOnAction(e -> doSupprimer(ev));
        btnRow1.getChildren().addAll(btnModifier, btnSupprimer);

        // Row 2: Participants
        HBox btnRow2 = new HBox();
        Button btnPart = new Button("👥  Participants");
        btnPart.setStyle(
            "-fx-background-color:transparent; -fx-border-color:#e2e8f0; " +
            "-fx-border-width:1.5px; -fx-border-radius:7px; -fx-background-radius:7px; " +
            "-fx-text-fill:#4a5568; -fx-font-size:12.5px; -fx-padding:6 14 6 14; " +
            "-fx-cursor:hand; -fx-pref-width:10000px;");
        btnPart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnPart, Priority.ALWAYS);
        btnPart.setOnAction(e -> handleNaviguerParticipants());
        btnRow2.getChildren().add(btnPart);

        body.getChildren().addAll(title, meta, sep, desc, btnRow1, btnRow2);

        // ══ CARD CONTAINER ══
        VBox card = new VBox();
        card.setPrefWidth(290);
        card.setMaxWidth(290);
        card.setStyle(
            "-fx-background-color:#ffffff; " +
            "-fx-background-radius:12; " +
            "-fx-border-color:" + getBorderColor(ev.getType()) + "; " +
            "-fx-border-width:0 0 0 4; " +
            "-fx-border-radius:12; " +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.09),8,0,0,2);");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
            "dropshadow(gaussian,rgba(0,0,0,0.09),8,0,0,2)",
            "dropshadow(gaussian,rgba(0,0,0,0.18),16,0,0,4)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
            "dropshadow(gaussian,rgba(0,0,0,0.18),16,0,0,4)",
            "dropshadow(gaussian,rgba(0,0,0,0.09),8,0,0,2)")));

        card.getChildren().addAll(banner, body);
        return card;
    }

    // ── Helper: meta chip row ─────────────────────────────────────────────────
    private HBox metaChip(String icon, String text) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:12px;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12px; -fx-text-fill:#4a5568;");
        lbl.setWrapText(false);
        lbl.setMaxWidth(230);
        row.getChildren().addAll(ico, lbl);
        return row;
    }

    // ── Helper: styled button ─────────────────────────────────────────────────
    private Button styledBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle(String.format(
            "-fx-background-color:%s; -fx-text-fill:%s; " +
            "-fx-background-radius:7; -fx-font-size:12.5px; " +
            "-fx-padding:7 14 7 14; -fx-cursor:hand; " +
            "-fx-font-weight:bold;", bg, fg));
        return b;
    }

    // ── Helper: type colors [dark, mid, badgeBg, badgeFg] ────────────────────
    private String[] getTypeColors(String type) {
        if (type == null) return new String[]{"#1a2332","#2d3748","#e2e8f0","#4a5568"};
        switch (normalizeType(type)) {
            // ── Catégories médicales ──────────────────────────────────────────
            case "DON DE SANG":          return new String[]{"#7f1d1d","#dc2626","#fee2e2","#7f1d1d"};
            case "VACCINATION":          return new String[]{"#14532d","#16a34a","#dcfce7","#14532d"};
            case "CONFERENCE MEDICALE":  return new String[]{"#1e3a5f","#2563eb","#dbeafe","#1e40af"};
            case "CONSULTATION":         return new String[]{"#312e81","#7c3aed","#ede9fe","#4c1d95"};
            case "SENSIBILISATION":      return new String[]{"#92400e","#d97706","#fef3c7","#92400e"};
            case "ATELIER SANTE":        return new String[]{"#134e4a","#0d9488","#ccfbf1","#134e4a"};
            case "DEPISTAGE":            return new String[]{"#1e3a5f","#0284c7","#e0f2fe","#075985"};
            case "PREMIERS SECOURS":     return new String[]{"#7c2d12","#ea580c","#ffedd5","#7c2d12"};
            // ── Catégories génériques ─────────────────────────────────────────
            case "CONFERENCE":           return new String[]{"#1e3a5f","#2563eb","#dbeafe","#1e40af"};
            case "SEMINAIRE":            return new String[]{"#14532d","#16a34a","#dcfce7","#14532d"};
            case "ATELIER":              return new String[]{"#78350f","#d97706","#fef9c3","#78350f"};
            case "COMPETITION":          return new String[]{"#3b0764","#9333ea","#f3e8ff","#6b21a8"};
            default:                     return new String[]{"#1a2332","#2d3748","#e2e8f0","#4a5568"};
        }
    }

    private String normalizeType(String type) {
        if (type == null) return "";
        // Remove accents + uppercase for switch matching
        return type.toUpperCase()
            .replace("É","E").replace("È","E").replace("Ê","E")
            .replace("À","A").replace("Â","A")
            .replace("Î","I").replace("Ï","I")
            .replace("Ô","O").replace("Ù","U").replace("Û","U").replace("Ü","U")
            .replace("Ç","C");
    }

    private String getBorderColor(String type) {
        if (type == null) return "#e2e8f0";
        switch (normalizeType(type)) {
            case "DON DE SANG":         return "#dc2626";
            case "VACCINATION":         return "#16a34a";
            case "CONFERENCE MEDICALE": return "#2563eb";
            case "CONSULTATION":        return "#7c3aed";
            case "SENSIBILISATION":     return "#d97706";
            case "ATELIER SANTE":       return "#0d9488";
            case "DEPISTAGE":           return "#0284c7";
            case "PREMIERS SECOURS":    return "#ea580c";
            case "CONFERENCE":          return "#2563eb";
            case "SEMINAIRE":           return "#16a34a";
            case "ATELIER":             return "#d97706";
            case "COMPETITION":         return "#9333ea";
            default:                    return "#e2e8f0";
        }
    }

    private String getTypeSubtitle(String type) {
        if (type == null) return "";
        switch (normalizeType(type)) {
            case "DON DE SANG":         return "Santé · Solidarité · Urgences";
            case "VACCINATION":         return "Prévention · Immunité · Protection";
            case "CONFERENCE MEDICALE": return "Médecine · Spécialistes · Innovations";
            case "CONSULTATION":        return "Santé · Diagnostic · Suivi";
            case "SENSIBILISATION":     return "Prévention · Éducation · Communauté";
            case "ATELIER SANTE":       return "Pratique · Bien-être · Formation";
            case "DEPISTAGE":           return "Dépistage · Prévention · Bilan";
            case "PREMIERS SECOURS":    return "Urgences · Formation · Sécurité";
            case "CONFERENCE":          return "Médecine · Spécialistes · Innovations";
            case "SEMINAIRE":           return "Échanges · Expertise · Collaboration";
            case "ATELIER":             return "Pratique · Interactif · Compétences";
            case "COMPETITION":         return "Compétition · Excellence · Innovation";
            default:                    return "Événement santé";
        }
    }

    // ── Helper: emoji icon per medical category ───────────────────────────────
    private String getCategoryEmoji(String type, String imagePath) {
        // If imagePath starts with "emoji:" use that directly
        if (imagePath != null && imagePath.startsWith("emoji:")) {
            return imagePath.substring(6);
        }
        if (type == null) return "📋";
        switch (normalizeType(type)) {
            case "DON DE SANG":         return "🩸";
            case "VACCINATION":         return "💉";
            case "CONFERENCE MEDICALE": return "🏥";
            case "CONSULTATION":        return "🩺";
            case "SENSIBILISATION":     return "❤";
            case "ATELIER SANTE":       return "🥗";
            case "DEPISTAGE":           return "🔬";
            case "PREMIERS SECOURS":    return "🚑";
            case "CONFERENCE":          return "🎓";
            case "SEMINAIRE":           return "📚";
            case "ATELIER":             return "🛠";
            case "COMPETITION":         return "🏆";
            default:                    return "🏥";
        }
    }

    // ── Helper: status colors [bg, fg, icon] ──────────────────────────────────
    private String[] getStatusColors(String status) {
        if (status == null) return new String[]{"#e2e8f0","#4a5568","◉"};
        switch (status.toLowerCase()) {
            case "en cours":  return new String[]{"#d4edda","#1a6b2e","✓"};
            case "planifié":  return new String[]{"#cce5ff","#0c4a8c","◷"};
            case "terminé":   return new String[]{"#e2e8f0","#4a5568","◉"};
            case "annulé":    return new String[]{"#fdeaea","#a0291b","✕"};
            // English fallbacks
            case "ongoing":   return new String[]{"#d4edda","#1a6b2e","✓"};
            case "planned":   return new String[]{"#cce5ff","#0c4a8c","◷"};
            case "completed": return new String[]{"#e2e8f0","#4a5568","◉"};
            case "cancelled": return new String[]{"#fdeaea","#a0291b","✕"};
            default:          return new String[]{"#e2e8f0","#4a5568","◉"};
        }
    }

    // ── Action: modifier from card ────────────────────────────────────────────
    private void doModifier(event ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Modifierevent.fxml"));
            Parent root = loader.load();
            ((Modifierevent) loader.getController()).setEvent(ev);
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    // ── Action: supprimer from card ───────────────────────────────────────────
    private void doSupprimer(event ev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer « " + ev.getTitre() + " » ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmer la suppression");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                service.supprimer(ev.getId());
                chargerDonnees();
            }
        });
    }

    // ── FXML handlers (kept exactly as before) ────────────────────────────────
    @FXML public void handleRecherche()    { appliquerFiltres(); }
    @FXML public void handleFiltreStatut() { appliquerFiltres(); }

    private void appliquerFiltres() {
        if (allEvents == null) return;
        String recherche    = tfRecherche    != null ? tfRecherche.getText().toLowerCase().trim() : "";
        String statutFiltre = cbFiltreStatut != null ? cbFiltreStatut.getValue() : "Tous";

        List<event> filtered = allEvents.stream().filter(e -> {
            boolean matchR = e.getTitre().toLowerCase().contains(recherche);
            boolean matchS = "Tous".equals(statutFiltre)
                          || statutFiltre.equalsIgnoreCase(e.getStatus());
            return matchR && matchS;
        }).collect(Collectors.toList());

        afficherCartes(filtered);
    }

    @FXML
    public void handleActualiser() {
        if (tfRecherche    != null) tfRecherche.clear();
        if (cbFiltreStatut != null) cbFiltreStatut.getSelectionModel().selectFirst();
        chargerDonnees();
    }

    @FXML
    public void handleAjouter() { naviguer("/Ajouterevent.fxml"); }

    // kept for backward compat (not used in card view — doModifier used instead)
    @FXML
    public void handleModifier() {
        showAlert("Cliquez sur le bouton ✏ Modifier de la carte souhaitée.");
    }

    @FXML
    public void handleSupprimer() {
        showAlert("Cliquez sur le bouton 🗑 Supprimer de la carte souhaitée.");
    }

    @FXML
    public void handleNaviguerParticipants() { naviguer("/GetAllparticipant.fxml"); }

    @FXML
    public void handleNaviguerAnalytiques() { naviguer("/DashboardAnalytics.fxml"); }

    @FXML
    public void handleNaviguerReservations()    { naviguer("/SmartBooking.fxml"); }
    @FXML
    public void handleNaviguerRecommandations() { naviguer("/Recommendations.fxml"); }

    @FXML
    public void handleNaviguerEvenements() { naviguer("/GetAllevent.fxml"); }

    @FXML
    public void handleLogout() {
        naviguer("/GetAllevent.fxml");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            // Use lblUserName to get the stage, it's safer
            Stage stage = (Stage) lblUserName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
