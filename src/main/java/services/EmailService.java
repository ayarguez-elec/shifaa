package services;

import utils.Config;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Service email unifié SHIFAA.
 *
 * Regroupe :
 *  – les emails médicaux (vérification, approbation, RDV, ordonnances, calendrier)
 *  – les emails d'événements (confirmation réservation + QR code, refus, recommandations)
 *
 * Deux modes de configuration :
 *  1. Config.java (amis) — pour les emails médecin/admin
 *  2. email.properties (module événements) — pour les emails QR/réservation
 */
public class EmailService {

    // ──────────────────────────────────────────────────────────
    //  CONFIG PARTAGÉE
    // ──────────────────────────────────────────────────────────

    private static final String CONFIG_FILE = "/email.properties";

    // ── Propriétés pour le mode email.properties (réservations) ──────────────
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

        boolean credentialsOk = !username.isEmpty()
                && !username.contains("VOTRE_")
                && !password.isEmpty()
                && !password.contains("VOTRE_");
        this.enabled = !useAuth || credentialsOk;

        System.out.println(enabled
            ? "✅ [Email] Service réservations activé → " + smtpHost + ":" + smtpPort
            : "ℹ️ [Email] Mode simulation (configurez email.properties pour activer).");
    }

    // ──────────────────────────────────────────────────────────
    //  SECTION 1 — EMAILS MÉDICAUX (Config.java, jakarta.mail)
    // ──────────────────────────────────────────────────────────

    /** Contourne la vérification SSL (développement). */
    private static void trustAllCerts() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] c, String t) {}
                    public void checkServerTrusted(X509Certificate[] c, String t) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            SSLContext.setDefault(sc);
        } catch (Exception e) {
            System.err.println("trustAllCerts: " + e.getMessage());
        }
    }

    private static Session getStaticSession() {
        trustAllCerts();
        Properties props = new Properties();
        props.put("mail.smtp.host",                   "smtp.gmail.com");
        props.put("mail.smtp.port",                   "465");
        props.put("mail.smtp.auth",                   "true");
        props.put("mail.smtp.socketFactory.port",     "465");
        props.put("mail.smtp.socketFactory.class",    "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.trust",              "*");
        props.put("mail.smtp.ssl.checkserveridentity","false");
        props.put("mail.smtp.connectiontimeout",      "30000");
        props.put("mail.smtp.timeout",                "30000");
        props.put("mail.smtp.writetimeout",           "30000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD);
            }
        });
    }

    public static boolean sendAccountApprovedEmail(String toEmail, String firstName) {
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("✅ Shifaa - Votre compte a été approuvé !");
            String html = "<div style='font-family:sans-serif;max-width:500px;margin:auto;padding:30px;'>"
                    + "<h2 style='color:#48BB78;'>🎉 Félicitations, " + firstName + " !</h2>"
                    + "<p>Votre compte professionnel sur <strong>Shifaa</strong> a été <strong style='color:#48BB78;'>approuvé</strong> par l'administrateur.</p>"
                    + "<p>Vous pouvez maintenant vous connecter et accéder à votre espace.</p>"
                    + "</div>";
            msg.setContent(html, "text/html; charset=utf-8");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ sendAccountApprovedEmail : " + e.getMessage());
            return false;
        }
    }

    public static String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public static boolean sendVerificationEmail(String toEmail, String code) {
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Shefaa - Code de vérification");
            String html = "<h2>Bienvenue sur Shefaa !</h2>"
                    + "<p>Voici votre code de vérification :</p>"
                    + "<h1 style='color:#4CAF50;'>" + code + "</h1>"
                    + "<p>Si vous n'avez pas demandé ce code, ignorez cet e-mail.</p>";
            msg.setContent(html, "text/html; charset=utf-8");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ sendVerificationEmail : " + e.getMessage());
            return false;
        }
    }

    public static boolean sendResetPasswordEmail(String toEmail, String code) {
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Shefaa - Réinitialisation de votre mot de passe");
            String html = "<h2>Réinitialisation de mot de passe</h2>"
                    + "<p>Votre code de sécurité :</p>"
                    + "<h1 style='color:#FF5C5C;'>" + code + "</h1>";
            msg.setContent(html, "text/html; charset=utf-8");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ sendResetPasswordEmail : " + e.getMessage());
            return false;
        }
    }

    public static boolean sendCalendarEmail(String toEmail, String patientName, String icsContent) {
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("💊 Rappels de votre Ordonnance - Shifaa");

            MimeBodyPart body = new MimeBodyPart();
            body.setText("Bonjour " + patientName + ",\n\nVeuillez trouver ci-joint votre calendrier de rappels.\n\nL'équipe Shifaa.");

            MimeBodyPart attach = new MimeBodyPart();
            DataSource ds = new ByteArrayDataSource(icsContent, "text/calendar; charset=UTF-8");
            attach.setDataHandler(new DataHandler(ds));
            attach.setFileName("rappels_shifaa.ics");

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(body);
            mp.addBodyPart(attach);
            msg.setContent(mp);
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("❌ sendCalendarEmail : " + e.getMessage());
            return false;
        }
    }

    public static void sendConfirmationEmail(String recipientEmail, String patientName,
                                              String date, String time,
                                              String doctorName, String doctorAddress) {
        String content = "Bonjour " + patientName + ",\n\n"
                + "Nous vous confirmons votre rendez-vous :\n\n"
                + "📅 Date : " + date + "\n"
                + "⏰ Heure : " + time + "\n"
                + "👨‍⚕️ Médecin : Dr " + doctorName + "\n"
                + "📍 Lieu : " + doctorAddress + "\n\n"
                + "Merci de vous présenter 10 minutes à l'avance.\n\nCordialement,\nShifaa";
        sendEmailGeneric(recipientEmail, "Confirmation de votre rendez-vous médical", content);
    }

    public static void sendOnlineConfirmation(String recipientEmail, String patientName,
                                               String date, String time,
                                               String doctorName, String meetLink) {
        String content = "Bonjour " + patientName + ",\n\n"
                + "Votre rendez-vous en ligne a été confirmé.\n\n"
                + "📅 Date : " + date + "\n"
                + "⏰ Heure : " + time + "\n"
                + "👨‍⚕️ Médecin : Dr " + doctorName + "\n"
                + "🔗 Lien : " + meetLink + "\n\n"
                + "Connectez-vous 5 minutes avant l'heure prévue.\n\nCordialement,\nShifaa";
        sendEmailGeneric(recipientEmail, "Confirmation de votre rendez-vous en ligne", content);
    }

    private static void sendEmailGeneric(String recipientEmail, String subject, String content) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            msg.setSubject(subject);
            msg.setText(content);
            Transport.send(msg);
            System.out.println("✓ Email envoyé : " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("✗ sendEmailGeneric : " + e.getMessage());
        }
    }

    public static boolean sendContactMessage(String userEmail, String userName, String subject, String messageContent) {
        String content = "Nouveau message de contact depuis la Landing Page Shifaa :\n\n"
                       + "De : " + userName + " (" + userEmail + ")\n"
                       + "Sujet : " + subject + "\n\n"
                       + "Message :\n" + messageContent;
        try {
            Session session = getStaticSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Config.EMAIL_USERNAME)); // Envoi à l'administrateur
            msg.setSubject("NOUVEAU CONTACT : " + subject);
            msg.setText(content);
            Transport.send(msg);
            System.out.println("✓ Email de contact envoyé avec succès.");
            return true;
        } catch (MessagingException e) {
            System.err.println("✗ sendContactMessage : " + e.getMessage());
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────
    //  SECTION 2 — EMAILS ÉVÉNEMENTS (email.properties + QR code)
    // ──────────────────────────────────────────────────────────

    public boolean sendConfirmation(String toEmail, String participantNom,
                                     String eventTitre, String eventDate,
                                     String eventLieu, int nombrePlaces,
                                     int reservationId, String qrCodePath) {
        String subject = "✅ Confirmation de réservation — " + eventTitre;
        String html    = buildConfirmationHtml(participantNom, eventTitre, eventDate,
                                               eventLieu, nombrePlaces, reservationId);
        return send(toEmail, subject, html, qrCodePath);
    }

    public boolean sendRecommendations(String toEmail, String participantNom,
                                        List<RecommendedEvent> recommendations) {
        String subject = "🧠 Vos recommandations personnalisées — SHIFAA";
        return send(toEmail, subject, buildRecommendationsHtml(participantNom, recommendations), null);
    }

    public boolean sendRefus(String toEmail, String participantNom,
                              String eventTitre, String motif) {
        String subject = "❌ Demande de réservation non acceptée — " + eventTitre;
        return send(toEmail, subject, buildRefusHtml(participantNom, eventTitre, motif), null);
    }

    private boolean send(String to, String subject, String htmlBody, String attachmentPath) {
        if (!enabled) {
            System.out.println("📧 [Email – SIMULATION] → " + to + " | " + subject);
            return true;
        }
        try {
            Session session = buildSession();
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddr, fromName, "UTF-8"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject, "UTF-8");

            MimeMultipart mp = new MimeMultipart("mixed");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            mp.addBodyPart(htmlPart);

            if (attachmentPath != null) {
                File qr = new File(attachmentPath);
                if (qr.exists()) {
                    MimeBodyPart qrPart = new MimeBodyPart();
                    qrPart.attachFile(qr);
                    qrPart.setFileName("qrcode_acces.png");
                    mp.addBodyPart(qrPart);
                }
            }
            msg.setContent(mp);
            Transport.send(msg);
            System.out.println("✅ [Email-Event] Envoyé à " + to);
            return true;
        } catch (Exception e) {
            System.err.println("❌ [Email-Event] " + to + " : " + e.getMessage());
            return false;
        }
    }

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
            props.put("mail.smtp.ssl.enable",             "true");
            props.put("mail.smtp.socketFactory.port",     String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class",    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.ssl.protocols",          "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust",              smtpHost);
        } else {
            props.put("mail.smtp.starttls.enable",   "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust",         smtpHost);
        }
        if (useAuth) {
            final String u = username, p = password;
            return Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(u, p);
                }
            });
        }
        return Session.getInstance(props);
    }

    // ── HTML Templates (événements) ──────────────────────────────────────────

    private String buildConfirmationHtml(String nom, String eventTitre, String date,
                                          String lieu, int places, int resId) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
             + "<body style='font-family:Arial,sans-serif;background:#f0f4f0;padding:20px;'>"
             + "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1);'>"
             + "<div style='background:linear-gradient(135deg,#1e3a5f,#2563eb);padding:32px;text-align:center;'>"
             + "<p style='color:rgba(255,255,255,.7);margin:0;font-size:13px;'>SHIFAA – Gestion Santé</p>"
             + "<h1 style='color:white;margin:10px 0 0 0;font-size:24px;'>✅ Réservation Confirmée</h1></div>"
             + "<div style='padding:32px;'>"
             + "<p>Bonjour <strong>" + esc(nom) + "</strong>,</p>"
             + "<p>Votre réservation pour l'événement suivant a été <strong style='color:#16a34a;'>confirmée</strong> :</p>"
             + "<div style='background:#f8fafc;border-left:4px solid #2563eb;border-radius:8px;padding:20px;margin:20px 0;'>"
             + "<p style='margin:4px 0;font-size:18px;font-weight:bold;color:#1e3a5f;'>" + esc(eventTitre) + "</p>"
             + "<p style='margin:4px 0;color:#4a5568;'>📅 " + esc(date) + "</p>"
             + "<p style='margin:4px 0;color:#4a5568;'>📍 " + esc(lieu) + "</p>"
             + "<p style='margin:4px 0;color:#4a5568;'>🎟 " + places + " place(s)</p>"
             + "<p style='margin:4px 0;color:#718096;font-size:12px;'>Réf. #" + resId + "</p></div>"
             + "<div style='background:#d4edda;border-radius:8px;padding:16px;text-align:center;'>"
             + "<p style='margin:0;color:#16a34a;font-weight:bold;'>🔳 Votre QR Code d'accès est joint à cet email.</p></div></div>"
             + "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>"
             + "<p style='margin:0;color:#a0aec0;font-size:12px;'>SHIFAA – Système de Gestion d'Événements Médicaux</p></div></div></body></html>";
    }

    private String buildRefusHtml(String nom, String eventTitre, String motif) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
             + "<body style='font-family:Arial,sans-serif;background:#f0f4f0;padding:20px;'>"
             + "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;'>"
             + "<div style='background:linear-gradient(135deg,#7f1d1d,#e05a5a);padding:32px;text-align:center;'>"
             + "<h1 style='color:white;margin:0;font-size:24px;'>❌ Demande Non Acceptée</h1></div>"
             + "<div style='padding:32px;'>"
             + "<p>Bonjour <strong>" + esc(nom) + "</strong>,</p>"
             + "<p>Votre demande pour <strong>" + esc(eventTitre) + "</strong> n'a pas pu être acceptée.</p>"
             + "<div style='background:#fdeaea;border-left:4px solid #e05a5a;border-radius:8px;padding:16px;'>"
             + "<p style='margin:0;color:#c53030;font-weight:bold;'>Motif :</p>"
             + "<p style='margin:6px 0 0 0;color:#7f1d1d;'>" + esc(motif) + "</p></div></div></div></body></html>";
    }

    private String buildRecommendationsHtml(String nom, List<RecommendedEvent> recs) {
        StringBuilder cards = new StringBuilder();
        int rank = 1;
        for (RecommendedEvent r : recs) {
            entities.event ev = r.getEvent();
            String color = r.getScore() >= 70 ? "#16a34a" : r.getScore() >= 40 ? "#d97706" : "#2563eb";
            cards.append("<div style='border:1px solid #e2e8f0;border-left:4px solid ").append(color)
                 .append(";border-radius:8px;padding:16px;margin:12px 0;'>"
                 + "<span style='background:").append(color).append(";color:white;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:bold;'>")
                 .append(r.getScore()).append(" pts</span>")
                 .append("<p style='margin:8px 0 4px;font-size:15px;font-weight:bold;'>").append(esc(ev.getTitre())).append("</p>")
                 .append("<p style='color:#4a5568;font-size:12px;'>").append(esc(ev.getType())).append(" – 📅 ")
                 .append(ev.getDate() != null ? ev.getDate() : "").append("</p>")
                 .append("<p style='color:#6b46c1;font-size:12px;font-style:italic;'>💡 ").append(esc(r.getReason())).append("</p></div>");
            rank++;
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
             + "<body style='font-family:Arial,sans-serif;background:#f0f4f0;padding:20px;'>"
             + "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;'>"
             + "<div style='background:linear-gradient(135deg,#4c1d95,#9333ea);padding:32px;text-align:center;'>"
             + "<h1 style='color:white;margin:0;font-size:22px;'>🧠 Recommandations Personnalisées</h1></div>"
             + "<div style='padding:28px;'>"
             + "<p>Bonjour <strong>" + esc(nom) + "</strong>,</p>"
             + "<p>Voici <strong>" + recs.size() + " événements</strong> sélectionnés pour vous :</p>"
             + cards + "</div></div></body></html>";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) p.load(is);
        } catch (IOException e) {
            System.err.println("⚠️ [Email] email.properties introuvable.");
        }
        return p;
    }
}
