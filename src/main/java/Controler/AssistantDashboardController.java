package Controler;

import entities.CalendarEvent;
import entities.CalendarEvent.EventType;
import entities.appointment;
import entities.user;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.CalendarEventService;
import services.appointmentServices;
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard controller for the Assistant role.
 * KPIs: total patients, today's appointments, pending.
 * Panels: today's schedule list, pending appointments with confirm/refuse.
 * Calendar: week-based calendar identical to the admin dashboard.
 */
public class AssistantDashboardController implements Initializable {

    private final appointmentServices apptSvc  = new appointmentServices();
    private final userServices         userSvc  = new userServices();
    private final CalendarEventService eventSvc = new CalendarEventService();

    // ── Hero ──────────────────────────────────────────────────────
    @FXML private Label dateLabel;
    @FXML private Label greetingLabel;

    // ── KPI Cards ─────────────────────────────────────────────────
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayApptsLabel;
    @FXML private Label pendingApptsLabel;

    // ── Schedule / Pending containers ─────────────────────────────
    @FXML private VBox pendingContainer;

    // ── Calendar ──────────────────────────────────────────────────
    @FXML private Label  monthYearLabel;
    @FXML private Label  selectedDateLabel;
    @FXML private HBox   weekRow;
    @FXML private VBox   eventsContainer;
    @FXML private VBox   addEventForm;
    @FXML private TextField eventTitleField;
    @FXML private TextField eventTimeField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextArea eventDescField;

    private int       doctorId        = 0;
    private LocalDate selectedDate    = LocalDate.now();
    private LocalDate currentWeekStart =
            LocalDate.now().with(java.time.DayOfWeek.MONDAY);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.FRENCH)));

        eventTypeCombo.setItems(FXCollections.observableArrayList(
                "Reunion", "Consultation", "Rappel", "Autre"));
        eventTypeCombo.setValue("Autre");

        loadAssistantInfo();
        refreshAll();
    }

    // ── Resolve linked doctor ─────────────────────────────────────
    private void loadAssistantInfo() {
        try {
            int uid = Main.getCurrentUserId();
            for (user u : userSvc.getAll()) {
                if (u.getUserId() == uid) {
                    greetingLabel.setText("Bonjour, " + u.getFirstName() + " ! 👋");
                    this.doctorId = u.getDoctorId();
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            greetingLabel.setText("Bonjour ! 👋");
        }
    }

    private void refreshAll() {
        try {
            List<appointment> all = apptSvc.getAll();
            List<appointment> doctorAppts = (doctorId > 0)
                    ? all.stream().filter(a -> a.getDoctorId() == doctorId).collect(Collectors.toList())
                    : all;

            LocalDate today   = LocalDate.now();
            Date      todaySql = Date.valueOf(today);

            List<appointment> todayAppts = doctorAppts.stream()
                    .filter(a -> a.getAppointmentDate() != null
                              && a.getAppointmentDate().equals(todaySql))
                    .sorted(Comparator.comparing(appointment::getAppointmentTime))
                    .collect(Collectors.toList());

            List<appointment> pendingAppts = doctorAppts.stream()
                    .filter(a -> "EN_ATTENTE".equals(a.getStatus()))
                    .collect(Collectors.toList());

            long totalPatients = (doctorId > 0)
                    ? userSvc.getAll().stream()
                        .filter(u -> u.getRole() == user.Role.patient
                                  && u.getDoctorId() == doctorId).count()
                    : userSvc.getAll().stream()
                        .filter(u -> u.getRole() == user.Role.patient).count();

            // KPIs
            totalPatientsLabel.setText(String.valueOf(totalPatients));
            todayApptsLabel.setText(String.valueOf(todayAppts.size()));
            pendingApptsLabel.setText(String.valueOf(pendingAppts.size()));

            loadPendingAppts(pendingAppts);
            updateCalendarUI();
            loadEventsForSelectedDate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Pending appointments ──────────────────────────────────────
    private void loadPendingAppts(List<appointment> appts) {
        pendingContainer.getChildren().clear();

        if (appts.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous en attente ✅");
            empty.setStyle("-fx-text-fill: #68D391; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
            pendingContainer.getChildren().add(empty);
            return;
        }

        appts.stream().limit(5).forEach(a -> {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setStyle("-fx-background-color: #FFFAF0; -fx-background-radius: 10; " +
                         "-fx-border-color: #F6AD55; -fx-border-radius: 10; -fx-border-width: 0 0 0 4;");

            VBox info = new VBox(3);
            Label nameLabel = new Label("👤 " + apptSvc.getUserFullName(a.getPatientId()));
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2D3748;");
            String dateStr = a.getAppointmentDate() != null
                    ? a.getAppointmentDate().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
            String timeStr = a.getAppointmentTime() != null
                    ? a.getAppointmentTime().toString().substring(0, 5) : "--:--";
            Label sub = new Label("📅 " + dateStr + "  🕐 " + timeStr);
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            info.getChildren().addAll(nameLabel, sub);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnConfirm = new Button("✓ Confirmer");
            btnConfirm.setStyle("-fx-background-color: #48BB78; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 5 10;");
            btnConfirm.setOnAction(e -> confirmAppointment(a));

            Button btnRefuse = new Button("✗ Refuser");
            btnRefuse.setStyle("-fx-background-color: #FC8181; -fx-text-fill: white; " +
                               "-fx-font-size: 11px; -fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 5 10;");
            btnRefuse.setOnAction(e -> refuseAppointment(a));

            row.getChildren().addAll(info, spacer, new HBox(6, btnConfirm, btnRefuse));
            pendingContainer.getChildren().add(row);
        });
    }

    // ── Calendar: week row ────────────────────────────────────────
    private void updateCalendarUI() {
        monthYearLabel.setText(currentWeekStart
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
                .toUpperCase());
        weekRow.getChildren().clear();

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);

            VBox dayBox = new VBox(4);
            dayBox.setAlignment(Pos.CENTER);
            dayBox.setPrefWidth(50);
            dayBox.setPadding(new Insets(8, 5, 8, 5));
            dayBox.setCursor(Cursor.HAND);

            Label dayName = new Label(date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    .substring(0, 3).toUpperCase());
            dayName.setStyle("-fx-font-size: 10; -fx-text-fill: #718096;");

            Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
            dayNum.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

            if (date.equals(selectedDate)) {
                dayBox.setStyle("-fx-background-color: #4A6CF7; -fx-background-radius: 10;");
                dayName.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10;");
                dayNum.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
            } else if (date.equals(LocalDate.now())) {
                dayBox.setStyle("-fx-border-color: #4A6CF7; -fx-border-radius: 10; -fx-border-width: 1;");
            }

            dayBox.setOnMouseClicked(e -> {
                selectedDate = date;
                updateCalendarUI();
                loadEventsForSelectedDate();
            });

            dayBox.getChildren().addAll(dayName, dayNum);
            weekRow.getChildren().add(dayBox);
        }
    }

    // ── Calendar: events for selected day ────────────────────────
    private void loadEventsForSelectedDate() {
        selectedDateLabel.setText(selectedDate
                .format(DateTimeFormatter.ofPattern("MMMM, d", Locale.FRENCH))
                .toUpperCase());
        eventsContainer.getChildren().clear();

        try {
            List<CalendarEvent> events =
                    eventSvc.getByUserAndDate(Main.getCurrentUserId(), selectedDate);

            if (events.isEmpty()) {
                Label empty = new Label("Aucun événement prévu");
                empty.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 12px; -fx-padding: 20 0 0 0;");
                eventsContainer.getChildren().add(empty);
                return;
            }

            for (CalendarEvent ev : events) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 0, 12, 0));
                row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; " +
                             "-fx-border-width: 0 0 1 0;");

                Label time = new Label(ev.getTime());
                time.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #4A5568;");

                VBox content = new VBox(2);
                Label title = new Label(ev.getTitle());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2D3748;");
                Label desc = new Label(ev.getDescription() != null ? ev.getDescription() : "");
                desc.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
                content.getChildren().addAll(title, desc);

                row.getChildren().addAll(time, content);
                eventsContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Calendar navigation ───────────────────────────────────────
    @FXML private void prevWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        updateCalendarUI();
    }

    @FXML private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        updateCalendarUI();
    }

    @FXML private void openAddEventForm() {
        addEventForm.setVisible(true);
        addEventForm.setManaged(true);
    }

    @FXML private void cancelAddEvent() {
        addEventForm.setVisible(false);
        addEventForm.setManaged(false);
        eventTitleField.clear();
        eventDescField.clear();
        eventTimeField.clear();
    }

    @FXML private void confirmAddEvent() {
        String title   = eventTitleField.getText();
        String time    = eventTimeField.getText();
        String desc    = eventDescField.getText();
        String typeStr = eventTypeCombo.getValue();

        if (title.isEmpty() || time.isEmpty()) return;

        try {
            CalendarEvent ev = new CalendarEvent();
            ev.setUserId(Main.getCurrentUserId());
            ev.setDate(selectedDate);
            ev.setTime(time);
            ev.setTitle(title);
            ev.setDescription(desc);
            ev.setType(EventType.valueOf(typeStr.toLowerCase()));
            ev.setColor("#4A6CF7");
            eventSvc.add(ev);
            cancelAddEvent();
            loadEventsForSelectedDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Appointment actions ───────────────────────────────────────
    private void confirmAppointment(appointment a) {
        try { a.setStatus("CONFIRME"); apptSvc.update(a); refreshAll(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private void refuseAppointment(appointment a) {
        try { a.setStatus("REFUSE"); apptSvc.update(a); refreshAll(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void goToPatients()     { Main.showAssistantPatients(); }
    @FXML private void goToAppointments() { Main.showAssistantAppointments(); }

    // ── Helpers ───────────────────────────────────────────────────
    private Label buildStatusBadge(String status) {
        Label badge = new Label();
        switch (status != null ? status : "") {
            case "CONFIRME" -> {
                badge.setText("✓ Confirmé");
                badge.setStyle("-fx-background-color: #C6F6D5; -fx-text-fill: #276749; " +
                               "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
            case "REFUSE" -> {
                badge.setText("✗ Refusé");
                badge.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #C53030; " +
                               "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
            default -> {
                badge.setText("⏳ En attente");
                badge.setStyle("-fx-background-color: #FEFCBF; -fx-text-fill: #744210; " +
                               "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        }
        return badge;
    }
}
