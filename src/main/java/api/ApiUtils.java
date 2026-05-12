package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared utilities: JSON reader/writer, HTTP response helpers,
 * and a minimal JSON parser (no external library required).
 */
public class ApiUtils {

    // ── HTTP response helpers ─────────────────────────────────────────────────

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, "{\"erreur\":\"" + escapeJson(message) + "\",\"statut\":\"ERREUR\"}");
    }

    public static void sendOptions(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(204, -1);
    }

    // ── Body reader ───────────────────────────────────────────────────────────

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    // ── Minimal JSON object parser ────────────────────────────────────────────
    // Handles flat {"key":"value","key2":123} — sufficient for our API DTOs.

    public static Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;
        // Strip outer braces
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))  json = json.substring(0, json.length() - 1);
        // Split on commas that are outside quoted strings (simple version)
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");
                String val = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, val);
            }
        }
        return map;
    }

    // ── Minimal JSON builder ──────────────────────────────────────────────────

    public static String toJson(Map<String, Object> fields) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null)             sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else                       sb.append("\"").append(escapeJson(v.toString())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    // ── URL path extraction ───────────────────────────────────────────────────

    /**
     * Extracts the path segment at the given index (0-based, split by '/').
     * Example: /event/42/places  → index 2 → "places", index 1 → "42"
     */
    public static String pathSegment(HttpExchange ex, int index) {
        String[] parts = ex.getRequestURI().getPath().split("/");
        // parts[0] is always "" (before leading slash)
        int actual = index + 1;
        return (actual < parts.length) ? parts[actual] : "";
    }

    // ── Query param ───────────────────────────────────────────────────────────

    public static int parseIntOrDefault(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return def; }
    }

    // ── Escape special JSON characters ────────────────────────────────────────

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
