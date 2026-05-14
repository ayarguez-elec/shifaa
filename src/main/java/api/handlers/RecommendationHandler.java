package api.handlers;

import api.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.RecommendedEvent;
import services.RecommendationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST handler for the Intelligent Recommendation API.
 *
 * GET  /recommendations/{participantId}          → personalized recs
 * GET  /recommendations/trending                 → trending events
 * GET  /recommendations/popular                  → most popular events
 * GET  /recommendations/category/{category}      → by category
 * GET  /recommendations/history/{participantId}  → recommendation history
 * GET  /recommendations/stats                    → global analytics
 * POST /recommendations/generate                 → generate + persist recs (body: {"participantId":5,"limit":10})
 * POST /recommendations/click                    → track click (body: {"participantId":5,"eventId":3})
 */
public class RecommendationHandler implements HttpHandler {

    private final RecommendationService service = new RecommendationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ApiUtils.sendOptions(ex);
            return;
        }

        String method  = ex.getRequestMethod();
        String seg1    = ApiUtils.pathSegment(ex, 1); // e.g. "trending", "5", "category", "history", "stats"
        String seg2    = ApiUtils.pathSegment(ex, 2); // e.g. category name or participantId after "history"

        try {
            if ("GET".equals(method)) {
                switch (seg1) {
                    case "trending":  handleTrending(ex);          break;
                    case "popular":   handlePopular(ex);            break;
                    case "stats":     handleStats(ex);              break;
                    case "category":  handleByCategory(ex, seg2);  break;
                    case "history":   handleHistory(ex, seg2);     break;
                    default:          handlePersonalized(ex, seg1); break;
                }
            } else if ("POST".equals(method)) {
                switch (seg1) {
                    case "generate": handleGenerate(ex);  break;
                    case "click":    handleClick(ex);     break;
                    default: ApiUtils.sendError(ex, 404, "Endpoint inconnu: POST /recommendations/" + seg1);
                }
            } else {
                ApiUtils.sendError(ex, 405, "Méthode non supportée: " + method);
            }
        } catch (Exception e) {
            System.err.println("❌ [RecommendationAPI] " + e.getMessage());
            ApiUtils.sendError(ex, 500, "Erreur interne: " + e.getMessage());
        }
    }

    // ── GET /recommendations/{participantId} ──────────────────────────────────
    private void handlePersonalized(HttpExchange ex, String idStr) throws IOException {
        int participantId = ApiUtils.parseIntOrDefault(idStr, -1);
        if (participantId < 1) {
            ApiUtils.sendError(ex, 400, "participantId invalide: " + idStr);
            return;
        }
        int limit = queryInt(ex, "limit", 10);
        List<RecommendedEvent> recs = service.getPersonalized(participantId, limit);
        service.refreshPreferences(participantId);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"participantId\":").append(participantId)
          .append(",\"count\":").append(recs.size())
          .append(",\"generatedAt\":\"").append(LocalDateTime.now()).append("\"")
          .append(",\"algorithm\":\"ContentBased+Collaborative\"")
          .append(",\"recommendations\":[");

        boolean first = true;
        for (RecommendedEvent r : recs) {
            if (!first) sb.append(",");
            first = false;
            appendEventRec(sb, r);
        }
        sb.append("]}");
        ApiUtils.sendJson(ex, 200, sb.toString());
    }

    // ── GET /recommendations/trending ────────────────────────────────────────
    private void handleTrending(HttpExchange ex) throws IOException {
        int limit = queryInt(ex, "limit", 10);
        List<Map<String, Object>> list = service.getTrending(limit);
        ApiUtils.sendJson(ex, 200,
            "{\"count\":" + list.size() +
            ",\"generatedAt\":\"" + LocalDateTime.now() + "\"" +
            ",\"trending\":" + mapsToJson(list) + "}");
    }

    // ── GET /recommendations/popular ─────────────────────────────────────────
    private void handlePopular(HttpExchange ex) throws IOException {
        int limit = queryInt(ex, "limit", 10);
        List<Map<String, Object>> list = service.getPopular(limit);
        ApiUtils.sendJson(ex, 200,
            "{\"count\":" + list.size() +
            ",\"generatedAt\":\"" + LocalDateTime.now() + "\"" +
            ",\"popular\":" + mapsToJson(list) + "}");
    }

    // ── GET /recommendations/category/{category} ──────────────────────────────
    private void handleByCategory(HttpExchange ex, String category) throws IOException {
        if (category == null || category.isBlank()) {
            ApiUtils.sendError(ex, 400, "Catégorie manquante dans l'URL");
            return;
        }
        int limit = queryInt(ex, "limit", 20);
        // URL decode basic %20 → space
        String cat = category.replace("%20", " ").replace("+", " ");
        List<Map<String, Object>> list = service.getByCategory(cat, limit);
        ApiUtils.sendJson(ex, 200,
            "{\"category\":\"" + ApiUtils.escapeJson(cat) + "\"" +
            ",\"count\":" + list.size() +
            ",\"events\":" + mapsToJson(list) + "}");
    }

    // ── GET /recommendations/history/{participantId} ──────────────────────────
    private void handleHistory(HttpExchange ex, String idStr) throws IOException {
        int participantId = ApiUtils.parseIntOrDefault(idStr, -1);
        if (participantId < 1) {
            ApiUtils.sendError(ex, 400, "participantId invalide: " + idStr);
            return;
        }
        int limit = queryInt(ex, "limit", 50);
        List<Map<String, Object>> list = service.getHistory(participantId, limit);
        ApiUtils.sendJson(ex, 200,
            "{\"participantId\":" + participantId +
            ",\"count\":" + list.size() +
            ",\"history\":" + mapsToJson(list) + "}");
    }

    // ── GET /recommendations/stats ────────────────────────────────────────────
    private void handleStats(HttpExchange ex) throws IOException {
        Map<String, Object> stats = service.getStatistics();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : stats.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            String key = e.getKey();
            Object val = e.getValue();
            if (key.startsWith("__") && key.endsWith("Json")) {
                // embed raw JSON (categoryDistributionJson)
                String cleanKey = key.replace("__","").replace("Json","");
                sb.append("\"").append(ApiUtils.escapeJson(cleanKey)).append("\":")
                  .append(val);
            } else {
                sb.append("\"").append(ApiUtils.escapeJson(key)).append("\":");
                if (val instanceof Number || val instanceof Boolean) sb.append(val);
                else sb.append("\"").append(ApiUtils.escapeJson(String.valueOf(val))).append("\"");
            }
        }
        sb.append("}");
        ApiUtils.sendJson(ex, 200, sb.toString());
    }

    // ── POST /recommendations/generate ───────────────────────────────────────
    private void handleGenerate(HttpExchange ex) throws IOException {
        Map<String, String> body = ApiUtils.parseJsonObject(ApiUtils.readBody(ex));
        int participantId = ApiUtils.parseIntOrDefault(body.get("participantId"), -1);
        int limit         = ApiUtils.parseIntOrDefault(body.get("limit"), 10);
        if (participantId < 1) {
            ApiUtils.sendError(ex, 400, "participantId requis dans le body JSON");
            return;
        }
        List<RecommendedEvent> recs = service.getPersonalized(participantId, limit);
        service.refreshPreferences(participantId);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"statut\":\"GENERE\"")
          .append(",\"participantId\":").append(participantId)
          .append(",\"generatedCount\":").append(recs.size())
          .append(",\"generatedAt\":\"").append(LocalDateTime.now()).append("\"")
          .append(",\"recommendations\":[");
        boolean first = true;
        for (RecommendedEvent r : recs) {
            if (!first) sb.append(",");
            first = false;
            appendEventRec(sb, r);
        }
        sb.append("]}");
        ApiUtils.sendJson(ex, 201, sb.toString());
    }

    // ── POST /recommendations/click ───────────────────────────────────────────
    private void handleClick(HttpExchange ex) throws IOException {
        Map<String, String> body = ApiUtils.parseJsonObject(ApiUtils.readBody(ex));
        int participantId = ApiUtils.parseIntOrDefault(body.get("participantId"), -1);
        int eventId       = ApiUtils.parseIntOrDefault(body.get("eventId"), -1);
        if (participantId < 1 || eventId < 1) {
            ApiUtils.sendError(ex, 400, "participantId et eventId requis");
            return;
        }
        service.markClicked(participantId, eventId);
        ApiUtils.sendJson(ex, 200, "{\"statut\":\"CLICK_ENREGISTRE\",\"participantId\":" + participantId + ",\"eventId\":" + eventId + "}");
    }

    // ── JSON serialization helpers ────────────────────────────────────────────

    private void appendEventRec(StringBuilder sb, RecommendedEvent r) {
        entities.event ev = r.getEvent();
        sb.append("{")
          .append("\"eventId\":").append(ev.getId()).append(",")
          .append("\"title\":\"").append(ApiUtils.escapeJson(ev.getTitre())).append("\",")
          .append("\"type\":\"").append(ApiUtils.escapeJson(ev.getType())).append("\",")
          .append("\"date\":\"").append(ev.getDate() != null ? ev.getDate().toString() : "").append("\",")
          .append("\"time\":\"").append(ApiUtils.escapeJson(ev.getTime() != null ? ev.getTime() : "")).append("\",")
          .append("\"lieu\":\"").append(ApiUtils.escapeJson(ev.getLieu() != null ? ev.getLieu() : "")).append("\",")
          .append("\"status\":\"").append(ApiUtils.escapeJson(ev.getStatus() != null ? ev.getStatus() : "")).append("\",")
          .append("\"capaciteMax\":").append(ev.getCapaciteMax()).append(",")
          .append("\"score\":").append(r.getScore()).append(",")
          .append("\"reason\":\"").append(ApiUtils.escapeJson(r.getReason())).append("\"")
          .append("}");
    }

    private String mapsToJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> m : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            boolean f2 = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!f2) sb.append(",");
                f2 = false;
                sb.append("\"").append(ApiUtils.escapeJson(e.getKey())).append("\":");
                Object v = e.getValue();
                if (v instanceof Number || v instanceof Boolean) sb.append(v);
                else sb.append("\"").append(ApiUtils.escapeJson(String.valueOf(v))).append("\"");
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private int queryInt(HttpExchange ex, String param, int def) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return def;
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) return ApiUtils.parseIntOrDefault(kv[1], def);
        }
        return def;
    }
}
