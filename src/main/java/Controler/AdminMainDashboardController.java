package Controler;

import entities.CalendarEvent;
import entities.CalendarEvent.EventType;
import entities.user;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.CalendarEventService;
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Main Admin Dashboard (AdminDashboardView.fxml).
 * Handles global stats, user distribution, and a mini-calendar with events.
 */
public class AdminMainDashboardController implements Initializable {

    private final userServices userSvc = new userServices();
    private final CalendarEventService eventSvc = new CalendarEventService();

    // -- Hero Banner --
    @FXML private Label dateLabel;
    @FXML private Label greetingLabel;

    // -- KPI Labels --
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label doctorsLabel;
    @FXML private Label patientsLabel;
    @FXML private Label pendingCountBadge;

    // -- Charts --
    @FXML private PieChart userPieChart;

    // -- Containers --
    @FXML private VBox pendingContainer;
    @FXML private VBox recentContainer;

    // -- Calendar --
    @FXML private Label monthYearLabel;
    @FXML private Label selectedDateLabel;
    @FXML private HBox weekRow;
    @FXML private VBox eventsContainer;
    @FXML private VBox addEventForm;

    // -- Event Form Fields --
    @FXML private TextField eventTitleField;
    @FXML private TextField eventTimeField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextArea eventDescField;

    private LocalDate selectedDate = LocalDate.now();
    private LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set date in banner
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.FRENCH)));

        // Configure ComboBox
        eventTypeCombo.setItems(FXCollections.observableArrayList("Reunion", "Consultation", "Rappel", "Autre"));
        eventTypeCombo.setValue("Autre");

        refreshAll();
    }

    private void refreshAll() {
        loadStats();
        loadUserPieChart();
        loadRecentUsers();
        loadPendingApprovals();
        updateCalendarUI();
        loadEventsForSelectedDate();
    }

    private void loadStats() {
        try {
            List<user> all = userSvc.getAll();
            long total = all.size();
            long pending = all.stream().filter(u -> u.getAccountStatus() == user.AccountStatus.pending).count();
            long active = all.stream().filter(u -> u.getAccountStatus() == user.AccountStatus.active).count();
            long doctors = all.stream().filter(u -> u.getRole() == user.Role.doctor).count();
            long patients = all.stream().filter(u -> u.getRole() == user.Role.patient).count();

            totalUsersLabel.setText(String.valueOf(total));
            pendingUsersLabel.setText(String.valueOf(pending));
            activeUsersLabel.setText(String.valueOf(active));
            doctorsLabel.setText(String.valueOf(doctors));
            patientsLabel.setText(String.valueOf(patients));

            pendingCountBadge.setText(String.valueOf(pending));
            pendingCountBadge.setVisible(pending > 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserPieChart() {
        try {
            List<user> all = userSvc.getAll();
            Map<user.Role, Long> roleCounts = all.stream()
                    .collect(Collectors.groupingBy(user::getRole, Collectors.counting()));

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            roleCounts.forEach((role, count) -> {
                data.add(new PieChart.Data(role.name().toUpperCase() + " (" + count + ")", count));
            });

            userPieChart.setData(data);
            userPieChart.setAnimated(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentUsers() {
        recentContainer.getChildren().clear();
        try {
            List<user> all = userSvc.getAll();
            List<user> recent = all.stream()
                    .sorted(Comparator.comparing(user::getUserId).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            if (recent.isEmpty()) {
                recentContainer.getChildren().add(new Label("Aucun membre récent"));
                return;
            }

            for (user u : recent) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));

                Label icon = new Label("👤");
                icon.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0AEC0;");

                VBox details = new VBox(2);
                Label name = new Label(u.getFirstName() + " " + u.getLastName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2D3748;");
                Label role = new Label(u.getRole().name() + " • " + u.getRegistrationDate());
                role.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

                details.getChildren().addAll(name, role);
                row.getChildren().addAll(icon, details);
                recentContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingApprovals() {
        pendingContainer.getChildren().clear();
        try {
            List<user> all = userSvc.getAll();
            List<user> pending = all.stream()
                    .filter(u -> u.getAccountStatus() == user.AccountStatus.pending)
                    .limit(5)
                    .collect(Collectors.toList());

            if (pending.isEmpty()) {
                pendingContainer.getChildren().add(new Label("Aucune demande en attente"));
                return;
            }

            for (user u : pending) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #F7FAFC; -fx-background-radius: 8; -fx-padding: 10;");

                VBox info = new VBox(2);
                Label name = new Label(u.getFirstName() + " " + u.getLastName());
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
                Label type = new Label("Compte " + u.getRole().name());
                type.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
                info.getChildren().addAll(name, type);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnVal = new Button("Valider");
                btnVal.setStyle("-fx-background-color: #4A6CF7; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5;");
                btnVal.setOnAction(e -> validateUser(u));

                row.getChildren().addAll(info, spacer, btnVal);
                pendingContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void validateUser(user u) {
        try {
            u.setAccountStatus(user.AccountStatus.active);
            userSvc.update(u);
            refreshAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -- Calendar Logic --

    private void updateCalendarUI() {
        monthYearLabel.setText(currentWeekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).toUpperCase());
        weekRow.getChildren().clear();

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            VBox dayBox = new VBox(4);
            dayBox.setAlignment(Pos.CENTER);
            dayBox.setPrefWidth(50);
            dayBox.setPadding(new Insets(8, 5, 8, 5));
            dayBox.setCursor(javafx.scene.Cursor.HAND);

            Label dayName = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH).substring(0, 3).toUpperCase());
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

    private void loadEventsForSelectedDate() {
        selectedDateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMMM, d", Locale.FRENCH)).toUpperCase());
        eventsContainer.getChildren().clear();

        try {
            List<CalendarEvent> events = eventSvc.getByUserAndDate(Main.getCurrentUserId(), selectedDate);
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
                row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; -fx-border-width: 0 0 1 0;");

                VBox timeBox = new VBox();
                Label time = new Label(ev.getTime());
                time.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #4A5568;");
                timeBox.getChildren().add(time);

                VBox content = new VBox(2);
                Label title = new Label(ev.getTitle());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2D3748;");
                Label desc = new Label(ev.getDescription());
                desc.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
                content.getChildren().addAll(title, desc);

                row.getChildren().addAll(timeBox, content);
                eventsContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void prevWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        updateCalendarUI();
    }

    @FXML
    private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        updateCalendarUI();
    }

    @FXML
    private void openAddEventForm() {
        addEventForm.setVisible(true);
        addEventForm.setManaged(true);
    }

    @FXML
    private void cancelAddEvent() {
        addEventForm.setVisible(false);
        addEventForm.setManaged(false);
        eventTitleField.clear();
        eventDescField.clear();
        eventTimeField.clear();
    }

    @FXML
    private void confirmAddEvent() {
        String title = eventTitleField.getText();
        String time = eventTimeField.getText();
        String desc = eventDescField.getText();
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

    @FXML private void goToAllPending() { /* Navigation logic if needed */ }
    @FXML private void goToAllUsers() { /* Navigation logic if needed */ }
}
