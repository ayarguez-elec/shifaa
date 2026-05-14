package Controler;

import entities.user;
import entities.user.Role;
import entities.user.AccountStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import services.userServices;
import services.EmailService;
import tests.Main;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UtilisateursListController implements Initializable {

    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private FlowPane cardsContainer;
    @FXML private HBox paginationBox;
    @FXML private ComboBox<String> sortComboBox;

    private ObservableList<user> allUsersList = FXCollections.observableArrayList();
    private ObservableList<user> filteredUsersList = FXCollections.observableArrayList();
    private ObservableList<user> currentPageList = FXCollections.observableArrayList();
    private userServices userService = new userServices();

    private int itemsPerPage = 8;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleFilter.setItems(FXCollections.observableArrayList("Tous", "Patient", "Docteur", "Pharmacien", "Assistant", "Admin"));
        roleFilter.setValue("Tous");

        statusFilter.setItems(FXCollections.observableArrayList("Tous", "Actif", "En attente", "Suspendu", "Bloqué"));
        statusFilter.setValue("Tous");

        sortComboBox.setItems(FXCollections.observableArrayList("Ordre alphabétique", "A - Z", "Z - A", "Plus récents"));
        sortComboBox.setValue("Ordre alphabétique");

        loadUsers();

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentPage = 1;
            filterUsers();
        });

        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1;
                filterUsers();
            }
        });

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1;
                filterUsers();
            }
        });

        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterUsers();
        });
    }

    private void loadUsers() {
        try {
            allUsersList.setAll(userService.getAll());
            filterUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de chargement");
        }
    }

    @FXML
    private void filterUsers() {
        String search = searchField.getText().toLowerCase();
        String role = roleFilter.getValue();
        String status = statusFilter.getValue();
        String sortOrder = sortComboBox.getValue();

        List<user> filtered = allUsersList.stream()
                .filter(u -> {
                    if (!search.isEmpty()) {
                        if (!u.getFirstName().toLowerCase().contains(search) &&
                                !u.getLastName().toLowerCase().contains(search) &&
                                !u.getEmail().toLowerCase().contains(search)) {
                            return false;
                        }
                    }
                    if (role != null && !role.equals("Tous")) {
                        String matchRole = u.getRole().name();
                        String expectedRole = "";
                        switch(role) {
                            case "Patient": expectedRole = "patient"; break;
                            case "Docteur": expectedRole = "doctor"; break;
                            case "Pharmacien": expectedRole = "pharmacist"; break;
                            case "Assistant": expectedRole = "assistant"; break;
                            case "Admin": expectedRole = "admin"; break;
                        }
                        if (!matchRole.equals(expectedRole)) return false;
                    }
                    if (status != null && !status.equals("Tous")) {
                        String matchStatus = u.getAccountStatus().name();
                        String expectedStatus = "";
                        switch(status) {
                            case "Actif": expectedStatus = "active"; break;
                            case "En attente": expectedStatus = "pending"; break;
                            case "Suspendu": expectedStatus = "suspended"; break;
                            case "Bloqué": expectedStatus = "blocked"; break;
                        }
                        if (!matchStatus.equals(expectedStatus)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Alphabetical sorting
        if ("A - Z".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u1.getFirstName() + u1.getLastName()).compareToIgnoreCase(u2.getFirstName() + u2.getLastName()));
        } else if ("Z - A".equals(sortOrder)) {
            filtered.sort((u1, u2) -> (u2.getFirstName() + u2.getLastName()).compareToIgnoreCase(u1.getFirstName() + u1.getLastName()));
        } else if ("Plus récents".equals(sortOrder)) {
            filtered.sort((u1, u2) -> {
                if (u1.getRegistrationDate() == null) return 1;
                if (u2.getRegistrationDate() == null) return -1;
                return u2.getRegistrationDate().compareTo(u1.getRegistrationDate());
            });
        }

        filteredUsersList.setAll(filtered);
        updatePagination();
        displayCurrentPage();
        statusLabel.setText("Total: " + filteredUsersList.size() + " utilisateurs");
    }

    @FXML
    private void resetFilters() {
        roleFilter.setValue("Tous");
        statusFilter.setValue("Tous");
        sortComboBox.setValue("Ordre alphabétique");
        searchField.clear();
        currentPage = 1;
        filterUsers();
    }

    /** Called from Main.showAdminPendingUsers() to pre-filter to pending accounts **/
    public void filterToPending() {
        statusFilter.setValue("En attente");
        sortComboBox.setValue("Plus récents");
        currentPage = 1;
        filterUsers();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredUsersList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        createPaginationButtons();
    }

    private void createPaginationButtons() {
        paginationBox.getChildren().clear();

        // Bouton Précédent
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

        // Numéros de page (max 5 affichés)
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

        // Bouton Suivant
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
        int end = Math.min(start + itemsPerPage, filteredUsersList.size());

        currentPageList.clear();
        if (start < filteredUsersList.size()) {
            currentPageList.setAll(filteredUsersList.subList(start, end));
        }

        displayCards(currentPageList);
    }

    private void displayCards(ObservableList<user> users) {
        cardsContainer.getChildren().clear();

        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucun utilisateur trouvé");
            emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (user u : users) {
            cardsContainer.getChildren().add(createUserCard(u));
        }
    }

    private VBox createUserCard(user u) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView photo = new ImageView();
        photo.setFitHeight(55);
        photo.setFitWidth(55);
        Circle clip = new Circle(27.5, 27.5, 27.5);
        photo.setClip(clip);
        loadUserPhoto(photo, u);

        VBox nameBox = new VBox(2);
        Label name = new Label(u.getFirstName() + " " + u.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2D3748;");
        Label roleLabel = new Label(getRoleText(u.getRole()));
        roleLabel.setStyle("-fx-background-color: " + getRoleColor(u.getRole()) + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10;");
        nameBox.getChildren().addAll(name, roleLabel);
        header.getChildren().addAll(photo, nameBox);

        VBox infoBox = new VBox(5);
        Label email = new Label("📧 " + u.getEmail());
        email.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label status = new Label(getStatusText(u.getAccountStatus()));
        status.setStyle("-fx-background-color: " + getStatusColor(u.getAccountStatus()) + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10;");
        infoBox.getChildren().addAll(email, status);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        if (u.getAccountStatus() == AccountStatus.pending) {
            // --- BOUTONS DE VALIDATION ---
            Button approveBtn = new Button("Accepter");
            approveBtn.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-weight: bold;");
            approveBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(approveBtn, Priority.ALWAYS);
            approveBtn.setOnAction(e -> approveUser(u));

            Button rejectBtn = new Button("Refuser");
            rejectBtn.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-weight: bold;");
            rejectBtn.setOnAction(e -> deleteUser(u)); // Refuser = Supprimer le compte

            HBox pendingActions = new HBox(10);
            pendingActions.setAlignment(Pos.CENTER);
            pendingActions.getChildren().addAll(approveBtn, rejectBtn);

            VBox pendingContainer = new VBox(10);
            pendingContainer.setAlignment(Pos.CENTER);
            
            if (u.getProfessionalDocument() != null && !u.getProfessionalDocument().isEmpty()) {
                Button viewDocBtn = new Button("👁 Voir Document (PDF)");
                viewDocBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand; -fx-font-size: 11;");
                viewDocBtn.setMaxWidth(Double.MAX_VALUE);
                viewDocBtn.setOnAction(e -> viewDocument(u));
                pendingContainer.getChildren().add(viewDocBtn);
            }
            
            pendingContainer.getChildren().add(pendingActions);
            actions.getChildren().add(pendingContainer);
        } else {
            // --- BOUTONS STANDARDS (MODIFIER/SUPPRIMER) ---
            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-size: 11;");
            editBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(editBtn, Priority.ALWAYS);
            editBtn.setOnAction(e -> Main.showAdminEditUser(u));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: #FF5C5C; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-size: 11;");
            deleteBtn.setOnAction(e -> deleteUser(u));
            
            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(header, new Separator(), infoBox, actions);
        return card;
    }

    private void loadUserPhoto(ImageView imageView, user u) {
        if (u.getUserPhoto() != null && !u.getUserPhoto().isEmpty()) {
            try {
                String cleanBase64 = u.getUserPhoto().trim()
                        .replaceAll("\\s+", "")
                        .replaceAll("\\n", "")
                        .replaceAll("\\r", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imageView.setImage(image);
            } catch (Exception e) {
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/default-avatar.png"));
            if (defaultImage != null) {
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {}
    }

    private void deleteUser(user u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment refuser/supprimer " + u.getFirstName() + " " + u.getLastName() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.delete(u);
                loadUsers();
                refreshAdminBadge();
                showAlert("Succès", "Demande refusée et compte supprimé");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'opération");
            }
        }
    }

    private void viewDocument(user u) {
        if (u.getProfessionalDocument() == null || u.getProfessionalDocument().isEmpty()) return;

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(u.getProfessionalDocument());
            java.io.File tempFile = java.io.File.createTempFile("doc_" + u.getFirstName() + "_", ".pdf");
            java.nio.file.Files.write(tempFile.toPath(), pdfBytes);
            
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("Erreur", "L'ouverture de fichiers n'est pas supportée sur ce système");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le document PDF");
        }
    }

    private void approveUser(user u) {
        try {
            u.setAccountStatus(AccountStatus.active);
            userService.update(u);
            loadUsers();
            refreshAdminBadge();
            showAlert("Succès", "Compte approuvé ! L'utilisateur peut maintenant se connecter.");
            // Envoyer l'email de confirmation en arrière-plan
            new Thread(() -> {
                EmailService.sendAccountApprovedEmail(u.getEmail(), u.getFirstName());
            }).start();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'approuver le compte");
        }
    }

    @FXML
    private void goToAddUser() {
        Main.showAdminAddUser();
    }

    private void refreshAdminBadge() {
        // Rafraîchit le badge dans la sidebar admin
        javafx.scene.Scene scene = cardsContainer.getScene();
        if (scene != null) {
            javafx.scene.Node root = scene.getRoot();
            if (root != null) {
                javafx.scene.control.Button btn = (javafx.scene.control.Button) root.lookup("#pendingBadgeBtn");
                if (btn != null) {
                    try {
                        long pendingCount = userService.getAll().stream()
                                .filter(u -> u.getAccountStatus() == AccountStatus.pending)
                                .count();
                        if (pendingCount > 0) {
                            btn.setText("⏳ En attente (" + pendingCount + ")");
                            btn.setStyle("-fx-text-fill: white; -fx-background-color: #ED8936; -fx-font-weight: bold;");
                        } else {
                            btn.setText("⏳ En attente (0)");
                            btn.setStyle("-fx-text-fill: #718096; -fx-font-weight: normal;");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getRoleText(Role role) {
        switch (role) {
            case patient: return "Patient";
            case doctor: return "Docteur";
            case pharmacist: return "Pharmacien";
            case assistant: return "Assistant";
            case admin: return "Admin";
            default: return "Utilisateur";
        }
    }

    private String getRoleColor(Role role) {
        switch (role) {
            case patient: return "#4299E1";
            case doctor: return "#48BB78";
            case pharmacist: return "#ED8936";
            case assistant: return "#9F7AEA";
            case admin: return "#FF5C5C";
            default: return "#718096";
        }
    }

    private String getStatusText(AccountStatus status) {
        switch (status) {
            case active: return "✓ Actif";
            case pending: return "⏳ En attente";
            case suspended: return "⛔ Suspendu";
            case blocked: return "🚫 Bloqué";
            default: return "❓ Inconnu";
        }
    }

    private String getStatusColor(AccountStatus status) {
        switch (status) {
            case active: return "#48BB78";
            case pending: return "#ED8936";
            case suspended: return "#ECC94B";
            case blocked: return "#F56565";
            default: return "#718096";
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
