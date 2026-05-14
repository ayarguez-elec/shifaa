package api.handlers;

import api.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Reservation;
import services.NotificationService;
import services.ReservationServ;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  ReservationHandler — API 1 : Réservation & Validation             ║
 * ╠══════════════════════════════════════════════════════════════════════╣
 * ║  POST   /reservation         → create & auto-process               ║
 * ║  GET    /reservation         → list all (FIFO order)               ║
 * ║  GET    /reservation/{id}    → not implemented (extensible)        ║
 * ║  DELETE /reservation/{id}    → cancel a confirmed reservation      ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * Expected JSON body for POST /reservation:
 * {
 *   "event_id"       : 1,
 *   "participant_id" : 3,
 *   "nombre_places"  : 2
 * }
 */
public class ReservationHandler implements HttpHandler {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ReservationServ    service     = new ReservationServ();
    private final NotificationService notifService = new NotificationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath(); // e.g. /reservation or /reservation/5

        // CORS preflight
        if ("OPTIONS".equals(method)) { ApiUtils.sendOptions(ex); return; }

        try {
            // ── POST /reservation ───────────────────────────────────────────
            if ("POST".equals(method) && path.equals("/reservation")) {
                handlePost(ex);

            // ── GET /reservation ────────────────────────────────────────────
            } else if ("GET".equals(method) && path.equals("/reservation")) {
                handleGetAll(ex);

            // ── GET /reservation/{id}/qrcode ────────────────────────────────
            } else if ("GET".equals(method) && path.matches("/reservation/\\d+/qrcode")) {
                int id = Integer.parseInt(ApiUtils.pathSegment(ex, 1));
                handleQrCode(ex, id);

            // ── GET /notification/{id}/resend ───────────────────────────────
            } else if ("GET".equals(method) && path.matches("/reservation/\\d+/resend")) {
                int id = Integer.parseInt(ApiUtils.pathSegment(ex, 1));
                handleResend(ex, id);

            // ── DELETE /reservation/{id} ────────────────────────────────────
            } else if ("DELETE".equals(method) && path.matches("/reservation/\\d+")) {
                int id = Integer.parseInt(ApiUtils.pathSegment(ex, 1));
                handleDelete(ex, id);

            } else {
                ApiUtils.sendError(ex, 405, "Méthode ou route non supportée : " + method + " " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ [ReservationHandler] " + e.getMessage());
            ApiUtils.sendError(ex, 500, "Erreur interne : " + e.getMessage());
        }
    }

    // ── POST /reservation ────────────────────────────────────────────────────
    private void handlePost(HttpExchange ex) throws IOException {
        String body = ApiUtils.readBody(ex);
        Map<String, String> params = ApiUtils.parseJsonObject(body);

        int eventId       = ApiUtils.parseIntOrDefault(params.get("event_id"),       0);
        int participantId = ApiUtils.parseIntOrDefault(params.get("participant_id"),  0);
        int nombrePlaces  = ApiUtils.parseIntOrDefault(params.get("nombre_places"),   1);

        // Basic input validation
        if (eventId <= 0 || participantId <= 0) {
            ApiUtils.sendError(ex, 400,
                "Champs requis manquants ou invalides : event_id=" + eventId +
                ", participant_id=" + participantId);
            return;
        }

        // ── Trigger the Smart Booking Engine ────────────────────────────────
        Reservation res = service.soumettreDemande(eventId, participantId, nombrePlaces);

        // ── Build response ───────────────────────────────────────────────────
        boolean accepted = res.getStatut() == Reservation.Statut.CONFIRME;
        int httpStatus   = accepted ? 201 : 422; // 201 Created | 422 Unprocessable

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",              res.getId());
        resp.put("statut",          res.getStatut().name());
        resp.put("accepte",         accepted);
        resp.put("event_id",        res.getEventId());
        resp.put("participant_id",  res.getParticipantId());
        resp.put("nombre_places",   res.getNombrePlaces());
        resp.put("places_restantes", service.getPlacesRestantes(eventId) == Integer.MAX_VALUE
                                     ? -1 : service.getPlacesRestantes(eventId));
        resp.put("message",         accepted
                    ? "Réservation confirmée. " + nombrePlaces + " place(s) accordée(s)."
                    : "Demande refusée. " + res.getMotifRefus());
        if (!accepted && res.getMotifRefus() != null) {
            resp.put("motif_refus", res.getMotifRefus());
        }
        resp.put("date_demande",    res.getDateDemande() != null
                                    ? res.getDateDemande().format(FMT) : null);

        ApiUtils.sendJson(ex, httpStatus, ApiUtils.toJson(resp));

        System.out.printf("[API] POST /reservation → %s (event=%d, part=%d, places=%d)%n",
            res.getStatut().name(), eventId, participantId, nombrePlaces);
    }

    // ── GET /reservation ─────────────────────────────────────────────────────
    private void handleGetAll(HttpExchange ex) throws IOException {
        List<Reservation> all = service.getAll();

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Reservation r : all) {
            if (!first) sb.append(",");
            first = false;
            sb.append(reservationToJson(r));
        }
        sb.append("]");

        // Wrap in envelope
        String body = "{\"total\":" + all.size() + ",\"reservations\":" + sb + "}";
        ApiUtils.sendJson(ex, 200, body);
    }

    // ── GET /reservation/{id}/qrcode ─────────────────────────────────
    private void handleQrCode(HttpExchange ex, int id) throws IOException {
        // Look up the QR path stored in DB for this reservation
        List<Reservation> all = service.getAll();
        Reservation res = all.stream().filter(r -> r.getId() == id).findFirst().orElse(null);

        if (res == null) {
            ApiUtils.sendError(ex, 404, "Réservation introuvable (id=" + id + ").");
            return;
        }
        if (res.getStatut() != Reservation.Statut.CONFIRME) {
            ApiUtils.sendError(ex, 422, "QR code uniquement disponible pour les réservations CONFIRMÉES.");
            return;
        }

        // Resolve QR file path
        services.QrCodeService qrSvc = new services.QrCodeService();
        String qrPath = qrSvc.getQrCodePath(id);

        if (qrPath == null) {
            ApiUtils.sendError(ex, 404, "QR code non encore généré pour cette réservation.");
            return;
        }

        // Stream the PNG file
        java.io.File file = new java.io.File(qrPath);
        if (!file.exists()) {
            ApiUtils.sendError(ex, 404, "Fichier QR introuvable sur le serveur.");
            return;
        }

        byte[] imageBytes = Files.readAllBytes(Paths.get(qrPath));
        ex.getResponseHeaders().set("Content-Type", "image/png");
        ex.getResponseHeaders().set("Content-Disposition",
            "attachment; filename=\"qrcode_reservation_" + id + ".png\"");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, imageBytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(imageBytes); }

        System.out.println("[API] GET /reservation/" + id + "/qrcode → PNG servi");
    }

    // ── GET /reservation/{id}/resend ────────────────────────────────
    private void handleResend(HttpExchange ex, int id) throws IOException {
        List<Reservation> all = service.getAll();
        Reservation res = all.stream().filter(r -> r.getId() == id).findFirst().orElse(null);

        if (res == null) {
            ApiUtils.sendError(ex, 404, "Réservation introuvable (id=" + id + ").");
            return;
        }

        // Resend notification synchronously
        notifService.resendNotification(res);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",      id);
        resp.put("statut",  res.getStatut().name());
        resp.put("message", "Notification renvoyée pour la réservation #" + id);
        ApiUtils.sendJson(ex, 200, ApiUtils.toJson(resp));
        System.out.println("[API] GET /reservation/" + id + "/resend → notification renvoyée");
    }

    // ── DELETE /reservation/{id} ─────────────────────────────────────────────
    private void handleDelete(HttpExchange ex, int id) throws IOException {
        boolean ok = service.annulerReservation(id);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",      id);
        resp.put("annule",  ok);
        resp.put("message", ok
            ? "Réservation #" + id + " annulée avec succès."
            : "Impossible d'annuler : réservation introuvable ou déjà annulée/refusée.");
        ApiUtils.sendJson(ex, ok ? 200 : 404, ApiUtils.toJson(resp));
    }

    // ── Helper: serialize one Reservation to JSON ────────────────────────────
    private String reservationToJson(Reservation r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              r.getId());
        m.put("event_id",        r.getEventId());
        m.put("participant_id",  r.getParticipantId());
        m.put("nombre_places",   r.getNombrePlaces());
        m.put("statut",          r.getStatut().name());
        m.put("date_demande",    r.getDateDemande() != null ? r.getDateDemande().format(FMT) : null);
        m.put("motif_refus",     r.getMotifRefus());
        return ApiUtils.toJson(m);
    }
}
