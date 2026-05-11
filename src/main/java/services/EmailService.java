package services;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class EmailService {

    // Paramètres SMTP (Exemple Gmail - Nécessite un "Mot de passe d'application")
    private final String SMTP_HOST = "smtp.gmail.com";
    private final String SMTP_PORT = "587";
    private final String FROM_EMAIL = "votre.email@gmail.com";
    private final String APP_PASSWORD = "votre_mot_de_passe_application";

    public void sendCalendarEmail(String toEmail, String patientName, String icsContent) throws MessagingException, java.io.IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("💊 Rappels de votre Ordonnance - Shifaa");

        // Corps du mail
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        String body = "Bonjour " + patientName + ",\n\n" +
                      "Veuillez trouver ci-joint votre calendrier de rappels pour votre traitement.\n" +
                      "Vous pouvez l'importer dans Google Calendar ou Outlook pour recevoir des notifications.\n\n" +
                      "Cordialement,\nL'équipe Shifaa.";
        messageBodyPart.setText(body);

        // Pièce jointe (Fichier .ics)
        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(icsContent, "text/calendar; charset=UTF-8");
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName("rappels_shifaa.ics");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        // Note: En environnement de test sans credentials valides, cela lèvera une exception.
        // Dans une vraie application, ces paramètres seraient dans un fichier de configuration sécurisé.
        Transport.send(message);
    }
}
