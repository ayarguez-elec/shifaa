package Controler;

import entities.Participant;
import entities.Reservation;
import entities.Reservation.Statut;
import entities.event;
import entities.user;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.Participantserv;
import services.ReservationServ;
import services.eventServ;
import services.userServices;
import tests.Main;

import java.util.*;
import java.util.stream.Collectors;

public class PatientEventsController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private FlowPane         cardsPane;
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label            lblTotal;
    @FXML private Label            lblStatTotal;
    @FXML private Label            lblStatMes;
    @FXML private Label            lblStatDispo;
    @FXML private Button           btnTabTous;
    @FXML private Button           btnTabMes;

    // ── State ─────────────────────────────────────────────────────────────────
    private final eventServ        eventService  = new eventServ();
    private final Participantserv  partService   = new Participantserv();
    private final ReservationServ  resService    = new ReservationServ();
    private final userServices     userService   = new userServices();

    private List<event>       allEvents     = new ArrayList<>();
    private Set<Integer>      myEventIds    = new HashSet<>();
    private Map<Integer,Statut> myStatuts   = new HashMap<>(); // eventId -> statut
    private int               myParticipantId = -1;

    private boolean           showingMyOnly = false;

    // ── Init ─────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Populate filter combo-boxes
        cbType.setItems(FXCollections.observableArrayList(
            "Tous les types",
            "Don de sang", "Vaccination", "Conférence médicale",
            "Consultation", "Sensibilisation", "Atelier santé",
            "Dépistage", "Premiers secours", "Conférence", "Séminaire", "Atelier"
        ));
        cbType.getSelectionModel().selectFirst();

        cbStatut.setItems(FXCollections.observableArrayList(
            "Tous", "Planifié", "En cours", "Terminé", "Annulé"
        ));
        cbStatut.getSelectionModel().selectFirst();

        loadMyProfile();
        loadData();
    }

    // ── Load patient profile (find / create Participant record) ──────────────
    private void loadMyProfile() {
        user me = currentUser();
        if (me == null) return;

        List<Participant> all = partService.getAll();
        Participant p = all.stream()
            .filter(x -> x.getEmail() != null
                      && x.getEmail().equalsIgnoreCase(me.getEmail()))
            .findFirst().orElse(null);

        if (p != null) {
            myParticipantId = p.getId();
            resService.getByParticipant(myParticipantId).forEach(r -> {
                myEventIds.add(r.getEventId());
                myStatuts.put(r.getEventId(), r.getStatut());
            });
        }
        // If p == null, the participant profile will be created on first booking
    }

    // ── Helper: fetch logged-in user from DB via Main.currentUserId ──────────
    private user currentUser() {
        int id = Main.currentUserId;
        if (id <= 0) return null;
        try {
            return userService.getById(id);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Load all events + update stats ───────────────────────────────────────
    private void loadData() {
        allEvents = eventService.getAll();

        long total = allEvents.size();
        long dispo = allEvents.stream()
            .filter(e -> {
                int cnt = eventService.getParticipantCount(e.getId());
                int max = e.getCapaciteMax();
                boolean open = "Planifié".equalsIgnoreCase(e.getStatus())
                            || "En cours".equalsIgnoreCase(e.getStatus());
                return open && (max == 0 || cnt < max);
            }).count();

        if (lblStatTotal != null) lblStatTotal.setText(String.valueOf(total));
        if (lblStatMes   != null) lblStatMes.setText(String.valueOf(myEventIds.size()));
        if (lblStatDispo != null) lblStatDispo.setText(String.valueOf(dispo));

        renderCards(filteredList());
    }

    // ── Filtering logic ───────────────────────────────────────────────────────
    private List<event> filteredList() {
        String search = tfRecherche != null ? tfRecherche.getText().toLowerCase().trim() : "";
        String type   = cbType   != null && cbType.getValue()   != null ? cbType.getValue()   : "Tous les types";
        String statut = cbStatut != null && cbStatut.getValue() != null ? cbStatut.getValue() : "Tous";

        List<event> base = showingMyOnly
            ? allEvents.stream().filter(e -> myEventIds.contains(e.getId())).collect(Collectors.toList())
            : allEvents;

        return base.stream().filter(e -> {
            boolean matchSearch = search.isEmpty()
                || e.getTitre().toLowerCase().contains(search)
                || (e.getLieu() != null && e.getLieu().toLowerCase().contains(search));
            boolean matchType   = "Tous les types".equals(type)
                || type.equalsIgnoreCase(e.getType());
            boolean matchStatut = "Tous".equals(statut)
                || statut.equalsIgnoreCase(e.getStatus());
            return matchSearch && matchType && matchStatut;
        }).collect(Collectors.toList());
    }

    // ── Render cards into FlowPane ────────────────────────────────────────────
    private void renderCards(List<event> list) {
        if (cardsPane == null) return;
        cardsPane.getChildren().clear();

        String context = showingMyOnly ? "participation(s)" : "événement(s)";
        if (lblTotal != null)
            lblTotal.setText("Affichage de " + list.size() + " " + context);

        if (list.isEmpty()) {
            Label empty = new Label(showingMyOnly
                ? "📭  Vous n'êtes inscrit(e) à aucun événement pour l'instant."
                : "📭  Aucun événement trouvé.");
            empty.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 14; -fx-padding: 40;");
            cardsPane.getChildren().add(empty);
            return;
        }

        for (event ev : list) {
            cardsPane.getChildren().add(buildCard(ev));
        }
    }

    // ── Build one event card ──────────────────────────────────────────────────
    private VBox buildCard(event ev) {
        int  count     = eventService.getParticipantCount(ev.getId());
        int  max       = ev.getCapaciteMax();
        boolean isFull = max > 0 && count >= max;
        boolean isOpen = "Planifié".equalsIgnoreCase(ev.getStatus())
                      || "En cours".equalsIgnoreCase(ev.getStatus());
        boolean inscrit = myEventIds.contains(ev.getId());
        Statut  statut  = myStatuts.get(ev.getId());

        // ── Card container ──
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15; " +
            "-fx-border-color: " + borderColor(ev.getType()) + "; " +
            "-fx-border-width: 0 0 0 5; -fx-border-radius: 15; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 12, 0, 0, 4);");

        // ── Banner ──
        StackPane banner = new StackPane();
        banner.setPrefHeight(130);
        String[] colors = bannerColors(ev.getType());
        banner.setStyle(
            "-fx-background-color: linear-gradient(to bottom right," + colors[0] + "," + colors[1] + ");" +
            "-fx-background-radius: 15 15 0 0;");

        // Type badge
        Label typeBadge = new Label(ev.getType() != null ? ev.getType().toUpperCase() : "");
        typeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.22); -fx-text-fill: white; " +
            "-fx-background-radius: 20; -fx-padding: 3 11 3 11; " +
            "-fx-font-size: 10.5; -fx-font-weight: bold;");
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        StackPane.setMargin(typeBadge, new Insets(12, 0, 0, 12));

        // Status badge
        String[] sc = statusColors(ev.getStatus());
        Label statusBadge = new Label(sc[2] + "  " + ev.getStatus());
        statusBadge.setStyle("-fx-background-color:" + sc[0] + "; -fx-text-fill:" + sc[1] + "; " +
            "-fx-background-radius: 20; -fx-padding: 3 11 3 11; " +
            "-fx-font-size: 10.5; -fx-font-weight: bold;");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(12, 12, 0, 0));

        // Center emoji
        Label emoji = new Label(typeEmoji(ev.getType()));
        emoji.setStyle("-fx-font-size: 44; -fx-opacity: 0.75;");
        StackPane.setAlignment(emoji, Pos.CENTER);

        banner.getChildren().addAll(emoji, typeBadge, statusBadge);

        // ── Body ──
        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 18, 16, 18));

        // Title
        Label title = new Label(ev.getTitre());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2D3748; -fx-wrap-text: true;");
        title.setMaxWidth(264);
        title.setWrapText(true);

        // Meta info
        VBox meta = new VBox(5);
        if (ev.getDate() != null) {
            String time = (ev.getTime() != null && !ev.getTime().isEmpty()) ? "  ·  " + ev.getTime() : "";
            meta.getChildren().add(infoRow("📅", ev.getDate().toString() + time));
        }
        if (ev.getLieu() != null && !ev.getLieu().isEmpty())
            meta.getChildren().add(infoRow("📍", ev.getLieu()));

        // Capacity + progress
        String capLabel = max > 0 ? count + " / " + max + " places" : count + " inscrits";
        String capColor = isFull ? "#E53E3E" : "#48BB78";
        HBox capRow = infoRow("👥", capLabel);
        ((Label) capRow.getChildren().get(1)).setStyle(
            "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + capColor + ";");
        meta.getChildren().add(capRow);

        if (max > 0) {
            ProgressBar pb = new ProgressBar(Math.min((double) count / max, 1.0));
            pb.setPrefWidth(264);
            pb.setMaxHeight(5);
            pb.setStyle(isFull ? "-fx-accent:#E53E3E;"
                : count >= max * 0.8 ? "-fx-accent:#ED8936;" : "-fx-accent:#48BB78;");
            meta.getChildren().add(pb);
        }

        // Description (clamped)
        Label desc = new Label(ev.getDescription() != null ? ev.getDescription() : "");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #718096; -fx-wrap-text: true;");
        desc.setWrapText(true);
        desc.setMaxWidth(264);
        desc.setMaxHeight(36);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #EDF2F7;");

        // ── Action button ──
        Button btnAction;
        if (inscrit) {
            // Already registered — show statut chip + annuler button
            String chipText, chipColor;
            if (statut == Statut.CONFIRME) {
                chipText = "✅  Inscription confirmée";
                chipColor = "#F0FFF4; -fx-text-fill: #276749;";
            } else if (statut == Statut.LISTE_D_ATTENTE) {
                chipText = "⏳  En liste d'attente";
                chipColor = "#FFFFF0; -fx-text-fill: #B7791F;";
            } else {
                chipText = "✅  Inscrit(e)";
                chipColor = "#F0FFF4; -fx-text-fill: #276749;";
            }
            Label chip = new Label(chipText);
            chip.setMaxWidth(Double.MAX_VALUE);
            chip.setStyle("-fx-background-color: " + chipColor +
                " -fx-background-radius: 9; -fx-padding: 8 14; " +
                "-fx-font-weight: bold; -fx-font-size: 12.5;");

            Button btnAnnuler = new Button("✕  Annuler ma participation");
            btnAnnuler.setMaxWidth(Double.MAX_VALUE);
            btnAnnuler.setStyle("-fx-background-color: #FFF5F5; -fx-text-fill: #C53030; " +
                "-fx-background-radius: 9; -fx-padding: 8 14; -fx-cursor: hand; " +
                "-fx-font-size: 12; -fx-border-color: #FED7D7; -fx-border-radius: 9;");
            btnAnnuler.setOnAction(e -> doAnnuler(ev));

            VBox btnBox = new VBox(7, chip, btnAnnuler);
            body.getChildren().addAll(title, meta, sep, desc, btnBox);
        } else if (!isOpen) {
            btnAction = new Button("🔒  Événement " + ev.getStatus());
            btnAction.setMaxWidth(Double.MAX_VALUE);
            btnAction.setStyle("-fx-background-color: #F7FAFC; -fx-text-fill: #A0AEC0; " +
                "-fx-background-radius: 9; -fx-padding: 10 14; -fx-font-size: 12.5; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 9;");
            btnAction.setDisable(true);
            body.getChildren().addAll(title, meta, sep, desc, btnAction);
        } else if (isFull) {
            btnAction = new Button("🔒  Complet — Liste d'attente");
            btnAction.setMaxWidth(Double.MAX_VALUE);
            btnAction.setStyle("-fx-background-color: #FFF5F5; -fx-text-fill: #C53030; " +
                "-fx-background-radius: 9; -fx-padding: 10 14; -fx-font-size: 12.5; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
            btnAction.setOnAction(e -> doParticiper(ev, true));
            body.getChildren().addAll(title, meta, sep, desc, btnAction);
        } else {
            btnAction = new Button("🎟  Participer à cet événement");
            btnAction.setMaxWidth(Double.MAX_VALUE);
            btnAction.setStyle("-fx-background-color: #4A6CF7; -fx-text-fill: white; " +
                "-fx-background-radius: 9; -fx-padding: 10 14; -fx-font-size: 12.5; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
            btnAction.setOnAction(e -> doParticiper(ev, false));

            btnAction.setOnMouseEntered(e -> btnAction.setStyle(btnAction.getStyle()
                .replace("#4A6CF7", "#3a5ce6")));
            btnAction.setOnMouseExited(e -> btnAction.setStyle(btnAction.getStyle()
                .replace("#3a5ce6", "#4A6CF7")));
            body.getChildren().addAll(title, meta, sep, desc, btnAction);
        }

        // Hover on card
        String baseStyle = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace(
            "rgba(0,0,0,0.07),12,0,0,4", "rgba(0,0,0,0.14),18,0,0,6")));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        card.getChildren().addAll(banner, body);
        return card;
    }

    // ── Participate ───────────────────────────────────────────────────────────
    private void doParticiper(event ev, boolean isWaitlist) {
        user me = currentUser();
        if (me == null) {
            alert(Alert.AlertType.ERROR, "Session expirée", "Veuillez vous reconnecter.");
            return;
        }

        // Double-check not already registered
        if (myEventIds.contains(ev.getId())) {
            alert(Alert.AlertType.INFORMATION, "Déjà inscrit(e)",
                "Vous êtes déjà inscrit(e) à « " + ev.getTitre() + " ».");
            return;
        }

        // Confirmation dialog
        String msg = isWaitlist
            ? "Cet événement est complet. Souhaitez-vous rejoindre la liste d'attente ?"
            : "Voulez-vous participer à « " + ev.getTitre() + " » ?";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmer la participation");
        confirm.setHeaderText(isWaitlist ? "🕐 Rejoindre la liste d'attente" : "🎟 Participer à l'événement");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return;

        // Find or create Participant profile
        ensureParticipantProfile(me);
        if (myParticipantId < 0) {
            alert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer votre profil participant.");
            return;
        }

        // Submit reservation
        Reservation res = resService.soumettreDemande(ev.getId(), myParticipantId, 1);
        if (res == null) {
            alert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue. Veuillez réessayer.");
            return;
        }

        switch (res.getStatut()) {
            case CONFIRME:
                myEventIds.add(ev.getId());
                myStatuts.put(ev.getId(), Statut.CONFIRME);
                updateStats();
                alert(Alert.AlertType.INFORMATION, "✅ Inscription confirmée !",
                    "Vous êtes maintenant inscrit(e) à « " + ev.getTitre() + " ».\nBonne participation !");
                break;
            case LISTE_D_ATTENTE:
                myEventIds.add(ev.getId());
                myStatuts.put(ev.getId(), Statut.LISTE_D_ATTENTE);
                updateStats();
                alert(Alert.AlertType.INFORMATION, "⏳ Liste d'attente",
                    "L'événement est complet. Vous avez été ajouté(e) à la liste d'attente.\n" +
                    "Vous serez notifié(e) si une place se libère.");
                break;
            case REFUSE:
                String motif = res.getMotifRefus() != null ? res.getMotifRefus() : "Capacité atteinte.";
                alert(Alert.AlertType.WARNING, "❌ Inscription refusée", motif);
                break;
            default:
                alert(Alert.AlertType.WARNING, "Statut inattendu", res.getStatut().name());
        }

        refresh();
    }

    // ── Cancel participation ──────────────────────────────────────────────────
    private void doAnnuler(event ev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Voulez-vous annuler votre participation à « " + ev.getTitre() + " » ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Annuler la participation");
        confirm.setHeaderText("✕  Annulation");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.YES) return;

        // Find the reservation and cancel it
        if (myParticipantId >= 0) {
            resService.getByParticipant(myParticipantId).stream()
                .filter(r -> r.getEventId() == ev.getId())
                .findFirst()
                .ifPresent(r -> resService.annulerReservation(r.getId()));
        }

        myEventIds.remove(ev.getId());
        myStatuts.remove(ev.getId());
        updateStats();
        alert(Alert.AlertType.INFORMATION, "✅ Participation annulée",
            "Votre inscription à « " + ev.getTitre() + " » a été annulée.");
        refresh();
    }

    // ── Find / create Participant record for current user ────────────────────
    private void ensureParticipantProfile(user me) {
        if (myParticipantId >= 0) return;
        if (me == null) return;
        List<Participant> all = partService.getAll();
        Participant p = all.stream()
            .filter(x -> x.getEmail() != null
                      && x.getEmail().equalsIgnoreCase(me.getEmail()))
            .findFirst().orElse(null);

        if (p == null) {
            String name  = extractName(me.getEmail());
            String phone = me.getPhone() != null ? me.getPhone() : "";
            p = new Participant(name, "", me.getEmail(), phone);
            partService.ajouter(p);
            // Re-fetch to get assigned id
            p = partService.getAll().stream()
                .filter(x -> x.getEmail() != null
                          && x.getEmail().equalsIgnoreCase(me.getEmail()))
                .findFirst().orElse(null);
        }
        if (p != null) myParticipantId = p.getId();
    }

    // ── Update stat labels without full reload ────────────────────────────────
    private void updateStats() {
        if (lblStatMes != null) lblStatMes.setText(String.valueOf(myEventIds.size()));
    }

    // ── Full refresh (re-query DB + re-render) ────────────────────────────────
    private void refresh() {
        // Reload reservations
        myEventIds.clear();
        myStatuts.clear();
        if (myParticipantId >= 0) {
            resService.getByParticipant(myParticipantId).forEach(r -> {
                myEventIds.add(r.getEventId());
                myStatuts.put(r.getEventId(), r.getStatut());
            });
        }
        loadData();
    }

    // ── FXML Handlers ─────────────────────────────────────────────────────────
    @FXML
    public void handleRecherche() {
        renderCards(filteredList());
    }

    @FXML
    public void handleFiltre() {
        renderCards(filteredList());
    }

    @FXML
    public void handleReset() {
        if (tfRecherche != null) tfRecherche.clear();
        if (cbType   != null) cbType.getSelectionModel().selectFirst();
        if (cbStatut != null) cbStatut.getSelectionModel().selectFirst();
        showingMyOnly = false;
        activateTab(true);
        renderCards(filteredList());
    }

    @FXML
    public void showTousLesEvenements() {
        showingMyOnly = false;
        activateTab(true);
        renderCards(filteredList());
    }

    @FXML
    public void showMesParticipations() {
        showingMyOnly = true;
        activateTab(false);
        renderCards(filteredList());
    }

    // ── Tab style toggle ──────────────────────────────────────────────────────
    private void activateTab(boolean tousActive) {
        String active   = "-fx-background-color:#4A6CF7; -fx-text-fill:white; -fx-font-weight:bold; " +
                          "-fx-font-size:13; -fx-cursor:hand;";
        String inactive = "-fx-background-color:#EBF8FF; -fx-text-fill:#2B6CB0; " +
                          "-fx-font-size:13; -fx-cursor:hand;";
        if (btnTabTous != null) {
            String s = tousActive ? active : inactive;
            btnTabTous.setStyle(s + " -fx-background-radius:9 0 0 9; -fx-padding:9 20;");
        }
        if (btnTabMes != null) {
            String s = tousActive ? inactive : active;
            btnTabMes.setStyle(s + " -fx-background-radius:0 9 9 0; -fx-padding:9 20;");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private HBox infoRow(String icon, String text) {
        HBox row = new HBox(7);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 12;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #4A5568;");
        lbl.setMaxWidth(230);
        row.getChildren().addAll(ico, lbl);
        return row;
    }

    private void alert(Alert.AlertType type, String header, String body) {
        Alert a = new Alert(type, body, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private String extractName(String email) {
        if (email == null) return "Patient";
        int at = email.indexOf('@');
        if (at > 0) {
            String n = email.substring(0, at);
            return n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase();
        }
        return email;
    }

    // ── Color / styling helpers ───────────────────────────────────────────────
    private String borderColor(String type) {
        if (type == null) return "#E2E8F0";
        switch (norm(type)) {
            case "DON DE SANG":         return "#DC2626";
            case "VACCINATION":         return "#16A34A";
            case "CONFERENCE MEDICALE": return "#2563EB";
            case "CONSULTATION":        return "#7C3AED";
            case "SENSIBILISATION":     return "#D97706";
            case "ATELIER SANTE":       return "#0D9488";
            case "DEPISTAGE":           return "#0284C7";
            case "PREMIERS SECOURS":    return "#EA580C";
            case "CONFERENCE":          return "#2563EB";
            case "SEMINAIRE":           return "#16A34A";
            case "ATELIER":             return "#D97706";
            default:                    return "#4A6CF7";
        }
    }

    private String[] bannerColors(String type) {
        if (type == null) return new String[]{"#2D3748", "#4A5568"};
        switch (norm(type)) {
            case "DON DE SANG":         return new String[]{"#7F1D1D", "#DC2626"};
            case "VACCINATION":         return new String[]{"#14532D", "#16A34A"};
            case "CONFERENCE MEDICALE": return new String[]{"#1E3A5F", "#2563EB"};
            case "CONSULTATION":        return new String[]{"#312E81", "#7C3AED"};
            case "SENSIBILISATION":     return new String[]{"#92400E", "#D97706"};
            case "ATELIER SANTE":       return new String[]{"#134E4A", "#0D9488"};
            case "DEPISTAGE":           return new String[]{"#1E3A5F", "#0284C7"};
            case "PREMIERS SECOURS":    return new String[]{"#7C2D12", "#EA580C"};
            case "CONFERENCE":          return new String[]{"#1E3A5F", "#4A6CF7"};
            case "SEMINAIRE":           return new String[]{"#14532D", "#16A34A"};
            case "ATELIER":             return new String[]{"#78350F", "#D97706"};
            default:                    return new String[]{"#1A202C", "#4A6CF7"};
        }
    }

    private String typeEmoji(String type) {
        if (type == null) return "📋";
        switch (norm(type)) {
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
            default:                    return "🏥";
        }
    }

    private String[] statusColors(String status) {
        if (status == null) return new String[]{"#E2E8F0", "#4A5568", "◉"};
        switch (status.toLowerCase()) {
            case "planifié":  return new String[]{"#EBF8FF", "#2B6CB0", "◷"};
            case "en cours":  return new String[]{"#F0FFF4", "#276749", "✓"};
            case "terminé":   return new String[]{"#F7FAFC", "#718096", "◉"};
            case "annulé":    return new String[]{"#FFF5F5", "#C53030", "✕"};
            default:          return new String[]{"#E2E8F0", "#4A5568", "◉"};
        }
    }

    private String norm(String s) {
        if (s == null) return "";
        return s.toUpperCase()
            .replace("É","E").replace("È","E").replace("Ê","E")
            .replace("À","A").replace("Â","A")
            .replace("Î","I").replace("Ï","I")
            .replace("Ô","O").replace("Ù","U")
            .replace("Û","U").replace("Ü","U")
            .replace("Ç","C");
    }
}
