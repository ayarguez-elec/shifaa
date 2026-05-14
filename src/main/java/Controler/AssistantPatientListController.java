package Controler;

import entities.user;
import entities.user.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import services.userServices;
import tests.Main;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AssistantPatientListController {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;
    @FXML private Label statusLabel;
    @FXML private HBox paginationBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> sortComboBox;

    private userServices userService = new userServices();
    private ObservableList<user> allPatientsList = FXCollections.observableArrayList();
    private ObservableList<user> filteredPatientsList = FXCollections.observableArrayList();

    private int itemsPerPage = 8;
    private int currentPage = 1;
    private int totalPages = 1;
    private int assistantDoctorId = 0;

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        sortComboBox.setItems(FXCollections.observableArrayList("Ordre alphabétique", "A - Z", "Z - A"));
        sortComboBox.setValue("Ordre alphabétique");

        // Listeners for real-time filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterPatients(); });
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> { filterPatients(); });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterPatients(); });

        loadAssistantInfo();
        loadPatients();
    }

    private void loadAssistantInfo() {
        try {
            int currentUserId = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == currentUserId) {
                    this.assistantDoctorId = u.getDoctorId();
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPatients() {
        try {
            allPatientsList.clear();
            List<user> users = userService.getAll();
            for (user u : users) {
                // Show patients linked to the same doctor as the assistant
                if (u.getRole() == Role.patient && (u.getDoctorId() == assistantDoctorId || assistantDoctorId == 0)) {
                    allPatientsList.add(u);
                }
            }
            filterPatients();
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Erreur de chargement");
        }
    }

    @FXML
    private void filterPatients() {
        String query = searchField.getText().toLowerCase();
        String sortOrder = sortComboBox.getValue();

        List<user> filtered = allPatientsList.stream()
            .filter(u -> (u.getFirstName().toLowerCase().contains(query) || u.getLastName().toLowerCase().contains(query)))
            .collect(Collectors.toList());

        // Alphabetical sorting
        if ("A - Z".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u1.getFirstName() + u1.getLastName()).compareToIgnoreCase(u2.getFirstName() + u2.getLastName()));
        } else if ("Z - A".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u2.getFirstName() + u2.getLastName()).compareToIgnoreCase(u1.getFirstName() + u1.getLastName()));
        }

        filteredPatientsList.setAll(filtered);
        updatePagination();
        displayCurrentPage();
        if (statusLabel != null) statusLabel.setText("Total patients: " + filteredPatientsList.size());
    }

    @FXML
    private void resetFilters() {
        searchField.setText("");
        sortComboBox.setValue("Ordre alphabétique");
        datePicker.setValue(null);
        currentPage = 1;
        filterPatients();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredPatientsList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        paginationBox.getChildren().clear();

        Button prevBtn = new Button("◀");
        prevBtn.setDisable(currentPage == 1);
        prevBtn.getStyleClass().add("pagination-button");
        prevBtn.setOnAction(e -> { currentPage--; displayCurrentPage(); });
        paginationBox.getChildren().add(prevBtn);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("pagination-button");
            if (i == currentPage) {
                pageBtn.getStyleClass().add("pagination-button-active");
            }
            int pageNum = i;
            pageBtn.setOnAction(e -> { currentPage = pageNum; displayCurrentPage(); });
            paginationBox.getChildren().add(pageBtn);
        }

        Button nextBtn = new Button("▶");
        nextBtn.setDisable(currentPage == totalPages);
        nextBtn.getStyleClass().add("pagination-button");
        nextBtn.setOnAction(e -> { currentPage++; displayCurrentPage(); });
        paginationBox.getChildren().add(nextBtn);
    }

    private void displayCurrentPage() {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        if (filteredPatientsList.isEmpty()) {
            Label emptyLabel = new Label("Aucun patient trouvé.");
            emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredPatientsList.size());

        for (int i = start; i < end; i++) {
            cardsContainer.getChildren().add(createPatientCard(filteredPatientsList.get(i)));
        }
        updatePagination();
    }

    private VBox createPatientCard(user patient) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView photo = new ImageView();
        photo.setFitHeight(55);
        photo.setFitWidth(55);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(27.5, 27.5, 27.5);
        photo.setClip(clip);

        String b64 = patient.getUserPhoto();
        if (b64 != null && !b64.trim().isEmpty()) {
            try {
                String clean = b64.trim().replaceAll("\\s+", "");
                byte[] bytes = java.util.Base64.getDecoder().decode(clean);
                photo.setImage(new javafx.scene.image.Image(new java.io.ByteArrayInputStream(bytes)));
            } catch (Exception ex) {
                photo.setImage(new javafx.scene.image.Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            }
        } else {
            photo.setImage(new javafx.scene.image.Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        }

        VBox nameBox = new VBox(2);
        Label name = new Label(patient.getFirstName() + " " + patient.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2D3748;");
        
        Label statusTag = new Label("Patient");
        statusTag.setStyle("-fx-background-color: #9F7AEA; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10;");
        
        nameBox.getChildren().addAll(name, statusTag);
        header.getChildren().addAll(photo, nameBox);

        VBox infoBox = new VBox(5);
        Label email = new Label("📧 " + patient.getEmail());
        email.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label phone = new Label("📞 " + patient.getPhone());
        phone.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label lastVisit = new Label("📅 Dernière visite: 2026-05-01");
        lastVisit.setStyle("-fx-font-size: 11; -fx-text-fill: #A0AEC0;");
        
        infoBox.getChildren().addAll(email, phone, lastVisit);

        Button btnDossier = new Button("📂 Voir Dossier");
        btnDossier.setStyle("-fx-background-color: #EDF2F7; -fx-text-fill: #4A5568; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        btnDossier.setMaxWidth(Double.MAX_VALUE);
        btnDossier.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dossier Médical");
            alert.setHeaderText("Dossier de " + patient.getFirstName());
            alert.setContentText("Consultation du dossier médical à venir.");
            alert.showAndWait();
        });

        card.getChildren().addAll(header, new Separator(), infoBox, btnDossier);
        return card;
    }
}
