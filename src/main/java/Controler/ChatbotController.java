package Controler;

import entities.HealthLog;
import entities.user;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import services.HealthLogService;
import services.userServices;
import tests.Main;

import java.sql.SQLException;

public class ChatbotController {

    @FXML private VBox messageContainer;
    @FXML private VBox optionsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox chatRoot;

    private user currentUser;
    private userServices userService = new userServices();
    private HealthLogService healthService = new HealthLogService();

    @FXML
    public void initialize() {
        loadUser();
        startFriendlyConversation();
    }

    private void loadUser() {
        try {
            int id = Main.getCurrentUserId();
            for (user u : userService.getAll()) {
                if (u.getUserId() == id) { this.currentUser = u; break; }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void startFriendlyConversation() {
        String name = (currentUser != null) ? currentUser.getFirstName() : "Patient";
        addBotMessage("Bonjour " + name + ". Je suis votre assistant Shifaa.");
        addDelayedBotMessage("Comment puis-je vous aider aujourd'hui ?", 1.0, () -> {
            showOptions(
                new Option("Faire mon suivi de routine", () -> handleRoutine()),
                new Option("Signaler un nouveau symptôme", () -> handleNewSymptom())
            );
        });
    }

    // --- SUIVI DE ROUTINE ---

    private void handleRoutine() {
        addUserMessage("Je souhaite faire mon suivi.");
        addDelayedBotMessage("C'est une excellente habitude. Comment s'est passée votre nuit ?", 1.0, () -> {
            showOptions(
                new Option("J'ai très bien dormi", () -> handleRoutineStep2(true)),
                new Option("Mon sommeil était agité", () -> handleRoutineStep2(false))
            );
        });
    }

    private void handleRoutineStep2(boolean goodSleep) {
        addUserMessage(goodSleep ? "Très bien dormi" : "Sommeil agité");
        String sleepResult = goodSleep ? "Sommeil: Excellent" : "Sommeil: Agité";
        
        addDelayedBotMessage("Avez-vous bien pris votre traitement habituel ?", 1.0, () -> {
            showOptions(
                new Option("Oui, c'est fait", () -> {
                    addUserMessage("Traitement pris");
                    saveLog("Routine", sleepResult + " | Traitement: Pris", 1);
                    addBotMessage("Très bien. Tout semble sous contrôle. Données enregistrées dans votre tableau de bord.");
                    showOptions(new Option("Merci, à bientôt", () -> closeChat()));
                }),
                new Option("Pas encore", () -> {
                    addUserMessage("Pas encore");
                    saveLog("Routine", sleepResult + " | Traitement: Non pris", 3);
                    addBotMessage("Pensez à le prendre dès que possible. Données enregistrées.");
                    showOptions(new Option("C'est noté", () -> closeChat()));
                })
            );
        });
    }

    // --- SYMPTÔMES ---

    private void handleNewSymptom() {
        addUserMessage("J'ai un nouveau symptôme");
        addDelayedBotMessage("D'accord. Quelle zone vous préoccupe principalement ?", 0.8, () -> {
            showOptions(
                new Option("Tête ou visage", () -> assessIntensity("Neurologue", "Tête")),
                new Option("Poitrine ou respiration", () -> handleEmergency()),
                new Option("Ventre ou digestion", () -> assessIntensity("Gastro-entérologue", "Ventre")),
                new Option("Dos ou articulations", () -> assessIntensity("Orthopédiste", "Articulations"))
            );
        });
    }

    private void assessIntensity(String specialty, String zone) {
        addUserMessage("Évaluation de la douleur: " + zone);
        addDelayedBotMessage("Sur une échelle de 1 à 10, comment évaluez-vous cette gêne ?", 1.0, () -> {
            showOptions(
                new Option("Faible (1-3)", () -> finalizeTriage(specialty, zone, 2)),
                new Option("Moyenne (4-6)", () -> finalizeTriage(specialty, zone, 5)),
                new Option("Forte (7-10)", () -> finalizeTriage(specialty, zone, 9))
            );
        });
    }

    private void finalizeTriage(String specialty, String zone, int severity) {
        addUserMessage("Intensité: " + severity + "/10");
        saveLog("Symptom", "Douleur " + zone + " (Intensité: " + severity + ")", severity);
        
        if (severity >= 7) {
            addDelayedBotMessage("Cette douleur semble importante. Je vous conseille de prendre rendez-vous rapidement avec un " + specialty + ".", 1.0, () -> {
                showOptions(new Option("Prendre rendez-vous", () -> { Main.showPatientDoctors(); closeChat(); }));
            });
        } else {
            addDelayedBotMessage("Données enregistrées. Un suivi avec un " + specialty + " est conseillé si cela persiste.", 1.0, () -> {
                showOptions(
                    new Option("Prendre rendez-vous", () -> { Main.showPatientDoctors(); closeChat(); }),
                    new Option("Fermer l'assistant", () -> closeChat())
                );
            });
        }
    }

    private void handleEmergency() {
        addUserMessage("Poitrine / Respiration");
        saveLog("Symptom", "URGENCE : Douleur thoracique", 10);
        chatRoot.setStyle(chatRoot.getStyle() + "; -fx-border-color: #EF4444; -fx-border-width: 2;");
        addDelayedBotMessage("ALERTE : Une douleur thoracique nécessite une attention immédiate.", 0.5, () -> {
            addDelayedBotMessage("Veuillez contacter immédiatement le 190 (SAMU Tunisie).", 1.0, () -> {
                addDelayedBotMessage("Ne restez pas seul et attendez les secours.", 0.8, () -> {
                    showOptions(new Option("Bien reçu", () -> closeChat()));
                });
            });
        });
    }

    private void saveLog(String type, String result, int severity) {
        if (currentUser != null) {
            try {
                HealthLog log = new HealthLog(currentUser.getUserId(), type, result, severity);
                healthService.add(log);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- UI ENGINE (CLEAN) ---

    private void addDelayedBotMessage(String text, double delay, Runnable onFinished) {
        HBox typing = new HBox(new Label("..."));
        typing.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 5 10; -fx-background-radius: 10; -fx-text-fill: #94A3B8;");
        messageContainer.getChildren().add(typing);
        scrollToBottom();

        new Timeline(new KeyFrame(Duration.seconds(delay), e -> {
            messageContainer.getChildren().remove(typing);
            addBotMessage(text);
            if (onFinished != null) onFinished.run();
        })).play();
    }

    private void addBotMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 10 12; -fx-background-radius: 15 15 15 5; -fx-text-fill: #334155; -fx-max-width: 240; -fx-font-size: 13;");
        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER_LEFT);
        messageContainer.getChildren().add(hbox);
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: #1E293B; -fx-padding: 8 10; -fx-background-radius: 15 15 5 15; -fx-text-fill: white; -fx-max-width: 240; -fx-font-size: 12;");
        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.getChildren().add(hbox);
        scrollToBottom();
    }

    private void showOptions(Option... options) {
        optionsContainer.getChildren().clear();
        for (Option opt : options) {
            Button btn = new Button(opt.text);
            btn.setPrefWidth(280);
            btn.setAlignment(Pos.CENTER);
            btn.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand; -fx-text-fill: #475569; -fx-font-size: 12;");
            btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "; -fx-background-color: #F8FAFC; -fx-border-color: #64748B;"));
            btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: #F8FAFC; -fx-border-color: #64748B;", "")));
            btn.setOnAction(e -> opt.action.run());
            optionsContainer.getChildren().add(btn);
        }
    }

    private void scrollToBottom() { Platform.runLater(() -> scrollPane.setVvalue(1.0)); }

    @FXML private void closeChat() {
        if (chatRoot.getParent() instanceof AnchorPane) {
            ((AnchorPane)chatRoot.getParent()).getChildren().remove(chatRoot);
        }
    }

    private static class Option {
        String text; Runnable action;
        Option(String t, Runnable a) { this.text = t; this.action = a; }
    }
}
