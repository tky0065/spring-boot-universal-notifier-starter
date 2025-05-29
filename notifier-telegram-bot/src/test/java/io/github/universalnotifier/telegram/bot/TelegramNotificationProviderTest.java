package io.github.universalnotifier.telegram.bot;

import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationProviderTest {

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.ProviderConfig providerConfig;

    private Map<String, String> configMap;

    // Ne pas initialiser le provider ici car nous devons modifier la configuration
    // avant de créer l'instance pour certains tests
    private TelegramNotificationProvider provider;

    @BeforeEach
    public void setUp() {
        configMap = new HashMap<>();
        configMap.put("bot-token", "test-bot-token");
        configMap.put("bot-username", "test-bot-username");

        when(notificationProperties.getTelegram()).thenReturn(providerConfig);
        when(providerConfig.getActiveProviderProperties()).thenReturn(configMap);
    }

    @Test
    public void testSupportsMethod() {
        // Créer un provider avec mocking pour le constructeur
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Permet d'éviter l'exception lors de l'initialisation du bot
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);
            doNothing().when(api).registerBot(any());

            provider = new TelegramNotificationProvider(notificationProperties);

            assertTrue(provider.supports("TELEGRAM"));
            assertTrue(provider.supports("telegram"));
            assertFalse(provider.supports("SMS"));
            assertFalse(provider.supports("EMAIL"));
            assertFalse(provider.supports(null));
        }
    }

    @Test
    public void testConstructorInitializesTelegramBot() {
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Mock pour la création de l'API Telegram
            mockedApi.when(() -> new TelegramBotsApi(DefaultBotSession.class)).thenReturn(api);

            // Créer le provider
            provider = new TelegramNotificationProvider(notificationProperties);

            // Vérifier que la méthode registerBot est appelée
            verify(api).registerBot(any());
        }
    }

    @Test
    public void testConstructorThrowsExceptionWhenTelegramFails() {
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Mock pour la création de l'API Telegram
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);

            // Simulation d'une exception lors de l'enregistrement du bot
            doThrow(new TelegramApiException("Test exception")).when(api).registerBot(any());

            // Vérifier que l'exception est propagée
            assertThrows(NotificationException.class, () ->
                new TelegramNotificationProvider(notificationProperties)
            );
        }
    }

    @Test
    public void testMissingBotTokenThrowsException() {
        configMap.remove("bot-token");

        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new TelegramNotificationProvider(notificationProperties)
            );

            assertTrue(exception.getMessage().contains("bot-token"));
        }
    }

    @Test
    public void testMissingBotUsernameThrowsException() {
        configMap.remove("bot-username");

        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new TelegramNotificationProvider(notificationProperties)
            );

            assertTrue(exception.getMessage().contains("bot-username"));
        }
    }

    @Test
    public void testSendTelegramMessage() throws Exception {
        // Créer un provider avec mocking
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Permet d'éviter l'exception lors de l'initialisation du bot
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);
            doNothing().when(api).registerBot(any());

            // Créer le provider (avec un bot mocké interne)
            provider = spy(new TelegramNotificationProvider(notificationProperties));

            // Créer une requête de notification
            NotificationRequest request = NotificationRequest.builder()
                    .type("TELEGRAM")
                    .to("123456789")
                    .subject("Test Title")
                    .message("Test message")
                    .build();

            // Simuler que le bot est enregistré
            doReturn(true).when(provider).isBotRegistered();

            // Créer un mock pour SendMessage
            Message responseMessage = mock(Message.class);

            // Intercepter les appels à execute dans le bot interne
            doReturn(responseMessage).when(provider).executeMessage(any(SendMessage.class));

            // Envoyer le message
            assertDoesNotThrow(() -> provider.send(request));

            // Vérifier que la méthode execute est appelée avec les bons paramètres
            verify(provider).executeMessage(argThat(message -> {
                return message.getChatId().equals("123456789") &&
                       message.getText().contains("Test Title") &&
                       message.getText().contains("Test message") &&
                       message.getParseMode() != null;
            }));
        }
    }

    @Test
    public void testSendWithoutSubject() throws Exception {
        // Créer un provider avec mocking
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Permet d'éviter l'exception lors de l'initialisation du bot
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);
            doNothing().when(api).registerBot(any());

            // Créer le provider (avec un bot mocké interne)
            provider = spy(new TelegramNotificationProvider(notificationProperties));

            // Créer une requête de notification sans sujet
            NotificationRequest request = NotificationRequest.builder()
                    .type("TELEGRAM")
                    .to("123456789")
                    .message("Test message only")
                    .build();

            // Simuler que le bot est enregistré
            doReturn(true).when(provider).isBotRegistered();

            // Créer un mock pour SendMessage
            Message responseMessage = mock(Message.class);

            // Intercepter les appels à execute dans le bot interne
            doReturn(responseMessage).when(provider).executeMessage(any(SendMessage.class));

            // Envoyer le message
            assertDoesNotThrow(() -> provider.send(request));

            // Vérifier que la méthode execute est appelée avec les bons paramètres
            verify(provider).executeMessage(argThat(message -> {
                return message.getChatId().equals("123456789") &&
                       message.getText().equals("Test message only") &&
                       message.getParseMode() == null;
            }));
        }
    }

    @Test
    public void testSendThrowsExceptionWhenBotNotRegistered() {
        // Créer un provider avec mocking
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Permet d'éviter l'exception lors de l'initialisation du bot
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);
            doNothing().when(api).registerBot(any());

            // Créer le provider (avec un bot mocké interne)
            provider = spy(new TelegramNotificationProvider(notificationProperties));

            // Simuler que le bot n'est pas enregistré
            doReturn(false).when(provider).isBotRegistered();

            // Créer une requête de notification
            NotificationRequest request = NotificationRequest.builder()
                    .type("TELEGRAM")
                    .to("123456789")
                    .message("Test message")
                    .build();

            // Vérifier que l'exception est levée
            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }

    @Test
    public void testSendThrowsExceptionWhenTelegramFails() throws Exception {
        // Créer un provider avec mocking
        try (MockedStatic<TelegramBotsApi> mockedApi = mockStatic(TelegramBotsApi.class)) {
            TelegramBotsApi api = mock(TelegramBotsApi.class);

            // Permet d'éviter l'exception lors de l'initialisation du bot
            mockedApi.when(() -> new TelegramBotsApi(any())).thenReturn(api);
            doNothing().when(api).registerBot(any());

            // Créer le provider (avec un bot mocké interne)
            provider = spy(new TelegramNotificationProvider(notificationProperties));

            // Simuler que le bot est enregistré
            doReturn(true).when(provider).isBotRegistered();

            // Simuler une exception lors de l'envoi
            doThrow(new TelegramApiException("Test exception")).when(provider).executeMessage(any(SendMessage.class));

            // Créer une requête de notification
            NotificationRequest request = NotificationRequest.builder()
                    .type("TELEGRAM")
                    .to("123456789")
                    .message("Test message")
                    .build();

            // Vérifier que l'exception est propagée
            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }
}
