package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public static void sendSms(String to, String body) {
        try {
            if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
                throw new IllegalStateException("Configuration Twilio manquante. Verifiez TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_PHONE_NUMBER.");
            }
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    body
            ).create();
            System.out.println("✅ SMS envoyé avec succès! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS: " + e.getMessage());
            throw e;
        }
    }
}
