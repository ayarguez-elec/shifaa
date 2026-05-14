package services;

import java.io.File;
import java.io.FileWriter;

public class VoiceService {

    public static void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                // Escape quotes
                String cleanText = text.replace("\"", "").replace("'", "");
                
                // We use a small VBScript to trigger Windows native TTS
                File vbsFile = File.createTempFile("speech_", ".vbs");
                vbsFile.deleteOnExit();

                try (FileWriter fw = new FileWriter(vbsFile)) {
                    fw.write("Dim objVoice\n");
                    fw.write("Set objVoice = CreateObject(\"SAPI.SpVoice\")\n");
                    fw.write("objVoice.Speak \"" + cleanText + "\"\n");
                }

                Process process = Runtime.getRuntime().exec("cscript //NoLogo \"" + vbsFile.getAbsolutePath() + "\"");
                process.waitFor();
                
            } catch (Exception e) {
                System.out.println("Erreur de synthèse vocale : " + e.getMessage());
            }
        });
        
        thread.setDaemon(true);
        thread.start();
    }
}
