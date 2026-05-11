package Controler;

import entities.CalendarEvent;
import entities.CalendarEvent.EventType;
import entities.user;
import entities.user.AccountStatus;
import entities.user.Role;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import services.CalendarEventService;
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label dateLabel;
    @FXML private Label greetingLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label doctorsLabel;
    @FXML private Label patientsLabel;
    @FXML private Label pendingCountBadge;
    @FXML private VBox pendingContainer;
    @FXML private VBox recentContainer;
    @FXML private PieChart userPieChart;

    // ── Nouveau calendrier ──
    @FXML private HBox weekRow;
    @FXML private Label monthYearLabel;
    @FXML private Label selectedDateLabel;
    @FXML private VBox eventsContainer;

    // ── Formulaire d'ajout ──
    @FXML private VBox addEventForm;
    @FXML private TextField eventTitleField;
    @FXML private TextField eventTimeField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextArea eventDescField;

    private userServices userService = new userServices();
    private CalendarEventService calendarService = new CalendarEventService();

    // Semaine courante : lundi de la semaine affichée
    private LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    private LocalDate selectedDate = LocalDate.now();

    private static final String[] DAY_NAMES_SHORT = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    @FXML
    public void initialize() {
        // Date
        LocalDate today = LocalDate.now();
        String day = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String date = today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
        dateLabel.setText("📅 " + day.substring(0, 1).toUpperCase() + day.substring(1) + ", " + date);

        // Salutation
        int hour = java.time.LocalTime.now().getHour();
        String salut = hour < 12 ? "Bonjour" : hour < 18 ? "Bon après-midi" : "Bonsoir";
        try {
            List<user> all = userService.getAll();
            all.stream()
               .filter(u -> u.getUserId() == Main.getCurrentUserId())
               .findFirst()
               .ifPresent(u -> greetingLabel.setText(salut + ", " + u.getFirstName() + " ! 👋"));
            if (greetingLabel.getText().isEmpty() || greetingLabel.getText().isBlank())
                greetingLabel.setText(salut + ", Administrateur ! 👋");
        } catch (Exception e) {
            greetingLabel.setText(salut + ", Administrateur ! 👋");
        }

        loadDashboard();
        // Initialiser le ComboBox des types
        eventTypeCombo.setItems(FXCollections.observableArrayList(
                "Réunion", "Consultation", "Rappel", "Autre"));
        eventTypeCombo.getSelectionModel().selectFirst();
        buildWeekCalendar();
    }

    // ─── Dashboard Data ────────────────────────────────────────────────
    private void loadDashboard() {
        try {
            List<user> allUsers = userService.getAll();

            long total   = allUsers.size();
            long pending = allUsers.stream().filter(u -> u.getAccountStatus() == AccountStatus.pending).count();
            long active  = allUsers.stream().filter(u -> u.getAccountStatus() == AccountStatus.active).count();
            long doctors  = allUsers.stream().filter(u -> u.getRole() == Role.doctor).count();
            long patients = allUsers.stream().filter(u -> u.getRole() == Role.patient).count();
            long pharmacists = allUsers.stream().filter(u -> u.getRole() == Role.pharmacist).count();
            long assistants  = allUsers.stream().filter(u -> u.getRole() == Role.assistant).count();

            totalUsersLabel.setText(String.valueOf(total));
            pendingUsersLabel.setText(String.valueOf(pending));
            activeUsersLabel.setText(String.valueOf(active));
            doctorsLabel.setText(String.valueOf(doctors));
            patientsLabel.setText(String.valueOf(patients));
            pendingCountBadge.setText(String.valueOf(pending));

            // Pending section
            List<user> pendingList = allUsers.stream()
                    .filter(u -> u.getAccountStatus() == AccountStatus.pending)
                    .collect(Collectors.toList());
            pendingContainer.getChildren().clear();
            if (pendingList.isEmpty()) {
                Label empty = new Label("✅ Aucune demande en attente !");
                empty.setStyle("-fx-text-fill: #48BB78; -fx-font-size: 13; -fx-font-weight: bold;");
                pendingContainer.getChildren().add(empty);
            } else {
                for (user u : pendingList) pendingContainer.getChildren().add(createPendingRow(u));
            }

            // Recent members (last 5)
            List<user> recent = allUsers.stream()
                    .sorted((a, b) -> Integer.compare(b.getUserId(), a.getUserId()))
                    .limit(5).collect(Collectors.toList());
            recentContainer.getChildren().clear();
            for (user u : recent) recentContainer.getChildren().add(createRecentRow(u));

            // Pie chart
            userPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Patients (" + patients + ")", patients),
                new PieChart.Data("Médecins (" + doctors + ")", doctors),
                new PieChart.Data("Pharmaciens (" + pharmacists + ")", pharmacists),
                new PieChart.Data("Assistants (" + assistants + ")", assistants)
            ));
            userPieChart.setTitle("");
            // Color slices
            javafx.application.Platform.runLater(() -> {
                String[] colors = {"#4299E1", "#48BB78", "#ED8936", "#9F7AEA"};
                var items = userPieChart.getData();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getNode() != null) {
                        items.get(i).getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── Nouveau Calendrier Semaine ────────────────────────────────────
    private void buildWeekCalendar() {
        // ── Mise à jour du label mois/année ──
        String monthName = currentWeekStart.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        monthYearLabel.setText(monthName + " " + currentWeekStart.getYear());

        // ── Récupérer les dates avec événements ──
        List<LocalDate> datesWithEvents = List.of();
        try {
            datesWithEvents = calendarService.getDatesWithEvents(Main.getCurrentUserId());
        } catch (SQLException ignored) {}

        final List<LocalDate> eventDates = datesWithEvents;

        // ── Rangée des 7 jours ──
        weekRow.getChildren().clear();
        weekRow.setSpacing(0);
        weekRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeekStart.plusDays(i);
            boolean isSelected = day.equals(selectedDate);
            boolean isToday    = day.equals(LocalDate.now());
            boolean hasEvents  = eventDates.contains(day);

            VBox dayCell = new VBox(3);
            dayCell.setAlignment(Pos.CENTER);
            dayCell.setPrefWidth(48);
            dayCell.setPrefHeight(72);

            Label dayName = new Label(DAY_NAMES_SHORT[i]);
            dayName.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: "
                    + (isSelected ? "white" : "#718096") + ";");

            Label dayNum = new Label(String.valueOf(day.getDayOfMonth()));
            dayNum.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: "
                    + (isSelected ? "white" : (isToday ? "#4A6CF7" : "#2D3748")) + ";");

            dayCell.getChildren().addAll(dayName, dayNum);

            // Petit point si le jour a des événements
            if (hasEvents) {
                StackPane dot = new StackPane();
                dot.setMinSize(6, 6); dot.setMaxSize(6, 6);
                dot.setStyle("-fx-background-color: " + (isSelected ? "white" : "#4A6CF7")
                        + "; -fx-background-radius: 50%;");
                dayCell.getChildren().add(dot);
            }

            if (isSelected) {
                dayCell.setStyle("-fx-background-color: #4A6CF7; -fx-background-radius: 14; -fx-padding: 8 6;");
            } else {
                dayCell.setStyle("-fx-background-color: transparent; -fx-padding: 8 6; -fx-cursor: hand;");
            }

            final LocalDate clickedDate = day;
            dayCell.setOnMouseClicked(e -> {
                selectedDate = clickedDate;
                buildWeekCalendar();
            });

            weekRow.getChildren().add(dayCell);
        }

        // ── Label date sélectionnée ──
        String selMonth = selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase();
        selectedDateLabel.setText(selMonth + ", " + selectedDate.getDayOfMonth());

        buildEventsList();
    }

    /** Construit la liste d'événements pour la date sélectionnée. */
    private void buildEventsList() {
        eventsContainer.getChildren().clear();
        try {
            int userId = Main.getCurrentUserId();
            List<CalendarEvent> userEvents = calendarService.getByUserAndDate(userId, selectedDate);

            // ── Événements créés par l'admin ──
            for (CalendarEvent ev : userEvents) {
                eventsContainer.getChildren().add(createEventRow(ev));
            }

            // ── Inscriptions du jour (lecture seule) ──
            List<user> allUsers = userService.getAll();
            List<user> dayRegistrations = allUsers.stream()
                    .filter(u -> u.getRegistrationDate() != null
                            && u.getRegistrationDate().toLocalDateTime().toLocalDate().equals(selectedDate))
                    .collect(Collectors.toList());
            for (user u : dayRegistrations) {
                String time = u.getRegistrationDate().toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                String label = "Inscription : " + u.getFirstName() + " " + u.getLastName()
                        + " (" + getRoleText(u.getRole()) + ")";
                eventsContainer.getChildren().add(createReadOnlyEventRow(time, label, "#FF5C5C"));
            }

            if (eventsContainer.getChildren().isEmpty()) {
                Label empty = new Label("Aucun événement — cliquez sur « + Ajouter »");
                empty.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 12; -fx-padding: 14 0;");
                eventsContainer.getChildren().add(empty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Ligne d'événement créé par l'admin (avec bouton supprimer). */
    private VBox createEventRow(CalendarEvent ev) {
        VBox wrapper = new VBox(0);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 4, 10, 4));

        Label timeLbl = new Label(ev.getTime());
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #718096; -fx-min-width: 42;");

        StackPane dot = new StackPane();
        dot.setMinSize(9, 9); dot.setMaxSize(9, 9);
        dot.setStyle("-fx-background-color: " + ev.getColor() + "; -fx-background-radius: 50%;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label titleLbl = new Label(ev.getTitle());
        titleLbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
        titleLbl.setWrapText(true);

        // Badge type
        Label typeBadge = new Label(getTypeLabel(ev.getType()));
        typeBadge.setStyle("-fx-background-color: " + ev.getColor() + "22; -fx-text-fill: " + ev.getColor() + ";"
                + "-fx-font-size: 9; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 1 6;");

        info.getChildren().addAll(titleLbl, typeBadge);
        if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
            Label desc = new Label(ev.getDescription());
            desc.setStyle("-fx-font-size: 10; -fx-text-fill: #718096;");
            desc.setWrapText(true);
            info.getChildren().add(desc);
        }

        // Bouton supprimer
        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E0; "
                + "-fx-cursor: hand; -fx-font-size: 12; -fx-padding: 0 4;");
        delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E53E3E; "
                + "-fx-cursor: hand; -fx-font-size: 12; -fx-padding: 0 4;"));
        delBtn.setOnMouseExited(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E0; "
                + "-fx-cursor: hand; -fx-font-size: 12; -fx-padding: 0 4;"));
        delBtn.setOnAction(e -> {
            try {
                calendarService.delete(ev.getId());
                buildEventsList();
                buildWeekCalendar(); // rafraîchir les points sur les jours
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        row.getChildren().addAll(timeLbl, dot, info, delBtn);

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.4;");
        wrapper.getChildren().addAll(row, sep);
        return wrapper;
    }

    /** Ligne d'événement en lecture seule (inscriptions). */
    private VBox createReadOnlyEventRow(String time, String title, String dotColor) {
        VBox wrapper = new VBox(0);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 4, 10, 4));

        Label timeLbl = new Label(time);
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #718096; -fx-min-width: 42;");

        StackPane dot = new StackPane();
        dot.setMinSize(9, 9); dot.setMaxSize(9, 9);
        dot.setStyle("-fx-background-color: " + dotColor + "; -fx-background-radius: 50%;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #2D3748;");
        titleLbl.setWrapText(true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        row.getChildren().addAll(timeLbl, dot, titleLbl);
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.4;");
        wrapper.getChildren().addAll(row, sep);
        return wrapper;
    }

    // ── Formulaire d'ajout ─────────────────────────────────────────────

    @FXML private void openAddEventForm() {
        addEventForm.setVisible(true);
        addEventForm.setManaged(true);
        eventTitleField.requestFocus();
    }

    @FXML private void cancelAddEvent() {
        hideForm();
    }

    @FXML private void confirmAddEvent() {
        String title = eventTitleField.getText().trim();
        String time  = eventTimeField.getText().trim();

        if (title.isEmpty()) {
            eventTitleField.setStyle("-fx-border-color: #E53E3E; -fx-border-radius: 8; "
                    + "-fx-background-radius: 8; -fx-padding: 7 10; -fx-font-size: 12;");
            eventTitleField.setPromptText("⚠ Titre requis");
            return;
        }
        if (!time.matches("\\d{2}:\\d{2}")) {
            eventTimeField.setStyle("-fx-border-color: #E53E3E; -fx-border-radius: 8; "
                    + "-fx-background-radius: 8; -fx-padding: 7 10; -fx-font-size: 12;");
            eventTimeField.setPromptText("⚠ HH:mm");
            return;
        }

        // Mapper le type sélectionné
        String typeStr = eventTypeCombo.getValue();
        EventType type;
        String color;
        switch (typeStr == null ? "Autre" : typeStr) {
            case "Réunion":      type = EventType.reunion;      color = "#4A6CF7"; break;
            case "Consultation": type = EventType.consultation; color = "#10B981"; break;
            case "Rappel":       type = EventType.rappel;       color = "#F59E0B"; break;
            default:             type = EventType.autre;        color = "#E879F9"; break;
        }

        CalendarEvent ev = new CalendarEvent(
                Main.getCurrentUserId(),
                selectedDate,
                time,
                title,
                eventDescField.getText().trim(),
                type,
                color
        );

        try {
            calendarService.add(ev);
            hideForm();
            buildEventsList();
            buildWeekCalendar();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer l'événement.");
        }
    }

    private void hideForm() {
        addEventForm.setVisible(false);
        addEventForm.setManaged(false);
        eventTitleField.clear();
        eventTimeField.clear();
        eventDescField.clear();
        eventTypeCombo.getSelectionModel().selectFirst();
        // Réinitialiser les styles
        eventTitleField.setStyle("-fx-background-color: white; -fx-border-color: #C7D2FE; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 10; -fx-font-size: 12;");
        eventTimeField.setStyle("-fx-background-color: white; -fx-border-color: #C7D2FE; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 7 10; -fx-font-size: 12;");
    }

    private String getTypeLabel(EventType t) {
        switch (t) {
            case reunion:      return "Réunion";
            case consultation: return "Consultation";
            case rappel:       return "Rappel";
            default:           return "Autre";
        }
    }

    @FXML private void prevWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        // Si la date sélectionnée n'est plus dans la semaine, on sélectionne le lundi
        if (selectedDate.isBefore(currentWeekStart) || selectedDate.isAfter(currentWeekStart.plusDays(6))) {
            selectedDate = currentWeekStart;
        }
        buildWeekCalendar();
    }

    @FXML private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        if (selectedDate.isBefore(currentWeekStart) || selectedDate.isAfter(currentWeekStart.plusDays(6))) {
            selectedDate = currentWeekStart;
        }
        buildWeekCalendar();
    }

    // ─── Card builders ─────────────────────────────────────────────────
    private HBox createPendingRow(user u) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #FFFAF0; -fx-background-radius: 12; -fx-padding: 13; "
                + "-fx-border-color: #F6AD55; -fx-border-radius: 12; -fx-border-width: 1;");

        ImageView photo = createAvatar(u, 42);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(u.getFirstName() + " " + u.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2D3748;");
        Label email = new Label("📧 " + u.getEmail());
        email.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        HBox tags = new HBox(8);
        tags.setAlignment(Pos.CENTER_LEFT);
        Label roleLabel = new Label(getRoleText(u.getRole()));
        roleLabel.setStyle("-fx-background-color: " + getRoleColor(u.getRole()) + "; -fx-text-fill: white; "
                + "-fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: bold;");
        tags.getChildren().add(roleLabel);
        if (u.getRegistrationDate() != null) {
            Label dateLbl = new Label("📅 " + u.getRegistrationDate().toString().substring(0, 10));
            dateLbl.setStyle("-fx-font-size: 10; -fx-text-fill: #A0AEC0;");
            tags.getChildren().add(dateLbl);
        }
        info.getChildren().addAll(name, email, tags);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        if (u.getProfessionalDocument() != null && !u.getProfessionalDocument().isEmpty()) {
            Button docBtn = new Button("📄");
            docBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-background-radius: 8; "
                    + "-fx-padding: 6 10; -fx-cursor: hand;");
            docBtn.setOnAction(e -> viewDocument(u));
            buttons.getChildren().add(docBtn);
        }
        Button approve = new Button("✅ Accepter");
        approve.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-padding: 6 12; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11;");
        approve.setOnAction(e -> approveUser(u));

        Button reject = new Button("❌ Refuser");
        reject.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-padding: 6 12; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11;");
        reject.setOnAction(e -> rejectUser(u));

        buttons.getChildren().addAll(approve, reject);
        row.getChildren().addAll(photo, info, buttons);
        return row;
    }

    private HBox createRecentRow(user u) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F7FAFC; -fx-background-radius: 10; -fx-padding: 10;");

        ImageView photo = createAvatar(u, 38);
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(u.getFirstName() + " " + u.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #2D3748;");
        Label email = new Label(u.getEmail());
        email.setStyle("-fx-font-size: 10; -fx-text-fill: #A0AEC0;");
        info.getChildren().addAll(name, email);

        VBox tagBox = new VBox(4);
        tagBox.setAlignment(Pos.CENTER_RIGHT);
        Label role = new Label(getRoleText(u.getRole()));
        role.setStyle("-fx-background-color: " + getRoleColor(u.getRole()) + "; -fx-text-fill: white; "
                + "-fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10;");
        Label status = new Label(getStatusText(u.getAccountStatus()));
        status.setStyle("-fx-background-color: " + getStatusColor(u.getAccountStatus()) + "; -fx-text-fill: white; "
                + "-fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10;");
        tagBox.getChildren().addAll(role, status);

        row.getChildren().addAll(photo, info, tagBox);
        return row;
    }

    private ImageView createAvatar(user u, double size) {
        ImageView iv = new ImageView();
        iv.setFitHeight(size); iv.setFitWidth(size);
        iv.setClip(new Circle(size / 2, size / 2, size / 2));
        String data = (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) ? u.getAvatarUrl() : u.getUserPhoto();
        if (data != null && !data.isEmpty() && !data.startsWith("generated_")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(data.trim().replaceAll("[^A-Za-z0-9+/=]", ""));
                iv.setImage(new Image(new ByteArrayInputStream(bytes)));
            } catch (Exception ex) { setDefaultPhoto(iv); }
        } else { setDefaultPhoto(iv); }
        return iv;
    }

    private void setDefaultPhoto(ImageView iv) {
        try { iv.setImage(new Image(getClass().getResourceAsStream("/default-avatar.png"))); } catch (Exception ignored) {}
    }

    private void approveUser(user u) {
        try {
            u.setAccountStatus(AccountStatus.active);
            userService.update(u);
            loadDashboard();
            showAlert("Succès", "✅ Compte de " + u.getFirstName() + " " + u.getLastName() + " approuvé !");
        } catch (SQLException e) { e.printStackTrace(); showAlert("Erreur", "Impossible d'approuver."); }
    }

    private void rejectUser(user u) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirmation"); c.setHeaderText(null);
        c.setContentText("Refuser le compte de " + u.getFirstName() + " " + u.getLastName() + " ?");
        if (c.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { userService.delete(u); loadDashboard(); showAlert("Refusé", "Compte supprimé."); }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void viewDocument(user u) {
        try {
            byte[] pdf = Base64.getDecoder().decode(u.getProfessionalDocument());
            java.io.File f = java.io.File.createTempFile("doc_" + u.getFirstName() + "_", ".pdf");
            java.nio.file.Files.write(f.toPath(), pdf);
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(f);
        } catch (Exception e) { showAlert("Erreur", "Impossible d'ouvrir le document."); }
    }

    @FXML private void goToAllPending() { Main.showAdminPendingUsers(); }
    @FXML private void goToAllUsers()   { Main.showAdminUtilisateurs(); }

    private String getRoleText(Role r) {
        switch (r) { case patient: return "Patient"; case doctor: return "Docteur"; case pharmacist: return "Pharmacien"; case assistant: return "Assistant"; case admin: return "Admin"; default: return "?"; }
    }
    private String getRoleColor(Role r) {
        switch (r) { case patient: return "#4299E1"; case doctor: return "#48BB78"; case pharmacist: return "#ED8936"; case assistant: return "#9F7AEA"; case admin: return "#FF5C5C"; default: return "#718096"; }
    }
    private String getStatusText(AccountStatus s) {
        switch (s) { case active: return "✓ Actif"; case pending: return "⏳ Attente"; case suspended: return "⛔ Suspendu"; case blocked: return "🚫 Bloqué"; default: return "?"; }
    }
    private String getStatusColor(AccountStatus s) {
        switch (s) { case active: return "#48BB78"; case pending: return "#ED8936"; case suspended: return "#ECC94B"; case blocked: return "#F56565"; default: return "#718096"; }
    }
    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}
