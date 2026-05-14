package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import utils.Config;

public class SmsService {

    public static void sendSms(String to, String body) {
        String sid    = Config.TWILIO_ACCOUNT_SID;
        String token  = Config.TWILIO_AUTH_TOKEN;
        String from   = Config.TWILIO_PHONE_NUMBER;

        if (sid == null || sid.isEmpty() || token == null || token.isEmpty() || from == null || from.isEmpty()) {
            throw new IllegalStateException(
                "Configuration Twilio manquante. Renseignez TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_PHONE_NUMBER dans utils/Config.java."
            );
        }

        try {
            Twilio.init(sid, token);
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    body
            ).create();
            System.out.println("✅ SMS envoyé avec succès! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS: " + e.getMessage());
            throw e;
        }
    }
}
