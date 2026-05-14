package services;

import java.util.List;

public record ModerationResult(
        boolean isHarmful,
        double confidence,
        List<String> categories,
        String explanation,
        String suggestedAction
) {

    public static ModerationResult fromJson(String json) {
        try {
            boolean harmful = json.toLowerCase().contains("\"is_harmful\": true");
            String explanation = "Contenu inapproprié détecté.";

            if (json.contains("\"explanation\"")) {
                // Very basic extraction
                int start = json.indexOf("\"explanation\":") + 15;
                int end = json.indexOf("\"", start);
                if (end > start) {
                    explanation = json.substring(start, end).replace("\\n", "\n").trim();
                }
            }

            return new ModerationResult(harmful, 0.9, List.of(), explanation, harmful ? "block" : "allow");
        } catch (Exception e) {
            return new ModerationResult(false, 0.5, List.of(), "Erreur de modération", "allow");
        }
    }
}
