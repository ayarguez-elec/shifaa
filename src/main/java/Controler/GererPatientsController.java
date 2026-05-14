package Controler;

import entities.userDS;
import entities.medicalFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import services.userservDS;
import services.medicalFileServ;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;

public class GererPatientsController {

    // ================= FXML COMPONENTS =================
    @FXML private TextField searchField;
    @FXML private Label lblStatistiques;
    @FXML private ComboBox<String> filterStatus;
    @FXML private DatePicker filterDate;
    @FXML private ComboBox<String> sortOrder;
    @FXML private Pagination pagination;
    
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;

    
    private final int ITEMS_PER_PAGE = 8;

    // ================= SERVICES =================
    private userservDS userService;
    private medicalFileServ medicalFileService;
    private List<medicalFile> dossiersAffiches = new ArrayList<>();
    private java.util.Map<Integer, userDS> patientCache = new java.util.HashMap<>();

    public GererPatientsController() {
        userService = new userservDS();
        medicalFileService = new medicalFileServ();
    }

    @FXML
    public void initialize() {
        // Initialisation des filtres
        filterStatus.setItems(FXCollections.observableArrayList("Tous", "stable", "critique", "en amelioration"));
        sortOrder.setItems(FXCollections.observableArrayList("Nom (A - Z)", "Nom (Z - A)"));
        
        // Listeners pour filtrage automatique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        filterStatus.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        filterDate.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        
        // Configuration de la pagination
        pagination.setPageFactory(this::createPage);
        
        chargerTousLesDossiers();
        chargerPatients(); // Pré-remplir le cache
        chargerProfilMedecin();
    }

    private void chargerPatients() {
        List<userDS> patients = userService.getAll();
        for (userDS p : patients) {
            patientCache.put(p.getUserId(), p);
        }
    }



    private void chargerProfilMedecin() {
        lblDoctorName.setText("Dr. Saif Gharbi");
        // Simuler la photo du médecin (on peut aussi chercher dans la DB)
        List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty()) {
                    try {
                        File file = new File(photoPath);
                        if (file.exists()) {
                            imgDoctor.setImage(new Image(file.toURI().toString()));
                        } else if (getClass().getResource("/" + photoPath) != null) {
                            imgDoctor.setImage(new Image(getClass().getResourceAsStream("/" + photoPath)));
                        }
                    } catch (Exception e) {}
                }
                break;
            }
        }
    }

    private Node createPage(int pageIndex) {
        FlowPane flowPane = new FlowPane(20, 20);
        flowPane.setPadding(new Insets(10));
        flowPane.setAlignment(Pos.TOP_LEFT);
        
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, dossiersAffiches.size());
        
        if (fromIndex < dossiersAffiches.size()) {
            for (int i = fromIndex; i < toIndex; i++) {
                medicalFile mf = dossiersAffiches.get(i);
                userDS patient = patientCache.get(mf.getPatientId());
                if (patient == null) {
                    patient = userService.getById(mf.getPatientId());
                    if (patient != null) patientCache.put(patient.getUserId(), patient);
                }
                if (patient != null) {
                    VBox card = createDossierCard(mf, patient);
                    flowPane.getChildren().add(card);
                }
            }
        }
        
        return flowPane;
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) dossiersAffiches.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
        // Forcer la mise à jour de la page actuelle si nécessaire
        pagination.setPageFactory(this::createPage);
    }

    @FXML
    public void appliquerFiltres() {
        String keyword = searchField.getText();
        List<medicalFile> baseList;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            baseList = medicalFileService.searchByPatientNameOrSurname(keyword, tests.Main.currentUserId);
        } else {
            baseList = medicalFileService.getByDoctorId(tests.Main.currentUserId);
        }
        
        // Filtrer par statut
        String selectedStatus = filterStatus.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tous")) {
            baseList = baseList.stream()
                .filter(mf -> mf.getPatientstatus() != null && mf.getPatientstatus().equalsIgnoreCase(selectedStatus))
                .toList();
        }
        
        // Filtrer par date de dernière visite
        if (filterDate.getValue() != null) {
            java.time.LocalDate selectedDate = filterDate.getValue();
            baseList = baseList.stream()
                .filter(mf -> mf.getLastvisit() != null && mf.getLastvisit().equals(selectedDate))
                .toList();
        }
        
        // Tri
        String selectedSort = sortOrder.getValue();
        if (selectedSort != null) {
            baseList = new java.util.ArrayList<>(baseList); // Rendre la liste mutable pour le tri
            baseList.sort((a, b) -> {
                userDS pA = patientCache.get(a.getPatientId());
                if (pA == null) {
                    pA = userService.getById(a.getPatientId());
                    if (pA != null) patientCache.put(pA.getUserId(), pA);
                }
                userDS pB = patientCache.get(b.getPatientId());
                if (pB == null) {
                    pB = userService.getById(b.getPatientId());
                    if (pB != null) patientCache.put(pB.getUserId(), pB);
                }
                String nameA = (pA != null) ? pA.getFullName() : "";
                String nameB = (pB != null) ? pB.getFullName() : "";
                if (selectedSort.equals("Nom (A - Z)")) {
                    return nameA.compareToIgnoreCase(nameB);
                } else {
                    return nameB.compareToIgnoreCase(nameA);
                }
            });
        }
        
        this.dossiersAffiches = baseList;
        updatePagination();
        lblStatistiques.setText("Total dossiers: " + baseList.size());
    }

    @FXML
    public void exporterPDF() {
        if (dossiersAffiches.isEmpty()) {
            showAlert("Export PDF", "Aucun dossier à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("Liste_Dossiers_Medicaux.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(pagination.getScene().getWindow());
        
        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Couleurs du design
                BaseColor colorPrimary = new BaseColor(255, 92, 92); // Corail #FF5C5C
                BaseColor colorSecondary = new BaseColor(74, 85, 104); // Bleu Ardoise #4A5568
                BaseColor colorHeaderBg = new BaseColor(45, 55, 72); // Gris foncé #2D3748
                BaseColor colorBorder = new BaseColor(226, 232, 240); // Gris clair #E2E8F0

                // Polices
                Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 22, Font.BOLD, colorPrimary);
                Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, colorSecondary);
                
                // Titre principal
                Paragraph title = new Paragraph("RAPPORT MÉDICAL - SHIFAA", fontTitle);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(10);
                document.add(title);
                
                // Sous-titre
                Paragraph subtitle = new Paragraph("Généré le : " + java.time.LocalDate.now().toString(), 
                                                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY));
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(30);
                document.add(subtitle);
                
                // Table
                PdfPTable table = new PdfPTable(5); // ID, Patient, Statut, Création, Visite
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                
                // Headers
                String[] headers = {"ID", "NOM DU PATIENT", "STATUT", "CRÉATION", "DERNIÈRE VISITE"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                    cell.setBackgroundColor(colorHeaderBg);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(10);
                    cell.setBorderColor(colorBorder);
                    table.addCell(cell);
                }
                
                // Data
                boolean alternate = false;
                for (medicalFile mf : dossiersAffiches) {
                    userDS p = userService.getById(mf.getPatientId());
                    String patientName = (p != null) ? p.getFullName() : "N/A";
                    
                    BaseColor rowColor = alternate ? new BaseColor(247, 250, 252) : BaseColor.WHITE;
                    
                    addStyledCell(table, String.valueOf(mf.getIdfile()), fontData, rowColor, colorBorder);
                    addStyledCell(table, patientName, fontData, rowColor, colorBorder);
                    addStyledCell(table, mf.getPatientstatus() != null ? mf.getPatientstatus().toUpperCase() : "N/A", fontData, rowColor, colorBorder);
                    addStyledCell(table, mf.getCreationdate() != null ? mf.getCreationdate().toString() : "N/A", fontData, rowColor, colorBorder);
                    addStyledCell(table, mf.getLastvisit() != null ? mf.getLastvisit().toString() : "N/A", fontData, rowColor, colorBorder);
                    
                    alternate = !alternate;
                }
                
                document.add(table);
                document.close();
                
                showAlert("Succès", "Le fichier PDF a été généré avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void reinitialiserFiltres() {
        searchField.clear();
        filterStatus.getSelectionModel().select("Tous");
        filterDate.setValue(null);
        sortOrder.getSelectionModel().clearSelection();
        chargerTousLesDossiers();
    }

    @FXML
    public void rechercherPatient() {
        appliquerFiltres();
    }

    @FXML
    public void afficherTousLesPatients() {
        reinitialiserFiltres();
    }

    private void chargerTousLesDossiers() {
        List<medicalFile> allFiles = medicalFileService.getByDoctorId(tests.Main.currentUserId);
        this.dossiersAffiches = allFiles;
        updatePagination();
        lblStatistiques.setText("Total dossiers: " + allFiles.size());
    }



    private VBox createDossierCard(medicalFile mf, userDS patient) {
        VBox card = new VBox(15);
        card.getStyleClass().add("patient-card");
        card.setPrefWidth(280);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        String dateCreation = (mf.getCreationdate() != null) ? mf.getCreationdate().toString() : "N/A";
        Label lblRole = new Label("📅 " + dateCreation);
        lblRole.getStyleClass().add("card-badge");
        
        String status = (mf.getPatientstatus() != null && !mf.getPatientstatus().isEmpty()) 
                        ? mf.getPatientstatus() : "stable";
        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().add("card-badge");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        header.getChildren().addAll(lblRole, headerSpacer, lblStatus);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("card-image");
        
        String photoPath = patient.getUserphoto();
        boolean hasImage = false;
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            try {
                // 1. Essayer comme chemin absolu ou relatif au projet
                File file = new File(photoPath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    hasImage = true;
                } 
                // 2. Essayer dans les ressources
                else if (getClass().getResource("/" + photoPath) != null) {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/" + photoPath)));
                    hasImage = true;
                }
                // 3. Essayer dans un dossier 'images' dans les ressources
                else if (getClass().getResource("/images/" + photoPath) != null) {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/images/" + photoPath)));
                    hasImage = true;
                }
            } catch (Exception e) { 
                System.err.println("Erreur chargement photo: " + photoPath);
            }
        }
        
        VBox imageWrapper = new VBox(imageView);
        imageWrapper.setAlignment(Pos.CENTER);
        imageWrapper.setMinHeight(160);
        if (!hasImage) {
            imageWrapper.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
        }

        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox dateBox = new HBox(5);
        String patientRole = (patient.getRole() != null) ? patient.getRole().toUpperCase() : "PATIENT";
        Label iconLabel = new Label("📁 dossier   👤 " + patientRole);
        iconLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        dateBox.getChildren().addAll(iconLabel);

        Label lblName = new Label(patient.getFullName());
        lblName.getStyleClass().add("card-title");
        
        String dateStr = (mf.getLastvisit() != null) ? mf.getLastvisit().toString() : 
                         (mf.getCreationdate() != null ? mf.getCreationdate().toString() : "Aucune date");
        Label lblVal = new Label(dateStr); 
        lblVal.setStyle("-fx-text-fill: #FF5C5C; -fx-font-weight: bold; -fx-font-size: 18px;");

        infoBox.getChildren().addAll(dateBox, lblName, lblVal);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        
        Button btnVoir = new Button("👁 Voir");
        btnVoir.getStyleClass().addAll("btn-card-action");
        btnVoir.setOnAction(e -> consulterDossier(mf, patient));
        
        Button btnModif = new Button("✎ Modi...");
        btnModif.getStyleClass().add("btn-card-action");
        btnModif.setOnAction(e -> modifierDossier(mf, patient));
        
        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().add("btn-card-delete");
        btnDel.setOnAction(e -> supprimerDossier(mf));

        actionBox.getChildren().addAll(btnVoir, btnModif, btnDel);

        card.getChildren().addAll(header, imageWrapper, infoBox, actionBox);
        return card;
    }

    public void consulterDossier(medicalFile mf, userDS patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConsultationMedicalFile.fxml"));
            Parent root = loader.load();
            ConsultationMedicalFileController controller = loader.getController();
            controller.setDossierData(mf, patient);
            Stage stage = new Stage();
            stage.setTitle("Consultation Dossier #" + mf.getIdfile());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la consultation: " + e.getMessage());
        }
    }

    public void modifierDossier(medicalFile mf, userDS patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifiermedicalFile.fxml"));
            Parent root = loader.load();
            ModifiermedicalFile controller = loader.getController();
            controller.prechargerPatient(patient);
            
            Stage stage = (Stage) pagination.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Modifier Dossier #" + mf.getIdfile());
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le dossier: " + e.getMessage());
        }
    }

    public void supprimerDossier(medicalFile mf) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le dossier");
        confirmation.setContentText("Êtes-vous sûr ?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                medicalFileService.delete(mf.getIdfile());
                chargerTousLesDossiers();
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void allerAjouterDossier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutermedicalFile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Ajouter dossier médical");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    public void allerStatistiques(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Mes Statistiques");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques : " + e.getMessage());
        }
    }

    @FXML
    public void allerPrescriptions(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Gestion des Prescriptions");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface des prescriptions : " + e.getMessage());
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
}
