package io.github.universalnotifier.push.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import io.github.universalnotifier.core.service.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fournisseur de notification par push utilisant Firebase Cloud Messaging.
 * Cette classe implémente la logique d'envoi de notifications push via FCM.
 */
public class FirebaseNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseNotificationProvider.class);

    private static final String TYPE = "PUSH";
    private static final String CREDENTIALS_FILE = "credentials-file";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private final ResourceLoader resourceLoader;


    /**
     * Constructeur du fournisseur Firebase utilisant les propriétés configurées.
     *
     * @param properties Configuration des propriétés de notification
     * @param resourceLoader Chargeur de ressources Spring pour accéder au fichier de clés
     * @throws NotificationException si l'initialisation de Firebase échoue
     */
    public FirebaseNotificationProvider(NotificationProperties properties, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

        Map<String, String> config = properties.getPush().getActiveProviderProperties();

        String credentialsPath = config.get(CREDENTIALS_FILE);
        if (credentialsPath == null || credentialsPath.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.push.firebase.credentials-file' est requise pour l'utilisation de Firebase");
        }

        // Initialiser Firebase s'il ne l'est pas déjà (pour éviter les initialisations multiples)
        if (!initialized.getAndSet(true)) {
            try {
                Resource resource = resourceLoader.getResource(credentialsPath);
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);

                logger.info("Firebase Cloud Messaging initialisé avec succès");
            } catch (IOException e) {
                logger.error("Erreur lors de l'initialisation de Firebase", e);
                throw new NotificationException("Erreur lors de l'initialisation de Firebase", e);
            }
        }
    }

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    @Override
    public void send(NotificationRequest request) {
        try {
            logger.debug("Préparation d'une notification push à envoyer au token: {}", request.getTo());

            // Extraction du titre depuis le sujet (s'il existe)
            String title = request.getSubject();
            String body = request.getMessage();

            // Construction de la notification Firebase
            Message message = Message.builder()
                    .setToken(request.getTo())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Envoi de la notification
            String messageId = FirebaseMessaging.getInstance().send(message);

            logger.info("Notification push envoyée avec succès au token {}. ID du message: {}",
                    request.getTo(), messageId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification push via Firebase", e);
            throw new NotificationException("Erreur lors de l'envoi de notification push via Firebase", e);
        }
    }
}
