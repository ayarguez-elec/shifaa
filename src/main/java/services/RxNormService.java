package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.Normalizer;

public class RxNormService {

    private static final String RXNORM_URL = "https://rxnav.nlm.nih.gov/REST/drugs.json?name=";
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final HttpClient httpClient;

    public RxNormService() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public List<String> searchDrugNames(String drugName) {
        Set<String> drugNames = new LinkedHashSet<>();

        try {
            List<String> searchTerms = buildSearchTerms(drugName);

            if (searchTerms.isEmpty()) {
                return new ArrayList<>(drugNames);
            }

            for (String searchTerm : searchTerms) {
                drugNames.addAll(searchRxNormTerm(searchTerm));
            }

        } catch (Exception e) {
            System.out.println("Erreur recherche RxNorm : " + e.getMessage());
        }

        return new ArrayList<>(drugNames);
    }

    private List<String> buildSearchTerms(String drugName) {
        Set<String> terms = new LinkedHashSet<>();

        if (drugName == null || drugName.trim().isEmpty()) {
            return new ArrayList<>(terms);
        }

        String original = drugName.trim();
        String cleaned = original.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        String normalized = removeAccents(cleaned);

        terms.add(original);
        terms.add(cleaned);
        terms.add(normalized);

        String mappedName = mapFrenchDrugName(normalized);

        if (!mappedName.isEmpty()) {
            terms.add(mappedName);
        }

        return new ArrayList<>(terms);
    }

    private String mapFrenchDrugName(String normalizedDrugName) {
        if (normalizedDrugName == null || normalizedDrugName.trim().isEmpty()) {
            return "";
        }

        switch (normalizedDrugName.trim()) {
            case "doliprane":
            case "paracetamol":
            case "efferalgan":
            case "dafalgan":
                return "acetaminophen";

            case "advil":
            case "ibuprofene":
                return "ibuprofen";

            case "amoxicilline":
                return "amoxicillin";

            case "augmentin":
                return "amoxicillin clavulanate";

            case "aspirine":
                return "aspirin";

            case "metformine":
                return "metformin";

            case "omeprazole":
                return "omeprazole";

            case "cetirizine":
                return "cetirizine";

            case "loratadine":
                return "loratadine";

            case "azithromycine":
                return "azithromycin";

            case "clarithromycine":
                return "clarithromycin";

            default:
                return "";
        }
    }

    private String removeAccents(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private List<String> searchRxNormTerm(String searchTerm) {
        List<String> drugNames = new ArrayList<>();

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return drugNames;
            }

            String encodedName = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);
            URI uri = URI.create(RXNORM_URL + encodedName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Erreur RxNorm HTTP : " + response.statusCode());
                return drugNames;
            }

            return extractDrugNames(response.body());

        } catch (Exception e) {
            System.out.println("Erreur recherche RxNorm : " + e.getMessage());
            return drugNames;
        }
    }

    private List<String> extractDrugNames(String json) {
        Set<String> names = new LinkedHashSet<>();

        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>(names);
        }

        int searchIndex = 0;

        while (searchIndex < json.length()) {
            int keyIndex = json.indexOf("\"conceptProperties\"", searchIndex);

            if (keyIndex < 0) {
                break;
            }

            int arrayStart = json.indexOf('[', keyIndex);

            if (arrayStart < 0) {
                break;
            }

            int arrayEnd = findMatchingBracket(json, arrayStart);

            if (arrayEnd < 0) {
                break;
            }

            String conceptPropertiesJson = json.substring(arrayStart, arrayEnd + 1);
            addNamesFromConceptProperties(conceptPropertiesJson, names);

            searchIndex = arrayEnd + 1;
        }

        return new ArrayList<>(names);
    }

    private void addNamesFromConceptProperties(String json, Set<String> names) {
        Matcher matcher = NAME_PATTERN.matcher(json);

        while (matcher.find()) {
            String name = unescapeJson(matcher.group(1)).trim();

            if (!name.isEmpty()) {
                names.add(name);
            }
        }
    }

    private int findMatchingBracket(String text, int openingBracketIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = openingBracketIndex; i < text.length(); i++) {
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

            if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;

                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
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
