package io.github.universalnotifier.whatsapp.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WhatsAppNotificationProviderTest {

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.ProviderConfig providerConfig;

    private WhatsAppNotificationProvider provider;

    private Map<String, String> configMap;

    @BeforeEach
    public void setUp() {
        configMap = new HashMap<>();
        configMap.put("account-sid", "test-account-sid");
        configMap.put("auth-token", "test-auth-token");
        configMap.put("from", "+33123456789");

        when(notificationProperties.getWhatsapp()).thenReturn(providerConfig);
        when(providerConfig.getActiveProviderProperties()).thenReturn(configMap);

        // Créer une instance de provider sans appeler réellement Twilio.init()
        // car nous allons le tester séparément
        provider = new WhatsAppNotificationProvider(notificationProperties);
    }

    @Test
    public void testSupportsMethod() {
        assertTrue(provider.supports("WHATSAPP"));
        assertTrue(provider.supports("whatsapp"));
        assertFalse(provider.supports("SMS"));
        assertFalse(provider.supports("EMAIL"));
        assertFalse(provider.supports(null));
    }

    @Test
    public void testConstructorInitializesTwilio() {
        try (MockedStatic<Twilio> mockedTwilio = mockStatic(Twilio.class)) {
            // Supprime l'instance existante et en crée une nouvelle pour ce test
            new WhatsAppNotificationProvider(notificationProperties);

            // Vérifie que Twilio.init() est appelé avec les bons paramètres
            mockedTwilio.verify(() -> Twilio.init(
                    eq("test-account-sid"),
                    eq("test-auth-token")));
        }
    }

    @Test
    public void testMissingAccountSidThrowsException() {
        configMap.remove("account-sid");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new WhatsAppNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("account-sid"));
    }

    @Test
    public void testMissingAuthTokenThrowsException() {
        configMap.remove("auth-token");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new WhatsAppNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("auth-token"));
    }

    @Test
    public void testMissingFromNumberThrowsException() {
        configMap.remove("from");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new WhatsAppNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("from"));
    }

    @Test
    public void testSendWhatsAppMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .type("WHATSAPP")
                .to("+33687654321")
                .message("Test message")
                .build();

        try (MockedStatic<Message> mockedMessage = mockStatic(Message.class)) {
            // Mock pour Message.creator()
            Message.Creator creator = mock(Message.Creator.class);
            Message message = mock(Message.class);

            mockedMessage.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString())).thenReturn(creator);
            when(creator.create()).thenReturn(message);
            when(message.getSid()).thenReturn("test-sid");

            // Test de l'envoi de message
            assertDoesNotThrow(() -> provider.send(request));

            // Vérifier que la méthode creator est appelée avec le bon préfixe "whatsapp:"
            mockedMessage.verify(() -> Message.creator(
                    argThat(phone -> phone.toString().equals("whatsapp:+33687654321")),
                    argThat(phone -> phone.toString().equals("whatsapp:+33123456789")),
                    eq("Test message")));
        }
    }

    @Test
    public void testSendThrowsExceptionWhenTwilioFails() {
        NotificationRequest request = NotificationRequest.builder()
                .type("WHATSAPP")
                .to("+33687654321")
                .message("Test message")
                .build();

        try (MockedStatic<Message> mockedMessage = mockStatic(Message.class)) {
            // Mock pour Message.creator() qui déclenche une exception
            Message.Creator creator = mock(Message.Creator.class);

            mockedMessage.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString())).thenReturn(creator);
            when(creator.create()).thenThrow(new RuntimeException("Test exception"));

            // Test de l'exception
            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }
}
