package Controler;

import entities.Reservation;
import entities.event;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.QrCodeService;
import services.ReservationServ;
import services.eventServ;
import services.Participantserv;
import utils.SessionManager;
import entities.user;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SmartBookingController {

    // ── Sidebar profile ─────────────────────────────────────────────────────
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblInitials;

    // ── Booking form ────────────────────────────────────────────────────────
    @FXML private ComboBox<String>  cbEvent;
    @FXML private ComboBox<String>  cbParticipant;
    @FXML private Spinner<Integer>  spPlaces;
    @FXML private Label             lblPlacesRestantes;
    @FXML private Label             lblResultat;

    // ── Reservations list ───────────────────────────────────────────────────
    @FXML private VBox              reservationsPane;
    @FXML private Label             lblTotalRes;
    @FXML private ComboBox<String>  cbFiltreStatut;

    private final ReservationServ resServ    = new ReservationServ();
    private final eventServ        eServ     = new eventServ();
    private final Participantserv  pServ     = new Participantserv();
    private final QrCodeService    qrService = new QrCodeService();

    private List<event>       events;
    private List<Participant> participants;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Init ────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadUserProfile();
        loadComboData();
        if (cbFiltreStatut != null) {
            cbFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "CONFIRME", "LISTE_D_ATTENTE", "CHECKED_IN", "REFUSE", "ANNULE", "EN_ATTENTE"));
            cbFiltreStatut.getSelectionModel().selectFirst();
            cbFiltreStatut.setOnAction(e -> chargerReservations());
        }
        chargerReservations();
    }

    private void loadComboData() {
        events = eServ.getAll();
        participants = pServ.getAll();

        if (cbEvent != null) {
            cbEvent.getItems().clear();
            events.forEach(ev -> cbEvent.getItems().add("[" + ev.getId() + "] " + ev.getTitre()));
            cbEvent.setOnAction(e -> majPlacesRestantes());
        }
        if (cbParticipant != null) {
            cbParticipant.getItems().clear();
            participants.forEach(p -> cbParticipant.getItems().add("[" + p.getId() + "] " + p.getNom() + " " + p.getPrenom()));
        }
        if (spPlaces != null) {
            SpinnerValueFactory<Integer> vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
            spPlaces.setValueFactory(vf);
        }
    }

    private void majPlacesRestantes() {
        int idx = cbEvent.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= events.size()) {
            if (lblPlacesRestantes != null) lblPlacesRestantes.setText("—");
            return;
        }
        event ev = events.get(idx);
        int restantes = resServ.getPlacesRestantes(ev.getId());
        if (lblPlacesRestantes != null) {
            String txt = restantes == Integer.MAX_VALUE ? "∞ (pas de limite)" : restantes + " place(s) restante(s)";
            lblPlacesRestantes.setText(txt);
            lblPlacesRestantes.setStyle(restantes <= 0
                ? "-fx-text-fill:#e05a5a; -fx-font-weight:bold;"
                : "-fx-text-fill:#16a34a; -fx-font-weight:bold;");
        }
    }

    // ── Handle booking submission ────────────────────────────────────────────
    @FXML
    public void handleReserver() {
        int evIdx = cbEvent.getSelectionModel().getSelectedIndex();
        int pIdx  = cbParticipant.getSelectionModel().getSelectedIndex();

        if (evIdx < 0) { showResult("⚠️ Sélectionnez un événement.", "#d97706"); return; }
        if (pIdx < 0)  { showResult("⚠️ Sélectionnez un participant.", "#d97706"); return; }

        int eventId       = events.get(evIdx).getId();
        int participantId = participants.get(pIdx).getId();
        int places        = spPlaces.getValue();

        // ── Automatic decision by the Smart Booking Engine ──────────────────
        Reservation res = resServ.soumettreDemande(eventId, participantId, places);

        if (res.getStatut() == Reservation.Statut.CONFIRME) {
            showResult("✅ Réservation CONFIRMÉE ! (" + places + " place(s) accordée(s))", "#16a34a");
        } else {
            showResult("❌ Réservation REFUSÉE – " + res.getMotifRefus(), "#e05a5a");
        }

        majPlacesRestantes();
        chargerReservations();
    }

    // ── Load reservation list ───────────────────────────────────────────────
    private void chargerReservations() {
        if (reservationsPane == null) return;
        reservationsPane.getChildren().clear();

        String filtre = (cbFiltreStatut != null && cbFiltreStatut.getValue() != null)
            ? cbFiltreStatut.getValue() : "Tous";

        List<Reservation> all = resServ.getAll();
        List<Reservation> affichees = all.stream()
            .filter(r -> "Tous".equals(filtre) || r.getStatut().name().equals(filtre))
            .collect(java.util.stream.Collectors.toList());

        if (lblTotalRes != null)
            lblTotalRes.setText("Total : " + affichees.size() + " réservation(s)");

        if (affichees.isEmpty()) {
            Label empty = new Label("📭  Aucune réservation trouvée.");
            empty.setStyle("-fx-text-fill:#a0aab8; -fx-font-size:14px; -fx-padding:30;");
            reservationsPane.getChildren().add(empty);
            return;
        }

        for (Reservation r : affichees) {
            reservationsPane.getChildren().add(buildReservationRow(r));
        }
    }

    private HBox buildReservationRow(Reservation r) {
        // Status color
        String[] sc = getStatusStyle(r.getStatut());

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; " +
                     "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2); " +
                     "-fx-border-color:" + sc[2] + "; -fx-border-width:0 0 0 4; -fx-border-radius:10;");

        // Status badge
        Label badge = new Label(sc[0]);
        badge.setStyle("-fx-background-color:" + sc[1] + "; -fx-text-fill:" + sc[2] +
                       "; -fx-background-radius:20; -fx-padding:3 10 3 10; " +
                       "-fx-font-size:11px; -fx-font-weight:bold;");
        badge.setMinWidth(110);

        // Details
        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);
        String eventName = getEventName(r.getEventId());
        String partName  = getPartName(r.getParticipantId());
        Label lEvent = new Label("📅 " + eventName);
        lEvent.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a2332;");
        Label lPart = new Label("👤 " + partName + "   |   🎟 " + r.getNombrePlaces() + " place(s)");
        lPart.setStyle("-fx-font-size:12px; -fx-text-fill:#4a5568;");
        Label lDate = new Label("🕐 " + (r.getDateDemande() != null ? r.getDateDemande().format(FMT) : "—"));
        lDate.setStyle("-fx-font-size:11px; -fx-text-fill:#718096;");
        details.getChildren().addAll(lEvent, lPart, lDate);

        if (r.getMotifRefus() != null && !r.getMotifRefus().isEmpty()) {
            Label lMotif = new Label("ℹ " + r.getMotifRefus());
            lMotif.setStyle("-fx-font-size:11px; -fx-text-fill:#c53030; -fx-wrap-text:true;");
            lMotif.setMaxWidth(400);
            lMotif.setWrapText(true);
            details.getChildren().add(lMotif);
        }

        // Buttons area
        VBox buttons = new VBox(6);
        buttons.setAlignment(Pos.CENTER);

        // QR Code button — visible for CONFIRME and CHECKED_IN
        if (r.getStatut() == Reservation.Statut.CONFIRME
                || r.getStatut() == Reservation.Statut.CHECKED_IN) {
            Button btnQr = new Button("🔳 QR");
            btnQr.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:#1d4ed8; " +
                           "-fx-background-radius:7; -fx-font-size:12px; -fx-padding:5 12 5 12; " +
                           "-fx-cursor:hand; -fx-font-weight:bold;");
            btnQr.setOnAction(e -> showQrCodePopup(r.getId(), eventName));
            buttons.getChildren().add(btnQr);
        }

        // Cancel button — only for CONFIRME
        if (r.getStatut() == Reservation.Statut.CONFIRME) {
            Button btnAnnuler = new Button("✕ Annuler");
            btnAnnuler.setStyle("-fx-background-color:#fff5f5; -fx-text-fill:#c53030; " +
                                "-fx-background-radius:7; -fx-font-size:12px; -fx-padding:5 12 5 12; -fx-cursor:hand;");
            btnAnnuler.setOnAction(e -> {
                resServ.annulerReservation(r.getId());
                chargerReservations();
                majPlacesRestantes();
            });
            buttons.getChildren().add(btnAnnuler);
        }

        row.getChildren().addAll(badge, details, buttons);

        return row;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private String[] getStatusStyle(Reservation.Statut s) {
        switch (s) {
            case CONFIRME:        return new String[]{"✓ CONFIRMÉ",      "#d4edda", "#16a34a"};
            case LISTE_D_ATTENTE: return new String[]{"⏳ LISTE ATTENTE", "#fef3c7", "#d97706"};
            case CHECKED_IN:      return new String[]{"🎯 PRÉSENT",       "#dbeafe", "#1d4ed8"};
            case REFUSE:          return new String[]{"✕ REFUSÉ",         "#fdeaea", "#c53030"};
            case ANNULE:          return new String[]{"↩ ANNULÉ",         "#f3f4f6", "#6b7280"};
            case EN_ATTENTE:      return new String[]{"◷ EN ATTENTE",     "#fef9c3", "#b45309"};
            default:              return new String[]{"? INCONNU",        "#e2e8f0", "#4a5568"};
        }
    }

    // ── QR Code popup viewer ─────────────────────────────────────────────────
    private void showQrCodePopup(int reservationId, String eventName) {
        String path = qrService.getQrCodePath(reservationId);
        if (path == null) {
            showResult("❌ QR Code introuvable pour la réservation #" + reservationId + ". Annulez et re-réservez.", "#e05a5a");
            return;
        }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("QR Code — Réservation #" + reservationId);
        popup.setResizable(false);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:white;");

        // Header
        Label titre = new Label("🔳  QR Code d'accès");
        titre.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#1e3a5f;");

        Label sous = new Label(eventName);
        sous.setStyle("-fx-font-size:13px; -fx-text-fill:#4a5568;");

        // QR image
        File qrFile = new File(path);
        Image qrImg = new Image(qrFile.toURI().toString(), 300, 300, true, true);
        ImageView qrView = new ImageView(qrImg);
        qrView.setFitWidth(300);
        qrView.setFitHeight(300);
        qrView.setPreserveRatio(true);
        qrView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");

        // Info
        Label info = new Label("Réservation #" + reservationId + "  ·  Présentez ce QR à l'entrée");
        info.setStyle("-fx-font-size:11px; -fx-text-fill:#718096; -fx-font-style:italic;");

        // Close button
        Button btnClose = new Button("  Fermer  ");
        btnClose.setStyle("-fx-background-color:#1e3a5f; -fx-text-fill:white; -fx-font-weight:bold; " +
                          "-fx-background-radius:8; -fx-padding:9 24 9 24; -fx-cursor:hand; -fx-font-size:13px;");
        btnClose.setOnAction(e -> popup.close());

        content.getChildren().addAll(titre, sous, qrView, info, btnClose);

        popup.setScene(new Scene(content, 400, 460));
        popup.show();
    }

    private String getEventName(int eventId) {
        return events.stream().filter(e -> e.getId() == eventId)
            .map(event::getTitre).findFirst().orElse("Event #" + eventId);
    }

    private String getPartName(int pId) {
        return participants.stream().filter(p -> p.getId() == pId)
            .map(p -> p.getNom() + " " + p.getPrenom()).findFirst().orElse("Participant #" + pId);
    }

    private void showResult(String msg, String color) {
        if (lblResultat != null) {
            lblResultat.setText(msg);
            lblResultat.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold; -fx-font-size:13px;");
        }
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && lblUserName != null) {
            String name = extractName(currentUser.getEmail());
            lblUserName.setText(name);
            if (lblUserRole != null) lblUserRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "");
            if (lblInitials != null) lblInitials.setText(initials(name));
        }
    }

    private String extractName(String email) {
        if (email == null || email.isEmpty()) return "Utilisateur";
        int at = email.indexOf('@');
        if (at > 0) { String n = email.substring(0, at); return n.substring(0,1).toUpperCase() + n.substring(1).toLowerCase(); }
        return email;
    }

    private String initials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] p = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : p) if (!s.isEmpty()) sb.append(s.charAt(0));
        return sb.toString().toUpperCase();
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    @FXML public void handleNaviguerEvenements()   { tests.Main.showAdminEvents(); }
    @FXML public void handleNaviguerParticipants() { tests.Main.showAdminEventParticipants(); }
    @FXML public void handleLogout()               { tests.Main.showLoginPage(); }
}
