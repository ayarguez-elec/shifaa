package Controler;

import entities.CalendarEvent;
import entities.CalendarEvent.EventType;
import entities.user;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import services.CalendarEventService;
import services.userServices;
import tests.Main;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DoctorDashboardController implements Initializable {

    @FXML private Label lblDate;
    @FXML private Label lblWelcome;
    @FXML private Label lblTodayRDV;
    @FXML private Label lblNewPatients;
    @FXML private Label lblRating;
    @FXML private VBox vboxAgenda;

    // Calendar elements
    @FXML private HBox weekRow;
    @FXML private Label monthYearLabel;
    @FXML private Label selectedDateLabel;
    @FXML private VBox calendarEventsContainer;

    // Add Event Form
    @FXML private VBox addEventForm;
    @FXML private TextField eventTitleField;
    @FXML private TextField eventTimeField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextArea eventDescField;

    private CalendarEventService calendarService = new CalendarEventService();
    private userServices userService = new userServices();
    private services.feedbackServices feedbackService = new services.feedbackServices();
    private services.appointmentServices appointmentService = new services.appointmentServices();

    private LocalDate currentWeekStart;
    private LocalDate selectedDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        selectedDate = LocalDate.now();
        
        setCurrentDate();
        loadDoctorInfo();
        loadRealStats();
        loadAgenda();
        
        // Init ComboBox
        eventTypeCombo.setItems(FXCollections.observableArrayList("Réunion", "Consultation", "Rappel", "Autre"));
        eventTypeCombo.getSelectionModel().selectFirst();
        
        renderWeek();
    }

    private void setCurrentDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.FRENCH);
        String formattedDate = now.format(formatter);
        lblDate.setText(formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1));
    }

    private void loadDoctorInfo() {
        try {
            int doctorId = Main.currentUserId;
            user doctor = userService.getById(doctorId);
            if (doctor != null) {
                lblWelcome.setText("Bonjour, Dr. " + doctor.getLastName() + " ! 👋");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRealStats() {
        try {
            int doctorId = Main.currentUserId;
            
            // 1. RDV Aujourd'hui (Appointments + Events)
            List<entities.appointment> allAppts = appointmentService.getAppointmentsByDoctor(doctorId);
            long todayApptCount = allAppts.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().equals(LocalDate.now()))
                .filter(a -> "CONFIRME".equals(a.getStatus()))
                .count();
                
            List<CalendarEvent> todayEvents = calendarService.getByUserAndDate(doctorId, LocalDate.now());
            lblTodayRDV.setText(String.valueOf(todayApptCount + todayEvents.size()));

            // 2. Count patients for this doctor
            long patientCount = userService.getAll().stream()
                    .filter(u -> u.getRole() == user.Role.patient && u.getDoctorId() == doctorId)
                    .count();
            lblNewPatients.setText(String.valueOf(patientCount));

            // 3. Average Rating
            double avg = feedbackService.getAll().stream()
                    .filter(f -> f.getTargetId() == doctorId && f.getTargetType() == entities.feedback.TargetType.doctor)
                    .mapToInt(entities.feedback::getRating)
                    .average().orElse(0.0);
            lblRating.setText(String.format("%.1f/5", avg));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAgenda() {
        try {
            int doctorId = Main.currentUserId;
            
            // Charger les rendez-vous réels
            List<entities.appointment> allAppts = appointmentService.getAppointmentsByDoctor(doctorId);
            List<entities.appointment> todayAppts = allAppts.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().equals(LocalDate.now()))
                .filter(a -> "CONFIRME".equals(a.getStatus()))
                .sorted((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()))
                .collect(java.util.stream.Collectors.toList());

            // Charger les événements manuels
            List<CalendarEvent> todayEvents = calendarService.getByUserAndDate(doctorId, LocalDate.now());
            
            vboxAgenda.getChildren().clear();
            
            if (todayAppts.isEmpty() && todayEvents.isEmpty()) {
                Label emptyLabel = new Label("Aucune consultation prévue aujourd'hui.");
                emptyLabel.setStyle("-fx-text-fill: #A0AEC0; -fx-font-style: italic;");
                vboxAgenda.getChildren().add(emptyLabel);
            } else {
                // Ajouter les rendez-vous en premier
                for (entities.appointment a : todayAppts) {
                    vboxAgenda.getChildren().add(createAppointmentCardFromAppt(a));
                }
                // Ajouter les événements
                for (CalendarEvent event : todayEvents) {
                    vboxAgenda.getChildren().add(createAppointmentCard(event));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createAppointmentCardFromAppt(entities.appointment a) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #EBF8FF; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #BEE3F8; -fx-border-radius: 10;");
        
        Label time = new Label(a.getAppointmentTime().toString().substring(0, 5));
        time.setStyle("-fx-font-weight: bold; -fx-text-fill: #2B6CB0; -fx-min-width: 50;");
        
        VBox info = new VBox(2);
        String patientName = appointmentService.getUserFullName(a.getPatientId());
        Label title = new Label("RDV : " + patientName);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        Label desc = new Label(a.getType() + " - " + (a.getReason() != null ? a.getReason() : ""));
        desc.setStyle("-fx-font-size: 11; -fx-text-fill: #4A5568;");
        info.getChildren().addAll(title, desc);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label badge = new Label("PATIENT");
        badge.setStyle("-fx-background-color: #BEE3F8; -fx-text-fill: #2B6CB0; -fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 5;");
        
        card.getChildren().addAll(time, info, spacer, badge);
        return card;
    }

    private Node createAppointmentCard(CalendarEvent event) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #F7FAFC; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        
        Label time = new Label(event.getTime());
        time.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A6CF7; -fx-min-width: 50;");
        
        VBox info = new VBox(2);
        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        Label desc = new Label(event.getDescription());
        desc.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        info.getChildren().addAll(title, desc);
        
        card.getChildren().addAll(time, info);
        return card;
    }

    // ========== CALENDAR LOGIC ==========

    private void renderWeek() {
        weekRow.getChildren().clear();
        String monthName = currentWeekStart.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        monthYearLabel.setText(monthName + " " + currentWeekStart.getYear());

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            weekRow.getChildren().add(createDayNode(date));
        }
        loadCalendarEvents();
    }

    private Node createDayNode(LocalDate date) {
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(50);
        vbox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        
        if (date.equals(selectedDate)) {
            vbox.setStyle(vbox.getStyle() + "-fx-background-color: #4A6CF7;");
        } else if (date.equals(LocalDate.now())) {
            vbox.setStyle(vbox.getStyle() + "-fx-background-color: #EEF2FF;");
        }

        Label dayName = new Label(date.format(DateTimeFormatter.ofPattern("E", Locale.FRENCH)).toUpperCase());
        dayName.setStyle("-fx-font-size: 10; -fx-text-fill: " + (date.equals(selectedDate) ? "white" : "#718096") + ";");
        
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (date.equals(selectedDate) ? "white" : "#2D3748") + ";");

        vbox.getChildren().addAll(dayName, dayNum);
        vbox.setOnMouseClicked(e -> {
            selectedDate = date;
            renderWeek();
        });
        
        return vbox;
    }

    private void loadCalendarEvents() {
        calendarEventsContainer.getChildren().clear();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
        selectedDateLabel.setText(selectedDate.format(dtf));

        try {
            int doctorId = Main.currentUserId;
            
            // 1. Ajouter les Rendez-vous réels
            List<entities.appointment> allAppts = appointmentService.getAppointmentsByDoctor(doctorId);
            List<entities.appointment> dayAppts = allAppts.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().equals(selectedDate))
                .filter(a -> "CONFIRME".equals(a.getStatus()))
                .collect(java.util.stream.Collectors.toList());

            for (entities.appointment a : dayAppts) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-padding: 5 0;");
                
                Circle dot = new Circle(4, javafx.scene.paint.Color.web("#4A6CF7")); // Blue for appointments
                String pName = appointmentService.getUserFullName(a.getPatientId());
                Label txt = new Label(a.getAppointmentTime().toString().substring(0, 5) + " - " + pName);
                txt.setStyle("-fx-font-size: 12; -fx-text-fill: #2D3748; -fx-font-weight: bold;");
                
                item.getChildren().addAll(dot, txt);
                calendarEventsContainer.getChildren().add(item);
            }

            // 2. Ajouter les Événements manuels
            List<CalendarEvent> events = calendarService.getByUserAndDate(doctorId, selectedDate);
            for (CalendarEvent ev : events) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-padding: 5 0;");
                
                Circle dot = new Circle(4, javafx.scene.paint.Color.web(ev.getColor()));
                Label txt = new Label(ev.getTime() + " - " + ev.getTitle());
                txt.setStyle("-fx-font-size: 12; -fx-text-fill: #4A5568;");
                
                item.getChildren().addAll(dot, txt);
                calendarEventsContainer.getChildren().add(item);
            }

            if (dayAppts.isEmpty() && events.isEmpty()) {
                Label l = new Label("Aucun événement");
                l.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11;");
                calendarEventsContainer.getChildren().add(l);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== ADD EVENT FORM LOGIC ==========

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
        String time = eventTimeField.getText().trim();

        if (title.isEmpty() || !time.matches("\\d{2}:\\d{2}")) {
            return;
        }

        String typeStr = eventTypeCombo.getValue();
        EventType type;
        String color;
        switch (typeStr) {
            case "Réunion": type = EventType.reunion; color = "#4A6CF7"; break;
            case "Consultation": type = EventType.consultation; color = "#10B981"; break;
            case "Rappel": type = EventType.rappel; color = "#F59E0B"; break;
            default: type = EventType.autre; color = "#E879F9"; break;
        }

        CalendarEvent ev = new CalendarEvent(Main.currentUserId, selectedDate, time, title, eventDescField.getText().trim(), type, color);
        try {
            calendarService.add(ev);
            hideForm();
            loadCalendarEvents();
            renderWeek();
            loadAgenda(); // refresh today's agenda if applicable
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hideForm() {
        addEventForm.setVisible(false);
        addEventForm.setManaged(false);
        eventTitleField.clear();
        eventTimeField.clear();
        eventDescField.clear();
    }

    @FXML private void prevWeek() { currentWeekStart = currentWeekStart.minusWeeks(1); renderWeek(); }
    @FXML private void nextWeek() { currentWeekStart = currentWeekStart.plusWeeks(1); renderWeek(); }

    @FXML private void goToAddPatient() { Main.showDoctorPatients(); }
    @FXML private void goToAddFile() { Main.showAddMedicalFile(); }
    @FXML private void goToAddPrescription() { Main.showAjouterPrescription(); }
}
