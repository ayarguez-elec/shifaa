package Controler;

import entities.appointment;
import entities.availability;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import services.appointmentServices;
import services.availabilityServices;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import tests.Main;

/**
 * DashboardPatient — Interface unifiée patient SHIFAA
 *
 * Fonctionnalités :
 *  - Liste des rendez-vous (cartes) avec noms de médecins (pas d'ID)
 *  - Calendrier de disponibilités par médecin (libre / occupé)
 *  - Formulaire de création de demande (pré-rempli depuis le calendrier)
 *  - Modification d'une demande existante
 */
public class DashboardPatient {

    // ── ID patient courant (à remplacer par la session réelle) ──────────────
    private static int CURRENT_PATIENT_ID = 14;
    private static String PATIENT_NAME     = "Aya Nguez";

    // ── Services ────────────────────────────────────────────────────────────
    private final appointmentServices  apptService = new appointmentServices();
    private final availabilityServices avService   = new availabilityServices();

    // ── Données chargées une seule fois ────────────────────────────────────
    /** doctorId → "Prénom Nom" */
    private Map<Integer, String>       doctorNames       = new LinkedHashMap<>();
    /** spécialité → liste "Prénom Nom (ID: X)" */
    private Map<String, List<String>>  doctorMap         = new LinkedHashMap<>();
    /** disponibilités du médecin sélectionné dans le calendrier */
    private List<availability>         currentAvailabilities = new ArrayList<>();

    // ── État calendrier ─────────────────────────────────────────────────────
    private LocalDate calendarMonth    = LocalDate.now().withDayOfMonth(1);
    private LocalDate selectedDate     = null;
    /** ID du RDV en cours de modification */
    private int       currentUpdateId  = -1;

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Sidebar
    // ════════════════════════════════════════════════════════════════════════
    @FXML private ImageView patientAvatar;
    @FXML private Label     patientNameLabel;
    @FXML private Label     patientIdLabel;

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Tabs / Panes
    // ════════════════════════════════════════════════════════════════════════
    @FXML private Button tabListe;
    @FXML private Button tabAjouter;

    @FXML private VBox paneListe;
    @FXML private VBox paneAjouter;
    @FXML private VBox paneModifier;

    @FXML private HBox  messageBox;
    @FXML private Label messageLabel;

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Liste
    // ════════════════════════════════════════════════════════════════════════
    @FXML private FlowPane listContainer;

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Liste des médecins (Pane Ajouter, côté gauche)
    // ════════════════════════════════════════════════════════════════════════
    @FXML private VBox             doctorCardsContainer;

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Calendrier & Formulaire ajout (Pane Ajouter, côté droit)
    // ════════════════════════════════════════════════════════════════════════
    @FXML private VBox             calendarCard;
    @FXML private Label            calendarTitleLabel;
    @FXML private Label            calendarMonthLabel;
    @FXML private GridPane         calendarGrid;
    @FXML private VBox             timeSlotsContainer;
    @FXML private Label            selectedDateLabel;

    @FXML private VBox             addFormContainer;
    @FXML private Label            addFormDoctorLabel;
    @FXML private DatePicker       addDate;
    @FXML private TextField        addTime;
    @FXML private ComboBox<String> addType;
    @FXML private ComboBox<String> addPayment;
    @FXML private ComboBox<String> addMode;
    @FXML private TextArea         addReason;

    private int selectedDoctorIdForAdd = -1;
    private List<appointment> currentAppointments = new ArrayList<>();

    // ════════════════════════════════════════════════════════════════════════
    //  FXML — Formulaire modification (Pane Modifier)
    // ════════════════════════════════════════════════════════════════════════
    @FXML private DatePicker       updDatePicker;
    @FXML private VBox             updResultsContainer;
    @FXML private VBox             updFormContainer;
    @FXML private ComboBox<String> updDoctorSpec;
    @FXML private ComboBox<String> updDoctorName;
    @FXML private DatePicker       updDate;
    @FXML private TextField        updTime;
    @FXML private ComboBox<String> updType;
    @FXML private ComboBox<String> updPayment;
    @FXML private ComboBox<String> updMode;
    @FXML private TextArea         updReason;

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // Charger les données de session
        entities.user currentUser = utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            CURRENT_PATIENT_ID = currentUser.getUserId();
            PATIENT_NAME = currentUser.getFirstName() + " " + currentUser.getLastName();
        } else {
            // Fallback si pas de session (mode test/debug)
            try {
                java.sql.Connection con = utils.MyDataBase.getInstance().getConnection();
                String sql = "SELECT id, firstName, lastName FROM user WHERE role='patient' LIMIT 1";
                java.sql.Statement st = con.createStatement();
                java.sql.ResultSet rs = st.executeQuery(sql);
                if (rs.next()) {
                    CURRENT_PATIENT_ID = rs.getInt(1);
                    PATIENT_NAME = rs.getString(2) + " " + rs.getString(3);
                }
            } catch (Exception ignored) {}
        }

        // Sidebar patient
        patientNameLabel.setText(PATIENT_NAME);
        patientIdLabel.setText("ID: " + CURRENT_PATIENT_ID);
        try {
            Image img = new Image(getClass().getResourceAsStream("/avatar_patient_woman.png"));
            patientAvatar.setImage(img);
            patientAvatar.setClip(new Circle(36, 36, 36));
        } catch (Exception ignored) {}

        // Options fixes
        String[] types    = {"Contrôle", "Urgence", "Suivi", "Consultation"};
        String[] payments = {"Espèces", "Carte bancaire"};
        String[] modes    = {"Présentiel", "En ligne"};

        addType.getItems().addAll(types);    updType.getItems().addAll(types);
        addPayment.getItems().addAll(payments); updPayment.getItems().addAll(payments);
        addMode.getItems().addAll(modes);    updMode.getItems().addAll(modes);

        loadDoctors();
        loadDoctorCards();
        showListePane();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CHARGEMENT DES MÉDECINS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Charge la map spécialité→médecins et construit doctorNames (id→nom).
     * Les entrées dans doctorMap ont le format "Prénom Nom (ID: X)" afin
     * qu'on puisse extraire l'ID via extractId(), mais on n'affiche
     * à l'utilisateur que "Prénom Nom" dans la ComboBox des médecins.
     */
    private void loadDoctors() {
        doctorMap = apptService.getDoctorsBySpecialty();
        doctorNames.clear();

        // Construire doctorNames (id → "Prénom Nom") à partir de doctorMap
        for (List<String> entries : doctorMap.values()) {
            for (String entry : entries) {
                int id = extractId(entry);
                if (id != -1) {
                    // entry : "Dr. Prénom Nom (ID: X)"  ou "Prénom Nom (ID: X)"
                    String name = entry
                            .replaceFirst("^Dr\\.\\s*", "")  // supprimer "Dr. " éventuel
                            .replaceAll("\\s*\\(ID:.*", "")  // supprimer "(ID: X)"
                            .replaceAll("\\s*\\(.*\\)$", "") // supprimer "(Spécialité)" éventuel
                            .trim();
                    doctorNames.put(id, name);
                }
            }
        }

        // Peupler spécialités dans la combo modification
        updDoctorSpec.getItems().setAll(doctorMap.keySet());
    }

    /**
     * Retourne le nom complet d'un médecin à partir de son ID,
     * sans afficher l'ID. Ex : "Amira Ben Trabelsi"
     */
    private String getDoctorDisplayName(int doctorId) {
        String name = doctorNames.get(doctorId);
        if (name != null) return name;
        
        // Fallback to service for a more robust lookup
        return apptService.getUserFullName(doctorId);
    }


    // ════════════════════════════════════════════════════════════════════════
    //  GESTION DES SPECS → NOM (listes déroulantes form)
    // ════════════════════════════════════════════════════════════════════════



    @FXML
    public void handleSpecChangeUpd() {
        String spec = updDoctorSpec.getValue();
        updDoctorName.getItems().clear();
        if (spec == null) return;

        List<String> raw = doctorMap.get(spec);
        if (raw == null) return;

        for (String entry : raw) {
            updDoctorName.getItems().add(cleanDoctorDisplay(entry));
        }
    }

    /**
     * Convertit une entrée brute "Dr. Prénom Nom (ID: X)" en "Prénom Nom".
     */
    private String cleanDoctorDisplay(String entry) {
        return entry
                .replaceFirst("^Dr\\.\\s*", "")
                .replaceAll("\\s*\\(ID:.*", "")
                .replaceAll("\\s*\\([^)]*\\)$", "")
                .trim();
    }

    /**
     * Retrouve l'ID d'un médecin depuis son nom affiché (recherche dans doctorNames).
     */
    private int getDoctorIdByDisplayName(String displayName) {
        if (displayName == null) return -1;
        for (Map.Entry<Integer, String> e : doctorNames.entrySet()) {
            if (e.getValue().equalsIgnoreCase(displayName.trim())) return e.getKey();
        }
        return -1;
    }

    /** Extrait l'ID depuis une entrée brute "... (ID: X)". */
    private int extractId(String raw) {
        if (raw == null || !raw.contains("ID: ")) return -1;
        try {
            return Integer.parseInt(raw.split("ID:\\s*")[1].replace(")", "").trim());
        } catch (Exception e) { return -1; }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION ENTRE PANES
    // ════════════════════════════════════════════════════════════════════════

    @FXML public void showListePane() {
        resetPanes();
        paneListe.setVisible(true);
        paneListe.setManaged(true);
        activateTab(tabListe);
        loadList();
    }

    @FXML public void showAjouterPane() {
        resetPanes();
        paneAjouter.setVisible(true);
        paneAjouter.setManaged(true);
        activateTab(tabAjouter);
        if (selectedDoctorIdForAdd != -1) {
            refreshCalendar();
        }
    }

    @FXML public void showModifierPane() {
        resetPanes();
        paneModifier.setVisible(true);
        paneModifier.setManaged(true);
        if (updFormContainer != null) updFormContainer.setVisible(false);
        if (updResultsContainer != null) updResultsContainer.getChildren().clear();
    }

    private void resetPanes() {
        paneListe.setVisible(false);
        paneListe.setManaged(false);
        paneAjouter.setVisible(false);
        paneAjouter.setManaged(false);
        paneModifier.setVisible(false);
        paneModifier.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        // Reset tab styles
        tabListe.getStyleClass().removeAll("nav-tab-active", "menu-button-active");
        tabListe.getStyleClass().add("menu-button");
        tabAjouter.getStyleClass().removeAll("nav-tab-active", "menu-button-active");
        tabAjouter.getStyleClass().add("menu-button");
    }

    private void activateTab(Button tab) {
        if (tab == null) return;
        tab.getStyleClass().remove("menu-button");
        tab.getStyleClass().add("menu-button-active");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LISTE DES RENDEZ-VOUS
    // ════════════════════════════════════════════════════════════════════════

    private void loadList() {
        listContainer.getChildren().clear();
        try {
            List<appointment> list = apptService.getAppointmentsByPatient(CURRENT_PATIENT_ID);
            if (list.isEmpty()) {
                Label empty = new Label("Vous n'avez aucune demande de rendez-vous pour le moment.");
                empty.setStyle("-fx-padding: 30; -fx-text-fill: #6B7280; -fx-font-size: 14px;");
                listContainer.getChildren().add(empty);
                return;
            }
            for (appointment a : list) {
                listContainer.getChildren().add(buildAppointmentCard(a));
            }
        } catch (SQLException e) {
            showMessage("Erreur de chargement : " + e.getMessage(), true);
        }
    }

    private VBox buildAppointmentCard(appointment a) {
        VBox card = new VBox(14);
        card.getStyleClass().add("appointment-card");

        // — Header : type + statut —
        HBox header = new HBox();
        header.getStyleClass().add("appointment-card-header");

        String type = a.getType() != null ? a.getType() : "Consultation";
        Label typeBadge = new Label(type);
        typeBadge.getStyleClass().addAll("badge", typeBadgeClass(type));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String status = a.getStatus() != null ? a.getStatus() : "EN_ATTENTE";
        Label statusBadge = new Label(statusLabel(status));
        statusBadge.getStyleClass().addAll("badge", statusBadgeClass(status));

        header.getChildren().addAll(typeBadge, spacer, statusBadge);

        // — Avatar —
        VBox avatarWrap = new VBox();
        avatarWrap.getStyleClass().add("appointment-card-avatar-container");
        try {
            ImageView av = new ImageView(new Image(getClass().getResourceAsStream("/avatar_doctor.png")));
            av.setFitWidth(70); av.setFitHeight(70);
            av.setClip(new Circle(35, 35, 35));
            avatarWrap.getChildren().add(av);
        } catch (Exception ignored) {}

        // — Infos : NOM du médecin (pas l'ID) —
        VBox info = new VBox(5);
        info.getStyleClass().add("appointment-card-info");

        // Afficher le vrai nom du médecin
        String docDisplay = getDoctorDisplayName(a.getDoctorId());
        Label docLabel  = new Label("Dr. " + docDisplay);
        docLabel.getStyleClass().add("appointment-card-patient-name");

        Label dateLabel = new Label("📅 " + (a.getAppointmentDate() != null ? a.getAppointmentDate() : "N/A"));
        dateLabel.getStyleClass().add("appointment-card-date");

        Label timeLabel = new Label("🕒 " + (a.getAppointmentTime() != null ? a.getAppointmentTime().toString().substring(0, 5) : "N/A"));
        timeLabel.getStyleClass().add("appointment-card-detail");

        Label reasonLabel = new Label(a.getReason() != null && !a.getReason().isBlank()
                ? a.getReason() : "Aucun motif renseigné");
        reasonLabel.getStyleClass().add("appointment-card-detail");
        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-font-style: italic;");

        info.getChildren().addAll(docLabel, dateLabel, timeLabel, reasonLabel);

        if ("CONFIRME".equals(status) && a.getMeetingLink() != null && !a.getMeetingLink().isBlank()) {
            Label linkLabel = new Label("Lien vidéo (Zoom / Meet) : " + a.getMeetingLink());
            linkLabel.getStyleClass().add("appointment-card-detail");
            linkLabel.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
            info.getChildren().add(linkLabel);
        } else if ("CONFIRME".equals(status)) {
            String addr = apptService.getUserAddress(a.getDoctorId());
            if (addr != null && !addr.isBlank() && !"Adresse non spécifiée".equals(addr)) {
                Button btnMap = new Button("📍 Voir l'itinéraire (Google Maps)");
                btnMap.getStyleClass().add("card-action-btn");
                btnMap.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #1F2937; -fx-border-color: #D1D5DB;");
                btnMap.setOnAction(e -> openMapWindow(addr));
                info.getChildren().add(btnMap);
            }
        }

        // — Actions —
        HBox actions = new HBox(10);
        actions.getStyleClass().add("appointment-card-actions");

        if ("EN_ATTENTE".equals(status)) {
            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("card-action-btn");
            btnEdit.setOnAction(e -> {
                showModifierPane();
                loadAppointmentToUpdate(a.getAppointmentId());
            });

            Button btnCancel = new Button("Annuler");
            btnCancel.getStyleClass().add("card-action-btn-danger");
            btnCancel.setOnAction(e -> {
                try {
                    apptService.delete(a);
                    showMessage("Demande annulée avec succès.", false);
                    loadList();
                } catch (Exception ex) {
                    showMessage("Erreur : " + ex.getMessage(), true);
                }
            });
            actions.getChildren().addAll(btnEdit, btnCancel);
        } else {
            Label lInfo = new Label("Demande traitée — aucune modification possible");
            lInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
            actions.getChildren().add(lInfo);
        }

        card.getChildren().addAll(header, avatarWrap, info, actions);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CARTE INTERACTIVE (GOOGLE MAPS)
    // ════════════════════════════════════════════════════════════════════════
    private void openMapWindow(String address) {
        if (address == null || address.isBlank()) return;
        try {
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
            String url = "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;
            
            // Ouvrir la carte dans le navigateur web par défaut du système.
            // Cela évite le bug de rendu JavaFX WebKit (Texture must be non-null)
            // et offre une meilleure expérience pour démarrer la navigation.
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            
        } catch (Exception ex) {
            showMessage("Impossible d'ouvrir la carte : " + ex.getMessage(), true);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CALENDRIER DE DISPONIBILITÉS
    // ════════════════════════════════════════════════════════════════════════

    private void loadDoctorCards() {
        doctorCardsContainer.getChildren().clear();
        
        if (doctorMap == null || doctorMap.isEmpty()) {
            Label lbl = new Label("Aucun médecin trouvé dans la base de données.");
            lbl.setStyle("-fx-padding: 20; -fx-text-fill: #9CA3AF;");
            doctorCardsContainer.getChildren().add(lbl);
            return;
        }

        for (Map.Entry<String, List<String>> specEntry : doctorMap.entrySet()) {
            String spec = specEntry.getKey();
            for (String raw : specEntry.getValue()) {
                int id = extractId(raw);
                if (id != -1) {
                    String name = cleanDoctorDisplay(raw);
                    doctorNames.putIfAbsent(id, name);
                    VBox card = buildDoctorSelectCard(id, name, spec);
                    doctorCardsContainer.getChildren().add(card);
                }
            }
        }
    }

    private VBox buildDoctorSelectCard(int docId, String name, String spec) {
        VBox card = new VBox();
        card.getStyleClass().add("doctor-select-card");

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView();
        try {
            avatar.setImage(new Image(getClass().getResourceAsStream("/avatar_doctor.png")));
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.setClip(new Circle(20, 20, 20));
        } catch (Exception ignored) {}
        avatar.getStyleClass().add("doctor-select-avatar");

        VBox info = new VBox(2);
        info.getStyleClass().add("doctor-select-info");
        Label nameLbl = new Label("Dr. " + name);
        nameLbl.getStyleClass().add("doctor-select-name");
        Label specLbl = new Label(spec);
        specLbl.getStyleClass().add("doctor-select-spec");
        info.getChildren().addAll(nameLbl, specLbl);

        content.getChildren().addAll(avatar, info);
        card.getChildren().add(content);

        card.setOnMouseClicked(e -> selectDoctorForAdd(docId, name, card));
        return card;
    }

    private void selectDoctorForAdd(int docId, String name, VBox selectedCard) {
        selectedDoctorIdForAdd = docId;

        // Update active styles
        for (javafx.scene.Node node : doctorCardsContainer.getChildren()) {
            node.getStyleClass().remove("doctor-select-card-active");
            if (!node.getStyleClass().contains("doctor-select-card")) {
                node.getStyleClass().add("doctor-select-card");
            }
        }
        selectedCard.getStyleClass().remove("doctor-select-card");
        selectedCard.getStyleClass().add("doctor-select-card-active");

        calendarTitleLabel.setText("Disponibilités - Dr. " + name);
        addFormDoctorLabel.setText("Dr. " + name);
        
        calendarCard.setVisible(true);
        calendarCard.setManaged(true);
        addFormContainer.setVisible(false);
        addFormContainer.setManaged(false);

        try {
            currentAvailabilities = avService.getAvailabilitiesByDoctor(docId);
        } catch (Exception e) {
            currentAvailabilities = new ArrayList<>();
            showMessage("Erreur chargement disponibilités : " + e.getMessage(), true);
        }

        try {
            currentAppointments = apptService.getAppointmentsByDoctor(docId);
        } catch (Exception e) {
            currentAppointments = new ArrayList<>();
        }
        
        selectedDate = null;
        timeSlotsContainer.getChildren().clear();
        selectedDateLabel.setText("Sélectionnez une date disponible");
        refreshCalendar();
    }

    private int extractDoctorIdFromCalendarItem(String item) {
        return getDoctorIdByDisplayName(item);
    }

    @FXML public void handlePrevMonth() {
        calendarMonth = calendarMonth.minusMonths(1);
        refreshCalendar();
    }

    @FXML public void handleNextMonth() {
        calendarMonth = calendarMonth.plusMonths(1);
        refreshCalendar();
    }

    private void refreshCalendar() {
        // Libellé du mois
        String mois = calendarMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        mois = Character.toUpperCase(mois.charAt(0)) + mois.substring(1);
        calendarMonthLabel.setText(mois + " " + calendarMonth.getYear());

        // Supprimer les jours (lignes > 0) — garder la ligne d'en-tête (row 0)
        calendarGrid.getChildren().removeIf(n -> {
            Integer row = GridPane.getRowIndex(n);
            return row != null && row > 0;
        });

        YearMonth ym = YearMonth.of(calendarMonth.getYear(), calendarMonth.getMonthValue());
        int days    = ym.lengthOfMonth();
        int startDow = calendarMonth.getDayOfWeek().getValue(); // 1=Lun

        int col = startDow - 1;
        int row = 1;

        for (int day = 1; day <= days; day++) {
            LocalDate date = LocalDate.of(calendarMonth.getYear(), calendarMonth.getMonthValue(), day);
            boolean isFree     = hasFreeSlotsOn(date);
            boolean isBusy     = hasOccupiedSlotsOn(date);
            boolean isSelected = date.equals(selectedDate);
            boolean isPast     = date.isBefore(LocalDate.now());

            Label lbl = new Label(String.valueOf(day));
            lbl.setMinSize(40, 40);
            lbl.setMaxSize(40, 40);
            lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("calendar-day");

            if (isPast) {
                lbl.getStyleClass().add("calendar-day-past");
            } else if (isSelected) {
                lbl.getStyleClass().add("calendar-day-selected");
            } else if (isFree) {
                lbl.getStyleClass().add("calendar-day-free");
                final LocalDate d = date;
                lbl.setOnMouseClicked(e -> selectDate(d));
                lbl.setStyle("-fx-cursor: hand;");
            } else if (isBusy) {
                lbl.getStyleClass().add("calendar-day-busy");
            } else {
                lbl.getStyleClass().add("calendar-day-none");
            }

            calendarGrid.add(lbl, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private boolean hasFreeSlotsOn(LocalDate date) {
        if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
            return false; // Samedi ou Dimanche
        }
        
        // On génère les créneaux de 08:00 à 16:00 (1 par heure)
        for (int hour = 8; hour <= 16; hour++) {
            String timeStr = String.format("%02d:00", hour);
            if (isSlotAvailable(date, timeStr)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOccupiedSlotsOn(LocalDate date) {
        if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
            return false;
        }
        // Occupé s'il y a un créneau bloqué dans 'availability' ce jour-là
        return currentAvailabilities.stream().anyMatch(a ->
                a.getDate().toLocalDate().equals(date) &&
                        !"libre".equalsIgnoreCase(a.getStatusRDVAsString()));
    }

    private boolean isSlotAvailable(LocalDate date, String timeStr) {
        // 1. Vérifier si ce créneau exact est marqué "occupé" dans availability
        boolean isOccupied = currentAvailabilities.stream().anyMatch(a ->
                a.getDate().toLocalDate().equals(date) &&
                a.getTime().toString().startsWith(timeStr) &&
                !"libre".equalsIgnoreCase(a.getStatusRDVAsString())
        );
        if (isOccupied) return false;

        // 2. Vérifier si un rendez-vous (non REFUSE) existe à cette date et heure
        boolean hasAppointment = currentAppointments.stream().anyMatch(a ->
                a.getAppointmentDate() != null && a.getAppointmentTime() != null &&
                a.getAppointmentDate().toLocalDate().equals(date) &&
                a.getAppointmentTime().toString().startsWith(timeStr) &&
                !"REFUSE".equals(a.getStatus())
        );
        if (hasAppointment) return false;

        return true;
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        String jourNom = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
        String moisNom = date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
        selectedDateLabel.setText("Créneaux du " + jourNom + " " + date.getDayOfMonth()
                + " " + moisNom + " " + date.getYear());
        refreshCalendar();
        loadTimeSlotsForDate(date);
    }

    private void loadTimeSlotsForDate(LocalDate date) {
        timeSlotsContainer.getChildren().clear();

        List<String> freeTimes = new ArrayList<>();
        if (date.getDayOfWeek().getValue() != 6 && date.getDayOfWeek().getValue() != 7) {
            for (int hour = 8; hour <= 16; hour++) {
                String timeStr = String.format("%02d:00", hour);
                if (isSlotAvailable(date, timeStr)) {
                    freeTimes.add(timeStr);
                }
            }
        }

        if (freeTimes.isEmpty()) {
            Label none = new Label("Aucun créneau libre ce jour.");
            none.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-padding: 6 0 0 0;");
            timeSlotsContainer.getChildren().add(none);
            return;
        }

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        int count = 0;
        for (String time : freeTimes) {
            Button slot = new Button(time);
            slot.getStyleClass().add("time-slot-btn");
            slot.setOnAction(e -> prefillFormWithSlot(time));
            row.getChildren().add(slot);
            count++;
            if (count % 4 == 0) {
                timeSlotsContainer.getChildren().add(row);
                row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
            }
        }
        if (!row.getChildren().isEmpty()) {
            timeSlotsContainer.getChildren().add(row);
        }
    }

    /**
     * Pré-remplit le formulaire (côté droit) avec le médecin
     * sélectionné dans le calendrier et le créneau choisi.
     * Seul le NOM du médecin est affiché (sans ID).
     */
    private void prefillFormWithSlot(String time) {
        if (selectedDate == null) return;
        if (selectedDoctorIdForAdd == -1) return;

        // Remplir date et heure
        addDate.setValue(selectedDate);
        addTime.setText(time);

        // Afficher le formulaire
        addFormContainer.setVisible(true);
        addFormContainer.setManaged(true);

        showMessage("Créneau sélectionné — complétez les détails ci-dessous.", false);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AJOUT D'UN RENDEZ-VOUS
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleAdd() {
        if (selectedDoctorIdForAdd == -1) { showMessage("Veuillez sélectionner un médecin.", true); return; }
        if (addDate.getValue() == null) { showMessage("Veuillez choisir une date.", true); return; }
        if (addTime.getText().isBlank()) { showMessage("Veuillez saisir une heure.", true); return; }
        if (addType.getValue() == null)  { showMessage("Veuillez choisir un type.", true); return; }
        if (addPayment.getValue() == null) { showMessage("Veuillez choisir un mode de paiement.", true); return; }

        try {
            String timeText = addTime.getText().trim();
            if (timeText.length() == 5) timeText += ":00";

            appointment a = new appointment();
            a.setPatientId(CURRENT_PATIENT_ID);
            a.setDoctorId(selectedDoctorIdForAdd);
            a.setAppointmentDate(Date.valueOf(addDate.getValue()));
            a.setAppointmentTime(Time.valueOf(timeText));
            a.setType(addType.getValue());
            String modeStr = addMode.getValue() != null ? "[" + addMode.getValue() + "] " : "";
            a.setReason(modeStr + (addReason.getText() != null ? addReason.getText().trim() : ""));
            a.setPaymentMethodFromString(addPayment.getValue());
            a.setStatus("EN_ATTENTE");

            apptService.add(a);
            showMessage("✓ Demande envoyée avec succès !", false);
            clearAddForm();
            showListePane();

        } catch (IllegalArgumentException e) {
            showMessage("Format d'heure invalide. Utilisez HH:mm", true);
        } catch (Exception e) {
            showMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleCancelAdd() {
        addFormContainer.setVisible(false);
        addFormContainer.setManaged(false);
        addDate.setValue(null);
        addTime.clear();
        addReason.clear();
        addType.setValue(null);
        addPayment.setValue(null);
        addMode.setValue(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR NAVIGATION (INTEGRATED)
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void consulterDossier() {
        Main.showPatientMedicalFile();
    }

    @FXML
    public void voirOrdonnances() {
        Main.showPatientPrescriptions();
    }

    @FXML
    public void goToHealthStats() {
        Main.showPatientHealthStats();
    }

    @FXML
    public void deconnexion() {
        Main.showLoginPage();
    }

    private void clearAddForm() {
        handleCancelAdd();
        selectedDoctorIdForAdd = -1;
        
        for (javafx.scene.Node node : doctorCardsContainer.getChildren()) {
            node.getStyleClass().remove("doctor-select-card-active");
            if (!node.getStyleClass().contains("doctor-select-card")) {
                node.getStyleClass().add("doctor-select-card");
            }
        }
        
        calendarCard.setVisible(false);
        calendarCard.setManaged(false);
        timeSlotsContainer.getChildren().clear();
        selectedDateLabel.setText("Sélectionnez une date disponible");
        selectedDate = null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MODIFICATION D'UN RENDEZ-VOUS
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleSearchByDateUpd() {
        if (updDatePicker.getValue() == null) return;
        try {
            Date d = Date.valueOf(updDatePicker.getValue());
            List<appointment> list = apptService.getAppointmentsByPatient(CURRENT_PATIENT_ID);
            updResultsContainer.getChildren().clear();
            updFormContainer.setVisible(false);
            int found = 0;

            for (appointment a : list) {
                if (a.getAppointmentDate() != null
                        && a.getAppointmentDate().toString().equals(d.toString())
                        && "EN_ATTENTE".equals(a.getStatus())) {
                    found++;
                    // Afficher le nom du médecin dans le bouton de résultat
                    String docName = getDoctorDisplayName(a.getDoctorId());
                    Button b = new Button(String.format("Demande #%d — Dr. %s — %s",
                            a.getAppointmentId(), docName,
                            a.getAppointmentTime() != null
                                    ? a.getAppointmentTime().toString().substring(0, 5) : ""));
                    b.getStyleClass().add("search-result-btn");
                    b.setPrefWidth(580);
                    b.setOnAction(e -> loadAppointmentToUpdate(a.getAppointmentId()));
                    updResultsContainer.getChildren().add(b);
                }
            }
            if (found == 0) showMessage("Aucune demande en attente le " + d, true);
            else showMessage(found + " demande(s) trouvée(s).", false);
        } catch (Exception e) {
            showMessage("Erreur : " + e.getMessage(), true);
        }
    }

    private void loadAppointmentToUpdate(int id) {
        try {
            appointment a = apptService.getAppointmentById(id);
            if (a == null || a.getPatientId() != CURRENT_PATIENT_ID) {
                showMessage("Demande introuvable.", true);
                return;
            }
            currentUpdateId = id;

            // Sélectionner spécialité + médecin (sans afficher l'ID)
            if (doctorMap != null) {
                boolean found = false;
                for (Map.Entry<String, List<String>> entry : doctorMap.entrySet()) {
                    if (found) break;
                    for (String raw : entry.getValue()) {
                        if (extractId(raw) == a.getDoctorId()) {
                            updDoctorSpec.setValue(entry.getKey());
                            handleSpecChangeUpd();
                            updDoctorName.setValue(cleanDoctorDisplay(raw));
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (a.getAppointmentDate() != null) updDate.setValue(a.getAppointmentDate().toLocalDate());
            if (a.getAppointmentTime() != null) updTime.setText(a.getAppointmentTime().toString().substring(0, 5));
            updType.setValue(a.getType());
            updPayment.setValue(a.getPaymentMethodAsString());
            updReason.setText(a.getReason() != null ? a.getReason() : "");
            updFormContainer.setVisible(true);
            showMessage("Demande #" + id + " chargée.", false);

        } catch (Exception e) {
            showMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleUpdate() {
        if (currentUpdateId == -1) return;

        int docId = getDoctorIdByDisplayName(updDoctorName.getValue());
        if (docId == -1) { showMessage("Veuillez sélectionner un médecin.", true); return; }
        if (updDate.getValue() == null) { showMessage("Date obligatoire.", true); return; }
        if (updTime.getText().isBlank()) { showMessage("Heure obligatoire.", true); return; }

        try {
            String t = updTime.getText().trim();
            if (t.length() == 5) t += ":00";

            appointment a = new appointment();
            a.setAppointmentId(currentUpdateId);
            a.setPatientId(CURRENT_PATIENT_ID);
            a.setDoctorId(docId);
            a.setAppointmentDate(Date.valueOf(updDate.getValue()));
            a.setAppointmentTime(Time.valueOf(t));
            a.setType(updType.getValue());
            a.setPaymentMethodFromString(updPayment.getValue());
            String modeStr = updMode.getValue() != null ? "[" + updMode.getValue() + "] " : "";
            a.setReason(modeStr + (updReason.getText() != null ? updReason.getText().trim() : ""));
            a.setStatus("EN_ATTENTE");

            apptService.update(a);
            showMessage("✓ Demande #" + currentUpdateId + " mise à jour !", false);
            currentUpdateId = -1;
            showListePane();

        } catch (IllegalArgumentException e) {
            showMessage("Format d'heure invalide. Utilisez HH:mm", true);
        } catch (Exception e) {
            showMessage("Erreur : " + e.getMessage(), true);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════════════════

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        if (messageBox != null) {
            messageBox.setVisible(true);
            messageBox.setManaged(true);
        }
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        messageLabel.getStyleClass().removeAll("badge-green", "badge-red", "message-success", "message-error");
        messageLabel.getStyleClass().add(isError ? "badge-red" : "badge-green");
    }

    private String statusLabel(String status) {
        return switch (status != null ? status : "") {
            case "CONFIRME"   -> "Confirmée";
            case "REFUSE"     -> "Refusée";
            case "EN_ATTENTE" -> "En attente";
            default           -> status != null ? status : "En attente";
        };
    }

    private String statusBadgeClass(String status) {
        return switch (status != null ? status : "") {
            case "CONFIRME" -> "badge-green";
            case "REFUSE"   -> "badge-red";
            default         -> "badge-yellow";
        };
    }

    private String typeBadgeClass(String type) {
        return switch (type != null ? type : "") {
            case "Urgence"      -> "badge-red";
            case "Suivi"        -> "badge-green";
            case "Contrôle"     -> "badge-blue";
            case "Consultation" -> "badge-yellow";
            default             -> "badge-gray";
        };
    }
}
