package io.github.universalnotifier.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration des propriétés pour le module de notification universel.
 * Cette classe mappe les propriétés définies dans application.properties/yml
 * sous le préfixe "notifier".
 */
@Data
@ConfigurationProperties(prefix = "notifier")
public class NotificationProperties {

    /**
     * Configuration du service d'email
     */
    private ProviderConfig email = new ProviderConfig();

    /**
     * Configuration du service SMS
     */
    private ProviderConfig sms = new ProviderConfig();

    /**
     * Configuration du service de notifications push
     */
    private ProviderConfig push = new ProviderConfig();

    /**
     * Configuration du service WhatsApp
     */
    private ProviderConfig whatsapp = new ProviderConfig();

    /**
     * Configuration du service Telegram
     */
    private ProviderConfig telegram = new ProviderConfig();

    /**
     * Classe de configuration pour un type de fournisseur spécifique
     */
    @Data
    public static class ProviderConfig {
        /**
         * Nom du fournisseur à utiliser (ex: sendgrid, twilio, firebase)
         */
        private String provider;

        /**
         * Propriétés spécifiques au fournisseur
         * La clé correspond au nom du fournisseur, la valeur est une map de ses propriétés de configuration
         */
        private Map<String, Map<String, String>> properties = new HashMap<>();

        /**
         * Récupère les propriétés de configuration pour le fournisseur actif
         *
         * @return Map de propriétés pour le fournisseur spécifié ou une map vide
         */
        public Map<String, String> getActiveProviderProperties() {
            if (provider == null) {
                return new HashMap<>();
            }
            return properties.getOrDefault(provider, new HashMap<>());
        }
    }
}
