package services;

import entities.user;
import javafx.application.Platform;
import org.json.JSONObject;
import utils.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Consumer;
import java.awt.Desktop;

public class OAuthService {

    private static String codeVerifier;

    public static void openOAuthWindow(String provider, Consumer<user> onSuccess, Consumer<String> onError) {
        if (!provider.equals("Google")) {
            Platform.runLater(() -> onError.accept("Seul Google est configuré avec le navigateur système pour le moment."));
            return;
        }

        try {
            // 1. Démarrer un ServerSocket bas niveau (plus fiable que HttpServer sur Windows)
            ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
            int port = serverSocket.getLocalPort();
            String redirectUri = "http://127.0.0.1:" + port;

            // Thread pour écouter la réponse de Google
            new Thread(() -> {
                try {
                    Socket socket = serverSocket.accept(); // Attend que le navigateur se connecte
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    
                    if (line != null && line.startsWith("GET /?")) {
                        String query = line.split(" ")[1];
                        String responseBody;
                        
                        if (query.contains("code=")) {
                            responseBody = "<html><body><h1 style='color:green; font-family:sans-serif; text-align:center; margin-top:50px;'>Authentification réussie !</h1><p style='text-align:center;'>Vous pouvez fermer cette fenêtre et retourner à l'application.</p><script>setTimeout(window.close, 3000);</script></body></html>";
                            String code = extractParameter(query, "code=");
                            exchangeCodeForTokenGoogle(code, redirectUri, onSuccess, onError);
                        } else {
                            responseBody = "<html><body><h1 style='color:red; font-family:sans-serif; text-align:center; margin-top:50px;'>Erreur d'authentification</h1></body></html>";
                            Platform.runLater(() -> onError.accept("Authentification annulée ou échouée."));
                        }
                        
                        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                              "Content-Type: text/html; charset=UTF-8\r\n" +
                                              "Connection: close\r\n" +
                                              "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                                              "\r\n" +
                                              responseBody;
                        
                        socket.getOutputStream().write(httpResponse.getBytes(StandardCharsets.UTF_8));
                        socket.getOutputStream().flush();
                    }
                    socket.close();
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // 2. Préparer l'URL Google
            generatePKCE();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + Config.GOOGLE_CLIENT_ID +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=email%20profile" +
                    "&code_challenge=" + codeChallenge +
                    "&code_challenge_method=S256";

            // 3. Ouvrir le VRAI navigateur de l'utilisateur
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                Platform.runLater(() -> onError.accept("Impossible d'ouvrir le navigateur système."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> onError.accept("Erreur lors de la préparation de la connexion."));
        }
    }

    private static void generatePKCE() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private static String extractParameter(String query, String paramName) {
        int startIndex = query.indexOf(paramName) + paramName.length();
        int endIndex = query.indexOf("&", startIndex);
        if (endIndex == -1) endIndex = query.length();
        return query.substring(startIndex, endIndex);
    }

    private static void exchangeCodeForTokenGoogle(String code, String redirectUri, Consumer<user> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                URL url = new URL("https://oauth2.googleapis.com/token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "client_id=" + Config.GOOGLE_CLIENT_ID +
                        "&code=" + code + // Le code est déjà URL-encodé depuis la requête brute
                        "&code_verifier=" + codeVerifier +
                        "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()) +
                        "&grant_type=authorization_code";
                        
                if (Config.GOOGLE_CLIENT_SECRET != null && !Config.GOOGLE_CLIENT_SECRET.isEmpty()) {
                    postData += "&client_secret=" + Config.GOOGLE_CLIENT_SECRET;
                }

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.writeBytes(postData);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode >= 400) {
                    // Lire l'erreur pour savoir pourquoi Google refuse
                    BufferedReader errStream = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errContent = new StringBuilder();
                    String line;
                    while ((line = errStream.readLine()) != null) {
                        errContent.append(line);
                    }
                    errStream.close();
                    System.err.println("Erreur Token Google (" + responseCode + "): " + errContent.toString());
                    Platform.runLater(() -> onError.accept("Refusé par Google. Voir console."));
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(content.toString());
                String accessToken = json.getString("access_token");
                
                fetchUserProfile("Google", accessToken, onSuccess, onError);

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onError.accept("Impossible d'obtenir le token Google."));
            }
        }).start();
    }

    private static void fetchUserProfile(String provider, String token, Consumer<user> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                String apiUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token;

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(content.toString());
                
                user authUser = new user();
                authUser.setEmail(json.optString("email", ""));
                authUser.setFirstName(json.optString("given_name", "Utilisateur"));
                authUser.setLastName(json.optString("family_name", "Google"));

                authUser.setRole(user.Role.patient);
                authUser.setAccountStatus(user.AccountStatus.active);
                authUser.setGender(user.Gender.M);
                authUser.setAge(18);
                authUser.setAddress("");
                authUser.setPhone("");
                authUser.setSpecialty("");
                authUser.setOrderNumber("");
                authUser.setConsultationFee(0);
                authUser.setPharmacyName("");
                authUser.setLicenceNumber("");
                authUser.setUserPhoto("");
                authUser.setDoctorId(0);

                Platform.runLater(() -> onSuccess.accept(authUser));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onError.accept("Impossible de récupérer le profil (" + provider + ")"));
            }
        }).start();
    }
}
