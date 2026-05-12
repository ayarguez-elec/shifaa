package services;

import java.util.List;

public class GeminiModerationService {   // Keeping the same name to avoid changing controllers

    public ModerationResult checkReclamation(String subject, String description) {
        String fullText = (subject + " " + description).toLowerCase().trim();

        System.out.println("🔍 [LOCAL MODERATION] Checking: " + subject);

        boolean isHarmful = isHarmfulContent(fullText);

        if (isHarmful) {
            System.out.println("🚨 [LOCAL] Harmful content detected!");
            return new ModerationResult(
                    true,
                    0.95,
                    List.of("hate_speech"),
                    "Contenu haineux, raciste ou insultant détecté.",
                    "block"
            );
        } else {
            System.out.println("✅ [LOCAL] Content approved");
            return new ModerationResult(
                    false,
                    0.0,
                    List.of(),
                    "Contenu acceptable",
                    "allow"
            );
        }
    }

    private boolean isHarmfulContent(String text) {
        // Add more words/phrases as needed
        String[] harmfulKeywords = {
                "arabe de merde", "sale arabe", "tous les arabes", "terroristes",
                "sale noir", "tous les noirs", "juif de merde", "sale juif",
                "fils de pute", "enculé", "nique ta mère", "nique ta race",
                "pd", "pédé", "sale pédé", "homosexuel de merde",
                "putain de", "espèce de", "va te faire foutre"
        };

        for (String word : harmfulKeywords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}