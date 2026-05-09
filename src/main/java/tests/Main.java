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
import Controler.AdminAddUserController;
import Controler.AdminEditUserController;
import entities.feedback;
import entities.user;
import services.userServices;
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
    private static int currentUserId = 1;
    private static String currentUserRole = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            showLandingPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== PAGES DE CONNEXION ==========

    public static void showLandingPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/LandingView.fxml"));
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1000, 650);
                scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
            }
            primaryStage.setTitle("SHIFAA - Bienvenue");
            primaryStage.setMaximized(true);
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/LoginView.fxml"));
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1000, 650);
                scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
            }
            primaryStage.setTitle("SHIFAA - Connexion");
            primaryStage.setMaximized(true);
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showSignUpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/SignUpView.fxml"));
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1000, 650);
                scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
            }
            primaryStage.setTitle("SHIFAA - Inscription");
            primaryStage.setMaximized(true);
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showForgotPasswordPage() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ForgotPasswordView.fxml"));
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1000, 650);
                scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
            }
            primaryStage.setTitle("SHIFAA - Récupération de mot de passe");
            primaryStage.setMaximized(true);
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== REDIRECTION SELON LE RÔLE ==========

    public static void redirectToSpace(int userId, String role) {
        currentUserId = userId;
        currentUserRole = role;

        // Reset all controllers to avoid cross-space view updates
        patientMainController = null;
        adminMainController = null;
        doctorMainController = null;
        assistantMainController = null;

        System.out.println("Redirection vers: " + role);

        if (role.equals("admin")) {
            showAdminSpace();
        } else if (role.equals("patient")) {
            showPatientSpace();
        } else if (role.equals("doctor")) {
            showDoctorSpace();
        } else if (role.equals("assistant")) {
            showAssistantSpace();
        } else if (role.equals("pharmacist")) {
            // Pharmacist interface removed, show profile
            showPatientSpace(); // Or show a default space
            showUserProfile();
        } else {
            showLoginPage();
        }
    }

    private static void showAdminSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminMainView.fxml"));
            Parent root = loader.load();
            adminMainController = loader.getController();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());

            primaryStage.setTitle("SHIFAA - Administration");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showPatientSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientMainView.fxml"));
            Parent root = loader.load();
            patientMainController = loader.getController();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());

            primaryStage.setTitle("SHIFAA - Espace Patient");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showDoctorSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DoctorMainView.fxml"));
            Parent root = loader.load();
            doctorMainController = loader.getController();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());

            primaryStage.setTitle("SHIFAA - Espace Médecin");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showAssistantSpace() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AssistantMainView.fxml"));
            Parent root = loader.load();
            assistantMainController = loader.getController();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Main.class.getResource("/theme-medical.css").toExternalForm());

            primaryStage.setTitle("SHIFAA - Espace Assistant");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== NAVIGATION PATIENT ==========

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void refreshMainMenu() {
        if (patientMainController != null) patientMainController.loadUserInfo();
        if (adminMainController != null) adminMainController.loadUserInfo();
        if (doctorMainController != null) doctorMainController.loadUserInfo();
        if (assistantMainController != null) assistantMainController.loadUserInfo();
    }

    public static void updateActiveContent(Parent content) {
        if (currentUserRole.equals("patient") && patientMainController != null) {
            patientMainController.updateContent(content);
        } else if ((currentUserRole.equals("admin") || currentUserRole.equals("assistant")) && adminMainController != null) {
            adminMainController.updateContent(content);
        } else if (currentUserRole.equals("doctor") && doctorMainController != null) {
            doctorMainController.updateContent(content);
        } else if (currentUserRole.equals("assistant") && assistantMainController != null) {
            assistantMainController.updateContent(content);
        }
    }

    // ========== NAVIGATION PATIENT ==========

    public static void showUserProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/UserProfileView.fxml"));
            Parent content = loader.load();
            Controler.UserProfileController controller = loader.getController();
            controller.loadUserData(currentUserId);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPatientDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientDoctorListView.fxml"));
            Parent content = loader.load();
            PatientDoctorListController controller = loader.getController();
            controller.loadDoctors();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPatientPharmacies() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientPharmacyListView.fxml"));
            Parent content = loader.load();
            PatientPharmacyListController controller = loader.getController();
            controller.loadPharmacies();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPatientMedicalFiles() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== NAVIGATION DOCTEUR ==========
    public static void showDoctorPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/DoctorPatientListView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void showDoctorFeedbacks() {
        try {
            int doctorId = currentUserId;
            String doctorName = "Docteur";
            
            // On cherche le nom du docteur
            userServices userService = new userServices();
            for (user u : userService.getAll()) {
                if (u.getUserId() == doctorId) {
                    doctorName = "Dr. " + u.getFirstName() + " " + u.getLastName();
                    break;
                }
            }

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientFeedbackView.fxml"));
            Parent content = loader.load();
            PatientFeedbackController controller = loader.getController();
            controller.loadFeedbacks("doctor", doctorId, doctorName);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== NAVIGATION ASSISTANT ==========
    public static void showAssistantPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AssistantPatientListView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showPatientProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPatientEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPatientHealthStats() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/HealthStatisticsView.fxml"));
            Parent root = loader.load();
            PatientMainController.getInstance().updateContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showPatientPillTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/MedicationTrackerView.fxml"));
            Parent root = loader.load();
            PatientMainController.getInstance().updateContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showPatientReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ========== GESTION DES FEEDBACKS PATIENT ==========

    public static void showPatientFeedbackForDoctor(int doctorId, String doctorName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientFeedbackView.fxml"));
            Parent content = loader.load();
            PatientFeedbackController controller = loader.getController();
            controller.loadFeedbacks("doctor", doctorId, doctorName);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAddFeedback(String targetType, int targetId, String targetName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientAddFeedbackView.fxml"));
            Parent content = loader.load();
            PatientAddFeedbackController controller = loader.getController();
            controller.initData(targetType, targetId, targetName);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showEditFeedback(feedback fb, String targetType, int targetId, String targetName) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientEditFeedbackView.fxml"));
            Parent content = loader.load();
            PatientEditFeedbackController controller = loader.getController();
            controller.initData(fb, targetType, targetId, targetName);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== ADMIN - GESTION DES UTILISATEURS ==========

    public static void showAdminUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/UtilisateursListView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminAddUserView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminEditUser(user u) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminEditUserView.fxml"));
            Parent content = loader.load();
            AdminEditUserController controller = loader.getController();
            controller.setUser(u);
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminAvis() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/AdminFeedbackView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminDossiers() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/PatientComingSoonView.fxml"));
            Parent content = loader.load();
            updateActiveContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}