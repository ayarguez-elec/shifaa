package controler;

import entities.availability;
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

import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.availabilityServices;
import services.appointmentServices;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import entities.appointment;
import java.util.HashMap;

import java.io.IOException;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardAvailability {

    @FXML private Button tabListe;
    @FXML private Button tabAjouter;

    // ==== PANES ====
    @FXML private VBox paneListe;
    @FXML private VBox paneAjouter;
    @FXML private VBox paneModifier;
    @FXML private VBox paneSupprimer;

    // ==== SIDEBAR ====
    @FXML private Button btnNavRdv;
    @FXML private Button btnNavDispo;

    // ==== GLOBAL ====
    @FXML private Label messageLabel;
    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;
    private final availabilityServices service = new availabilityServices();
    private final appointmentServices appService = new appointmentServices();

    private Map<String, List<String>> doctorMap;

    // ==== PANE LISTE ====
    @FXML private Label statTotal;
    @FXML private Label statFree;
    @FXML private FlowPane listContainer;

    // ==== PANE AJOUTER ====
    @FXML private ComboBox<String> addDoctorSpec;
    @FXML private ComboBox<String> addDoctorName;
    @FXML private DatePicker addDate;
    @FXML private TextField addTime;
    @FXML private ComboBox<String> addStatus;

    // ==== PANE MODIFIER ====
    @FXML private DatePicker updDatePicker;
    @FXML private VBox updResultsContainer;
    @FXML private VBox updFormContainer;
    @FXML private ComboBox<String> updDoctorSpec;
    @FXML private ComboBox<String> updDoctorName;
    @FXML private DatePicker updDate;
    @FXML private TextField updTime;
    @FXML private ComboBox<String> updStatus;
    private int currentUpdateId = -1;

    // ==== PANE SUPPRIMER ====
    @FXML private DatePicker delDatePicker;
    @FXML private VBox delResultsContainer;
    @FXML private VBox delInfoContainer;
    @FXML private Label delDetailsLabel;

    private int currentDeleteId = -1;


    @FXML
    public void initialize() {
        String[] statuses = {"libre", "occupé"};
        addStatus.getItems().addAll(statuses);
        updStatus.getItems().addAll(statuses);

        loadDoctors();
        loadAssistantProfile();
        showListePane();
    }

    private void loadAssistantProfile() {
        String[] info = appService.getAssistantInfo();
        if (info != null) {
            sidebarUserName.setText(info[0]);
            sidebarUserRole.setText(info[1]);
        }
    }


    private void loadDoctors() {
        doctorMap = appService.getDoctorsBySpecialty();
        addDoctorSpec.getItems().clear();
        updDoctorSpec.getItems().clear();
        addDoctorSpec.getItems().addAll(doctorMap.keySet());
        updDoctorSpec.getItems().addAll(doctorMap.keySet());
    }

    @FXML
    private void handleSpecChangeAdd() {
        String spec = addDoctorSpec.getValue();
        addDoctorName.getItems().clear();
        if (spec != null && doctorMap.containsKey(spec)) {
            addDoctorName.getItems().addAll(doctorMap.get(spec));
        }
    }

    @FXML
    private void handleSpecChangeUpd() {
        String spec = updDoctorSpec.getValue();
        updDoctorName.getItems().clear();
        if (spec != null && doctorMap.containsKey(spec)) {
            updDoctorName.getItems().addAll(doctorMap.get(spec));
        }
    }

    private int extractId(String doctorInfo) {
        if (doctorInfo == null || !doctorInfo.contains("ID: ")) return -1;
        try {
            return Integer.parseInt(doctorInfo.split("ID: ")[1].replace(")", ""));
        } catch (Exception e) { return -1; }
    }

    // ==========================================
    // TAB SWITCHING LOGIC
    // ==========================================
    private void resetTabs() {
        tabListe.getStyleClass().remove("nav-tab-active");
        tabListe.getStyleClass().add("nav-tab");
        paneListe.setVisible(false);
        paneAjouter.setVisible(false);
        paneModifier.setVisible(false);
        paneSupprimer.setVisible(false);
        messageLabel.setVisible(false);
    }

    private void activate(Button tab, VBox pane) {
        if (tab != null) {
            tab.getStyleClass().remove("nav-tab");
            if (tab == tabAjouter) {
                // Keep highlight style
            } else {
                tab.getStyleClass().add("nav-tab-active");
            }
        }
        pane.setVisible(true);
    }

    @FXML public void showListePane()    { resetTabs(); activate(tabListe,     paneListe); loadList(); }
    @FXML public void showAjouterPane()  { resetTabs(); activate(tabAjouter,   paneAjouter); }
    @FXML public void showModifierPane() { resetTabs(); activate(null,  paneModifier); updFormContainer.setVisible(false); }
    @FXML public void showSupprimerPane() { resetTabs(); activate(null, paneSupprimer); delInfoContainer.setVisible(false); }

    // ==========================================
    // LIST LOGIC
    // ==========================================
    private void loadList() {
        try {
            List<availability> list = service.getAll();
            listContainer.getChildren().clear();

            int freeCount = 0;
            for (availability a : list) {
                boolean isFree = "libre".equalsIgnoreCase(a.getStatusRDVAsString());
                if (isFree) freeCount++;

                // --- CARD MAIN CONTAINER ---
                VBox card = new VBox();
                card.getStyleClass().add("appointment-card"); // Reuse same style for consistency

                // --- HEADER: BADGES ---
                HBox header = new HBox();
                header.getStyleClass().add("appointment-card-header");
                
                Label idBadge = new Label("#" + a.getAvailabilityId());
                idBadge.getStyleClass().add("badge-blue");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                
                Label statusBadge = new Label(a.getStatusRDVAsString().toUpperCase());
                statusBadge.getStyleClass().add(isFree ? "badge-green" : "badge-red");
                
                header.getChildren().addAll(idBadge, spacer, statusBadge);

                // --- AVATAR SECTION ---
                VBox avatarContainer = new VBox();
                avatarContainer.getStyleClass().add("appointment-card-avatar-container");
                
                ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/avatar_doctor.png")));
                avatar.setFitWidth(70);
                avatar.setFitHeight(70);
                avatar.getStyleClass().add("appointment-card-avatar");
                
                Circle clip = new Circle(35, 35, 35);
                avatar.setClip(clip);
                
                avatarContainer.getChildren().add(avatar);

                // --- INFO SECTION ---
                VBox info = new VBox();
                info.getStyleClass().add("appointment-card-info");
                
                String docName = appService.getUserFullName(a.getDoctorId());
                Label nameLabel = new Label("Dr. " + docName);
                nameLabel.getStyleClass().add("appointment-card-patient-name");
                
                Label dateLabel = new Label("📅 " + a.getDate().toString());
                dateLabel.getStyleClass().add("appointment-card-date");
                
                Label timeLabel = new Label("🕒 " + a.getTime().toString());
                timeLabel.getStyleClass().add("appointment-card-detail");
                
                info.getChildren().addAll(nameLabel, dateLabel, timeLabel);

                // --- ACTIONS SECTION ---
                HBox actions = new HBox();
                actions.getStyleClass().add("appointment-card-actions");

                Button btnEdit = new Button("Modifier");
                btnEdit.getStyleClass().add("card-action-btn");
                btnEdit.setOnAction(e -> {
                    showModifierPane();
                    loadAvailabilityToUpdate(a.getAvailabilityId());
                });

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("card-action-btn-danger");
                btnDel.setOnAction(e -> {
                    showSupprimerPane();
                    loadAvailabilityToDelete(a.getAvailabilityId());
                });

                actions.getChildren().addAll(btnEdit, btnDel);

                // --- ASSEMBLE CARD ---
                card.getChildren().addAll(header, avatarContainer, info, actions);
                listContainer.getChildren().add(card);
            }

            statTotal.setText(String.valueOf(list.size()));
            statFree.setText(String.valueOf(freeCount));

        } catch (SQLException e) {
            showMessage("Erreur chargement: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // ADD LOGIC
    // ==========================================
    @FXML
    public void handleAdd() {
        try {
            int docId = extractId(addDoctorName.getValue());
            if (docId == -1) { showMessage("Veuillez sélectionner un médecin.", true); return; }
            if (addDate.getValue() == null) { showMessage("Date obligatoire.", true); return; }
            if (addTime.getText().isBlank()) { showMessage("Heure obligatoire.", true); return; }
            if (addStatus.getValue() == null) { showMessage("Statut obligatoire.", true); return; }

            availability a = new availability();
            a.setDoctorId(docId);
            a.setDate(Date.valueOf(addDate.getValue()));
            a.setTime(Time.valueOf(addTime.getText().trim()));
            a.setStatusRDVFromString(addStatus.getValue());

            service.add(a);
            showMessage("Disponibilité ajoutée avec succès !", false);
            showListePane();
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // UPDATE LOGIC (Search by Date)
    // ==========================================
    @FXML
    public void handleSearchByDateUpd() {
        if (updDatePicker.getValue() == null) return;
        try {
            Date d = Date.valueOf(updDatePicker.getValue());
            List<availability> list = service.getAll();
            updResultsContainer.getChildren().clear();
            updFormContainer.setVisible(false);

            int count = 0;
            for (availability a : list) {
                if (a.getDate().toString().equals(d.toString())) {
                    count++;
                    Button b = new Button(String.format("Dispo #%d - Dr. ID %d à %s", 
                                          a.getAvailabilityId(), a.getDoctorId(), a.getTime()));
                    b.getStyleClass().add("search-result-btn");
                    b.setPrefWidth(600);
                    b.setOnAction(e -> loadAvailabilityToUpdate(a.getAvailabilityId()));
                    updResultsContainer.getChildren().add(b);
                }
            }
            if (count == 0) showMessage("Aucun créneau trouvé pour le " + d, true);
            else showMessage(count + " créneaux trouvés.", false);
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    private void loadAvailabilityToUpdate(int id) {
        try {
            availability a = service.getAvailabilityById(id);
            if (a != null) {
                currentUpdateId = id;
                
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
                
                updDate.setValue(a.getDate().toLocalDate());
                updTime.setText(a.getTime().toString());
                updStatus.setValue(a.getStatusRDVAsString());
                updFormContainer.setVisible(true);
            }
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleUpdate() {
        if (currentUpdateId == -1) return;
        try {
            int docId = extractId(updDoctorName.getValue());
            if (docId == -1) { showMessage("Médecin invalide.", true); return; }

            availability a = new availability();
            a.setAvailabilityId(currentUpdateId);
            a.setDoctorId(docId);
            a.setDate(Date.valueOf(updDate.getValue()));
            a.setTime(Time.valueOf(updTime.getText().trim()));
            a.setStatusRDVFromString(updStatus.getValue());

            service.update(a);
            showMessage("Mise à jour réussie !", false);
            showListePane();
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // DELETE LOGIC (Search by Date)
    // ==========================================
    @FXML
    public void handleSearchByDateDel() {
        if (delDatePicker.getValue() == null) return;
        try {
            Date d = Date.valueOf(delDatePicker.getValue());
            List<availability> list = service.getAll();
            delResultsContainer.getChildren().clear();
            delInfoContainer.setVisible(false);

            int count = 0;
            for (availability a : list) {
                if (a.getDate().toString().equals(d.toString())) {
                    count++;
                    Button b = new Button(String.format("Supprimer #%d - Dr. ID %d à %s", 
                                          a.getAvailabilityId(), a.getDoctorId(), a.getTime()));
                    b.getStyleClass().add("search-result-btn-red");
                    b.setPrefWidth(600);
                    b.setOnAction(e -> loadAvailabilityToDelete(a.getAvailabilityId()));
                    delResultsContainer.getChildren().add(b);
                }
            }
            if (count == 0) showMessage("Aucun créneau à supprimer pour le " + d, true);
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    private void loadAvailabilityToDelete(int id) {
        try {
            availability a = service.getAvailabilityById(id);
            if (a != null) {
                currentDeleteId = id;
                delDetailsLabel.setText(String.format("Supprimer le créneau #%d du Dr. ID %d\nLe %s à %s ?", 
                                        id, a.getDoctorId(), a.getDate(), a.getTime()));
                delInfoContainer.setVisible(true);
            }
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleDelete() {
        if (currentDeleteId == -1) return;
        try {
            availability a = new availability();
            a.setAvailabilityId(currentDeleteId);
            service.delete(a);
            showMessage("Suppression effectuée.", false);
            showListePane();
        } catch (Exception e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(isError ? "message-error" : "message-success");
    }

    @FXML
    public void goToAppointments(ActionEvent event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/DashboardAppointment.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
