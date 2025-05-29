package io.github.universalnotifier.core.service;

import io.github.universalnotifier.core.model.NotificationRequest;

/**
 * Interface pour les fournisseurs de notifications.
 * Chaque fournisseur (email, SMS, push) doit implémenter cette interface.
 */
public interface NotificationProvider {

    /**
     * Vérifie si ce fournisseur peut traiter le type de notification demandé.
     *
     * @param type Type de notification
     * @return true si ce fournisseur peut traiter ce type de notification
     */
    boolean supports(String type);

    /**
     * Envoie une notification via ce fournisseur.
     *
     * @param request Détails de la notification à envoyer
     * @throws NotificationException Si l'envoi échoue
     */
    void send(NotificationRequest request);
}
