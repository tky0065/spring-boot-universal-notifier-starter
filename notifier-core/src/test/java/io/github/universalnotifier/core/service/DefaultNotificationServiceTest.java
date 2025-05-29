package io.github.universalnotifier.core.service;

import io.github.universalnotifier.core.model.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le DefaultNotificationService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultNotificationServiceTest {

    @Mock
    private NotificationProvider emailProvider;

    @Mock
    private NotificationProvider smsProvider;

    @Mock
    private NotificationProvider pushProvider;

    private DefaultNotificationService notificationService;

    @BeforeEach
    public void setUp() {
        // Configuration des supports pour chaque provider
        when(emailProvider.supports("EMAIL")).thenReturn(true);
        when(smsProvider.supports("SMS")).thenReturn(true);
        when(pushProvider.supports("PUSH")).thenReturn(true);

        // Configuration pour tous les autres types
        when(emailProvider.supports(argThat(t -> t != null && !t.equals("EMAIL")))).thenReturn(false);
        when(smsProvider.supports(argThat(t -> t != null && !t.equals("SMS")))).thenReturn(false);
        when(pushProvider.supports(argThat(t -> t != null && !t.equals("PUSH")))).thenReturn(false);

        List<NotificationProvider> providers = Arrays.asList(emailProvider, smsProvider, pushProvider);
        notificationService = new DefaultNotificationService(providers);
    }

    @Test
    public void testSendEmailNotification() {
        NotificationRequest request = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);

        notificationService.send(request);

        verify(emailProvider).send(request);
        verify(smsProvider, never()).send(any());
        verify(pushProvider, never()).send(any());
    }

    @Test
    public void testSendSmsNotification() {
        NotificationRequest request = new NotificationRequest("SMS", "+33612345678", null, "Message", null);

        notificationService.send(request);

        verify(smsProvider).send(request);
        verify(emailProvider, never()).send(any());
        verify(pushProvider, never()).send(any());
    }

    @Test
    public void testSendPushNotification() {
        NotificationRequest request = new NotificationRequest("PUSH", "device-token", "Title", "Message", null);

        notificationService.send(request);

        verify(pushProvider).send(request);
        verify(emailProvider, never()).send(any());
        verify(smsProvider, never()).send(any());
    }

    @Test
    public void testSendWithNullRequest() {
        assertThrows(NotificationException.class, () ->
                notificationService.send(null)
        );
    }

    @Test
    public void testSendWithNullType() {
        NotificationRequest request = new NotificationRequest(null, "test@example.com", "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithEmptyType() {
        NotificationRequest request = new NotificationRequest("", "test@example.com", "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithNullTo() {
        NotificationRequest request = new NotificationRequest("EMAIL", null, "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithEmptyTo() {
        NotificationRequest request = new NotificationRequest("EMAIL", "", "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithNullMessage() {
        NotificationRequest request = new NotificationRequest("EMAIL", "test@example.com", "Subject", null, null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithEmptyMessage() {
        NotificationRequest request = new NotificationRequest("EMAIL", "test@example.com", "Subject", "", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testSendWithUnsupportedType() {
        NotificationRequest request = new NotificationRequest("UNSUPPORTED", "test@example.com", "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }

    @Test
    public void testNoProviders() {
        DefaultNotificationService serviceWithNoProviders = new DefaultNotificationService(Collections.emptyList());

        NotificationRequest request = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);

        assertThrows(NotificationException.class, () ->
                serviceWithNoProviders.send(request)
        );
    }

    @Test
    public void testProviderThrowsException() {
        NotificationRequest request = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);

        // Utiliser lenient ici pour éviter l'erreur UnnecessaryStubbingException
        lenient().doThrow(new RuntimeException("Test exception")).when(emailProvider).send(request);

        assertThrows(NotificationException.class, () ->
                notificationService.send(request)
        );
    }
}
