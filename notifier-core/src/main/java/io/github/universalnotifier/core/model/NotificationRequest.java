package io.github.universalnotifier.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant une demande de notification.
 * Cette classe est utilisée pour envoyer des notifications à travers différents canaux.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    /**
     * Type de notification (EMAIL, SMS, PUSH)
     */
    private String type;

    /**
     * Destinataire de la notification (email, numéro de téléphone, token de device)
     */
    private String to;

    /**
     * Sujet de la notification (optionnel, principalement utilisé pour les emails)
     */
    private String subject;

    /**
     * Contenu du message à envoyer
     */
    private String message;

    /**
     * Données supplémentaires spécifiques au canal (optionnel)
     * Peut être utilisé pour des options spécifiques aux fournisseurs
     */
    private Object additionalData;
}
