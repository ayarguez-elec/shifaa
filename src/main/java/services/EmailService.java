package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  EmailService — API 2 : Envoi automatique d'email              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Sends HTML emails with embedded QR code attachment.           ║
 * ║  Configured via src/main/resources/email.properties            ║
 * ║  Uses Gmail SMTP (TLS 587) by default.                         ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class EmailService {

    private static final String CONFIG_FILE = "/email.properties";

    private final String  smtpHost;
    private final int     smtpPort;
    private final String  username;
    private final String  password;
    private final String  fromAddr;
    private final String  fromName;
    private final boolean useAuth;
    private final boolean enabled;

    public EmailService() {
        Properties cfg = loadConfig();
        this.smtpHost = cfg.getProperty("mail.smtp.host",   "localhost");
        this.smtpPort = Integer.parseInt(cfg.getProperty("mail.smtp.port", "25"));
        this.username = cfg.getProperty("mail.username",    "");
        this.password = cfg.getProperty("mail.password",    "");
        this.fromAddr = cfg.getProperty("mail.from",        "noreply@shifaa.tn");
        this.fromName = cfg.getProperty("mail.from.name",   "SHIFAA");
        this.useAuth  = Boolean.parseBoolean(cfg.getProperty("mail.smtp.auth", "false"));

        // Activé si mode sans auth (fake SMTP local), ou si credentials configurés
        boolean credentialsOk = !username.isEmpty()
                && !username.contains("VOTRE_")
                && !password.isEmpty()
                && !password.contains("VOTRE_");
        this.enabled = !useAuth || credentialsOk;

        if (enabled) {
            System.out.println("✅ [Email] Service activé → " + smtpHost + ":" + smtpPort
                + (useAuth ? " (auth)" : " (fake SMTP — sans auth)"));
        } else {
            System.out.println("ℹ️ [Email] Service désactivé. Configurez email.properties pour activer l'envoi réel.");
        }
    }

    // ── Send confirmation email ───────────────────────────────────────────────
    /**
     * Sends a booking confirmation email with the QR code attached.
     *
     * @param toEmail       recipient's email address
     * @param participantNom full name
     * @param eventTitre    event title
     * @param eventDate     event date string
     * @param eventLieu     event location
     * @param nombrePlaces  seats booked
     * @param reservationId reservation ID
     * @param qrCodePath    absolute path to the QR code PNG (may be null)
     * @return true if sent successfully
     */
    public boolean sendConfirmation(String toEmail, String participantNom,
                                     String eventTitre, String eventDate,
                                     String eventLieu, int nombrePlaces,
                                     int reservationId, String qrCodePath) {
        String subject = "✅ Confirmation de réservation — " + eventTitre;
        String html    = buildConfirmationHtml(participantNom, eventTitre, eventDate,
                                               eventLieu, nombrePlaces, reservationId);
        return send(toEmail, subject, html, qrCodePath);
    }

    // ── Send recommendation email ─────────────────────────────────────────────
    public boolean sendRecommendations(String toEmail, String participantNom,
                                        List<RecommendedEvent> recommendations) {
        String subject = "🧠 Vos recommandations personnalisées — SHIFAA";
        String html    = buildRecommendationsHtml(participantNom, recommendations);
        return send(toEmail, subject, html, null);
    }

    // ── Send refusal email ────────────────────────────────────────────────────
    public boolean sendRefus(String toEmail, String participantNom,
                              String eventTitre, String motif) {
        String subject = "❌ Demande de réservation non acceptée — " + eventTitre;
        String html    = buildRefusHtml(participantNom, eventTitre, motif);
        return send(toEmail, subject, html, null);
    }

    // ── Core send method ──────────────────────────────────────────────────────
    private boolean send(String to, String subject, String htmlBody, String attachmentPath) {
        if (!enabled) {
            // Log the email content when disabled (useful for development)
            System.out.println("📧 [Email – MODE SIMULATION]");
            System.out.println("   À      : " + to);
            System.out.println("   Objet  : " + subject);
            System.out.println("   QR     : " + (attachmentPath != null ? attachmentPath : "aucun"));
            return true; // simulate success
        }

        try {
            Session session = buildSession();
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddr, fromName, "UTF-8"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject, "UTF-8");

            // ── Multipart: HTML body + optional QR attachment ──
            MimeMultipart multipart = new MimeMultipart("mixed");

            // HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // QR Code attachment (if available)
            if (attachmentPath != null) {
                File qrFile = new File(attachmentPath);
                if (qrFile.exists()) {
                    MimeBodyPart qrPart = new MimeBodyPart();
                    qrPart.attachFile(qrFile);
                    qrPart.setFileName("qrcode_acces.png");
                    multipart.addBodyPart(qrPart);
                }
            }

            msg.setContent(multipart);
            Transport.send(msg);

            System.out.println("✅ [Email] Envoyé à " + to + " — " + subject);
            return true;

        } catch (Exception e) {
            System.err.println("❌ [Email] Erreur envoi à " + to + " : " + e.getMessage());
            return false;
        }
    }

    // ── JavaMail session ──────────────────────────────────────────────────────
    private Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host",              smtpHost);
        props.put("mail.smtp.port",              String.valueOf(smtpPort));
        props.put("mail.smtp.auth",              String.valueOf(useAuth));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout",           "15000");

        boolean useSsl = Boolean.parseBoolean(
            loadConfig().getProperty("mail.smtp.ssl.enable", "false"));

        if (useSsl) {
            // Port 465 — SSL direct (plus fiable que STARTTLS sur 587)
            props.put("mail.smtp.ssl.enable",             "true");
            props.put("mail.smtp.socketFactory.port",     String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class",    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.ssl.protocols",          "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust",              smtpHost);
        } else {
            // Port 587 — STARTTLS
            props.put("mail.smtp.starttls.enable",        "true");
            props.put("mail.smtp.starttls.required",      "true");
            props.put("mail.smtp.ssl.trust",              smtpHost);
        }

        if (useAuth) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        // Fake SMTP local — pas d'authentification
        return Session.getInstance(props);
    }

    // ── HTML templates ────────────────────────────────────────────────────────
    private String buildConfirmationHtml(String nom, String eventTitre, String date,
                                          String lieu, int places, int resId) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='" +
               "font-family:Arial,sans-serif;background:#f0f4f0;margin:0;padding:20px;'>" +
               "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;" +
               "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);'>" +

               // Header
               "<div style='background:linear-gradient(135deg,#1e3a5f,#2563eb);padding:32px;text-align:center;'>" +
               "<p style='color:rgba(255,255,255,0.7);margin:0;font-size:13px;'>SHIFAA – Gestion Santé</p>" +
               "<h1 style='color:white;margin:10px 0 0 0;font-size:24px;'>✅ Réservation Confirmée</h1>" +
               "</div>" +

               // Body
               "<div style='padding:32px;'>" +
               "<p style='font-size:16px;color:#1a2332;'>Bonjour <strong>" + escHtml(nom) + "</strong>,</p>" +
               "<p style='color:#4a5568;'>Votre réservation pour l'événement suivant a été <strong style='color:#16a34a;'>confirmée avec succès</strong> :</p>" +

               // Event card
               "<div style='background:#f8fafc;border-left:4px solid #2563eb;border-radius:8px;padding:20px;margin:20px 0;'>" +
               "<p style='margin:4px 0;font-size:18px;font-weight:bold;color:#1e3a5f;'>" + escHtml(eventTitre) + "</p>" +
               "<p style='margin:4px 0;color:#4a5568;'>📅 " + escHtml(date) + "</p>" +
               "<p style='margin:4px 0;color:#4a5568;'>📍 " + escHtml(lieu) + "</p>" +
               "<p style='margin:4px 0;color:#4a5568;'>🎟 " + places + " place(s) réservée(s)</p>" +
               "<p style='margin:4px 0;color:#718096;font-size:12px;'>Réf. #" + resId + "</p>" +
               "</div>" +

               "<div style='background:#d4edda;border-radius:8px;padding:16px;text-align:center;margin:20px 0;'>" +
               "<p style='margin:0;color:#16a34a;font-weight:bold;'>🔳 Votre QR Code d'accès est joint à cet email.</p>" +
               "<p style='margin:4px 0 0 0;color:#1a6b2e;font-size:13px;'>Présentez-le à l'entrée de l'événement.</p>" +
               "</div>" +

               "<p style='color:#718096;font-size:13px;'>Pour annuler : contactez-nous ou utilisez votre espace participant.</p>" +
               "</div>" +

               // Footer
               "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>" +
               "<p style='margin:0;color:#a0aec0;font-size:12px;'>SHIFAA – Système de Gestion d'Événements Médicaux</p>" +
               "</div>" +
               "</div></body></html>";
    }

    private String buildRefusHtml(String nom, String eventTitre, String motif) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='" +
               "font-family:Arial,sans-serif;background:#f0f4f0;margin:0;padding:20px;'>" +
               "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;" +
               "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);'>" +

               "<div style='background:linear-gradient(135deg,#7f1d1d,#e05a5a);padding:32px;text-align:center;'>" +
               "<p style='color:rgba(255,255,255,0.7);margin:0;font-size:13px;'>SHIFAA – Gestion Santé</p>" +
               "<h1 style='color:white;margin:10px 0 0 0;font-size:24px;'>❌ Demande Non Acceptée</h1>" +
               "</div>" +

               "<div style='padding:32px;'>" +
               "<p style='font-size:16px;color:#1a2332;'>Bonjour <strong>" + escHtml(nom) + "</strong>,</p>" +
               "<p style='color:#4a5568;'>Votre demande de réservation pour <strong>" + escHtml(eventTitre) +
               "</strong> n'a pas pu être acceptée.</p>" +

               "<div style='background:#fdeaea;border-left:4px solid #e05a5a;border-radius:8px;padding:16px;margin:20px 0;'>" +
               "<p style='margin:0;color:#c53030;font-weight:bold;'>Motif :</p>" +
               "<p style='margin:6px 0 0 0;color:#7f1d1d;'>" + escHtml(motif) + "</p>" +
               "</div>" +

               "<p style='color:#4a5568;'>Pour toute question, contactez notre équipe.</p>" +
               "</div>" +

               "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>" +
               "<p style='margin:0;color:#a0aec0;font-size:12px;'>SHIFAA – Système de Gestion d'Événements Médicaux</p>" +
               "</div>" +
               "</div></body></html>";
    }

    private String buildRecommendationsHtml(String nom, List<RecommendedEvent> recs) {
        StringBuilder cards = new StringBuilder();
        int rank = 1;
        for (RecommendedEvent r : recs) {
            entities.event ev = r.getEvent();
            String scoreColor = r.getScore() >= 70 ? "#16a34a" : r.getScore() >= 40 ? "#d97706" : "#2563eb";
            String date = ev.getDate() != null ? ev.getDate().toString() : "";
            String lieu = ev.getLieu() != null ? ev.getLieu() : "";
            cards.append(
                "<div style='border:1px solid #e2e8f0; border-left:4px solid ").append(scoreColor)
                .append("; border-radius:8px; padding:16px; margin:12px 0; background:#fafafa;'>")
                .append("<div style='display:flex; justify-content:space-between; align-items:center;'>")
                .append("<span style='background:#f3e8ff; color:#6b21a8; font-size:11px; padding:2px 8px; border-radius:20px; font-weight:bold;'>#").append(rank++).append("</span>")
                .append("<span style='background:").append(scoreColor).append("; color:white; font-size:11px; padding:2px 8px; border-radius:20px; font-weight:bold;'>")
                .append(r.getScore()).append(" pts</span></div>")
                .append("<p style='margin:8px 0 4px 0; font-size:15px; font-weight:bold; color:#1a2332;'>")
                .append(escHtml(ev.getTitre())).append("</p>")
                .append("<p style='margin:2px 0; color:#4a5568; font-size:12px;'>")
                .append("<span style='background:#e9d5ff; color:#6b21a8; padding:1px 6px; border-radius:10px; font-size:11px;'>")
                .append(escHtml(ev.getType() != null ? ev.getType() : "")).append("</span>")
                .append("  📅 ").append(escHtml(date))
                .append("  📍 ").append(escHtml(lieu)).append("</p>")
                .append("<p style='margin:6px 0 0 0; color:#6b46c1; font-size:12px; font-style:italic;'>")
                .append("💡 ").append(escHtml(r.getReason())).append("</p>")
                .append("</div>");
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
               "<body style='font-family:Arial,sans-serif; background:#f0f4f0; margin:0; padding:20px;'>" +
               "<div style='max-width:600px; margin:auto; background:white; border-radius:12px;" +
               " overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.1);'>" +

               // Header
               "<div style='background:linear-gradient(135deg,#4c1d95,#9333ea); padding:32px; text-align:center;'>" +
               "<p style='color:rgba(255,255,255,0.7); margin:0; font-size:13px;'>SHIFAA – Gestion Santé</p>" +
               "<h1 style='color:white; margin:10px 0 0 0; font-size:22px;'>🧠 Recommandations Personnalisées</h1>" +
               "</div>" +

               // Body
               "<div style='padding:28px;'>" +
               "<p style='font-size:16px; color:#1a2332;'>Bonjour <strong>" + escHtml(nom) + "</strong>,</p>" +
               "<p style='color:#4a5568;'>Voici <strong>" + recs.size() + " événements</strong> sélectionnés " +
               "spécialement pour vous selon votre profil et votre historique :</p>" +
               cards +
               "<div style='background:#f3e8ff; border-radius:8px; padding:14px; text-align:center; margin-top:20px;'>" +
               "<p style='margin:0; color:#6b21a8; font-size:13px;'>Connectez-vous à SHIFAA pour réserver vos événements.</p>" +
               "</div></div>" +

               // Footer
               "<div style='background:#f8fafc; padding:16px; text-align:center; border-top:1px solid #e2e8f0;'>" +
               "<p style='margin:0; color:#a0aec0; font-size:12px;'>SHIFAA – Système de Gestion d'Événements Médicaux</p>" +
               "</div></div></body></html>";
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    // ── Config loader ─────────────────────────────────────────────────────────
    private Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) p.load(is);
        } catch (IOException e) {
            System.err.println("⚠️ [Email] email.properties non trouvé.");
        }
        return p;
    }
}
