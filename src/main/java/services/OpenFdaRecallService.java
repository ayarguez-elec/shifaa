package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OpenFdaRecallService {

    private static final String OPEN_FDA_URL = "https://api.fda.gov/drug/enforcement.json?search=";

    private final HttpClient httpClient;

    public OpenFdaRecallService() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public List<String> searchDrugRecalls(String productName) {
        Set<String> recalls = new LinkedHashSet<>();

        try {
            List<String> searchTerms = buildSearchTerms(productName);

            for (String term : searchTerms) {
                recalls.addAll(searchOpenFdaTerm(term));

                if (recalls.size() >= 5) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur recherche openFDA : " + e.getMessage());
        }

        return new ArrayList<>(recalls);
    }

    private List<String> buildSearchTerms(String productName) {
        Set<String> terms = new LinkedHashSet<>();

        if (productName == null || productName.trim().isEmpty()) {
            return new ArrayList<>(terms);
        }

        String original = productName.trim();
        String cleaned = cleanDrugName(original);
        String mappedName = mapFrenchDrugName(cleaned);

        if (!mappedName.isEmpty()) {
            terms.add(mappedName);
        }

        if (!cleaned.isEmpty()) {
            terms.add(cleaned);
        }

        return new ArrayList<>(terms);
    }

    private String cleanDrugName(String value) {
        String normalized = normalizeBasic(value);
        normalized = normalized.replaceAll("\\d+", " ");
        normalized = normalized.replaceAll("[^a-z\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }

    private String normalizeBasic(String value) {
        if (value == null) {
            return "";
        }

        String lowered = value.trim().toLowerCase(Locale.ROOT);
        String withoutAccents = removeAccents(lowered);
        return withoutAccents.replaceAll("\\s+", " ").trim();
    }

    private String mapFrenchDrugName(String cleanedProductName) {
        if (cleanedProductName == null || cleanedProductName.trim().isEmpty()) {
            return "";
        }

        String normalized = cleanedProductName.trim();

        if (containsAnyWord(normalized, "panadol", "panadol22", "doliprane", "paracetamol", "efferalgan", "dafalgan")) {
            return "acetaminophen";
        }

        if (containsAnyWord(normalized, "spasfon")) {
            return "phloroglucinol";
        }

        if (containsAnyWord(normalized, "ibuprofene", "advil")) {
            return "ibuprofen";
        }

        if (containsAnyWord(normalized, "amoxicilline")) {
            return "amoxicillin";
        }

        if (containsAnyWord(normalized, "augmentin")) {
            return "amoxicillin clavulanate";
        }

        if (containsAnyWord(normalized, "aspirine")) {
            return "aspirin";
        }

        if (containsAnyWord(normalized, "metformine")) {
            return "metformin";
        }

        if (containsAnyWord(normalized, "omeprazole")) {
            return "omeprazole";
        }

        return "";
    }

    private boolean containsAnyWord(String text, String... words) {
        String paddedText = " " + text + " ";

        for (String word : words) {
            if (paddedText.contains(" " + word + " ")) {
                return true;
            }
        }

        return false;
    }

    private String removeAccents(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private List<String> searchOpenFdaTerm(String term) {
        List<String> recalls = new ArrayList<>();

        try {
            if (term == null || term.trim().isEmpty()) {
                return recalls;
            }

            String cleanTerm = term.trim();
            String queryTerm = cleanTerm.contains(" ") ? "\"" + cleanTerm + "\"" : cleanTerm;
            String searchExpression = "product_description:" + queryTerm;
            String encodedSearch = URLEncoder.encode(searchExpression, StandardCharsets.UTF_8);
            String url = OPEN_FDA_URL + encodedSearch + "&limit=5";

            System.out.println("Recherche openFDA avec : " + cleanTerm);
            System.out.println("URL openFDA : " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();

            System.out.println("Code réponse openFDA : " + responseCode);

            if (responseCode == 404) {
                System.out.println("Aucun rappel trouvé pour ce médicament");
                return recalls;
            }

            if (responseCode != 200) {
                System.out.println("Erreur openFDA HTTP : " + responseCode);
                return recalls;
            }

            return extractRecalls(response.body());

        } catch (Exception e) {
            System.out.println("Erreur recherche openFDA : " + e.getMessage());
            return recalls;
        }
    }

    private List<String> extractRecalls(String json) {
        List<String> recalls = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return recalls;
        }

        int resultsIndex = json.indexOf("\"results\"");

        if (resultsIndex < 0) {
            return recalls;
        }

        int arrayStart = json.indexOf('[', resultsIndex);

        if (arrayStart < 0) {
            return recalls;
        }

        int arrayEnd = findMatching(json, arrayStart, '[', ']');

        if (arrayEnd < 0) {
            return recalls;
        }

        String resultsJson = json.substring(arrayStart + 1, arrayEnd);
        int searchIndex = 0;

        while (searchIndex < resultsJson.length()) {
            int objectStart = resultsJson.indexOf('{', searchIndex);

            if (objectStart < 0) {
                break;
            }

            int objectEnd = findMatching(resultsJson, objectStart, '{', '}');

            if (objectEnd < 0) {
                break;
            }

            String recallJson = resultsJson.substring(objectStart, objectEnd + 1);
            String formattedRecall = formatRecall(recallJson);

            if (!formattedRecall.isEmpty()) {
                recalls.add(formattedRecall);
            }

            searchIndex = objectEnd + 1;
        }

        return recalls;
    }

    private String formatRecall(String recallJson) {
        String productDescription = extractJsonString(recallJson, "product_description");
        String reason = extractJsonString(recallJson, "reason_for_recall");
        String status = extractJsonString(recallJson, "status");
        String classification = extractJsonString(recallJson, "classification");
        String recallDate = formatDate(extractJsonString(recallJson, "recall_initiation_date"));

        if (productDescription.isEmpty() && reason.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Produit : ").append(defaultValue(productDescription)).append("\n");
        builder.append("Raison : ").append(defaultValue(reason)).append("\n");
        builder.append("Statut : ").append(defaultValue(status)).append("\n");
        builder.append("Classification : ").append(defaultValue(classification)).append("\n");
        builder.append("Date de rappel : ").append(defaultValue(recallDate));

        return builder.toString();
    }

    private String extractJsonString(String json, String key) {
        String keyPattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(keyPattern);

        if (keyIndex < 0) {
            return "";
        }

        int colonIndex = json.indexOf(':', keyIndex + keyPattern.length());

        if (colonIndex < 0) {
            return "";
        }

        int valueStart = colonIndex + 1;

        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return "";
        }

        if (json.charAt(valueStart) == '"') {
            int valueEnd = findStringEnd(json, valueStart);

            if (valueEnd > valueStart) {
                return unescapeJson(json.substring(valueStart + 1, valueEnd)).trim();
            }
        }

        return "";
    }

    private int findStringEnd(String text, int quoteIndex) {
        boolean escaped = false;

        for (int i = quoteIndex + 1; i < text.length(); i++) {
            char current = text.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            if (current == '"') {
                return i;
            }
        }

        return -1;
    }

    private int findMatching(String text, int openingIndex, char openingChar, char closingChar) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = openingIndex; i < text.length(); i++) {
            char current = text.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (current == openingChar) {
                depth++;
            } else if (current == closingChar) {
                depth--;

                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String formatDate(String value) {
        if (value == null || value.trim().length() != 8) {
            return value == null ? "" : value.trim();
        }

        String date = value.trim();
        return date.substring(6, 8) + "/" + date.substring(4, 6) + "/" + date.substring(0, 4);
    }

    private String defaultValue(String value) {
        return value == null || value.trim().isEmpty() ? "Non renseigne" : value.trim();
    }

    private String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (current != '\\' || i + 1 >= value.length()) {
                result.append(current);
                continue;
            }

            char next = value.charAt(++i);

            switch (next) {
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '/':
                    result.append('/');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);

                        try {
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            result.append("\\u").append(hex);
                            i += 4;
                        }
                    } else {
                        result.append("\\u");
                    }
                    break;
                default:
                    result.append(next);
                    break;
            }
        }

        return result.toString();
    }
}
