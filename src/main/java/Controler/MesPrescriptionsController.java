package Controler;

import entities.Prescription;
import entities.userDS;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.Prescriptionserv;
import services.userservDS;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesPrescriptionsController {

    @FXML private Pagination pagination;
    
    // Sidebar components
    @FXML private ImageView imgPatient;
    @FXML private Circle circleImage;
    @FXML private Label lblPatientName;

    private final int ITEMS_PER_PAGE = 4;
    private List<Prescription> allPrescriptions = new ArrayList<>();
    private Prescriptionserv prescriptionService = new Prescriptionserv();
    private userservDS userService = new userservDS();
    private int currentPatientId;

    public void chargerPrescriptionsPatient(int patientId) {
        this.currentPatientId = patientId;
        try {
            allPrescriptions = prescriptionService.getByPatientId(patientId);
            updatePagination();
            chargerSidebar(patientId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerSidebar(int patientId) {
        userDS user = userService.getById(patientId);
        if (user != null) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            lblPatientName.setText(fullName);
            
            String photoPath = user.getUserphoto();
            if (photoPath != null && !photoPath.trim().isEmpty()) {
                try {
                    Image image = null;
                    File file = new File(photoPath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else if (getClass().getResource("/" + photoPath) != null) {
                        image = new Image(getClass().getResourceAsStream("/" + photoPath));
                    }
                    
                    if (image != null) {
                        imgPatient.setImage(image);
                        circleImage.setFill(new ImagePattern(image));
                        imgPatient.setVisible(false);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur photo sidebar: " + e.getMessage());
                }
            }
        }
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) allPrescriptions.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        FlowPane flowPane = new FlowPane(20, 20);
        flowPane.setPadding(new Insets(10));
        flowPane.setAlignment(Pos.TOP_LEFT);
        
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allPrescriptions.size());
        
        if (fromIndex < allPrescriptions.size()) {
            for (int i = fromIndex; i < toIndex; i++) {
                Prescription p = allPrescriptions.get(i);
                flowPane.getChildren().add(createPrescriptionCard(p));
            }
        }
        
        return flowPane;
    }

    private VBox createPrescriptionCard(Prescription p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("patient-card");
        card.setPrefWidth(240);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);

        Label lblDate = new Label("📅 " + p.getPrescriptionDate().toString());
        lblDate.getStyleClass().add("card-badge");
        
        Label iconLabel = new Label("💊");
        iconLabel.setStyle("-fx-font-size: 40;");

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblTitle = new Label("Ordonnance");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        
        Label lblDetail = new Label(p.getPrescription());
        lblDetail.setWrapText(true);
        lblDetail.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 13;");
        lblDetail.setMaxHeight(60);

        Label lblDuree = new Label("⏳ Durée : " + p.getTreatmentDuration());
        lblDuree.setStyle("-fx-text-fill: #FF5C5C; -fx-font-weight: bold; -fx-font-size: 11;");

        Button btnVoir = new Button("👁 Voir");
        btnVoir.getStyleClass().add("btn-card-action");
        btnVoir.setOnAction(e -> consulterPrescription(p));

        infoBox.getChildren().addAll(lblTitle, lblDetail, lblDuree, btnVoir);

        card.getChildren().addAll(lblDate, iconLabel, infoBox);
        return card;
    }

    private void consulterPrescription(Prescription p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConsultationPrescriptionPatient.fxml"));
            Parent root = loader.load();
            ConsultationPrescriptionPatientController controller = loader.getController();
            controller.setPrescriptionData(p);
            Stage stage = (Stage) pagination.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Consultation Ordonnance #" + p.getPrescriptionId());
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void allerAuDashboard(Event event) {
        retourDashboard(new ActionEvent(event.getSource(), null));
    }

    @FXML
    public void ouvrirMonDossier(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MonDossierMedical.fxml"));
            Parent root = loader.load();
            MonDossierMedicalController controller = loader.getController();
            controller.chargerDonneesPatient(currentPatientId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Dossier Médical");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void retourDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PatientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
