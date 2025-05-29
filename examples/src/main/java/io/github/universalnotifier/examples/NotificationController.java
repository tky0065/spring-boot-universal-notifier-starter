package io.github.universalnotifier.examples;

import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant des endpoints pour tester les différentes
 * fonctionnalités du module de notification universel.
 *
 * Ce contrôleur permet d'envoyer des emails, SMS et notifications push
 * à travers une API REST simple.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Constructeur avec injection du service de notification.
     *
     * @param notificationService Service utilisé pour envoyer les notifications
     */
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint pour envoyer un email.
     *
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param message Contenu de l'email
     * @return Réponse HTTP avec message de confirmation
     */
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String message) {

        NotificationRequest request = NotificationRequest.builder()
                .type("EMAIL")
                .to(to)
                .subject(subject)
                .message(message)
                .build();

        notificationService.send(request);

        return ResponseEntity.ok("Email envoyé avec succès à " + to);
    }

    /**
     * Endpoint pour envoyer un SMS.
     *
     * @param to Numéro de téléphone du destinataire
     * @param message Contenu du SMS
     * @return Réponse HTTP avec message de confirmation
     */
    @PostMapping("/sms")
    public ResponseEntity<String> sendSms(
            @RequestParam String to,
            @RequestParam String message) {

        NotificationRequest request = NotificationRequest.builder()
                .type("SMS")
                .to(to)
                .message(message)
                .build();

        notificationService.send(request);

        return ResponseEntity.ok("SMS envoyé avec succès à " + to);
    }

    /**
     * Endpoint pour envoyer une notification push.
     *
     * @param token Token du device destinataire
     * @param title Titre de la notification
     * @param message Corps de la notification
     * @return Réponse HTTP avec message de confirmation
     */
    @PostMapping("/push")
    public ResponseEntity<String> sendPush(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String message) {

        NotificationRequest request = NotificationRequest.builder()
                .type("PUSH")
                .to(token)
                .subject(title)
                .message(message)
                .build();

        notificationService.send(request);

        return ResponseEntity.ok("Notification push envoyée avec succès");
    }

    /**
     * Endpoint pour envoyer une notification générique
     * à travers n'importe quel canal configuré.
     *
     * @param request Détails de la notification à envoyer
     * @return Réponse HTTP avec message de confirmation
     */
    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.send(request);
        return ResponseEntity.ok("Notification envoyée avec succès");
    }
}
