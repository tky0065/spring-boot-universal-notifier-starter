package io.github.universalnotifier.sms.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import io.github.universalnotifier.core.service.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Fournisseur de notification par SMS utilisant l'API Twilio.
 * Cette classe implémente la logique d'envoi de SMS via Twilio.
 */
public class TwilioNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(TwilioNotificationProvider.class);

    private static final String TYPE = "SMS";
    private static final String ACCOUNT_SID = "account-sid";
    private static final String AUTH_TOKEN = "auth-token";
    private static final String FROM_PHONE = "from";

    private final String fromPhone;

    /**
     * Constructeur du fournisseur Twilio utilisant les propriétés configurées.
     *
     * @param properties Configuration des propriétés de notification
     * @throws IllegalArgumentException si des propriétés requises sont manquantes
     */
    public TwilioNotificationProvider(NotificationProperties properties) {
        Map<String, String> config = properties.getSms().getActiveProviderProperties();

        String accountSid = config.get(ACCOUNT_SID);
        if (accountSid == null || accountSid.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.sms.twilio.account-sid' est requise pour l'utilisation de Twilio");
        }

        String authToken = config.get(AUTH_TOKEN);
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.sms.twilio.auth-token' est requise pour l'utilisation de Twilio");
        }

        this.fromPhone = config.get(FROM_PHONE);
        if (fromPhone == null || fromPhone.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.sms.twilio.from' est requise pour l'utilisation de Twilio");
        }

        // Initialisation de l'API Twilio avec les identifiants
        Twilio.init(accountSid, authToken);

        logger.info("Fournisseur de notification Twilio initialisé avec le numéro d'expéditeur: {}", fromPhone);
    }

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    @Override
    public void send(NotificationRequest request) {
        try {
            logger.debug("Préparation d'un SMS à envoyer à: {}", request.getTo());

            Message message = Message.creator(
                    new PhoneNumber(request.getTo()),    // Numéro de destination
                    new PhoneNumber(fromPhone),          // Numéro d'expéditeur
                    request.getMessage()                  // Contenu du message
            ).create();

            logger.info("SMS envoyé avec succès à {}. SID du message: {}", request.getTo(), message.getSid());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du SMS via Twilio", e);
            throw new NotificationException("Erreur lors de l'envoi du SMS via Twilio", e);
        }
    }
}
