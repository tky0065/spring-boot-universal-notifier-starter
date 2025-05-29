package io.github.universalnotifier.starter;

import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.service.DefaultNotificationService;
import io.github.universalnotifier.core.service.NotificationProvider;
import io.github.universalnotifier.core.service.NotificationService;
import io.github.universalnotifier.email.sendgrid.SendGridNotificationProvider;
import io.github.universalnotifier.push.firebase.FirebaseNotificationProvider;
import io.github.universalnotifier.sms.twilio.TwilioNotificationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration automatique du système de notification universel.
 * Cette classe configure tous les beans nécessaires pour le fonctionnement
 * du système de notification en fonction des propriétés définies.
 */
@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class NotifierAutoConfiguration {

    /**
     * Crée le service principal de notification qui va orchestrer les différents fournisseurs.
     *
     * @param providers Liste des fournisseurs de notification disponibles
     * @return Le service de notification configuré
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationService notificationService(List<NotificationProvider> providers) {
        return new DefaultNotificationService(providers);
    }

    /**
     * Configuration du fournisseur de notification Email SendGrid.
     */
    @Configuration
    @ConditionalOnClass(name = "com.sendgrid.SendGrid")
    @ConditionalOnProperty(prefix = "notifier.email", name = "provider", havingValue = "sendgrid")
    public static class SendGridConfiguration {

        /**
         * Crée le fournisseur de notification SendGrid.
         *
         * @param properties Configuration des propriétés de notification
         * @return Le fournisseur SendGrid configuré
         */
        @Bean
        @ConditionalOnMissingBean
        public NotificationProvider sendGridNotificationProvider(NotificationProperties properties) {
            return new SendGridNotificationProvider(properties);
        }
    }

    /**
     * Configuration du fournisseur de notification SMS Twilio.
     */
    @Configuration
    @ConditionalOnClass(name = "com.twilio.Twilio")
    @ConditionalOnProperty(prefix = "notifier.sms", name = "provider", havingValue = "twilio")
    public static class TwilioConfiguration {

        /**
         * Crée le fournisseur de notification Twilio.
         *
         * @param properties Configuration des propriétés de notification
         * @return Le fournisseur Twilio configuré
         */
        @Bean
        @ConditionalOnMissingBean
        public NotificationProvider twilioNotificationProvider(NotificationProperties properties) {
            return new TwilioNotificationProvider(properties);
        }
    }

    /**
     * Configuration du fournisseur de notification Push Firebase.
     */
    @Configuration
    @ConditionalOnClass(name = "com.google.firebase.messaging.FirebaseMessaging")
    @ConditionalOnProperty(prefix = "notifier.push", name = "provider", havingValue = "firebase")
    public static class FirebaseConfiguration {

        /**
         * Crée le fournisseur de notification Firebase.
         *
         * @param properties Configuration des propriétés de notification
         * @param resourceLoader Chargeur de ressources Spring
         * @return Le fournisseur Firebase configuré
         */
        @Bean
        @ConditionalOnMissingBean
        public NotificationProvider firebaseNotificationProvider(NotificationProperties properties, ResourceLoader resourceLoader) {
            return new FirebaseNotificationProvider(properties, resourceLoader);
        }
    }
}
