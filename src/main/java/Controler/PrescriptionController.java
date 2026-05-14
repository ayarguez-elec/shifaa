package Controler;

import entities.Prescription;
import entities.userDS;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.Prescriptionserv;
import services.userservDS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

public class PrescriptionController {

    @FXML private Pagination pagination;
    @FXML private DatePicker filterDate;
    @FXML private ImageView imgDoctor;
    @FXML private Label lblDoctorName;
    
    private final int ITEMS_PER_PAGE = 4;
    private List<Prescription> allPrescriptions = new ArrayList<>();
    private List<Prescription> prescriptionsAffichees = new ArrayList<>();
    private int currentPatientId = -1;
    
    private Prescriptionserv prescriptionService;
    private userservDS userService;
    private java.util.Map<Integer, userDS> patientCache = new java.util.HashMap<>();

    public PrescriptionController() {
        prescriptionService = new Prescriptionserv();
        userService = new userservDS();
    }

    @FXML
    public void initialize() {
        // Listener pour filtrage par date
        filterDate.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        
        pagination.setPageFactory(this::createPage);
        chargerPatients();
        chargerPrescriptions();
        chargerProfilMedecin();
    }

    private void chargerPatients() {
        List<userDS> patients = userService.getAll();
        for (userDS p : patients) {
            patientCache.put(p.getUserId(), p);
        }
    }

    private void chargerProfilMedecin() {
        if (lblDoctorName != null) lblDoctorName.setText("Dr. Saif Gharbi");
        List<userDS> docs = userService.searchAllByNameOrSurname("Saif");
        for (userDS d : docs) {
            if (d.getFirstName().equalsIgnoreCase("Saif") && d.getLastName().equalsIgnoreCase("Gharbi")) {
                String photoPath = d.getUserphoto();
                if (photoPath != null && !photoPath.trim().isEmpty() && imgDoctor != null) {
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

    @FXML
    public void appliquerFiltres() {
        List<Prescription> filtered = new ArrayList<>(allPrescriptions);

        // Filtrer par date uniquement
        if (filterDate.getValue() != null) {
            java.time.LocalDate selectedDate = filterDate.getValue();
            filtered = filtered.stream()
                .filter(p -> p.getPrescriptionDate().equals(selectedDate))
                .toList();
        }

        this.prescriptionsAffichees = filtered;
        updatePagination();
    }

    @FXML
    public void reinitialiserFiltres() {
        filterDate.setValue(null);
        this.prescriptionsAffichees = new ArrayList<>(allPrescriptions);
        updatePagination();
    }

    private Node createPage(int pageIndex) {
        // Cas vide : aucune ordonnance pour ce patient
        if (prescriptionsAffichees.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(80));

            Label icon = new Label("📋");
            icon.setStyle("-fx-font-size: 60;");

            Label msg = new Label("Pas d'ordonnances ici");
            msg.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #4A5568;");

            Label sub = new Label("Ce patient n'a aucune prescription enregistrée.");
            sub.setStyle("-fx-font-size: 14; -fx-text-fill: #A0AEC0;");

            emptyState.getChildren().addAll(icon, msg, sub);
            return emptyState;
        }

        FlowPane flowPane = new FlowPane(20, 20);
        flowPane.setPadding(new Insets(10));
        flowPane.setAlignment(Pos.TOP_LEFT);

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, prescriptionsAffichees.size());

        if (fromIndex < prescriptionsAffichees.size()) {
            for (int i = fromIndex; i < toIndex; i++) {
                Prescription p = prescriptionsAffichees.get(i);
                userDS patient = patientCache.get(p.getPatientId());
                if (patient == null) patient = userService.getById(p.getPatientId());
                VBox card = createPrescriptionCard(p, patient);
                flowPane.getChildren().add(card);
            }
        }

        return flowPane;
    }

    private VBox createPrescriptionCard(Prescription p, userDS patient) {
        VBox card = new VBox(10);
        card.getStyleClass().add("patient-card");
        card.setPrefWidth(240);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.TOP_CENTER);

        // Header (Date)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Label lblDate = new Label("📅 " + p.getPrescriptionDate().toString());
        lblDate.getStyleClass().add("card-badge");
        header.getChildren().add(lblDate);

        // Icon (Prescription)
        VBox iconWrapper = new VBox();
        iconWrapper.setAlignment(Pos.CENTER);
        iconWrapper.setMinHeight(60);
        Label iconLabel = new Label("📄");
        iconLabel.setStyle("-fx-font-size: 35;");
        iconWrapper.getChildren().add(iconLabel);

        // Content
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblPatient = new Label(patient != null ? patient.getFullName() : "Patient Inconnu");
        lblPatient.getStyleClass().add("card-title");
        
        Label lblTreatment = new Label(p.getPrescription());
        lblTreatment.setWrapText(true);
        lblTreatment.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        Label lblDuration = new Label("⏳ " + p.getTreatmentDuration());
        lblDuration.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(lblPatient, lblTreatment, lblDuration);

        // Actions
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        
        Button btnVoir = new Button("👁 Voir");
        btnVoir.getStyleClass().add("btn-card-action");
        btnVoir.setOnAction(e -> consulterPrescription(p, patient));
        
        Button btnModif = new Button("✎ Modif");
        btnModif.getStyleClass().add("btn-card-action");
        btnModif.setOnAction(e -> modifierPrescription(p));
        
        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().add("btn-card-delete");
        btnDel.setOnAction(e -> supprimerPrescription(p));

        actionBox.getChildren().addAll(btnVoir, btnModif, btnDel);

        card.getChildren().addAll(header, iconWrapper, infoBox, actionBox);
        return card;
    }

    private void modifierPrescription(Prescription p) {
        tests.Main.showModifierPrescription(p);
    }

    private void consulterPrescription(Prescription p, userDS patient) {
        tests.Main.showConsultationPrescription(p, patient);
    }

    private void chargerPrescriptions() {
        try {
            // Sécurité stricte : Ne charger QUE les ordonnances de CE médecin
            allPrescriptions = prescriptionService.getByDoctorId(tests.Main.currentUserId);
            this.prescriptionsAffichees = allPrescriptions.stream()
                .filter(p -> p.getDoctorId() == tests.Main.currentUserId)
                .collect(java.util.stream.Collectors.toList());
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void filterByPatient(int patientId) {
        this.currentPatientId = patientId;
        try {
            // Sécurité stricte : Ne récupérer que les prescriptions de CE médecin pour CE patient
            List<Prescription> doctorPrescriptions = prescriptionService.getByDoctorId(tests.Main.currentUserId);
            this.allPrescriptions = doctorPrescriptions.stream()
                .filter(p -> p.getDoctorId() == tests.Main.currentUserId)
                .collect(java.util.stream.Collectors.toList());
            
            this.prescriptionsAffichees = allPrescriptions.stream()
                .filter(p -> p.getPatientId() == patientId)
                .collect(java.util.stream.Collectors.toList());
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) prescriptionsAffichees.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private void supprimerPrescription(Prescription p) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la prescription");
        confirmation.setContentText("Voulez-vous vraiment supprimer cette prescription ?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                prescriptionService.delete(p);
                chargerPrescriptions();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void allerAjouterPrescription(javafx.event.Event event) {
        if (currentPatientId != -1) {
            tests.Main.showAjouterPrescriptionForPatientId(currentPatientId);
        } else {
            tests.Main.showAjouterPrescription();
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
            e.printStackTrace();
        }
    }

    @FXML
    public void allerGererPatients(javafx.event.Event event) {
        // Redirection vers la liste du portail docteur
        tests.Main.showDoctorPatients();
    }
    
    @FXML
    public void allerAjouterDossier(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutermedicalFile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Ajouter dossier médical");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exporterPDF() {
        if (prescriptionsAffichees.isEmpty()) {
            showAlert("Export PDF", "Aucune prescription à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des Prescriptions");
        fileChooser.setInitialFileName("Rapport_Prescriptions.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(pagination.getScene().getWindow());
        
        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Couleurs du design (Thème Vert)
                BaseColor colorPrimary = new BaseColor(0, 153, 41); // Vert #009929
                BaseColor colorSecondary = new BaseColor(74, 85, 104); // Bleu Ardoise #4A5568
                BaseColor colorHeaderBg = new BaseColor(45, 55, 72); // Gris foncé #2D3748
                BaseColor colorBorder = new BaseColor(226, 232, 240); // Gris clair #E2E8F0

                // Polices
                Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 22, Font.BOLD, colorPrimary);
                Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, colorSecondary);
                
                // Titre principal
                Paragraph title = new Paragraph("RAPPORT DES PRESCRIPTIONS - SHIFAA", fontTitle);
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
                PdfPTable table = new PdfPTable(4); // ID, Patient, Date, Traitement
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                float[] columnWidths = {1f, 3f, 2f, 5f};
                table.setWidths(columnWidths);
                
                // Headers
                String[] headers = {"ID", "PATIENT", "DATE", "PRESCRIPTION / TRAITEMENT"};
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
                for (Prescription p : prescriptionsAffichees) {
                    userDS patient = userService.getById(p.getPatientId());
                    String patientName = (patient != null) ? patient.getFullName() : "N/A";
                    
                    BaseColor rowColor = alternate ? new BaseColor(247, 250, 252) : BaseColor.WHITE;
                    
                    addStyledCell(table, String.valueOf(p.getPrescriptionId()), fontData, rowColor, colorBorder);
                    addStyledCell(table, patientName, fontData, rowColor, colorBorder);
                    addStyledCell(table, p.getPrescriptionDate().toString(), fontData, rowColor, colorBorder);
                    addStyledCell(table, p.getPrescription(), fontData, rowColor, colorBorder);
                    
                    alternate = !alternate;
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
}
