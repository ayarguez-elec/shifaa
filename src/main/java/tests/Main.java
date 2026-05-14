package tests;

import Controler.PatientMainController;
import Controler.UserProfileController;
import Controler.PatientDoctorListController;
import Controler.PatientPharmacyListController;
import Controler.PatientFeedbackController;
import Controler.PatientAddFeedbackController;
import Controler.PatientEditFeedbackController;
import Controler.AdminMainController;
import Controler.UtilisateursListController;
import Controler.AdminEditUserController;
import Controler.GetAllevent;
import Controler.PharmacistMainController;
import entities.feedback;
import entities.user;
import services.userServices;
import utils.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;
    private static PatientMainController patientMainController;
    private static AdminMainController adminMainController;
    private static Controler.DoctorMainController doctorMainController;
    private static Controler.AssistantMainController assistantMainController;
    private static PharmacistMainController pharmacistMainController;
    public static int currentUserId = -1;
    public static String currentUserRole = "";
    public static entities.Reclamation selectedReclamation = null;
    public static entities.Reponse selectedReponse = null;
    public static int selectedReclamationId = 0;
    public static int notificationUserId = 0;
    public static String notificationMessage = "";
    public static void clearSelection() { selectedReclamation = null; selectedReponse = null; selectedReclamationId = 0; }
    public static void setNotification(int userId, String message) { notificationUserId = userId; notificationMessage = message; }
    public static void clearNotification() { notificationUserId = 0; notificationMessage = ""; }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showPatientReclamation() {
        showPatientReclamations();
    }

    @Override
    public void start(Stage stage) {
        // Initialiser les tables manquantes (événements, participants, etc.)
        DatabaseInitializer.init();

        try {
            primaryStage = stage;
            showLandingPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════
    //  PAGES DE CONNEXION / INSCRIPTION
    // ═══════════════════════════════════════════════

    public static void showLandingPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/LandingView.fxml"));
            Parent root = loader.load();
            applyScene(root, "SHIFAA - Bienvenue");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showLoginPage() {
        try {
            utils.SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/LoginView.fxml"));
            Parent root = loader.load();
            applyScene(root, "SHIFAA - Connexion");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showSignUpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/SignUpView.fxml"));
            Parent root = loader.load();
            applyScene(root, "SHIFAA - Inscription");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showForgotPasswordPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ForgotPasswordView.fxml"));
            Parent root = loader.load();
            applyScene(root, "SHIFAA - Récupération de mot de passe");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void applyScene(Parent root, String title) {
        Scene currentScene = primaryStage.getScene();
        if (currentScene == null) {
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            currentScene.setRoot(root);
        }
        primaryStage.setTitle(title);
        primaryStage.setMaximized(true);
        if (!primaryStage.isShowing()) primaryStage.show();
    }

    // ═══════════════════════════════════════════════
    //  REDIRECTION SELON LE RÔLE
    // ═══════════════════════════════════════════════

    public static void redirectToSpace(int userId, String role) {
        currentUserId = userId;
        currentUserRole = role;

        patientMainController   = null;
        adminMainController     = null;
        doctorMainController    = null;
        assistantMainController = null;
        pharmacistMainController = null;

        System.out.println("[Auth] Redirection → " + role + " (id=" + userId + ")");

        switch (role) {
            case "admin"      -> showAdminSpace();
            case "patient"    -> showPatientSpace();
            case "doctor"     -> showDoctorSpace();
            case "assistant"  -> showAssistantSpace();
            case "pharmacist" -> showPharmacistSpace();
            default           -> showLoginPage();
        }
    }

    private static void showAdminSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminMainView.fxml"));
            Parent root = loader.load();
            adminMainController = loader.getController();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setTitle("SHIFAA - Administration");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            showAdminDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void showPatientSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientMainView.fxml"));
            Parent root = loader.load();
            patientMainController = loader.getController();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setTitle("SHIFAA - Espace Patient");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            showPatientDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void showDoctorSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DoctorMainView.fxml"));
            Parent root = loader.load();
            doctorMainController = loader.getController();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setTitle("SHIFAA - Espace Médecin");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            showDoctorDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void showAssistantSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AssistantMainView.fxml"));
            Parent root = loader.load();
            assistantMainController = loader.getController();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setTitle("SHIFAA - Espace Assistant");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            showAssistantDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void showPharmacistSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PharmacistMainView.fxml"));
            Parent root = loader.load();
            pharmacistMainController = loader.getController();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
            primaryStage.setTitle("SHIFAA - Espace Pharmacien");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
            showPharmacistDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════
    //  UTILITAIRES DE NAVIGATION
    // ═══════════════════════════════════════════════

    public static int getCurrentUserId()   { return currentUserId; }
    public static void setCurrentUserId(int id) { currentUserId = id; }
    public static String getCurrentUserRole()   { return currentUserRole; }

    public static void refreshMainMenu() {
        if (patientMainController    != null) patientMainController.loadUserInfo();
        if (adminMainController      != null) adminMainController.loadUserInfo();
        if (doctorMainController     != null) doctorMainController.loadUserInfo();
        if (assistantMainController  != null) assistantMainController.loadUserInfo();
        if (pharmacistMainController != null) pharmacistMainController.loadUserInfo();
    }

    public static void updateActiveContent(Parent content) {
        if ("patient".equals(currentUserRole) && patientMainController != null) {
            patientMainController.updateContent(content);
        } else if ("admin".equals(currentUserRole) && adminMainController != null) {
            adminMainController.updateContent(content);
        } else if ("doctor".equals(currentUserRole) && doctorMainController != null) {
            doctorMainController.updateContent(content);
        } else if ("assistant".equals(currentUserRole) && assistantMainController != null) {
            assistantMainController.updateContent(content);
        } else if ("pharmacist".equals(currentUserRole) && pharmacistMainController != null) {
            pharmacistMainController.updateContent(content);
        }
    }

    // ═══════════════════════════════════════════════
    //  NAVIGATION PATIENT
    // ═══════════════════════════════════════════════

    public static void showPatientDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientDashboardView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DashboardPatient.fxml"));
            Parent content = loader.load();
            Controler.DashboardPatient ctrl = loader.getController();
            ctrl.showListePane();
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientRequestAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DashboardPatient.fxml"));
            Parent content = loader.load();
            Controler.DashboardPatient ctrl = loader.getController();
            ctrl.showAjouterPane();
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientMedicalFile() { showPatientMedicalFiles(); }

    public static void showUserProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/UserProfileView.fxml"));
            Parent content = loader.load();
            Controler.UserProfileController ctrl = loader.getController();
            ctrl.loadUserData(currentUserId);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientDoctorListView.fxml"));
            Parent content = loader.load();
            Controler.PatientDoctorListController ctrl = loader.getController();
            ctrl.loadDoctors();
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientPharmacies() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/userpharmacies.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientMedicalFiles() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/MesDossiersPatient.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientPrescriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/MesOrdonnancesPatient.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showConsultationPrescriptionPatient(entities.Prescription p) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ConsultationPrescriptionPatient.fxml"));
            Parent content = loader.load();
            Controler.ConsultationPrescriptionPatientController ctrl = loader.getController();
            ctrl.initData(p);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showMonDossierMedicalDetail(int idfile) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/MonDossierMedical.fxml"));
            Parent content = loader.load();
            Controler.MonDossierMedicalController ctrl = loader.getController();
            services.medicalFileServ service = new services.medicalFileServ();
            entities.medicalFile mf = service.getById(idfile);
            if (mf != null) ctrl.chargerDossierSpecifique(idfile);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── ÉVÉNEMENTS (module intégré) ──────────────────────────────

    public static void showPatientEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientEventsView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
            showComingSoon();
        }
    }

    public static void showPatientEventBooking() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/SmartBooking.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── SANTÉ & SUIVI ────────────────────────────────────────────

    public static void showPatientHealthStats() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/HealthStatisticsView.fxml"));
            PatientMainController.getInstance().updateContent(loader.load());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void showPatientPillTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/MedicationTrackerView.fxml"));
            PatientMainController.getInstance().updateContent(loader.load());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void showPatientReclamations() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/userreclamations.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showUserDashboard() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/userdashboard.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showUserNotifications() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/Usernotifications.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showPatientProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/userproducts.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientProductDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/userproductdetails.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPatientAssistant() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/userassistant.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void showComingSoon() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════
    //  NAVIGATION MÉDECIN
    // ═══════════════════════════════════════════════

    public static void showDoctorDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DoctorDashboard.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showDoctorPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DoctorPatientListView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showConsultationDossier(entities.user patient) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ConsultationMedicalFile.fxml"));
            Parent content = loader.load();
            Controler.ConsultationMedicalFileController ctrl = loader.getController();
            services.medicalFileServ medSrv = new services.medicalFileServ();
            entities.medicalFile mf = medSrv.getByPatientIdAndDoctorId(patient.getUserId(), currentUserId);
            if (mf != null) {
                entities.userDS patientDS = new entities.userDS(
                    patient.getUserId(), patient.getFirstName(), patient.getLastName(),
                    patient.getUserPhoto(), patient.getRole().name(), patient.getEmail()
                );
                ctrl.setDossierData(mf, patientDS);
                updateActiveContent(content);
            } else {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                a.setTitle("Dossier introuvable");
                a.setHeaderText("Aucun dossier médical pour ce patient.");
                a.setContentText("Veuillez d'abord créer un dossier médical.");
                a.showAndWait();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showModifierDossier(entities.medicalFile mf, entities.userDS patient) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ModifiermedicalFile.fxml"));
            Parent content = loader.load();
            Controler.ModifiermedicalFile ctrl = loader.getController();
            ctrl.prechargerDossier(mf, patient);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAddMedicalFile() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AjoutermedicalFile.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPrescriptionsForPatient(entities.user patient) {
        if (patient != null) showPrescriptionsForPatientId(patient.getUserId());
    }

    public static void showPrescriptionsForPatientId(int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Prescription.fxml"));
            Parent content = loader.load();
            Controler.PrescriptionController ctrl = loader.getController();
            ctrl.filterByPatient(patientId);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPrescriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Prescription.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showConsultationPrescription(entities.Prescription p, entities.userDS patient) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ConsultationPrescription.fxml"));
            Parent content = loader.load();
            Controler.ConsultationPrescriptionController ctrl = loader.getController();
            ctrl.setPrescriptionData(p, patient);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showModifierPrescription(entities.Prescription p) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ModifierPrescription.fxml"));
            Parent content = loader.load();
            Controler.ModifierPrescriptionController ctrl = loader.getController();
            ctrl.prechargerDonnees(p);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAjouterPrescriptionForPatientId(int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AjouterPrescription.fxml"));
            Parent content = loader.load();
            Controler.AjouterPrescriptionController ctrl = loader.getController();
            ctrl.preselectPatient(patientId);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAjouterPrescription() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AjouterPrescription.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Statistiques.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showDoctorFeedbacks() {
        try {
            int doctorId = currentUserId;
            String doctorName = "Docteur";
            userServices userSrv = new userServices();
            for (user u : userSrv.getAll()) {
                if (u.getUserId() == doctorId) {
                    doctorName = "Dr. " + u.getFirstName() + " " + u.getLastName();
                    break;
                }
            }
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientFeedbackView.fxml"));
            Parent content = loader.load();
            PatientFeedbackController ctrl = loader.getController();
            ctrl.loadFeedbacks("doctor", doctorId, doctorName);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── ÉVÉNEMENTS (participation) ───────────────────────────────

    public static void showDoctorEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientEventsView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPharmacistEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientEventsView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════
    //  NAVIGATION ASSISTANT
    // ═══════════════════════════════════════════════

    public static void showAssistantPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AssistantPatientListView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAssistantDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AssistantDashboardView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAssistantAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DashboardAppointment.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAssistantAvailability() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DashboardAvailability.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════
    //  NAVIGATION ADMIN
    // ═══════════════════════════════════════════════

    public static void showAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminDashboardView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/UtilisateursListView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminPendingUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/UtilisateursListView.fxml"));
            Parent content = loader.load();
            UtilisateursListController ctrl = loader.getController();
            ctrl.filterToPending();
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminAddUserView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminEditUser(user u) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminEditUserView.fxml"));
            Parent content = loader.load();
            AdminEditUserController ctrl = loader.getController();
            ctrl.setUser(u);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminAvis() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminFeedbackView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── ÉVÉNEMENTS côté admin (module intégré) ───────────────────

    public static void showAdminEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/GetAllevent.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // [SECURITY] Layer 1 — Navigation guard: blocks non-admin access before FXML even loads

    public static void showAdminAjouterEvent() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Ajouterevent.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminModifierEvent(entities.event ev) {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Modifierevent.fxml"));
            Parent content = loader.load();
            ((Controler.Modifierevent) loader.getController()).setEvent(ev);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminAjouterParticipant() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Ajouterparticipant.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminModifierParticipant(entities.Participant p) {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Modifierparticipant.fxml"));
            Parent content = loader.load();
            ((Controler.Modifierparticipant) loader.getController()).setParticipant(p);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminParticipantProfile(entities.Participant p) {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ParticipantProfile.fxml"));
            Parent content = loader.load();
            ((Controler.ParticipantProfileController) loader.getController()).setParticipant(p);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminEventAnalytics() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DashboardAnalytics.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminEventParticipants() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/GetAllparticipant.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminEventReservations() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/SmartBooking.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminEventRecommendations() {
        if (utils.RoleGuard.denyIfNotAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Recommendations.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminDossiers() { showComingSoon(); }
    public static void showPatientNotifications() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/Usernotifications.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showAdminNotifications() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/Adminnotifications.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void changeScene(String fxmlPath) { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource(fxmlPath)); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showAdminReclamations() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/adminreclamations.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }
    public static void showAdminDashboardReclamations() { try { javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/admindashboard.fxml")); updateActiveContent(loader.load()); } catch (Exception e) { e.printStackTrace(); } }

    // ═══════════════════════════════════════════════
    //  NAVIGATION PHARMACIE / PRODUIT (Admin)
    // ═══════════════════════════════════════════════

    public static void showAdminPharmacies() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminpharmacies.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminAddPharmacy() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminaddpharmacy.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminUpdatePharmacy() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminupdatepharmacy.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminproducts.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminaddproduct.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAdminUpdateProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/adminupdateproduct.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════
    //  NAVIGATION PHARMACIEN (Stock)
    // ═══════════════════════════════════════════════

    public static void showPharmacyStock() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pharmacystock.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPharmacistDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PharmacistDashboardView.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPharmacyAddStock() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pharmacyaddstock.fxml"));
            updateActiveContent(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showPharmacySpace(int pharmacyId) {
        utils.SessionManager.setConnectedPharmacyId(pharmacyId);
        showPharmacyStock();
    }

    // ═══════════════════════════════════════════════
    //  GESTION DES FEEDBACKS
    // ═══════════════════════════════════════════════

    public static void showPatientFeedbackForDoctor(int doctorId, String doctorName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientFeedbackView.fxml"));
            Parent content = loader.load();
            PatientFeedbackController ctrl = loader.getController();
            ctrl.loadFeedbacks("doctor", doctorId, doctorName);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showAddFeedback(String targetType, int targetId, String targetName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientAddFeedbackView.fxml"));
            Parent content = loader.load();
            PatientAddFeedbackController ctrl = loader.getController();
            ctrl.initData(targetType, targetId, targetName);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showEditFeedback(feedback fb, String targetType, int targetId, String targetName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientEditFeedbackView.fxml"));
            Parent content = loader.load();
            PatientEditFeedbackController ctrl = loader.getController();
            ctrl.initData(fb, targetType, targetId, targetName);
            updateActiveContent(content);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
