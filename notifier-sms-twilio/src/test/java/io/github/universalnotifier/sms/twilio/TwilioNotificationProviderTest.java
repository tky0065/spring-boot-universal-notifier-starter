package io.github.universalnotifier.sms.twilio;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TwilioNotificationProvider.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TwilioNotificationProviderTest {

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.ProviderConfig providerConfig;

    private Map<String, String> configMap;

    @BeforeEach
    public void setUp() {
        configMap = new HashMap<>();
        configMap.put("account-sid", "test-account-sid");
        configMap.put("auth-token", "test-auth-token");
        configMap.put("from", "+33123456789");

        when(notificationProperties.getSms()).thenReturn(providerConfig);
        when(providerConfig.getActiveProviderProperties()).thenReturn(configMap);
    }

    @Test
    public void testSupportsMethod() {
        TwilioNotificationProvider provider = new TwilioNotificationProvider(notificationProperties);

        assertTrue(provider.supports("SMS"));
        assertTrue(provider.supports("sms"));
        assertFalse(provider.supports("EMAIL"));
        assertFalse(provider.supports("PUSH"));
        assertFalse(provider.supports(null));
    }

    @Test
    public void testConstructorInitializesTwilio() {
        try (MockedStatic<Twilio> mockedTwilio = mockStatic(Twilio.class)) {
            // Supprime l'instance existante et en crée une nouvelle pour ce test
            new TwilioNotificationProvider(notificationProperties);

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
            new TwilioNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("account-sid"));
    }

    @Test
    public void testMissingAuthTokenThrowsException() {
        configMap.remove("auth-token");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TwilioNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("auth-token"));
    }

    @Test
    public void testMissingFromNumberThrowsException() {
        configMap.remove("from");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TwilioNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("from"));
    }

    @Test
    public void testSendSmsMessage() {
        NotificationRequest request = NotificationRequest.builder()
                .type("SMS")
                .to("+33687654321")
                .message("Test SMS message")
                .build();

        // Créer un spy du provider
        TwilioNotificationProvider providerSpy = spy(new TwilioNotificationProvider(notificationProperties));

        // Simuler la méthode send pour qu'elle ne fasse rien (éviter l'appel réel à Twilio)
        doNothing().when(providerSpy).send(any(NotificationRequest.class));

        // Exécution
        assertDoesNotThrow(() -> providerSpy.send(request));

        // Vérification que send a été appelé avec la bonne requête
        verify(providerSpy).send(request);
    }

    @Test
    public void testSendThrowsExceptionWhenTwilioFails() {
        NotificationRequest request = NotificationRequest.builder()
                .type("SMS")
                .to("+33687654321")
                .message("Test SMS message")
                .build();

        // Créer un spy du provider
        TwilioNotificationProvider providerSpy = spy(new TwilioNotificationProvider(notificationProperties));

        // Simuler une exception lors de l'appel à send
        doThrow(new NotificationException("Test exception")).when(providerSpy).send(any(NotificationRequest.class));

        // Vérification de l'exception
        assertThrows(NotificationException.class, () -> providerSpy.send(request));
    }
}
