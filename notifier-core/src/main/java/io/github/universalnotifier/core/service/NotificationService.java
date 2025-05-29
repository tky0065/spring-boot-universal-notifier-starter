package io.github.universalnotifier.core.service;

import io.github.universalnotifier.core.model.NotificationRequest;

/**
 * Interface principale pour l'envoi de notifications.
 * Cette interface fournit une méthode unifiée pour envoyer des notifications
 * quel que soit le canal utilisé (email, SMS, push, etc.).
 */
public interface NotificationService {

    /**
     * Envoie une notification selon les détails fournis dans la requête.
     *
     * @param request La demande de notification contenant toutes les informations nécessaires
     * @throws NotificationException Si l'envoi de la notification échoue
     */
    void send(NotificationRequest request);
}
