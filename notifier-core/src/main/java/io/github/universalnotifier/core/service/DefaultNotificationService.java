package io.github.universalnotifier.core.service;

import io.github.universalnotifier.core.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Implémentation principale du service de notification.
 * Cette classe orchestre l'envoi de notifications en utilisant
 * les différents fournisseurs disponibles selon le type demandé.
 */
public class DefaultNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotificationService.class);

    private final List<NotificationProvider> providers;

    /**
     * Constructeur du service qui injecte la liste des fournisseurs disponibles.
     *
     * @param providers Liste des fournisseurs de notification (email, SMS, etc.)
     */
    @Autowired
    public DefaultNotificationService(List<NotificationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public void send(NotificationRequest request) {
        if (request == null) {
            throw new NotificationException("La demande de notification ne peut pas être null");
        }

        if (request.getType() == null || request.getType().isEmpty()) {
            throw new NotificationException("Le type de notification doit être spécifié");
        }

        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new NotificationException("Le destinataire de la notification doit être spécifié");
        }

        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            throw new NotificationException("Le message de notification ne peut pas être vide");
        }

        logger.debug("Traitement de la demande de notification de type: {}", request.getType());

        // Recherche d'un fournisseur qui supporte ce type de notification
        NotificationProvider provider = findProvider(request.getType());

        if (provider == null) {
            throw new NotificationException("Aucun fournisseur disponible pour le type de notification: " + request.getType());
        }

        try {
            provider.send(request);
            logger.info("Notification envoyée avec succès via le fournisseur pour: {}", request.getType());
        } catch (Exception e) {
            logger.error("Échec de l'envoi de la notification", e);
            throw new NotificationException("Échec de l'envoi de la notification", e);
        }
    }

    /**
     * Trouve un fournisseur approprié pour le type de notification donné.
     *
     * @param type Type de notification
     * @return Le premier fournisseur qui peut traiter ce type, ou null si aucun n'est trouvé
     */
    private NotificationProvider findProvider(String type) {
        return providers.stream()
                .filter(provider -> provider.supports(type))
                .findFirst()
                .orElse(null);
    }
}
