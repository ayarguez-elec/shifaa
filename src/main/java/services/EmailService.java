package services;

import utils.Config;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6 digit code
        return String.valueOf(code);
    }

    public static boolean sendVerificationEmail(String toEmail, String code) {
        // Paramètres pour le serveur SMTP de Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Shefaa - Code de vérification");
            
            String htmlContent = "<h2>Bienvenue sur Shefaa !</h2>"
                    + "<p>Voici votre code de vérification pour valider votre inscription :</p>"
                    + "<h1 style='color: #4CAF50;'>" + code + "</h1>"
                    + "<p>Si vous n'avez pas demandé ce code, veuillez ignorer cet e-mail.</p>";
                    
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("E-mail de vérification envoyé à " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendResetPasswordEmail(String toEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Shefaa - Réinitialisation de votre mot de passe");
            
            String htmlContent = "<h2>Réinitialisation de mot de passe</h2>"
                    + "<p>Vous avez demandé la réinitialisation de votre mot de passe sur Shefaa.</p>"
                    + "<p>Voici votre code de sécurité :</p>"
                    + "<h1 style='color: #FF5C5C;'>" + code + "</h1>"
                    + "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet e-mail.</p>";
                    
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
