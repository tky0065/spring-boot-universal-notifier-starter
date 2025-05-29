package io.github.universalnotifier.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application d'exemple démontrant l'utilisation du module de notification universel.
 * Cette application expose une API REST simple pour envoyer des notifications.
 *
 * @author Universal Notifier Team
 */
@SpringBootApplication
public class NotificationExampleApplication {

    /**
     * Point d'entrée principal de l'application d'exemple.
     *
     * @param args Arguments de ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationExampleApplication.class, args);
    }
}
