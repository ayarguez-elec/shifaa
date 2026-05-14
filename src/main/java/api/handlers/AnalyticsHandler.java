package api.handlers;

import api.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.AnalyticsServ;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  AnalyticsHandler — API 3 : Analytics & Statistiques                ║
 * ╠══════════════════════════════════════════════════════════════════════╣
 * ║  GET  /analytics             → full report summary                 ║
 * ║  GET  /analytics/events      → event focus stats                   ║
 * ║  GET  /analytics/participants → participant behavior stats         ║
 * ║  GET  /analytics/seats       → occupancy & saturation stats         ║
 * ║  GET  /analytics/temporal    → time-based trend analysis           ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class AnalyticsHandler implements HttpHandler {

    private final AnalyticsServ service = new AnalyticsServ();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();

        // CORS
        if ("OPTIONS".equals(method)) { ApiUtils.sendOptions(ex); return; }

        if (!"GET".equals(method)) {
            ApiUtils.sendError(ex, 405, "Seule la méthode GET est supportée sur /analytics.");
            return;
        }

        try {
            if (path.equals("/analytics/events")) {
                ApiUtils.sendJson(ex, 200, ApiUtils.toJson(service.getEventStats()));

            } else if (path.equals("/analytics/participants")) {
                ApiUtils.sendJson(ex, 200, ApiUtils.toJson(service.getParticipantStats()));

            } else if (path.equals("/analytics/seats")) {
                ApiUtils.sendJson(ex, 200, ApiUtils.toJson(service.getSeatStats()));

            } else if (path.equals("/analytics/temporal")) {
                ApiUtils.sendJson(ex, 200, ApiUtils.toJson(service.getTemporalStats()));

            } else if (path.equals("/analytics")) {
                handleFullSummary(ex);

            } else {
                ApiUtils.sendError(ex, 404, "Route analytique inconnue : " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ [AnalyticsHandler] " + e.getMessage());
            ApiUtils.sendError(ex, 500, "Erreur interne : " + e.getMessage());
        }
    }

    private void handleFullSummary(HttpExchange ex) throws IOException {
        Map<String, Object> full = new LinkedHashMap<>();
        full.put("timestamp", System.currentTimeMillis());
        full.put("evenements", service.getEventStats());
        full.put("participants", service.getParticipantStats());
        full.put("places", service.getSeatStats());
        full.put("temporel", service.getTemporalStats());

        ApiUtils.sendJson(ex, 200, ApiUtils.toJson(full));
        System.out.println("[API] GET /analytics → Rapport complet généré");
    }
}
