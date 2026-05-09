package Controler;

import entities.user;
import entities.user.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.userServices;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PatientDoctorListController implements Initializable {

    @FXML private TextField searchNameField;
    @FXML private ComboBox<String> specialtyFilter;
    @FXML private ComboBox<String> locationFilter;
    @FXML private HBox paginationBox;
    @FXML private Label statusLabel;
    @FXML private FlowPane cardsContainer;
    @FXML private ComboBox<String> sortComboBox;

    private ObservableList<user> allDoctorList = FXCollections.observableArrayList();
    private ObservableList<user> filteredDoctorList = FXCollections.observableArrayList();
    private ObservableList<user> currentPageList = FXCollections.observableArrayList();
    private userServices userService = new userServices();

    private int itemsPerPage = 6;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specialtyFilter.setItems(FXCollections.observableArrayList(
                "Toutes les spécialités", "Généraliste", "Cardiologue", "Dentiste",
                "Ophtalmologue", "Pédiatre", "Dermatologue", "Neurologue", "Gynécologue"
        ));
        specialtyFilter.setValue("Toutes les spécialités");

        locationFilter.setItems(FXCollections.observableArrayList(
                "Toutes les villes", "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès",
                "Gafsa", "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef",
                "Mahdia", "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax",
                "Sidi Bouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));
        locationFilter.setValue("Toutes les villes");

        sortComboBox.setItems(FXCollections.observableArrayList("Ordre alphabétique", "A - Z", "Z - A"));
        sortComboBox.setValue("Ordre alphabétique");

        searchNameField.textProperty().addListener((obs, oldText, newText) -> {
            currentPage = 1;
            filterDoctors();
        });

        specialtyFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1;
                filterDoctors();
            }
        });

        locationFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1;
                filterDoctors();
            }
        });

        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterDoctors();
        });

        loadDoctors();
    }

    public void loadDoctors() {
        try {
            List<user> allUsers = userService.getAll();
            List<user> doctors = allUsers.stream()
                    .filter(u -> u.getRole() == Role.doctor && u.getAccountStatus() == user.AccountStatus.active)
                    .collect(Collectors.toList());
            allDoctorList.setAll(doctors);
            filterDoctors();
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Erreur de chargement");
        }
    }

    @FXML
    public void filterDoctors() {
        String nameStr = searchNameField.getText().toLowerCase();
        String specStr = specialtyFilter.getValue();
        String locStr = locationFilter.getValue();
        String sortOrder = sortComboBox.getValue();

        List<user> filtered = allDoctorList.stream()
                .filter(u -> {
                    boolean matchName = true;
                    if (!nameStr.isEmpty()) {
                        matchName = u.getFirstName().toLowerCase().contains(nameStr) ||
                                u.getLastName().toLowerCase().contains(nameStr);
                    }
                    boolean matchSpec = true;
                    if (specStr != null && !"Toutes les spécialités".equals(specStr)) {
                        matchSpec = (u.getSpecialty() != null && u.getSpecialty().trim().equalsIgnoreCase(specStr.trim()));
                    }
                    boolean matchLoc = true;
                    if (locStr != null && !"Toutes les villes".equals(locStr)) {
                        matchLoc = (u.getAddress() != null && u.getAddress().toLowerCase().contains(locStr.toLowerCase()));
                    }
                    return matchName && matchSpec && matchLoc;
                })
                .collect(Collectors.toList());

        // Alphabetical sorting
        if ("A - Z".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u1.getFirstName() + u1.getLastName()).compareToIgnoreCase(u2.getFirstName() + u2.getLastName()));
        } else if ("Z - A".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u2.getFirstName() + u2.getLastName()).compareToIgnoreCase(u1.getFirstName() + u1.getLastName()));
        }

        filteredDoctorList.setAll(filtered);
        updatePagination();
        displayCurrentPage();
        if (statusLabel != null) statusLabel.setText("Total: " + filteredDoctorList.size() + " docteurs");
    }

    @FXML
    public void resetFilters() {
        searchNameField.setText("");
        specialtyFilter.setValue("Toutes les spécialités");
        locationFilter.setValue("Toutes les villes");
        sortComboBox.setValue("Ordre alphabétique");
        currentPage = 1;
        filterDoctors();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredDoctorList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        createPaginationButtons();
    }

    private void createPaginationButtons() {
        if (paginationBox == null) return;
        paginationBox.getChildren().clear();

        Button prevBtn = new Button("◀");
        prevBtn.getStyleClass().add("pagination-button");
        prevBtn.setDisable(currentPage == 1);
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                displayCurrentPage();
                createPaginationButtons();
            }
        });
        paginationBox.getChildren().add(prevBtn);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        if (startPage > 1) {
            Button firstBtn = createPageButton(1);
            paginationBox.getChildren().add(firstBtn);
            if (startPage > 2) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #718096; -fx-padding: 2; -fx-font-size: 11;");
                paginationBox.getChildren().add(dots);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageBtn = createPageButton(i);
            paginationBox.getChildren().add(pageBtn);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #718096; -fx-padding: 2; -fx-font-size: 11;");
                paginationBox.getChildren().add(dots);
            }
            Button lastBtn = createPageButton(totalPages);
            paginationBox.getChildren().add(lastBtn);
        }

        Button nextBtn = new Button("▶");
        nextBtn.getStyleClass().add("pagination-button");
        nextBtn.setDisable(currentPage == totalPages);
        nextBtn.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                displayCurrentPage();
                createPaginationButtons();
            }
        });
        paginationBox.getChildren().add(nextBtn);
    }

    private Button createPageButton(int pageNum) {
        Button btn = new Button(String.valueOf(pageNum));
        btn.getStyleClass().add("pagination-button");
        if (pageNum == currentPage) {
            btn.getStyleClass().add("pagination-button-active");
        }
        btn.setOnAction(e -> {
            currentPage = pageNum;
            displayCurrentPage();
            createPaginationButtons();
        });
        return btn;
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredDoctorList.size());

        currentPageList.clear();
        if (start < filteredDoctorList.size()) {
            currentPageList.setAll(filteredDoctorList.subList(start, end));
        }

        displayCards(currentPageList);
    }

    private void displayCards(ObservableList<user> users) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucun docteur trouvé");
            emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (user u : users) {
            VBox card = createDoctorCard(u);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createDoctorCard(user doctor) {
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
        
        if (doctor.getUserPhoto() != null && !doctor.getUserPhoto().isEmpty()) {
            try {
                String cleanBase64 = doctor.getUserPhoto().trim().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                photo.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                photo.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            }
        } else {
            photo.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        }

        VBox nameBox = new VBox(2);
        Label name = new Label("Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2D3748;");
        Label specialty = new Label(doctor.getSpecialty() != null ? doctor.getSpecialty() : "Généraliste");
        specialty.setStyle("-fx-background-color: #48BB78; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10;");
        nameBox.getChildren().addAll(name, specialty);
        header.getChildren().addAll(photo, nameBox);

        VBox infoBox = new VBox(5);
        Label phone = new Label("📞 " + doctor.getPhone());
        phone.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label fee = new Label("💰 Consultation: " + doctor.getConsultationFee() + " DT");
        fee.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        infoBox.getChildren().addAll(phone, fee);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        Button feedbackBtn = new Button("⭐ Avis");
        feedbackBtn.setStyle("-fx-background-color: #EDF2F7; -fx-text-fill: #4A5568; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-size: 11;");
        HBox.setHgrow(feedbackBtn, Priority.ALWAYS);
        feedbackBtn.setMaxWidth(Double.MAX_VALUE);
        feedbackBtn.setOnAction(e -> Main.showPatientFeedbackForDoctor(doctor.getUserId(), "Dr. " + doctor.getFirstName() + " " + doctor.getLastName()));

        Button appointementBtn = new Button("📅 RDV");
        appointementBtn.setStyle("-fx-background-color: #FF5C5C; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        appointementBtn.setOnAction(e -> showAlert("Rendez-vous", "Fonctionnalité de prise de rendez-vous à venir"));
        
        actions.getChildren().addAll(feedbackBtn, appointementBtn);

        card.getChildren().addAll(header, new Separator(), infoBox, actions);
        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}