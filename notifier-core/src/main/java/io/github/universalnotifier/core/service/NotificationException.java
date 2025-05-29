package io.github.universalnotifier.core.service;

/**
 * Exception dédiée aux erreurs liées à l'envoi des notifications.
 * Cette exception encapsule les erreurs provenant des différents
 * fournisseurs de notifications (email, SMS, push, etc.)
 */
public class NotificationException extends RuntimeException {

    /**
     * Crée une nouvelle exception avec le message spécifié.
     *
     * @param message Description de l'erreur
     */
    public NotificationException(String message) {
        super(message);
    }

    /**
     * Crée une nouvelle exception avec le message et la cause spécifiés.
     *
     * @param message Description de l'erreur
     * @param cause Exception originale ayant causé l'erreur
     */
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
