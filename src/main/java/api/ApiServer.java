package api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  API 1 : Réservation & Validation                               ║
 * ║  Embedded HTTP server (built-in JDK, no extra dependency)       ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Routes:                                                         ║
 * ║   POST /reservation           → create & auto-process booking   ║
 * ║   GET  /event/{id}/places     → available seats for an event    ║
 * ║   GET  /reservation           → list all reservations (FIFO)    ║
 * ║   GET  /reservation/{id}      → get one reservation             ║
 * ║   DELETE /reservation/{id}    → cancel a reservation            ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class ApiServer {

    public static final int PORT = 8080;
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Register handlers
        server.createContext("/reservation",    new api.handlers.ReservationHandler());
        server.createContext("/event",          new api.handlers.PlacesHandler());
        server.createContext("/analytics",      new api.handlers.AnalyticsHandler());
        server.createContext("/checkin",        new api.handlers.CheckInHandler());
        server.createContext("/recommendations",new api.handlers.RecommendationHandler());

        // Thread pool for concurrent handling
        server.setExecutor(Executors.newFixedThreadPool(6));
        server.start();
        System.out.println("🚀 [API] Serveur démarré sur http://localhost:" + PORT);
        System.out.println("   POST   /reservation");
        System.out.println("   GET    /event/{id}/places");
        System.out.println("   GET    /recommendations/{participantId}");
        System.out.println("   GET    /recommendations/trending");
        System.out.println("   GET    /recommendations/popular");
        System.out.println("   GET    /recommendations/category/{cat}");
        System.out.println("   GET    /recommendations/history/{participantId}");
        System.out.println("   GET    /recommendations/stats");
        System.out.println("   POST   /recommendations/generate");
        System.out.println("   POST   /recommendations/click");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 [API] Serveur arrêté.");
        }
    }

    /**
     * Launch in a background daemon thread so it does not block JavaFX.
     */
    public static ApiServer launchAsync() {
        ApiServer api = new ApiServer();
        Thread t = new Thread(() -> {
            try {
                api.start();
            } catch (IOException e) {
                System.err.println("❌ [API] Impossible de démarrer le serveur : " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.setName("API-Server-Thread");
        t.start();
        return api;
    }
}
