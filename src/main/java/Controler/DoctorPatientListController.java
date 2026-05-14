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
import services.medicalFileServ;
import entities.medicalFile;
import tests.Main;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import javafx.scene.control.TextField;

public class DoctorPatientListController {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;
    @FXML private Label statusLabel;
    @FXML private HBox paginationBox;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> sortComboBox;

    private userServices userService = new userServices();
    private ObservableList<user> allPatientsList = FXCollections.observableArrayList();
    private ObservableList<user> filteredPatientsList = FXCollections.observableArrayList();

    private int itemsPerPage = 8;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        statusFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "En attente", "Suspendu"));
        statusFilter.setValue("Tous les statuts");

        sortComboBox.setItems(FXCollections.observableArrayList("Ordre alphabétique", "A - Z", "Z - A"));
        sortComboBox.setValue("Ordre alphabétique");

        // Listeners for real-time filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterPatients(); });
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterPatients(); });
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> { filterPatients(); });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterPatients(); });

        loadPatients();
    }

    public void loadPatients() {
        try {
            allPatientsList.clear();
            medicalFileServ medicalFileService = new medicalFileServ();
            List<medicalFile> doctorFiles = medicalFileService.getByDoctorId(Main.currentUserId);
            java.util.Set<Integer> patientIds = doctorFiles.stream().map(medicalFile::getPatientId).collect(java.util.stream.Collectors.toSet());

            List<user> users = userService.getAll();
            for (user u : users) {
                if (u.getRole() == Role.patient && patientIds.contains(u.getUserId())) {
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
        String status = statusFilter.getValue();
        String sortOrder = sortComboBox.getValue();

        List<user> filtered = allPatientsList.stream()
            .filter(u -> (u.getFirstName().toLowerCase().contains(query) || u.getLastName().toLowerCase().contains(query)))
            .filter(u -> {
                if (status == null || status.equals("Tous les statuts")) return true;
                String userStatus = u.getAccountStatus() != null ? u.getAccountStatus().name() : "";
                if (status.equals("Actif")) return userStatus.equalsIgnoreCase("active");
                if (status.equals("En attente")) return userStatus.equalsIgnoreCase("pending");
                if (status.equals("Suspendu")) return userStatus.equalsIgnoreCase("suspended");
                return true;
            })
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
        if (statusLabel != null) statusLabel.setText("Total dossiers: " + filteredPatientsList.size());
    }

    @FXML
    private void resetFilters() {
        searchField.setText("");
        statusFilter.setValue("Tous les statuts");
        sortComboBox.setValue("Ordre alphabétique");
        datePicker.setValue(null);
        currentPage = 1;
        filterPatients();
    }

    @FXML
    private void handleNewDossier() {
        tests.Main.showAddMedicalFile();
    }

    @FXML
    private void handleExportPDF() {
        if (filteredPatientsList.isEmpty()) {
            showAlert("Export PDF", "Aucun patient à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la liste des patients");
        fileChooser.setInitialFileName("Liste_Patients_Doctor.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());
        
        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Design colors
                BaseColor colorPrimary = new BaseColor(74, 108, 247); // Blue #4A6CF7
                BaseColor colorSecondary = new BaseColor(45, 55, 72); // Slate #2D3748
                BaseColor colorBorder = new BaseColor(226, 232, 240); // Light gray
                
                // Fonts
                Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.BOLD, colorPrimary);
                Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, colorSecondary);
                
                // Title
                Paragraph title = new Paragraph("LISTE DES PATIENTS - SHIFAA", fontTitle);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);
                
                Paragraph subtitle = new Paragraph("Généré le : " + java.time.LocalDate.now().toString(), 
                                                 FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY));
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(30);
                document.add(subtitle);
                
                // Table
                PdfPTable table = new PdfPTable(3); // Patient, Téléphone, Dernière Visite
                table.setWidthPercentage(100);
                table.setWidths(new float[]{4f, 3f, 3f});
                
                // Headers
                String[] headers = {"NOM DU PATIENT", "NUMÉRO DE TÉLÉPHONE", "DERNIÈRE VISITE"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                    cell.setBackgroundColor(colorSecondary);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(10);
                    table.addCell(cell);
                }
                
                // Data
                for (user p : filteredPatientsList) {
                    addStyledCell(table, p.getFirstName() + " " + p.getLastName(), fontData, BaseColor.WHITE, colorBorder);
                    addStyledCell(table, (p.getPhone() != null ? p.getPhone() : "N/A"), fontData, BaseColor.WHITE, colorBorder);
                    addStyledCell(table, "2026-05-01", fontData, BaseColor.WHITE, colorBorder); // Valeur simulée ici car non dispo dans entité user directe
                }
                
                document.add(table);
                document.close();
                
                showAlert("Succès", "Le rapport PDF a été généré avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void addStyledCell(PdfPTable table, String text, Font font, BaseColor bgColor, BaseColor borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

        // Photo circulaire
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
        
        Label statusTag = new Label(patient.getAccountStatus() == entities.user.AccountStatus.active ? "Actif" : "En attente");
        statusTag.getStyleClass().add("status-tag");
        statusTag.getStyleClass().add(patient.getAccountStatus() == entities.user.AccountStatus.active ? "status-stable" : "status-amelioration");
        
        nameBox.getChildren().addAll(name, statusTag);
        header.getChildren().addAll(photo, nameBox);

        // Informations détaillées
        VBox infoBox = new VBox(5);
        Label email = new Label("📧 " + patient.getEmail());
        email.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label phone = new Label("📞 " + patient.getPhone());
        phone.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
        Label lastVisit = new Label("📅 Dernière visite: 2026-05-01");
        lastVisit.setStyle("-fx-font-size: 11; -fx-text-fill: #A0AEC0;");
        
        infoBox.getChildren().addAll(email, phone, lastVisit);

        // Boutons d'action spécifiques au Docteur
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        
        Button btnDossier = new Button("📂 Dossier");
        btnDossier.setStyle("-fx-background-color: #EDF2F7; -fx-text-fill: #4A5568; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        HBox.setHgrow(btnDossier, Priority.ALWAYS);
        btnDossier.setMaxWidth(Double.MAX_VALUE);
        btnDossier.setOnAction(e -> {
            tests.Main.showConsultationDossier(patient);
        });
        
        Button btnOrdonnance = new Button("💊 Ordonnance");
        btnOrdonnance.setStyle("-fx-background-color: #FF5C5C; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        HBox.setHgrow(btnOrdonnance, Priority.ALWAYS);
        btnOrdonnance.setMaxWidth(Double.MAX_VALUE);
        btnOrdonnance.setOnAction(e -> {
            tests.Main.showPrescriptionsForPatient(patient);
        });

        actions.getChildren().addAll(btnDossier, btnOrdonnance);

        card.getChildren().addAll(header, new Separator(), infoBox, actions);
        return card;
    }
}
