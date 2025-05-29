package io.github.universalnotifier.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import io.github.universalnotifier.core.service.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Fournisseur de notification par email utilisant l'API SendGrid.
 * Cette classe implémente la logique d'envoi d'emails via SendGrid.
 */
public class SendGridNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(SendGridNotificationProvider.class);

    private static final String TYPE = "EMAIL";
    private static final String API_KEY = "api-key";
    private static final String FROM_EMAIL = "from";

    private final String apiKey;
    private final String fromEmail;

    /**
     * Constructeur du fournisseur SendGrid utilisant les propriétés configurées.
     *
     * @param properties Configuration des propriétés de notification
     * @throws IllegalArgumentException si des propriétés requises sont manquantes
     */
    public SendGridNotificationProvider(NotificationProperties properties) {
        Map<String, String> config = properties.getEmail().getActiveProviderProperties();

        this.apiKey = config.get(API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.email.sendgrid.api-key' est requise pour l'utilisation de SendGrid");
        }

        this.fromEmail = config.get(FROM_EMAIL);
        if (fromEmail == null || fromEmail.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.email.sendgrid.from' est requise pour l'utilisation de SendGrid");
        }

        logger.info("Fournisseur de notification SendGrid initialisé avec l'adresse d'expéditeur: {}", fromEmail);
    }

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    @Override
    public void send(NotificationRequest request) {
        try {
            logger.debug("Préparation d'un email à envoyer à: {}", request.getTo());

            SendGrid sg = new SendGrid(apiKey);

            Email from = new Email(fromEmail);
            Email to = new Email(request.getTo());
            String subject = request.getSubject() != null ? request.getSubject() : "";
            Content content = new Content("text/plain", request.getMessage());

            Mail mail = new Mail(from, subject, to, content);

            Request sendgridRequest = new Request();
            sendgridRequest.setMethod(Method.POST);
            sendgridRequest.setEndpoint("mail/send");
            sendgridRequest.setBody(mail.build());

            Response response = sg.api(sendgridRequest);

            int statusCode = response.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                logger.error("Échec de l'envoi d'email. Code de statut: {}. Corps: {}",
                        statusCode, response.getBody());
                throw new NotificationException("Échec de l'envoi d'email via SendGrid. Code de statut: " + statusCode);
            }

            logger.info("Email envoyé avec succès à {}. Code de statut: {}", request.getTo(), statusCode);
        } catch (IOException e) {
            logger.error("Erreur lors de l'envoi d'email via SendGrid", e);
            throw new NotificationException("Erreur lors de l'envoi d'email via SendGrid", e);
        }
    }
}
