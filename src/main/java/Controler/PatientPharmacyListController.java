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

public class PatientPharmacyListController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> locationFilter;
    @FXML private Label statusLabel;
    @FXML private FlowPane cardsContainer;

    @FXML private HBox paginationBox;

    private ObservableList<user> allPharmacyList = FXCollections.observableArrayList();
    private ObservableList<user> filteredPharmacyList = FXCollections.observableArrayList();
    private ObservableList<user> currentPageList = FXCollections.observableArrayList();
    private userServices userService = new userServices();

    private int itemsPerPage = 8;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        locationFilter.setItems(FXCollections.observableArrayList(
                "Toutes les villes", "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès",
                "Gafsa", "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef",
                "Mahdia", "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax",
                "Sidi Bouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));
        locationFilter.setValue("Toutes les villes");

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentPage = 1;
            filterPharmacies();
        });

        locationFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            filterPharmacies();
        });
    }

    public void loadPharmacies() {
        try {
            List<user> allUsers = userService.getAll();
            List<user> pharmacies = allUsers.stream()
                    .filter(u -> u.getRole() == Role.pharmacist && u.getAccountStatus() == user.AccountStatus.active)
                    .collect(Collectors.toList());
            allPharmacyList.setAll(pharmacies);
            filterPharmacies();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de chargement");
        }
    }

    private void filterPharmacies() {
        String searchText = searchField.getText().toLowerCase();
        String locText = locationFilter.getValue();

        List<user> filtered = allPharmacyList.stream()
            .filter(u -> {
                boolean matchSearch = true;
                if (!searchText.isEmpty()) {
                    matchSearch = (u.getPharmacyName() != null && u.getPharmacyName().toLowerCase().contains(searchText)) ||
                                 u.getFirstName().toLowerCase().contains(searchText) ||
                                 u.getLastName().toLowerCase().contains(searchText);
                }
                
                boolean matchLoc = true;
                if (!"Toutes les villes".equals(locText)) {
                    matchLoc = (u.getAddress() != null && u.getAddress().toLowerCase().contains(locText.toLowerCase()));
                }
                
                return matchSearch && matchLoc;
            })
            .collect(Collectors.toList());
        
        filteredPharmacyList.setAll(filtered);
        updatePagination();
        displayCurrentPage();
        statusLabel.setText("Total: " + filteredPharmacyList.size() + " pharmacies");
    }

    @FXML
    public void filterPharmaciesBtn() {
        currentPage = 1;
        filterPharmacies();
    }

    @FXML
    public void resetFilters() {
        searchField.setText("");
        locationFilter.setValue("Toutes les villes");
        currentPage = 1;
        filterPharmacies();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredPharmacyList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        createPaginationButtons();
    }

    private void createPaginationButtons() {
        if (paginationBox == null) return;
        paginationBox.getChildren().clear();

        Button prevBtn = new Button("◀");
        prevBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11;");
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
        nextBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11;");
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
        String style = (pageNum == currentPage)
                ? "-fx-background-color: #FF5C5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11;"
                : "-fx-background-color: #E2E8F0; -fx-text-fill: #4A5568; -fx-background-radius: 4; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11;";
        btn.setStyle(style);
        btn.setOnAction(e -> {
            currentPage = pageNum;
            displayCurrentPage();
            createPaginationButtons();
        });
        return btn;
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredPharmacyList.size());

        currentPageList.clear();
        if (start < filteredPharmacyList.size()) {
            currentPageList.setAll(filteredPharmacyList.subList(start, end));
        }

        displayCards(currentPageList);
    }

    private void displayCards(ObservableList<user> users) {
        cardsContainer.getChildren().clear();

        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucune pharmacie trouvée");
            emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 16;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (user u : users) {
            VBox card = createPharmacyCard(u);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createPharmacyCard(user pharmacy) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        ImageView profileView = new ImageView();
        profileView.setFitHeight(55);
        profileView.setFitWidth(55);
        
        // Clip circulaire
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(27.5, 27.5, 27.5);
        profileView.setClip(clip);

        if (pharmacy.getUserPhoto() != null && !pharmacy.getUserPhoto().isEmpty()) {
            try {
                String cleanBase64 = pharmacy.getUserPhoto().trim().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                profileView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                profileView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            }
        } else {
            profileView.setImage(new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
        }

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(pharmacy.getPharmacyName() != null ? pharmacy.getPharmacyName() : pharmacy.getFirstName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        Label typeLabel = new Label("Pharmacie");
        typeLabel.setStyle("-fx-background-color: #ED8936; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11;");

        nameBox.getChildren().addAll(nameLabel, typeLabel);
        headerBox.getChildren().addAll(profileView, nameBox);

        VBox infoBox = new VBox(5);
        Label emailLabel = new Label("📧 " + pharmacy.getEmail());
        emailLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12;");
        Label phoneLabel = new Label("📞 " + pharmacy.getPhone());
        phoneLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12;");
        
        infoBox.getChildren().addAll(emailLabel, phoneLabel);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button appointementBtn = new Button("📅 Contacter");
        appointementBtn.setStyle("-fx-background-color: #FF5C5C; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 12;");
        appointementBtn.setOnAction(e -> showAlert("Contact", "Fonctionnalité de contact à venir"));

        buttonsBox.getChildren().addAll(appointementBtn);

        card.getChildren().addAll(headerBox, new Separator(), infoBox, buttonsBox);
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
