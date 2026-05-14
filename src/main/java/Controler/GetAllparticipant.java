package Controler;

import entities.Participant;
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
import services.LoyaltyService;
import services.Participantserv;
import utils.SessionManager;
import entities.user;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GetAllparticipant {

    @FXML private FlowPane     cardsPane;
    @FXML private TextField    tfRecherche;
    @FXML private Label        lblTotal;

    // Profile Sidebar
    @FXML private ImageView imgAvatar;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblInitials;

    private Participantserv service      = new Participantserv();
    private LoyaltyService  loyaltyServ = new LoyaltyService();
    private List<Participant> allParticipants;

    @FXML
    public void initialize() {
        chargerDonnees();
        loadUserProfile();
    }

    private void chargerDonnees() {
        allParticipants = service.getAll();
        // Enrich each participant with live loyalty data
        allParticipants.forEach(p -> loyaltyServ.enrich(p));
        afficherCartes(allParticipants);
    }

    private void afficherCartes(List<Participant> list) {
        if (cardsPane == null) return;
        cardsPane.getChildren().clear();

        if (lblTotal != null)
            lblTotal.setText("Total : " + list.size() + " participant(s) trouvé(s)");

        if (list.isEmpty()) {
            Label empty = new Label("📭 Aucun participant trouvé.");
            empty.setStyle("-fx-text-fill:#a0aab8; -fx-font-size:15px; -fx-padding:40;");
            cardsPane.getChildren().add(empty);
            return;
        }

        for (Participant p : list) {
            cardsPane.getChildren().add(buildCard(p));
        }
    }

    private VBox buildCard(Participant p) {
        String borderColor = p.getPriorityColor();

        // ══ CARD CONTAINER ══
        VBox card = new VBox(0);
        card.setPrefWidth(270);
        card.setMaxWidth(270);
        card.setStyle(
            "-fx-background-color:#ffffff; -fx-background-radius:14; " +
            "-fx-border-color:" + borderColor + "; -fx-border-width:0 0 0 4; " +
            "-fx-border-radius:14; " +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        // ── BANNER (badge + priority) ─────────────────────────────────────────
        HBox banner = new HBox(8);
        banner.setPadding(new Insets(10, 14, 10, 14));
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:14 14 0 0;");

        Label badgeLbl = new Label(p.getBadgeEmoji() + " " + p.getBadge().name());
        badgeLbl.setStyle("-fx-background-color:" + p.getBadgeColor() + "22; " +
                          "-fx-text-fill:" + p.getBadgeColor() + "; -fx-font-weight:bold; " +
                          "-fx-font-size:11px; -fx-background-radius:14; -fx-padding:3 9 3 9;");

        Label prioLbl = new Label(p.getPriorityEmoji() + " " + p.getPriorityLevel().name());
        prioLbl.setStyle("-fx-background-color:" + p.getPriorityColor() + "22; " +
                         "-fx-text-fill:" + p.getPriorityColor() + "; -fx-font-weight:bold; " +
                         "-fx-font-size:11px; -fx-background-radius:14; -fx-padding:3 9 3 9;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreLbl = new Label("🏅 " + p.getLoyaltyPoints() + " pts");
        scoreLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#4a5568;");

        banner.getChildren().addAll(badgeLbl, prioLbl, spacer, scoreLbl);

        // ── BODY ──────────────────────────────────────────────────────────────
        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));

        // Avatar + Name
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(p.getNom().substring(0, 1).toUpperCase());
        avatar.setStyle(
            "-fx-background-color:" + p.getPriorityColor() + "; " +
            "-fx-background-radius:22; -fx-text-fill:white; -fx-font-weight:bold; " +
            "-fx-alignment:center; -fx-min-width:44; -fx-min-height:44; -fx-font-size:17px;");

        VBox nameBox = new VBox(2);
        Label name = new Label(p.getNom() + " " + p.getPrenom());
        name.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2332;");
        Label sub = new Label("ID: #" + p.getId());
        sub.setStyle("-fx-font-size:11px; -fx-text-fill:#718096;");
        nameBox.getChildren().addAll(name, sub);
        header.getChildren().addAll(avatar, nameBox);

        // Info
        VBox info = new VBox(6);
        info.getChildren().addAll(
            infoRow("✉", p.getEmail()),
            infoRow("📞", p.getTelephone() != null ? p.getTelephone() : "—"),
            infoRow("✅", p.getAttendanceCount() + " présences   |   ↩ " + p.getCancellationCount() + " annulations")
        );

        // Actions
        HBox actions = new HBox(6);
        actions.setPadding(new Insets(6, 0, 0, 0));

        Button btnProfil = new Button("👤 Profil");
        btnProfil.setStyle("-fx-background-color:#ede9fe; -fx-text-fill:#7c3aed; " +
                           "-fx-font-size:11px; -fx-background-radius:7; -fx-padding:6 10 6 10; -fx-cursor:hand; -fx-font-weight:bold;");
        btnProfil.setOnAction(e -> doProfil(p));

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color:#edf2f7; -fx-text-fill:#4a5568; " +
                         "-fx-font-size:11px; -fx-background-radius:7; -fx-padding:6 10 6 10; -fx-cursor:hand;");
        btnEdit.setOnAction(e -> doModifier(p));

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color:#fff5f5; -fx-text-fill:#c53030; " +
                        "-fx-font-size:11px; -fx-background-radius:7; -fx-padding:6 10 6 10; -fx-cursor:hand;");
        btnDel.setOnAction(e -> doSupprimer(p));

        actions.getChildren().addAll(btnProfil, btnEdit, btnDel);

        body.getChildren().addAll(header, new Separator(), info, actions);
        card.getChildren().addAll(banner, body);

        // Hover
        String baseStyle = card.getStyle();
        card.setOnMouseEntered(ev -> card.setStyle(baseStyle.replace(
            "dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2)",
            "dropshadow(gaussian,rgba(0,0,0,0.16),14,0,0,4)")));
        card.setOnMouseExited(ev -> card.setStyle(baseStyle));

        return card;
    }

    private void doProfil(Participant p) {
        tests.Main.showAdminParticipantProfile(p);
    }

    private HBox infoRow(String icon, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-text-fill:#a0aec0; -fx-font-size:13px;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill:#4a5568; -fx-font-size:12px;");
        row.getChildren().addAll(ico, lbl);
        return row;
    }

    @FXML
    public void handleRecherche() {
        String mot = tfRecherche.getText().toLowerCase().trim();
        List<Participant> filtered = allParticipants.stream()
            .filter(p -> p.getNom().toLowerCase().contains(mot) || p.getPrenom().toLowerCase().contains(mot))
            .collect(Collectors.toList());
        afficherCartes(filtered);
    }

    @FXML
    public void handleActualiser() {
        service = new Participantserv();
        chargerDonnees();
        loadUserProfile();
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = extractNameFromEmail(currentUser.getEmail());
            if (lblUserName != null) lblUserName.setText(userName);
            if (lblUserRole != null) lblUserRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "");

            String avatarPath = currentUser.getAvatarUrl();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                try {
                    if (!avatarPath.startsWith("/")) avatarPath = "/" + avatarPath;
                    Image img = new Image(getClass().getResourceAsStream(avatarPath));
                    if (imgAvatar != null) imgAvatar.setImage(img);
                } catch (Exception e) {
                    if (imgAvatar != null) imgAvatar.setVisible(false);
                    if (lblInitials != null) { lblInitials.setVisible(true); lblInitials.setText(getInitials(userName)); }
                }
            } else {
                if (imgAvatar != null) imgAvatar.setVisible(false);
                if (lblInitials != null) { lblInitials.setVisible(true); lblInitials.setText(getInitials(userName)); }
            }
        }
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) return "Utilisateur";
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String namePart = email.substring(0, atIndex);
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

    @FXML
    public void handleAjouter() {
        tests.Main.showAdminAjouterParticipant();
    }

    @FXML
    public void handleNaviguerEvenements() { tests.Main.showAdminEvents(); }

    @FXML
    public void handleNaviguerParticipants() { tests.Main.showAdminEventParticipants(); }

    @FXML
    public void handleLogout() { tests.Main.showLoginPage(); }

    private void doModifier(Participant p) {
        tests.Main.showAdminModifierParticipant(p);
    }

    private void doSupprimer(Participant p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + p.getNom() + " " + p.getPrenom() + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                service.supprimer(p.getId());
                chargerDonnees();
            }
        });
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
