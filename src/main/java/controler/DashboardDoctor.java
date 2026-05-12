package controler;

import entities.appointment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import services.appointmentServices;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardDoctor {

    private final int CURRENT_DOCTOR_ID = 1;

    // ==== FXML ====
    @FXML private DatePicker inlineDatePicker;
    @FXML private VBox planningContainer;
    @FXML private Label statDayCount;
    @FXML private Label messageLabel;

    private final appointmentServices service = new appointmentServices();

    @FXML
    public void initialize() {
        inlineDatePicker.setValue(LocalDate.now());
        showPlanningPane();
    }

    @FXML private HBox panePlanning;

    @FXML
    public void showPlanningPane() {
        if (panePlanning != null) panePlanning.setVisible(true);
        loadPlanning();
    }

    // ==========================================
    //  PLANNING LOAD (from DB)
    // ==========================================
    @FXML
    public void loadPlanning() {
        if (inlineDatePicker == null || inlineDatePicker.getValue() == null) return;

        try {
            List<appointment> allAppointments = service.getAppointmentsByDoctor(CURRENT_DOCTOR_ID);

            List<appointment> dayAppointments = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null
                          && a.getAppointmentDate().toLocalDate().equals(inlineDatePicker.getValue()))
                .sorted((a1, a2) -> {
                    if (a1.getAppointmentTime() == null) return 1;
                    if (a2.getAppointmentTime() == null) return -1;
                    return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
                })
                .collect(Collectors.toList());

            planningContainer.getChildren().clear();

            if (dayAppointments.isEmpty()) {
                Label empty = new Label("Aucun rendez-vous pour cette date.");
                empty.setStyle("-fx-padding: 20; -fx-text-fill: #6B7280; -fx-font-size: 14px;");
                planningContainer.getChildren().add(empty);
                statDayCount.setText("0");
                return;
            }

            statDayCount.setText(String.valueOf(dayAppointments.size()));

            for (appointment a : dayAppointments) {
                // --- TABLE ROW (HBox) ---
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 15 25 15 25; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-background-color: white;");
                
                // Hover effect
                row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 15 25 15 25; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-background-color: #F8FAFC;"));
                row.setOnMouseExited(e -> row.setStyle("-fx-padding: 15 25 15 25; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-background-color: white;"));

                // --- 1. HEURE ---
                Label timeLabel = new Label(a.getAppointmentTime() != null ? a.getAppointmentTime().toString().substring(0, 5) : "--:--");
                timeLabel.setPrefWidth(100);
                timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");

                // --- 2. PATIENT ID (with Avatar) ---
                HBox patientBox = new HBox(10);
                patientBox.setAlignment(Pos.CENTER_LEFT);
                patientBox.setPrefWidth(150);
                ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/avatar_patient.png")));
                avatar.setFitWidth(30); avatar.setFitHeight(30);
                avatar.setClip(new Circle(15, 15, 15));
                Label nameLabel = new Label(service.getUserFullName(a.getPatientId()));
                patientBox.getChildren().addAll(avatar, nameLabel);

                // --- 3. TYPE ---
                String typeStr = a.getType() != null ? a.getType() : "Consultation";
                Label typeBadge = new Label(typeStr);
                typeBadge.setPrefWidth(120);
                typeBadge.getStyleClass().add("badge-blue");

                // --- 4. RAISON ---
                Label reasonLabel = new Label(a.getReason() != null ? a.getReason() : "---");
                reasonLabel.setWrapText(true);
                HBox.setHgrow(reasonLabel, Priority.ALWAYS);
                reasonLabel.setMaxWidth(Double.MAX_VALUE);
                reasonLabel.setStyle("-fx-text-fill: #64748B;");

                // --- 5. STATUT ---
                String status = a.getStatus() != null ? a.getStatus() : "EN_ATTENTE";
                Label statusBadge = new Label(statusLabel(status));
                statusBadge.setPrefWidth(100);
                statusBadge.setAlignment(Pos.CENTER);
                switch (status) {
                    case "CONFIRME" -> statusBadge.getStyleClass().add("badge-green");
                    case "REFUSE"   -> statusBadge.getStyleClass().add("badge-red");
                    default         -> statusBadge.getStyleClass().add("badge-yellow");
                }

                // --- 6. ACTIONS (Hover only or small buttons) ---
                HBox actions = new HBox(5);
                actions.setAlignment(Pos.CENTER_RIGHT);
                if ("EN_ATTENTE".equals(status)) {
                    Button btnOk = new Button("✓"); btnOk.setStyle("-fx-text-fill: green; -fx-background-color: transparent;");
                    btnOk.setOnAction(e -> { 
                        if (a.getReason() != null && a.getReason().contains("[En ligne]")) {
                            TextInputDialog dialog = new TextInputDialog();
                            dialog.setTitle("Lien de la réunion");
                            dialog.setHeaderText("Rendez-vous en ligne");
                            dialog.setContentText("Veuillez saisir le lien vidéo (Zoom, Meet, etc.) :");
                            
                            java.util.Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                a.setMeetingLink(result.get());
                                try { a.setStatus("CONFIRME"); service.update(a); loadPlanning(); } catch (Exception ex) {}
                            }
                        } else {
                            try { a.setStatus("CONFIRME"); service.update(a); loadPlanning(); } catch (Exception ex) {}
                        }
                    });
                    Button btnNo = new Button("✕"); btnNo.setStyle("-fx-text-fill: red; -fx-background-color: transparent;");
                    btnNo.setOnAction(e -> { try { a.setStatus("REFUSE"); service.update(a); loadPlanning(); } catch (Exception ex) {} });
                    actions.getChildren().addAll(btnOk, btnNo);
                }

                // --- ASSEMBLE ---
                row.getChildren().addAll(timeLabel, patientBox, typeBadge, reasonLabel, statusBadge, actions);
                planningContainer.getChildren().add(row);
            }

        } catch (SQLException e) {
            showMessage("Erreur DB: " + e.getMessage(), true);
        }
    }



    // ==========================================
    //  UTILS
    // ==========================================
    private String statusLabel(String status) {
        return switch (status) {
            case "CONFIRME"  -> "✓ Confirmé";
            case "REFUSE"    -> "✖ Refusé";
            default          -> "⏳ En attente";
        };
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(isError ? "message-error" : "message-success");
    }
}
