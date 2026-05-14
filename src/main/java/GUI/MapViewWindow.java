package GUI;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapViewWindow {

    private static final Map<String, GeoPoint> LOCAL_POINTS = new HashMap<>();

    static {
        LOCAL_POINTS.put("tunis", new GeoPoint(36.8065, 10.1815));
        LOCAL_POINTS.put("tunis centre", new GeoPoint(36.8008, 10.1800));
        LOCAL_POINTS.put("avenue habib bourguiba tunis", new GeoPoint(36.8008, 10.1800));
        LOCAL_POINTS.put("pharmacie centrale rue de marseille tunis", new GeoPoint(36.8002, 10.1845));
        LOCAL_POINTS.put("para2 avenue habib bourguiba tunis", new GeoPoint(36.8008, 10.1800));

        LOCAL_POINTS.put("ariana", new GeoPoint(36.8665, 10.1647));
        LOCAL_POINTS.put("ariana centre", new GeoPoint(36.8665, 10.1647));
        LOCAL_POINTS.put("ariana soghra", new GeoPoint(36.8625, 10.1956));

        LOCAL_POINTS.put("pharmacie ariana soghra ariana soghra ariana", new GeoPoint(36.8650, 10.1990));
        LOCAL_POINTS.put("pharmacie de nuit ariana soghra ariana soghra ariana", new GeoPoint(36.8680, 10.2010));
        LOCAL_POINTS.put("pharmacie de nuit ariana ariana centre ariana", new GeoPoint(36.8665, 10.1647));
        LOCAL_POINTS.put("pharmacie el yasmine ariana cite ennasr 1 ariana", new GeoPoint(36.8610, 10.1640));
        LOCAL_POINTS.put("pharmacie ennasr sante avenue hedi nouira ennasr 2 ariana", new GeoPoint(36.8620, 10.1680));
        LOCAL_POINTS.put("pharmacie riadh andalous riadh andalous ariana", new GeoPoint(36.8660, 10.1870));
        LOCAL_POINTS.put("pharmacie garde ariana ariana centre ariana", new GeoPoint(36.8665, 10.1647));

        LOCAL_POINTS.put("sousse", new GeoPoint(35.8256, 10.6370));
        LOCAL_POINTS.put("sousse centre", new GeoPoint(35.8256, 10.6370));
        LOCAL_POINTS.put("pharmacie el amal rue de la republique sousse", new GeoPoint(35.8260, 10.6375));

        LOCAL_POINTS.put("sfax", new GeoPoint(34.7406, 10.7603));
        LOCAL_POINTS.put("sfax centre", new GeoPoint(34.7406, 10.7603));
        LOCAL_POINTS.put("pharmacie garde sfax route de tunis sfax", new GeoPoint(34.7600, 10.7600));

        LOCAL_POINTS.put("monastir", new GeoPoint(35.7643, 10.8113));
        LOCAL_POINTS.put("monastir centre", new GeoPoint(35.7643, 10.8113));
        LOCAL_POINTS.put("pharmacie sante plus monastir centre monastir", new GeoPoint(35.7648, 10.8120));

        LOCAL_POINTS.put("kairouan", new GeoPoint(35.6781, 10.0963));
        LOCAL_POINTS.put("kairouan centre", new GeoPoint(35.6781, 10.0963));
    }

    public static void showMap(String pharmacyName, String pharmacyAddress) {
        try {
            String destinationQuery = prepareAddress(pharmacyName + " " + pharmacyAddress);

            Stage loadingStage = createLoadingStage("Chargement de la carte...");
            loadingStage.show();

            Thread thread = new Thread(() -> {
                GeoPoint destinationPoint = geocode(destinationQuery);

                Platform.runLater(() -> {
                    loadingStage.close();

                    if (destinationPoint == null) {
                        showError("Localisation impossible", "Impossible de trouver cette adresse :\n" + destinationQuery);
                        return;
                    }

                    openSinglePointMap(pharmacyName, destinationQuery, destinationPoint);
                });
            });

            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur carte", "Impossible d’ouvrir la carte.");
        }
    }

    public static void showRouteInApp(String origin, String pharmacyName, String destinationAddress) {
        try {
            String startQuery = prepareAddress(origin);
            String destinationQuery = prepareAddress(pharmacyName + " " + destinationAddress);

            Stage loadingStage = createLoadingStage("Calcul de l’itinéraire en cours...");
            loadingStage.show();

            Thread thread = new Thread(() -> {
                GeoPoint startPoint = geocode(startQuery);
                GeoPoint destinationPoint = geocode(destinationQuery);

                Platform.runLater(() -> {
                    loadingStage.close();

                    if (startPoint == null || destinationPoint == null) {
                        showError(
                                "Itinéraire impossible",
                                "Impossible de trouver le départ ou l’arrivée.\n\n" +
                                        "Départ : " + startQuery + "\n" +
                                        "Arrivée : " + destinationQuery
                        );
                        return;
                    }

                    openRouteWindow(startQuery, pharmacyName, destinationQuery, startPoint, destinationPoint);
                });
            });

            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur itinéraire", "Impossible d’ouvrir l’itinéraire.");
        }
    }

    private static void openSinglePointMap(String pharmacyName, String destinationText, GeoPoint destination) {
        Stage stage = new Stage();
        stage.setTitle("Localisation - " + safe(pharmacyName));

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent(buildSinglePointHtml(pharmacyName, destinationText, destination));

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Localisation : " + safe(pharmacyName)));
        root.setCenter(webView);

        Scene scene = new Scene(root, 1100, 750);
        stage.setScene(scene);
        stage.show();
    }

    private static void openRouteWindow(String originText, String pharmacyName, String destinationText, GeoPoint start, GeoPoint destination) {
        Stage stage = new Stage();
        stage.setTitle("Itinéraire vers " + safe(pharmacyName));

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        String html = buildRouteHtml(originText, pharmacyName, destinationText, start, destination);
        webEngine.loadContent(html);

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Itinéraire : " + safe(originText) + " → " + safe(pharmacyName)));
        root.setCenter(webView);

        Scene scene = new Scene(root, 1100, 750);
        stage.setScene(scene);
        stage.show();
    }

    private static String buildSinglePointHtml(String pharmacyName, String destinationText, GeoPoint destination) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        html, body, #map {
                            height: 100%;
                            margin: 0;
                            padding: 0;
                        }
                    </style>
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        var map = L.map('map').setView([DEST_LAT, DEST_LON], 16);
                
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '© OpenStreetMap'
                        }).addTo(map);
                
                        L.marker([DEST_LAT, DEST_LON])
                            .addTo(map)
                            .bindPopup('<b>PHARMACY_NAME</b><br>DESTINATION_TEXT')
                            .openPopup();
                    </script>
                </body>
                </html>
                """
                .replace("DEST_LAT", formatDouble(destination.lat))
                .replace("DEST_LON", formatDouble(destination.lon))
                .replace("PHARMACY_NAME", escapeJs(pharmacyName))
                .replace("DESTINATION_TEXT", escapeJs(destinationText));
    }

    private static String buildRouteHtml(String originText, String pharmacyName, String destinationText, GeoPoint start, GeoPoint destination) {
        double distanceKm = calculateDistanceKm(start, destination);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        html, body, #map {
                            height: 100%;
                            width: 100%;
                            margin: 0;
                            padding: 0;
                        }
                
                        .info {
                            position: absolute;
                            top: 12px;
                            left: 55px;
                            z-index: 999;
                            background: white;
                            padding: 12px 16px;
                            border-radius: 12px;
                            box-shadow: 0 4px 14px rgba(0,0,0,0.18);
                            font-family: Arial;
                            font-size: 14px;
                            color: #0b1f44;
                        }
                
                        .leaflet-routing-container {
                            background: white;
                            border-radius: 14px;
                            box-shadow: 0 4px 14px rgba(0,0,0,0.18);
                            padding: 8px;
                            max-height: 520px;
                            overflow-y: auto;
                            font-family: Arial;
                            font-size: 13px;
                        }
                    </style>
                
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                    <link rel="stylesheet" href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css"/>
                
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.min.js"></script>
                </head>
                <body>
                    <div id="map"></div>
                
                    <div class="info">
                        <b>Itinéraire dans SHIFAA</b><br>
                        Départ : ORIGIN_TEXT<br>
                        Arrivée : PHARMACY_NAME<br>
                        Distance estimée : DISTANCE_KM km
                    </div>
                
                    <script>
                        var start = L.latLng(START_LAT, START_LON);
                        var end = L.latLng(DEST_LAT, DEST_LON);
                
                        var map = L.map('map').setView(start, 14);
                
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '© OpenStreetMap'
                        }).addTo(map);
                
                        L.Routing.control({
                            waypoints: [start, end],
                            routeWhileDragging: false,
                            addWaypoints: false,
                            draggableWaypoints: false,
                            fitSelectedRoutes: true,
                            show: true,
                            lineOptions: {
                                styles: [
                                    { color: '#2563eb', opacity: 0.9, weight: 6 }
                                ]
                            },
                            createMarker: function(i, waypoint) {
                                if (i === 0) {
                                    return L.marker(waypoint.latLng).bindPopup('<b>Départ</b><br>ORIGIN_TEXT');
                                }
                                return L.marker(waypoint.latLng).bindPopup('<b>PHARMACY_NAME</b><br>DESTINATION_TEXT');
                            }
                        }).addTo(map);
                    </script>
                </body>
                </html>
                """
                .replace("START_LAT", formatDouble(start.lat))
                .replace("START_LON", formatDouble(start.lon))
                .replace("DEST_LAT", formatDouble(destination.lat))
                .replace("DEST_LON", formatDouble(destination.lon))
                .replace("DISTANCE_KM", String.format(Locale.US, "%.2f", distanceKm))
                .replace("ORIGIN_TEXT", escapeJs(originText))
                .replace("PHARMACY_NAME", escapeJs(pharmacyName))
                .replace("DESTINATION_TEXT", escapeJs(destinationText));
    }

    private static GeoPoint geocode(String query) {
        GeoPoint localPoint = getLocalPoint(query);

        if (localPoint != null) {
            return localPoint;
        }

        try {
            String urlText = "https://nominatim.openstreetmap.org/search"
                    + "?format=json"
                    + "&limit=1"
                    + "&accept-language=fr"
                    + "&q=" + encode(query);

            URL url = new URL(urlText);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SHIFAA-JavaFX-App/1.0");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            String lat = extractJsonValue(json, "lat");
            String lon = extractJsonValue(json, "lon");

            if (lat != null && lon != null) {
                return new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static GeoPoint getLocalPoint(String value) {
        String clean = normalize(value);

        String bestKey = "";
        GeoPoint bestPoint = null;

        for (Map.Entry<String, GeoPoint> entry : LOCAL_POINTS.entrySet()) {
            String key = normalize(entry.getKey());

            if (clean.contains(key)) {
                if (key.length() > bestKey.length()) {
                    bestKey = key;
                    bestPoint = entry.getValue();
                }
            }
        }

        return bestPoint;
    }

    private static String prepareAddress(String value) {
        String clean = safe(value);
        String normalized = normalize(clean);

        if (normalized.equals("tunis centre") || normalized.equals("centre tunis")) {
            clean = "Tunis centre";
        } else if (normalized.equals("tunis")) {
            clean = "Tunis centre";
        } else if (normalized.equals("ariana")) {
            clean = "Ariana centre";
        } else if (normalized.equals("ariana soghra")) {
            clean = "Ariana Soghra";
        } else if (normalized.equals("sousse")) {
            clean = "Sousse centre";
        } else if (normalized.equals("sfax")) {
            clean = "Sfax centre";
        } else if (normalized.equals("monastir")) {
            clean = "Monastir centre";
        } else if (normalized.equals("kairouan")) {
            clean = "Kairouan centre";
        }

        if (!normalize(clean).contains("tunisie")) {
            clean = clean + ", Tunisie";
        }

        return clean;
    }

    private static double calculateDistanceKm(GeoPoint start, GeoPoint end) {
        final int earthRadiusKm = 6371;

        double dLat = Math.toRadians(end.lat - start.lat);
        double dLon = Math.toRadians(end.lon - start.lon);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(start.lat))
                        * Math.cos(Math.toRadians(end.lat))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private static Stage createLoadingStage(String message) {
        Stage stage = new Stage();
        stage.setTitle("Chargement");

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #0b1f44;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 30px;"
        );

        Scene scene = new Scene(label, 360, 120);
        stage.setScene(scene);

        return stage;
    }

    private static Label createHeader(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 14px;"
        );
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String escapeJs(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", " ");
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.7f", value);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Non renseigné" : value.trim();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ç", "c")
                .replace(",", " ")
                .replaceAll("\\s+", " ");
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class GeoPoint {
        double lat;
        double lon;

        GeoPoint(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}