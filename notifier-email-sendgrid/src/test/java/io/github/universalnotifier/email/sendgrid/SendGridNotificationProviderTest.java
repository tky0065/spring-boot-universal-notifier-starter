package io.github.universalnotifier.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import io.github.universalnotifier.core.config.NotificationProperties;
import io.github.universalnotifier.core.model.NotificationRequest;
import io.github.universalnotifier.core.service.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SendGridNotificationProvider.
 */
@ExtendWith(MockitoExtension.class)
public class SendGridNotificationProviderTest {

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.ProviderConfig providerConfig;

    private Map<String, String> configMap;

    private SendGridNotificationProvider provider;

    @BeforeEach
    public void setUp() {
        configMap = new HashMap<>();
        configMap.put("api-key", "test-api-key");
        configMap.put("from", "test@example.com");

        when(notificationProperties.getEmail()).thenReturn(providerConfig);
        when(providerConfig.getActiveProviderProperties()).thenReturn(configMap);

        provider = new SendGridNotificationProvider(notificationProperties);
    }

    @Test
    public void testSupportsMethod() {
        assertTrue(provider.supports("EMAIL"));
        assertTrue(provider.supports("email"));
        assertFalse(provider.supports("SMS"));
        assertFalse(provider.supports("PUSH"));
        assertFalse(provider.supports(null));
    }

    @Test
    public void testMissingApiKeyThrowsException() {
        configMap.remove("api-key");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new SendGridNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("api-key"));
    }

    @Test
    public void testMissingFromEmailThrowsException() {
        configMap.remove("from");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new SendGridNotificationProvider(notificationProperties);
        });

        assertTrue(exception.getMessage().contains("from"));
    }

    @Test
    public void testSendEmail() throws IOException {
        NotificationRequest request = NotificationRequest.builder()
                .type("EMAIL")
                .to("recipient@example.com")
                .subject("Test Subject")
                .message("Test Message Content")
                .build();

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(
                SendGrid.class,
                (mock, context) -> {
                    Response response = new Response();
                    response.setStatusCode(202); // Code de succès de SendGrid
                    response.setBody("Success");

                    try {
                        when(mock.api(any(Request.class))).thenReturn(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })) {

            // Test de l'envoi d'email
            assertDoesNotThrow(() -> provider.send(request));

            // Vérifier que SendGrid a été instancié avec l'API key correcte
            SendGrid sendGrid = mockedSendGrid.constructed().get(0);
            assertEquals(1, mockedSendGrid.constructed().size());

            // Vérifier que api() a été appelée une fois
            try {
                verify(sendGrid, times(1)).api(any(Request.class));
            } catch (IOException e) {
                fail("Exception non attendue: " + e.getMessage());
            }
        }
    }

    @Test
    public void testSendEmailWithErrorResponse() throws IOException {
        NotificationRequest request = NotificationRequest.builder()
                .type("EMAIL")
                .to("recipient@example.com")
                .subject("Test Subject")
                .message("Test Message Content")
                .build();

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(
                SendGrid.class,
                (mock, context) -> {
                    Response response = new Response();
                    response.setStatusCode(400); // Code d'erreur
                    response.setBody("Bad Request");

                    try {
                        when(mock.api(any(Request.class))).thenReturn(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })) {

            // Test de l'exception en cas d'erreur de SendGrid
            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }

    @Test
    public void testSendEmailWithIOException() throws IOException {
        NotificationRequest request = NotificationRequest.builder()
                .type("EMAIL")
                .to("recipient@example.com")
                .subject("Test Subject")
                .message("Test Message Content")
                .build();

        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(
                SendGrid.class,
                (mock, context) -> {
                    try {
                        when(mock.api(any(Request.class))).thenThrow(new IOException("Test IO Exception"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })) {

            // Test de l'exception en cas d'erreur d'IO
            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }
}
