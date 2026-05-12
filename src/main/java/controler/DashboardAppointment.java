package controler;

import entities.appointment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.appointmentServices;
import services.availabilityServices;
import services.EmailService;
import entities.availability;
import javafx.scene.chart.PieChart;
import java.util.HashMap;
import java.util.stream.Collectors;


import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import utils.myDataBase;

public class DashboardAppointment {

    @FXML private Button tabListe;
    @FXML private Button tabStats;
    @FXML private Button tabAjouter;
    
    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;


    // PANES
    @FXML private VBox paneListe;
    @FXML private VBox paneStats;
    @FXML private VBox paneAjouter;
    @FXML private VBox paneModifier;
    @FXML private VBox paneSupprimer;

    // ==== GLOBAL ====
    @FXML private Label messageLabel;
    private final appointmentServices service = new appointmentServices();

    @FXML private Label statTotal;
    @FXML private Label statToday;
    @FXML private FlowPane listContainer;

    // ==== PANE AJOUTER ====
    @FXML private ComboBox<String> addPatientId;
    @FXML private ComboBox<String> addDoctorSpec;
    @FXML private ComboBox<String> addDoctorName;
    @FXML private DatePicker addDate;
    @FXML private TextField addTime;
    @FXML private ComboBox<String> addType;
    @FXML private ComboBox<String> addPayment;
    @FXML private ComboBox<String> addMode;
    @FXML private TextArea addReason;

    // ==== PANE MODIFIER ====
    @FXML private DatePicker updDatePicker;
    @FXML private VBox updResultsContainer;
    @FXML private VBox updFormContainer;
    @FXML private ComboBox<String> updPatientId;
    @FXML private ComboBox<String> updDoctorSpec;
    @FXML private ComboBox<String> updDoctorName;
    @FXML private DatePicker updDate;
    @FXML private TextField updTime;
    @FXML private ComboBox<String> updType;
    @FXML private ComboBox<String> updPayment;
    @FXML private ComboBox<String> updMode;
    @FXML private TextArea updReason;
    private int currentUpdateId = -1;

    // ==== PANE SUPPRIMER ====
    @FXML private DatePicker delDatePicker;
    @FXML private VBox delResultsContainer;
    @FXML private VBox delInfoContainer;
    @FXML private Label delDetailsLabel;
    private int currentDeleteId = -1;

    // ==== PANE STATS ====
    @FXML private VBox doctorPerformanceVBox;
    @FXML private PieChart typePieChart;
    @FXML private Label insightFillRate;
    @FXML private Label insightOptimalSlots;

    private java.util.Map<String, List<String>> doctorMap;

    @FXML
    public void initialize() {
        String[] types    = {"Contrôle", "Urgence", "Suivi", "Consultation"};
        String[] payments = {"espèce", "carte bancaire"};
        String[] modes    = {"Présentiel", "En ligne"};

        addType.getItems().addAll(types);
        updType.getItems().addAll(types);
        addPayment.getItems().addAll(payments);
        updPayment.getItems().addAll(payments);
        addMode.getItems().addAll(modes);
        updMode.getItems().addAll(modes);

        loadDoctors();
        loadAssistantProfile();
        showListePane();
    }

    private void loadAssistantProfile() {
        String[] info = service.getAssistantInfo();
        if (info != null) {
            sidebarUserName.setText(info[0]);
            sidebarUserRole.setText(info[1]);
        }
    }


    private void loadDoctors() {
        doctorMap = service.getDoctorsBySpecialty();
        addDoctorSpec.getItems().clear();
        updDoctorSpec.getItems().clear();
        addDoctorSpec.getItems().addAll(doctorMap.keySet());
        updDoctorSpec.getItems().addAll(doctorMap.keySet());
        
        List<String> patients = service.getPatientsList();
        addPatientId.getItems().clear();
        updPatientId.getItems().clear();
        addPatientId.getItems().addAll(patients);
        updPatientId.getItems().addAll(patients);
    }

    @FXML
    private void handleSpecChangeAdd() {
        String spec = addDoctorSpec.getValue();
        addDoctorName.getItems().clear();
        
        // Update types based on specialty
        addType.getItems().clear();
        String[] baseTypes = {"Contrôle", "Urgence", "Suivi", "Consultation"};
        addType.getItems().addAll(baseTypes);
        
        if (spec != null) {
            addDoctorName.getItems().addAll(doctorMap.get(spec));
        }
    }

    @FXML
    private void handleSpecChangeUpd() {
        String spec = updDoctorSpec.getValue();
        updDoctorName.getItems().clear();
        
        // Update types based on specialty
        updType.getItems().clear();
        String[] baseTypes = {"Contrôle", "Urgence", "Suivi", "Consultation"};
        updType.getItems().addAll(baseTypes);
        
        if (spec != null) {
            updDoctorName.getItems().addAll(doctorMap.get(spec));
        }
    }

    private int extractId(String doctorInfo) {
        if (doctorInfo == null || !doctorInfo.contains("ID: ")) return -1;
        try {
            return Integer.parseInt(doctorInfo.split("ID: ")[1].replace(")", ""));
        } catch (Exception e) { return -1; }
    }

    // TAB SWITCHING
    private void resetTabs() {
        tabListe.getStyleClass().remove("nav-tab-active");
        tabListe.getStyleClass().add("nav-tab");
        if (tabStats != null) {
            tabStats.getStyleClass().remove("nav-tab-active");
            tabStats.getStyleClass().add("nav-tab");
        }
        
        tabAjouter.getStyleClass().remove("btn-add-highlight-active");
        paneListe.setVisible(false);
        paneStats.setVisible(false);
        paneAjouter.setVisible(false);
        paneModifier.setVisible(false);
        paneSupprimer.setVisible(false);
        messageLabel.setVisible(false);
    }

    @FXML public void showListePane()     { resetTabs(); activate(tabListe,     paneListe);     loadList(); }
    @FXML public void showStatsPane()     { resetTabs(); activate(tabStats,     paneStats);     loadStats(); }
    @FXML public void showAjouterPane()   { 
        resetTabs(); 
        activate(tabAjouter, paneAjouter); 
        loadDoctors(); // This method reloads both doctors and patients
    }

    @FXML public void showModifierPane()  {
        resetTabs(); activate(null,  paneModifier);
        updFormContainer.setVisible(false);
        if (updDatePicker != null) updDatePicker.setValue(null);
        if (updResultsContainer != null) updResultsContainer.getChildren().clear();
    }
    @FXML public void showSupprimerPane() {
        resetTabs(); activate(null, paneSupprimer);
        delInfoContainer.setVisible(false);
        if (delDatePicker != null) delDatePicker.setValue(null);
        if (delResultsContainer != null) delResultsContainer.getChildren().clear();
    }

    private void activate(Button tab, VBox pane) {
        if (tab != null) {
            tab.getStyleClass().remove("nav-tab");
            if (tab == tabAjouter) {
                // Keep the highlight style but maybe add an active state if desired
            } else {
                tab.getStyleClass().add("nav-tab-active");
            }
        }
        pane.setVisible(true);
    }


    private boolean isUrgent(appointment a) {
        if (a.getReason() == null) return false;
        String reason = a.getReason().toLowerCase();
        return reason.contains("urgence") || 
               reason.contains("douleur thoracique") || 
               reason.contains("saignement") ||
               reason.contains("malaise") ||
               reason.contains("grave");
    }

    private void loadList() {
        try {
            List<appointment> all = service.getAll();
            
            // === ALGORITHME DE TRIAGE INTELLIGENT ===
            all.sort((a1, a2) -> {
                boolean wait1 = "EN_ATTENTE".equals(a1.getStatus());
                boolean wait2 = "EN_ATTENTE".equals(a2.getStatus());
                
                // Les "En attente" passent avant les autres
                if (wait1 && !wait2) return -1;
                if (!wait1 && wait2) return 1;
                
                if (wait1 && wait2) {
                    boolean urg1 = isUrgent(a1);
                    boolean urg2 = isUrgent(a2);
                    // Les Urgences passent avant les non-urgences
                    if (urg1 && !urg2) return -1;
                    if (!urg1 && urg2) return 1;
                }
                
                // Sinon on garde l'ordre d'arrivée (FIFO par ID)
                return Integer.compare(a1.getAppointmentId(), a2.getAppointmentId());
            });

            listContainer.getChildren().clear();

            if (all.isEmpty()) {
                Label empty = new Label("Aucun rendez-vous enregistré.");
                empty.setStyle("-fx-padding: 20; -fx-text-fill: #6B7280; -fx-font-size: 14px;");
                listContainer.getChildren().add(empty);
                statTotal.setText("0");
                statToday.setText("0");
                return;
            }

            int totalCount = all.size();
            int confirmedCount = 0;

            for (appointment a : all) {
                if ("CONFIRME".equals(a.getStatus())) confirmedCount++;

                // --- CARD MAIN CONTAINER ---
                VBox card = new VBox();
                card.getStyleClass().add("appointment-card");

                // --- HEADER: BADGES ---
                HBox header = new HBox();
                header.getStyleClass().add("appointment-card-header");
                
                String type = a.getType() != null ? a.getType() : "Général";
                Label typeBadge = new Label(type);
                switch (type) {
                    case "Urgence" -> typeBadge.getStyleClass().add("badge-red");
                    case "Suivi" -> typeBadge.getStyleClass().add("badge-green");
                    case "Consultation" -> typeBadge.getStyleClass().add("badge-yellow");
                    default -> typeBadge.getStyleClass().add("badge-blue");
                }
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                
                String status = a.getStatus() != null ? a.getStatus() : "EN_ATTENTE";
                Label statusBadge = new Label(statusLabel(status));
                switch (status) {
                    case "CONFIRME" -> statusBadge.getStyleClass().add("badge-green");
                    case "REFUSE" -> statusBadge.getStyleClass().add("badge-red");
                    default -> statusBadge.getStyleClass().add("badge-yellow");
                }
                
                if ("EN_ATTENTE".equals(status) && isUrgent(a)) {
                    Label urgBadge = new Label("🚨 PRIORITÉ URGENCE");
                    urgBadge.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-border-color: #FCA5A5; -fx-border-radius: 12;");
                    
                    // Simple animation (clignotement)
                    javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(800), urgBadge);
                    ft.setFromValue(1.0);
                    ft.setToValue(0.4);
                    ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    ft.setAutoReverse(true);
                    ft.play();
                    
                    header.getChildren().addAll(urgBadge, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, typeBadge, spacer, statusBadge);
                } else {
                    header.getChildren().addAll(typeBadge, spacer, statusBadge);
                }

                // --- AVATAR SECTION ---
                VBox avatarContainer = new VBox();
                avatarContainer.getStyleClass().add("appointment-card-avatar-container");
                
                ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/avatar_patient.png")));
                avatar.setFitWidth(70);
                avatar.setFitHeight(70);
                avatar.getStyleClass().add("appointment-card-avatar");
                
                // Circle clip
                Circle clip = new Circle(35, 35, 35);
                avatar.setClip(clip);
                
                avatarContainer.getChildren().add(avatar);

                // --- INFO SECTION ---
                VBox info = new VBox();
                info.getStyleClass().add("appointment-card-info");
                
                Label nameLabel = new Label(service.getUserFullName(a.getPatientId()));
                nameLabel.getStyleClass().add("appointment-card-patient-name");
                
                Label docLabel = new Label("Dr. " + service.getUserFullName(a.getDoctorId()));
                docLabel.getStyleClass().add("appointment-card-detail");
                
                Label dateLabel = new Label(a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "Date N/A");
                dateLabel.getStyleClass().add("appointment-card-date");
                
                Label timeLabel = new Label("🕒 " + (a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "Heure N/A"));
                timeLabel.getStyleClass().add("appointment-card-detail");
                
                Label reasonLabel = new Label(a.getReason() != null ? a.getReason() : "Pas de motif");
                reasonLabel.getStyleClass().add("appointment-card-detail");
                reasonLabel.setWrapText(true);
                reasonLabel.setStyle("-fx-font-style: italic;");

                info.getChildren().addAll(nameLabel, docLabel, dateLabel, timeLabel, reasonLabel);

                // --- ACTIONS SECTION ---
                HBox actions = new HBox();
                actions.getStyleClass().add("appointment-card-actions");

                if ("EN_ATTENTE".equals(status)) {
                    Button btnConf = new Button("Confirmer");
                    btnConf.getStyleClass().add("card-action-btn-primary");
                    btnConf.setOnAction(e -> {
                        if (a.getReason() != null && a.getReason().contains("[En ligne]")) {
                            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
                            String meetLink = "https://meet.google.com/" + uuid.substring(0, 3) + "-" + uuid.substring(3, 7) + "-" + uuid.substring(7, 10);
                            
                            a.setMeetingLink(meetLink);
                            a.setStatus("CONFIRME");
                            try { 
                                service.update(a); 
                                loadList(); 
                                showMessage("Confirmé ! Lien généré.", false); 
                                
                                // ENVOI EMAIL AVEC LIEN POUR EN LIGNE
                                String patientName = service.getUserFullName(a.getPatientId());
                                String patientEmail = service.getUserEmail(a.getPatientId());
                                String docName = service.getUserFullName(a.getDoctorId());
                                String dateStr = a.getAppointmentDate().toString();
                                String timeStr = a.getAppointmentTime().toString();
                                
                                EmailService.sendOnlineConfirmation(patientEmail, patientName, dateStr, timeStr, docName, meetLink);

                            }
                            catch (Exception ex) { showMessage("Erreur: " + ex.getMessage(), true); }
                        } else {
                            a.setStatus("CONFIRME");
                            try { 
                                service.update(a); 
                                loadList(); 
                                showMessage("Confirmé !", false); 
                                
                                // ENVOI EMAIL SI PRÉSENTIEL
                                boolean isPresentiel = a.getReason() == null || !a.getReason().contains("[En ligne]");
                                
                                if (isPresentiel) {
                                    String patientName = service.getUserFullName(a.getPatientId());
                                    String patientEmail = service.getUserEmail(a.getPatientId());
                                    String docName = service.getUserFullName(a.getDoctorId());
                                    String dateStr = a.getAppointmentDate().toString();
                                    String timeStr = a.getAppointmentTime().toString();
                                    String docAddress = service.getUserAddress(a.getDoctorId());
                                    
                                    EmailService.sendConfirmationEmail(patientEmail, patientName, dateStr, timeStr, docName, docAddress);
                                }
                            }
                            catch (Exception ex) { showMessage("Erreur: " + ex.getMessage(), true); }
                        }



                    });

                    Button btnRef = new Button("Refuser");
                    btnRef.getStyleClass().add("card-action-btn-danger");
                    btnRef.setOnAction(e -> {
                        a.setStatus("REFUSE");
                        try { service.update(a); loadList(); showMessage("Refusé.", false); }
                        catch (Exception ex) { showMessage("Erreur: " + ex.getMessage(), true); }
                    });
                    actions.getChildren().addAll(btnConf, btnRef);
                } else {
                    Button btnMod = new Button("Modifier");
                    btnMod.getStyleClass().add("card-action-btn");
                    btnMod.setOnAction(e -> {
                        showModifierPane();
                        loadAppointmentToUpdate(a.getAppointmentId());
                    });

                    Button btnDel = new Button("Supprimer");
                    btnDel.getStyleClass().add("card-action-btn-danger");
                    btnDel.setOnAction(e -> {
                        showSupprimerPane();
                        if (a.getAppointmentDate() != null) {
                            delDatePicker.setValue(a.getAppointmentDate().toLocalDate());
                            handleSearchByDate();
                        }
                    });
                    actions.getChildren().addAll(btnMod, btnDel);
                }

                // --- ASSEMBLE CARD ---
                card.getChildren().addAll(header, avatarContainer, info, actions);
                listContainer.getChildren().add(card);
            }

            statTotal.setText(String.valueOf(totalCount));
            statToday.setText(String.valueOf(confirmedCount));


        } catch (SQLException e) {
            showMessage("Erreur de chargement DB: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // ADD — écriture en DB
    // ==========================================
    @FXML
    public void handleAdd() {
        try {
            int docId = extractId(addDoctorName.getValue());
            int patId = extractId(addPatientId.getValue());
            if (patId == -1 || docId == -1) {
                showMessage("Patient et Médecin sont obligatoires.", true); return;
            }
            if (addDate.getValue() == null) { showMessage("Veuillez choisir une date.", true); return; }
            if (addTime.getText().isBlank())  { showMessage("Veuillez entrer une heure.", true); return; }
            if (addType.getValue() == null)   { showMessage("Veuillez choisir un type.", true); return; }
            if (addPayment.getValue() == null) { showMessage("Veuillez choisir un mode de paiement.", true); return; }

            String timeText = addTime.getText().trim();
            if (timeText.length() == 5) timeText += ":00";

            appointment a = new appointment();
            a.setPatientId(patId);
            a.setDoctorId(docId);
            a.setAppointmentDate(Date.valueOf(addDate.getValue()));
            a.setAppointmentTime(Time.valueOf(timeText));
            a.setType(addType.getValue());
            
            String modeStr = addMode.getValue() != null ? "[" + addMode.getValue() + "] " : "";
            a.setReason(modeStr + addReason.getText().trim());
            
            a.setPaymentMethodFromString(addPayment.getValue());
            a.setStatus("CONFIRME");

            service.add(a);
            showMessage("✓ Rendez-vous ajouté avec succès !", false);

            addPatientId.setValue(null); addTime.clear(); addReason.clear();
            addDate.setValue(null); addType.setValue(null); addPayment.setValue(null);
            addDoctorSpec.setValue(null); addDoctorName.getItems().clear();

            showListePane();

        } catch (NumberFormatException e) {
            showMessage("L'ID Patient doit être un nombre.", true);
        } catch (IllegalArgumentException e) {
            showMessage("Format d'heure invalide. Utilisez HH:mm", true);
        } catch (Exception e) {
            showMessage("Erreur d'ajout: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // UPDATE — modification en DB
    // ==========================================
    @FXML
    public void handleSearchByDateUpd() {
        if (updDatePicker.getValue() == null) return;
        try {
            Date d = Date.valueOf(updDatePicker.getValue());
            List<appointment> list = service.getAppointmentsByDate(d);
            
            updResultsContainer.getChildren().clear();
            updFormContainer.setVisible(false);
            
            if (list.isEmpty()) {
                showMessage("Aucun rendez-vous le " + d, true);
                return;
            }
            
            for (appointment a : list) {
                Button b = new Button(String.format("Modifier RDV #%d — %s — %s", 
                                      a.getAppointmentId(), a.getAppointmentTime(), service.getUserFullName(a.getPatientId())));
                b.getStyleClass().add("search-result-btn");
                b.setPrefWidth(600);
                b.setOnAction(e -> {
                    loadAppointmentToUpdate(a.getAppointmentId());
                });
                updResultsContainer.getChildren().add(b);
            }
            showMessage(list.size() + " rendez-vous trouvés pour modification.", false);
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    private void loadAppointmentToUpdate(int id) {
        try {
            appointment a = service.getAppointmentById(id);
            if (a != null) {
                currentUpdateId = id;
                // Select patient in ComboBox
                String patFullName = service.getUserFullName(a.getPatientId());
                updPatientId.setValue(patFullName + " (ID: " + a.getPatientId() + ")");
                
                // Select doctor in ComboBoxes
                boolean found = false;
                if (doctorMap != null) {
                    for (Map.Entry<String, List<String>> entry : doctorMap.entrySet()) {
                        for (String docInfo : entry.getValue()) {
                            if (extractId(docInfo) == a.getDoctorId()) {
                                updDoctorSpec.setValue(entry.getKey());
                                handleSpecChangeUpd();
                                updDoctorName.setValue(docInfo);
                                found = true; break;
                            }
                        }
                        if (found) break;
                    }
                }

                if (a.getAppointmentDate() != null) updDate.setValue(a.getAppointmentDate().toLocalDate());
                if (a.getAppointmentTime() != null) updTime.setText(a.getAppointmentTime().toString());
                updType.setValue(a.getType());
                updPayment.setValue(a.getPaymentMethodAsString());
                updReason.setText(a.getReason() != null ? a.getReason() : "");
                updFormContainer.setVisible(true);
                showMessage("Rendez-vous #" + id + " chargé.", false);
            } else {
                updFormContainer.setVisible(false);
                showMessage("Aucun rendez-vous avec l'ID " + id + ".", true);
            }
        } catch (Exception e) {
            showMessage("Erreur de chargement: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleUpdate() {
        if (currentUpdateId == -1) return;
        try {
            int docId = extractId(updDoctorName.getValue());
            int patId = extractId(updPatientId.getValue());
            if (docId == -1 || patId == -1) { showMessage("Veuillez sélectionner un médecin et un patient.", true); return; }

            String timeText = updTime.getText().trim();
            if (timeText.length() == 5) timeText += ":00";

            appointment original = service.getAppointmentById(currentUpdateId);
            String originalStatus = (original != null && original.getStatus() != null)
                    ? original.getStatus() : "EN_ATTENTE";

            appointment a = new appointment();
            a.setAppointmentId(currentUpdateId);
            a.setPatientId(patId);
            a.setDoctorId(docId);
            a.setAppointmentDate(Date.valueOf(updDate.getValue()));
            a.setAppointmentTime(Time.valueOf(timeText));
            a.setType(updType.getValue());
            
            String modeStr = updMode.getValue() != null ? "[" + updMode.getValue() + "] " : "";
            a.setReason(modeStr + updReason.getText().trim());
            
            a.setPaymentMethodFromString(updPayment.getValue());
            a.setStatus(originalStatus);

            service.update(a);
            showMessage("✓ Rendez-vous #" + currentUpdateId + " mis à jour !", false);
            showListePane();

        } catch (NumberFormatException e) {
            showMessage("Patient ID doit être un nombre.", true);
        } catch (IllegalArgumentException e) {
            showMessage("Heure invalide.", true);
        } catch (Exception e) {
            showMessage("Erreur de mise à jour: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // DELETE — suppression en DB
    // ==========================================
    @FXML
    public void handleSearchByDate() {
        if (delDatePicker.getValue() == null) return;
        try {
            Date d = Date.valueOf(delDatePicker.getValue());
            List<appointment> list = service.getAppointmentsByDate(d);
            
            delResultsContainer.getChildren().clear();
            delInfoContainer.setVisible(false);
            
            if (list.isEmpty()) {
                showMessage("Aucun rendez-vous le " + d, true);
                return;
            }
            
            for (appointment a : list) {
                Button b = new Button(String.format("RDV #%d — %s — %s", 
                                      a.getAppointmentId(), a.getAppointmentTime(), service.getUserFullName(a.getPatientId())));
                b.getStyleClass().add("btn-secondary");
                b.setPrefWidth(500);
                b.setOnAction(e -> {
                    currentDeleteId = a.getAppointmentId();
                    delDetailsLabel.setText(String.format(
                        "Patient: %s | Médecin: %s | Heure: %s | Type: %s",
                        service.getUserFullName(a.getPatientId()), service.getUserFullName(a.getDoctorId()), a.getAppointmentTime(), a.getType()));
                    delInfoContainer.setVisible(true);
                });
                delResultsContainer.getChildren().add(b);
            }
            showMessage(list.size() + " rendez-vous trouvés.", false);
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleDelete() {
        if (currentDeleteId == -1) return;
        try {
            appointment a = new appointment();
            a.setAppointmentId(currentDeleteId);
            service.delete(a);
            showMessage("✓ Rendez-vous #" + currentDeleteId + " supprimé !", false);
            currentDeleteId = -1;
            handleSearchByDate(); // Refresh the list for the current date
        } catch (Exception e) {
            showMessage("Erreur lors de la suppression: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // UTILS / NAVIGATION
    // ==========================================
    private String statusLabel(String status) {
        return switch (status) {
            case "CONFIRME" -> "✓ Confirmé";
            case "REFUSE"   -> "✖ Refusé";
            default         -> "⏳ En attente";
        };
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(isError ? "message-error" : "message-success");
    }

    // ==========================================
    // STATS LOGIC
    // ==========================================
    private void loadStats() {
        try {
            List<appointment> all = service.getAll();
            
            // 1. Pie Chart: Répartition par Type
            Map<String, Integer> typeCounts = new HashMap<>();
            for (appointment a : all) {
                String t = a.getType() != null ? a.getType() : "Autre";
                typeCounts.put(t, typeCounts.getOrDefault(t, 0) + 1);
            }
            
            typePieChart.getData().clear();
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                typePieChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }

            // 2. Doctor Performance
            doctorPerformanceVBox.getChildren().clear();
            Map<Integer, Integer> docAppts = new HashMap<>();
            for (appointment a : all) {
                docAppts.put(a.getDoctorId(), docAppts.getOrDefault(a.getDoctorId(), 0) + 1);
            }

            int maxAppts = docAppts.values().stream().max(Integer::compare).orElse(1);

            for (Map.Entry<Integer, Integer> entry : docAppts.entrySet()) {
                int docId = entry.getKey();
                int count = entry.getValue();
                double ratio = (double) count / maxAppts;
                String docName = service.getUserFullName(docId);

                VBox row = new VBox(5);
                HBox info = new HBox();
                Label lblName = new Label(docName);
                lblName.getStyleClass().add("stats-progress-label");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label lblVal = new Label(count + " RDV");
                lblVal.getStyleClass().add("stats-progress-value");
                info.getChildren().addAll(lblName, sp, lblVal);

                StackPane barBg = new StackPane();
                barBg.getStyleClass().add("custom-progress-bar");
                barBg.setAlignment(Pos.CENTER_LEFT);
                Region fill = new Region();
                fill.getStyleClass().add("custom-progress-bar-fill");
                double fillWidth = 400 * ratio;
                fill.setPrefWidth(fillWidth);
                fill.setMinWidth(fillWidth);
                fill.setMaxWidth(fillWidth);
                fill.setMaxHeight(8);
                barBg.getChildren().add(fill);


                row.getChildren().addAll(info, barBg);
                doctorPerformanceVBox.getChildren().add(row);
            }

            // 3. Insights
            availabilityServices dispoService = new availabilityServices();
            List<availability> allDispos = dispoService.getAll();
            double fillRate = allDispos.size() > 0 ? (double) all.size() / allDispos.size() : 0;
            insightFillRate.setText("Taux d'occupation global : " + String.format("%.1f", fillRate * 100) + "%. " + 
                                   (fillRate > 0.7 ? "Excellent travail !" : "Peut être amélioré."));

            Map<String, Integer> hourFreq = new HashMap<>();
            for (appointment a : all) {
                if (a.getAppointmentTime() != null) {
                    String h = a.getAppointmentTime().toString().substring(0, 2) + "h";
                    hourFreq.put(h, hourFreq.getOrDefault(h, 0) + 1);
                }
            }
            String bestH = hourFreq.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
            insightOptimalSlots.setText("L'heure de pointe est à " + bestH + ". Prévoyez plus de personnel.");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToDisponibilites(ActionEvent event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/DashboardAvailability.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
