package api.handlers;

import api.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.ReservationServ;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  CheckInHandler — Sécurité & Vérification à l'entrée                 ║
 * ╠══════════════════════════════════════════════════════════════════════╣
 * ║  POST /checkin/{id}  → Valide la présence d'un participant          ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class CheckInHandler implements HttpHandler {

    private final ReservationServ service = new ReservationServ();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) { ApiUtils.sendOptions(ex); return; }

        if ("POST".equals(method) && path.matches("/checkin/\\d+")) {
            int id = Integer.parseInt(ApiUtils.pathSegment(ex, 1));
            handleCheckIn(ex, id);
        } else {
            ApiUtils.sendError(ex, 405, "Méthode non autorisée. Utilisez POST /checkin/{id}");
        }
    }

    private void handleCheckIn(HttpExchange ex, int id) throws IOException {
        boolean ok = service.checkIn(id);

        if (ok) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", id);
            resp.put("statut", "CHECKED_IN");
            resp.put("message", "Vérification réussie. Bienvenue à l'événement !");
            ApiUtils.sendJson(ex, 200, ApiUtils.toJson(resp));
        } else {
            ApiUtils.sendError(ex, 422, "Check-in impossible : la réservation #" + id + 
                " n'existe pas ou n'est pas confirmée.");
        }
    }
}
