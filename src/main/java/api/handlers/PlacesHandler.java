package api.handlers;

import api.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.event;
import services.ReservationServ;
import services.eventServ;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  PlacesHandler — API 1 : Places disponibles                        ║
 * ╠══════════════════════════════════════════════════════════════════════╣
 * ║  GET  /event/{id}/places    → seats availability for one event     ║
 * ║  GET  /event                → list all events with their capacity  ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class PlacesHandler implements HttpHandler {

    private final ReservationServ resServ = new ReservationServ();
    private final eventServ        eServ  = new eventServ();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();

        // CORS preflight
        if ("OPTIONS".equals(method)) { ApiUtils.sendOptions(ex); return; }

        if (!"GET".equals(method)) {
            ApiUtils.sendError(ex, 405, "Seule la méthode GET est supportée sur /event.");
            return;
        }

        try {
            // ── GET /event/{id}/places ──────────────────────────────────────
            if (path.matches("/event/\\d+/places")) {
                int id = ApiUtils.parseIntOrDefault(ApiUtils.pathSegment(ex, 1), 0);
                handlePlaces(ex, id);

            // ── GET /event/{id} ─────────────────────────────────────────────
            } else if (path.matches("/event/\\d+")) {
                int id = ApiUtils.parseIntOrDefault(ApiUtils.pathSegment(ex, 1), 0);
                handleGetOne(ex, id);

            // ── GET /event ──────────────────────────────────────────────────
            } else if (path.equals("/event")) {
                handleGetAll(ex);

            } else {
                ApiUtils.sendError(ex, 404, "Route inconnue : " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ [PlacesHandler] " + e.getMessage());
            ApiUtils.sendError(ex, 500, "Erreur interne : " + e.getMessage());
        }
    }

    // ── GET /event/{id}/places ────────────────────────────────────────────────
    private void handlePlaces(HttpExchange ex, int id) throws IOException {
        event ev = eServ.getById(id);
        if (ev == null) {
            ApiUtils.sendError(ex, 404, "Événement introuvable (id=" + id + ").");
            return;
        }

        int capaciteMax      = ev.getCapaciteMax();
        int placesConfirmees = resServ.getPlacesConfirmees(id);
        int placesRestantes  = resServ.getPlacesRestantes(id);
        boolean complet      = (capaciteMax > 0 && placesRestantes <= 0);
        double tauxRemplissage = (capaciteMax > 0)
            ? Math.round((double) placesConfirmees / capaciteMax * 1000.0) / 10.0
            : 0.0;

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("event_id",          id);
        resp.put("titre",             ev.getTitre());
        resp.put("statut_evenement",  ev.getStatus());
        resp.put("capacite_max",      capaciteMax > 0 ? capaciteMax : "illimitée");
        resp.put("places_confirmees", placesConfirmees);
        resp.put("places_restantes",  capaciteMax > 0 ? placesRestantes : "∞");
        resp.put("taux_remplissage",  tauxRemplissage + "%");
        resp.put("complet",           complet);
        resp.put("accepte_reservations",
            !complet && !"Annulé".equalsIgnoreCase(ev.getStatus())
                     && !"Terminé".equalsIgnoreCase(ev.getStatus()));

        ApiUtils.sendJson(ex, 200, ApiUtils.toJson(resp));
        System.out.println("[API] GET /event/" + id + "/places → restantes=" + placesRestantes);
    }

    // ── GET /event/{id} ───────────────────────────────────────────────────────
    private void handleGetOne(HttpExchange ex, int id) throws IOException {
        event ev = eServ.getById(id);
        if (ev == null) {
            ApiUtils.sendError(ex, 404, "Événement introuvable (id=" + id + ").");
            return;
        }
        ApiUtils.sendJson(ex, 200, eventToJson(ev));
    }

    // ── GET /event ────────────────────────────────────────────────────────────
    private void handleGetAll(HttpExchange ex) throws IOException {
        List<event> events = eServ.getAll();

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (event ev : events) {
            if (!first) sb.append(",");
            first = false;

            // Enrich with capacity info
            int conf     = resServ.getPlacesConfirmees(ev.getId());
            int restantes = resServ.getPlacesRestantes(ev.getId());
            int cap      = ev.getCapaciteMax();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",                 ev.getId());
            m.put("titre",              ev.getTitre());
            m.put("type",               ev.getType());
            m.put("statut",             ev.getStatus());
            m.put("date",               ev.getDate() != null ? ev.getDate().toString() : null);
            m.put("lieu",               ev.getLieu());
            m.put("capacite_max",       cap > 0 ? cap : 0);
            m.put("places_confirmees",  conf);
            m.put("places_restantes",   cap > 0 ? restantes : -1);
            m.put("complet",            cap > 0 && restantes <= 0);
            sb.append(ApiUtils.toJson(m));
        }
        sb.append("]");

        String body = "{\"total\":" + events.size() + ",\"evenements\":" + sb + "}";
        ApiUtils.sendJson(ex, 200, body);
    }

    // ── Serialize event ───────────────────────────────────────────────────────
    private String eventToJson(event ev) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          ev.getId());
        m.put("titre",       ev.getTitre());
        m.put("type",        ev.getType());
        m.put("description", ev.getDescription());
        m.put("date",        ev.getDate() != null ? ev.getDate().toString() : null);
        m.put("time",        ev.getTime());
        m.put("lieu",        ev.getLieu());
        m.put("statut",      ev.getStatus());
        m.put("capacite_max", ev.getCapaciteMax());
        return ApiUtils.toJson(m);
    }
}
