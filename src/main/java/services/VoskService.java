package services;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class VoskService {
    private static VoskService instance;
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine line;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isListening = false;

    private VoskService() {
        try {
            // Note: Le dossier "model" doit être à la racine du projet
            model = new Model("model");
        } catch (IOException e) {
            System.err.println("⚠️ Modèle Vosk non trouvé à la racine ('/model'). Reconnaissance vocale désactivée.");
        }
    }

    public static synchronized VoskService getInstance() {
        if (instance == null) {
            instance = new VoskService();
        }
        return instance;
    }

    public boolean isReady() {
        return model != null;
    }

    public void startListening(Consumer<String> onResult) {
        if (model == null || isListening) return;

        executor.execute(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    return;
                }

                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                recognizer = new Recognizer(model, 16000);
                isListening = true;

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (isListening && (bytesRead = line.read(buffer, 0, buffer.length)) > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String text = extractText(recognizer.getResult());
                        if (!text.isEmpty()) onResult.accept(text);
                    }
                }
                
                String finalText = extractText(recognizer.getFinalResult());
                if (!finalText.isEmpty()) onResult.accept(finalText);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stopListening();
            }
        });
    }

    public void stopListening() {
        isListening = false;
        if (line != null) {
            line.stop();
            line.close();
        }
    }

    private String extractText(String json) {
        if (json == null) return "";
        String marker = "\"text\" : \"";
        int start = json.indexOf(marker);
        if (start != -1) {
            start += marker.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return "";
    }
}
