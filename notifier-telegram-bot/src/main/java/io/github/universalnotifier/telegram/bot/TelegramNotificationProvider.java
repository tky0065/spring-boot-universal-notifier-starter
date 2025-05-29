package io.github.universalnotifier.telegram.bot;

import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import io.github.universalnotifier.core.service.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fournisseur de notification Telegram utilisant l'API Bot.
 * Cette classe implémente la logique d'envoi de messages via un bot Telegram.
 */
public class TelegramNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationProvider.class);

    private static final String TYPE = "TELEGRAM";
    private static final String BOT_TOKEN = "bot-token";
    private static final String BOT_USERNAME = "bot-username";

    private final TelegramBot bot;
    private boolean botRegistered = false;

    /**
     * Constructeur du fournisseur Telegram utilisant les propriétés configurées.
     *
     * @param properties Configuration des propriétés de notification
     * @throws NotificationException si l'initialisation du bot Telegram échoue
     */
    public TelegramNotificationProvider(NotificationProperties properties) {
        Map<String, String> config = properties.getTelegram().getActiveProviderProperties();

        String botToken = config.get(BOT_TOKEN);
        if (botToken == null || botToken.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.telegram.bot.bot-token' est requise pour l'utilisation de Telegram");
        }

        String botUsername = config.get(BOT_USERNAME);
        if (botUsername == null || botUsername.isEmpty()) {
            throw new IllegalArgumentException("La propriété 'notifier.telegram.bot.bot-username' est requise pour l'utilisation de Telegram");
        }

        this.bot = new TelegramBot(botToken, botUsername);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this.bot);
            botRegistered = true;
            logger.info("Bot Telegram '{}' enregistré avec succès", botUsername);
        } catch (TelegramApiException e) {
            logger.error("Erreur lors de l'initialisation du bot Telegram", e);
            throw new NotificationException("Erreur lors de l'initialisation du bot Telegram", e);
        }
    }

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    @Override
    public void send(NotificationRequest request) {
        if (!botRegistered) {
            throw new NotificationException("Le bot Telegram n'est pas correctement initialisé");
        }

        try {
            logger.debug("Préparation d'un message Telegram à envoyer à: {}", request.getTo());

            SendMessage message = new SendMessage();
            message.setChatId(request.getTo());

            // Si un sujet est fourni, l'ajouter en gras au début du message
            if (request.getSubject() != null && !request.getSubject().isEmpty()) {
                message.setText("*" + request.getSubject() + "*\n\n" + request.getMessage());
                message.enableMarkdown(true);
            } else {
                message.setText(request.getMessage());
            }

            bot.execute(message);
            logger.info("Message Telegram envoyé avec succès au chat ID: {}", request.getTo());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du message Telegram", e);
            throw new NotificationException("Erreur lors de l'envoi du message Telegram", e);
        }
    }

    /**
     * Classe interne représentant le bot Telegram qui gère l'envoi des messages.
     */
    private static class TelegramBot extends TelegramLongPollingBot {

        private final String botToken;
        private final String botUsername;
        private final Map<String, String> chatIdToUsername = new ConcurrentHashMap<>();

        public TelegramBot(String botToken, String botUsername) {
            super(botToken);
            this.botToken = botToken;
            this.botUsername = botUsername;
        }

        @Override
        public String getBotUsername() {
            return botUsername;
        }

        @Override
        public void onUpdateReceived(Update update) {
            // Stocker la correspondance chatId -> username pour référence future
            if (update.hasMessage() && update.getMessage().hasFrom()) {
                String chatId = update.getMessage().getChatId().toString();
                String username = update.getMessage().getFrom().getUserName();
                if (username != null) {
                    chatIdToUsername.put(chatId, username);
                }
            }
        }
    }
}
