# Spring Boot Universal Notifier Starter

## 📢 Présentation

Le module **Spring Boot Universal Notifier Starter** est une solution complète pour l'envoi de notifications à travers différents canaux (email, SMS, push, WhatsApp, Telegram) dans les applications Spring Boot. Ce starter permet une intégration rapide et une configuration simple via le fichier application.properties/yml.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🚀 Fonctionnalités

- **Interface unifiée** pour envoyer des notifications
- Support pour **plusieurs canaux de communication** :
  - 📧 Email via SendGrid
  - 📱 SMS via Twilio
  - 🔔 Notifications push via Firebase Cloud Messaging
  - 💬 WhatsApp via Twilio
  - 📡 Telegram via Bot API
- **Configuration simple** via application.properties/yml
- **Extensible** - Facile d'ajouter de nouveaux fournisseurs
- **Auto-configuration Spring Boot** - Prêt à l'emploi

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>spring-boot-universal-notifier-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.tky0065:spring-boot-universal-notifier-starter:1.0.0'
```

## ⚙️ Configuration

La configuration se fait entièrement via le fichier `application.yml` ou `application.properties`. Vous pouvez activer et configurer les fournisseurs dont vous avez besoin.

### Example de configuration complète (application.yml)

```yaml
notifier:
  email:
    provider: sendgrid
    properties:
      sendgrid:
        api-key: YOUR_SENDGRID_API_KEY
        from: your@email.com

  sms:
    provider: twilio
    properties:
      twilio:
        account-sid: YOUR_TWILIO_SID
        auth-token: YOUR_TWILIO_TOKEN
        from: "+123456789"

  push:
    provider: firebase
    properties:
      firebase:
        credentials-file: classpath:fcm-service-account.json
        
  whatsapp:
    provider: twilio
    properties:
      twilio:
        account-sid: YOUR_TWILIO_SID
        auth-token: YOUR_TWILIO_TOKEN
        from: "+226XXXXXXXXX"  # Numéro WhatsApp enregistré
        
  telegram:
    provider: bot
    properties:
      bot:
        bot-token: YOUR_TELEGRAM_BOT_TOKEN
        bot-username: YOUR_BOT_USERNAME
```

## 🔍 Utilisation

L'utilisation du notifier est simple et directe grâce à l'interface unifiée `NotificationService`.

### Exemple d'envoi d'email

```java
@Autowired
private NotificationService notificationService;

public void sendWelcomeEmail(String email) {
    NotificationRequest request = NotificationRequest.builder()
            .type("EMAIL")
            .to(email)
            .subject("Bienvenue!")
            .message("Merci de vous être inscrit à notre service.")
            .build();
    
    notificationService.send(request);
}
```

### Exemple d'envoi de SMS

```java
public void sendVerificationSms(String phoneNumber, String code) {
    NotificationRequest request = NotificationRequest.builder()
            .type("SMS")
            .to(phoneNumber)
            .message("Votre code de vérification est: " + code)
            .build();
    
    notificationService.send(request);
}
```

### Exemple d'envoi de notification push

```java
public void sendPushNotification(String deviceToken, String title, String body) {
    NotificationRequest request = NotificationRequest.builder()
            .type("PUSH")
            .to(deviceToken)
            .subject(title)
            .message(body)
            .build();
    
    notificationService.send(request);
}
```

### Exemple d'envoi de message WhatsApp

```java
public void sendWhatsAppMessage(String phoneNumber, String message) {
    NotificationRequest request = NotificationRequest.builder()
            .type("WHATSAPP")
            .to(phoneNumber)  // Le numéro doit être au format international, ex: +226XXXXXXXXX
            .message(message)
            .build();
    
    notificationService.send(request);
}
```

### Exemple d'envoi de message Telegram

```java
public void sendTelegramMessage(String chatId, String title, String message) {
    NotificationRequest request = NotificationRequest.builder()
            .type("TELEGRAM")
            .to(chatId)       // L'identifiant du chat Telegram
            .subject(title)   // Optional, sera affiché en gras
            .message(message)
            .build();
    
    notificationService.send(request);
}
```

## 🧩 Architecture

Le projet est organisé en plusieurs modules pour une meilleure séparation des responsabilités :

- **notifier-core**: Contient les interfaces et classes communes
- **notifier-email-sendgrid**: Implémentation pour l'envoi d'emails via SendGrid
- **notifier-sms-twilio**: Implémentation pour l'envoi de SMS via Twilio
- **notifier-push-firebase**: Implémentation pour l'envoi de notifications push via Firebase
- **notifier-whatsapp-twilio**: Implémentation pour l'envoi de messages WhatsApp via l'API Twilio
- **notifier-telegram-bot**: Implémentation pour l'envoi de messages via l'API Bot Telegram
- **notifier-starter**: Module d'auto-configuration Spring Boot

## 🔧 Extension

Vous pouvez facilement étendre le système pour ajouter de nouveaux fournisseurs de notification en implémentant l'interface `NotificationProvider`.

```java
public class MyCustomProvider implements NotificationProvider {

    @Override
    public boolean supports(String type) {
        return "CUSTOM".equalsIgnoreCase(type);
    }

    @Override
    public void send(NotificationRequest request) {
        // Logique d'envoi personnalisée
    }
}
```

## 📝 ToDo

- Améliorer la documentation des API
- Ajouter des fonctionnalités de retry/fallback
- Ajouter des templates pour les notifications
- Améliorer la gestion des erreurs et le reporting
- Ajouter des tests d'intégration supplémentaires

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à soumettre une issue ou une pull request.

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👨‍💻 Auteur

Yacouba KONE - [EnokDev](https://enokdev-com.vercel.app/)
