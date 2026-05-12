package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    // Configuration - A adapter par l'utilisateur
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "ysmn1422022@gmail.com"; 
    private static final String SENDER_PASSWORD = "jeyy hvpd vehu rxye";

    public static void sendConfirmationEmail(String recipientEmail, String patientName, String date, String time, String doctorName, String doctorAddress) {
        String content = "Bonjour " + patientName + ",\n\n" +
                         "Nous vous confirmons que votre rendez-vous a été bien enregistré.\n\n" +
                         "📅 Date : " + date + "\n" +
                         "⏰ Heure : " + time + "\n" +
                         "👨‍⚕️ Médecin : Dr " + doctorName + "\n" +
                         "📍 Lieu : " + doctorAddress + "\n\n" +
                         "Nous vous remercions de vous présenter 10 minutes à l’avance.\n\n" +
                         "En cas d’empêchement, merci de nous prévenir à l’avance.\n\n" +
                         "Cordialement,\n" +
                         "Le cabinet médical";
        
        sendEmail(recipientEmail, "Confirmation de votre rendez-vous médical", content);
    }

    public static void sendOnlineConfirmation(String recipientEmail, String patientName, String date, String time, String doctorName, String meetLink) {
        String content = "Bonjour " + patientName + ",\n\n" +
                         "Votre rendez-vous en ligne a été confirmé avec succès.\n\n" +
                         "📅 Date : " + date + "\n" +
                         "⏰ Heure : " + time + "\n" +
                         "👨‍⚕️ Médecin : Dr " + doctorName + "\n\n" +
                         "🔗 Lien de consultation :\n" + 
                         meetLink + "\n\n" +
                         "Merci de vous connecter 5 minutes avant l’heure prévue.\n\n" +
                         "En cas de problème technique, veuillez nous contacter.\n\n" +
                         "Cordialement,\n" +
                         "Le cabinet médical";
        
        sendEmail(recipientEmail, "Confirmation de votre rendez-vous en ligne", content);
    }

    private static void sendEmail(String recipientEmail, String subject, String content) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            System.err.println("X Erreur: Email du patient manquant.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);

            if (SENDER_PASSWORD.equals("votre_mot_de_passe_app")) {
                System.out.println("--- SIMULATION EMAIL ---");
                System.out.println("À : " + recipientEmail);
                System.out.println("Sujet : " + subject);
                System.out.println("Contenu : \n" + content);
                System.out.println("-------------------------");
            } else {
                Transport.send(message);
                System.out.println("✓ Email envoyé à : " + recipientEmail);
            }
        } catch (MessagingException e) {
            System.err.println("X Échec de l'envoi de l'email : " + e.getMessage());
        }
    }
}

