package services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import entities.user;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.effect.DropShadow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceIdService {

    private static final List<String> PYTHON_CMD = findPythonCommand();
    private static final String SCRIPT_PATH = "python/face_reco.py";

    private static List<String> findPythonCommand() {
        String[][] candidates = {
            {"py", "-3.10"},
            {"python"},
            {"python3"},
            {"C:\\Python310\\python.exe"},
            {"C:\\Users\\marie\\AppData\\Local\\Programs\\Python\\Python310\\python.exe"}
        };
        for (String[] candidate : candidates) {
            try {
                List<String> cmd = new java.util.ArrayList<>(java.util.Arrays.asList(candidate));
                cmd.add("--version");
                Process p = new ProcessBuilder(cmd).start();
                p.waitFor();
                if (p.exitValue() == 0) {
                    System.out.println("✅ Python trouvé : " + String.join(" ", candidate));
                    return java.util.Arrays.asList(candidate);
                }
            } catch (Exception ignored) {}
        }
        System.err.println("⚠️ Python non trouvé, utilisation de 'python' par défaut");
        return java.util.Arrays.asList("python");
    }

    public interface FaceIdCallback {
        void onSuccess(user user);
        void onFailure(String message);
    }

    public void scanAndLogin(String email, FaceIdCallback callback) {
        userServices userService = new userServices();
        
        try {
            // 1. Trouver l'utilisateur
            user targetUser = null;
            for (user u : userService.getAll()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    targetUser = u;
                    break;
                }
            }

            if (targetUser == null) {
                callback.onFailure("Utilisateur non trouvé.");
                return;
            }

            if (targetUser.getUserPhoto() == null || targetUser.getUserPhoto().isEmpty()) {
                callback.onFailure("Aucune photo de profil enregistrée.");
                return;
            }

            user finalTargetUser = targetUser;
            
            // 2. Ouvrir l'interface de capture
            openCameraWindow(image -> {
                if (image == null) {
                    callback.onFailure("Capture annulée.");
                    return;
                }

                // 3. Traitement dans un thread séparé
                new Thread(() -> {
                    try {
                        File tempDir = new File("temp");
                        if (!tempDir.exists()) tempDir.mkdir();

                        File webcamFile = new File(tempDir, "webcam_" + UUID.randomUUID() + ".jpg");
                        ImageIO.write(image, "JPG", webcamFile);

                        File profileFile = new File(tempDir, "profile_" + UUID.randomUUID() + ".jpg");
                        byte[] profileBytes = Base64.getDecoder().decode(finalTargetUser.getUserPhoto().trim());
                        Files.write(profileFile.toPath(), profileBytes);

                        boolean isMatch = callPythonRecognition(webcamFile.getAbsolutePath(), profileFile.getAbsolutePath());

                        webcamFile.delete();
                        profileFile.delete();

                        if (isMatch) {
                            Platform.runLater(() -> callback.onSuccess(finalTargetUser));
                        } else {
                            Platform.runLater(() -> callback.onFailure("Le visage ne correspond pas à la photo de profil."));
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> callback.onFailure("Erreur: " + e.getMessage()));
                    }
                }).start();
            });

        } catch (Exception e) {
            callback.onFailure("Erreur technique: " + e.getMessage());
        }
    }

    private void openCameraWindow(java.util.function.Consumer<BufferedImage> onCapture) {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            onCapture.accept(null);
            return;
        }

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.TRANSPARENT); // Fenêtre sans bordures Windows pour un look plus moderne
        window.setTitle("Scan Face ID");

        // 1. Image de la caméra (forcée pour remplir le carré 350x350)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(350);
        imageView.setFitHeight(350);
        imageView.setPreserveRatio(false); 
        imageView.setScaleX(-1); // Effet miroir

        // 2. Conteneur carré pour le recadrage
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(350, 350);
        imageContainer.setMaxSize(350, 350);
        
        // Masque circulaire
        Circle clip = new Circle(175, 175, 175);
        imageContainer.setClip(clip);

        // 3. Cadre décoratif extérieur
        Circle frame = new Circle(175, 175, 180);
        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(Color.web("#3498db"));
        frame.setStrokeWidth(5);
        frame.setStrokeType(StrokeType.OUTSIDE);
        
        // Effet de lueur bleue
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#3498db"));
        glow.setRadius(20);
        frame.setEffect(glow);

        StackPane mainStack = new StackPane(imageContainer, frame);
        mainStack.setStyle("-fx-background-color: transparent;");

        Label statusLabel = new Label("RECONNAISSANCE FACIALE");
        statusLabel.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subLabel = new Label("Positionnez-vous face à la caméra");
        subLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Button captureBtn = new Button("CAPTURER");
        captureBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-background-radius: 30; -fx-padding: 12 40; -fx-font-size: 14px; -fx-cursor: hand;");
        
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            window.close();
            onCapture.accept(null);
        });

        VBox layout = new VBox(20, statusLabel, subLabel, mainStack, captureBtn, cancelBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 20; " +
                       "-fx-border-color: #ecf0f1; -fx-border-width: 2; -fx-border-radius: 20;");

        // Ombre portée sur toute la fenêtre
        DropShadow windowShadow = new DropShadow();
        windowShadow.setRadius(30);
        windowShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        layout.setEffect(windowShadow);

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();

        AtomicBoolean running = new AtomicBoolean(true);
        Thread videoThread = new Thread(() -> {
            while (running.get()) {
                BufferedImage frameImg = webcam.getImage();
                if (frameImg != null) {
                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(frameImg, null)));
                }
                try { Thread.sleep(30); } catch (InterruptedException e) { break; }
            }
        });
        videoThread.setDaemon(true);
        videoThread.start();

        captureBtn.setOnAction(e -> {
            BufferedImage finalImage = webcam.getImage();
            running.set(false);
            webcam.close();
            window.close();
            onCapture.accept(finalImage);
        });

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.show();
    }

    private boolean callPythonRecognition(String img1, String img2) {
        try {
            List<String> cmd = new java.util.ArrayList<>(PYTHON_CMD);
            cmd.add(SCRIPT_PATH);
            cmd.add(img1);
            cmd.add(img2);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().put("PYTHONIOENCODING", "UTF-8");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            boolean matchFound = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("[Python FaceID] " + line);
                if (line.contains("MATCH") && !line.contains("NO_MATCH")) {
                    matchFound = true;
                }
            }
            p.waitFor();
            return matchFound;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
