package Controler;

import entities.userDS;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import services.userservDS;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PatientDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblPatientName;
    @FXML private ImageView imgPatient;
    @FXML private Circle circleImage;
    
    // Identifiant par défaut
    private int loggedPatientId = 1;
    private userservDS userService = new userservDS();

    @FXML
    public void initialize() {
        // Recherche de Mohsen Dridi pour simuler sa connexion (doit être un patient)
        List<userDS> patients = userService.searchByNameOrSurname("Mohsen");
        userDS loggedUser = null;
        for (userDS u : patients) {
            if (u.getFirstName().equalsIgnoreCase("Mohsen") && u.getLastName().equalsIgnoreCase("Dridi")) {
                loggedUser = u;
                this.loggedPatientId = u.getUserId();
                break;
            }
        }
        
        if (loggedUser != null) {
            String fullName = loggedUser.getFirstName() + " " + loggedUser.getLastName();
            lblWelcome.setText("Bienvenue, " + fullName);
            lblPatientName.setText(fullName);
            
            // Chargement de la photo avec ImagePattern pour éviter les coupures
            String photoPath = loggedUser.getUserphoto();
            if (photoPath != null && !photoPath.trim().isEmpty()) {
                try {
                    Image image = null;
                    File file = new File(photoPath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else if (getClass().getResource("/" + photoPath) != null) {
                        image = new Image(getClass().getResourceAsStream("/" + photoPath));
                    } else if (getClass().getResource("/images/" + photoPath) != null) {
                        image = new Image(getClass().getResourceAsStream("/images/" + photoPath));
                    }

                    if (image != null) {
                        imgPatient.setImage(image);
                        circleImage.setFill(new ImagePattern(image));
                        imgPatient.setVisible(false); // On utilise le cercle pour l'affichage propre
                    }
                } catch (Exception e) {
                    System.err.println("Erreur chargement photo patient: " + e.getMessage());
                }
            }
        }
        
        System.out.println("DEBUG: Dashboard Patient initialisé pour ID: " + loggedPatientId);
    }



    @FXML
    public void ouvrirMonDossier(Event event) {
        System.out.println("DEBUG: Clic sur Mon Dossier");
        chargerVueDossier((Node) event.getSource());
    }

    @FXML
    public void ouvrirMesPrescriptions(Event event) {
        System.out.println("DEBUG: Clic sur Mes Ordonnances");
        chargerVuePrescriptions((Node) event.getSource());
    }

    private void chargerVuePrescriptions(Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesPrescriptions.fxml"));
            Parent root = loader.load();
            
            MesPrescriptionsController controller = loader.getController();
            controller.chargerPrescriptionsPatient(loggedPatientId);
            
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Ordonnances");
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("ERREUR lors du chargement des prescriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerVueDossier(Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MonDossierMedical.fxml"));
            Parent root = loader.load();
            
            MonDossierMedicalController controller = loader.getController();
            controller.chargerDonneesPatient(loggedPatientId);
            
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Dossier Médical");
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("ERREUR lors du chargement du dossier: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
