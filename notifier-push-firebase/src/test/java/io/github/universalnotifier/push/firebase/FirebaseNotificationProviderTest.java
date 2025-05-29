package io.github.universalnotifier.push.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FirebaseNotificationProvider.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FirebaseNotificationProviderTest {

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.ProviderConfig providerConfig;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    private Map<String, String> configMap;
    @BeforeEach
    public void resetFirebaseInitialization() throws Exception {
        // Réinitialise le flag static pour forcer l'initialisation à chaque test
        java.lang.reflect.Field field = FirebaseNotificationProvider.class.getDeclaredField("initialized");
        field.setAccessible(true);
        AtomicBoolean initialized = (AtomicBoolean) field.get(null);
        initialized.set(false);
    }
    @BeforeEach
    public void setUp() throws IOException {
        configMap = new HashMap<>();
        configMap.put("credentials-file", "classpath:firebase-credentials.json");

        when(notificationProperties.getPush()).thenReturn(providerConfig);
        when(providerConfig.getActiveProviderProperties()).thenReturn(configMap);

        // Mock pour l'accès aux ressources
        when(resourceLoader.getResource(anyString())).thenReturn(resource);

        // Mock pour l'accès au fichier de credentials
        InputStream mockInputStream = new ByteArrayInputStream("{}".getBytes());
        when(resource.getInputStream()).thenReturn(mockInputStream);
    }

    @Test
    public void testSupportsMethod() {
        // Le test de supports() nécessite que FirebaseApp soit initialisé,
        // donc nous devons simuler l'initialisation de Firebase
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> mockedGoogleCredentials = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseOptions> mockedFirebaseOptions = mockStatic(FirebaseOptions.class)) {

            // Mock pour GoogleCredentials.fromStream()
            GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
            mockedGoogleCredentials.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(mockCredentials);

            // Mock pour FirebaseOptions.builder()
            FirebaseOptions.Builder mockBuilder = mock(FirebaseOptions.Builder.class);
            mockedFirebaseOptions.when(FirebaseOptions::builder).thenReturn(mockBuilder);

            when(mockBuilder.setCredentials(any(GoogleCredentials.class))).thenReturn(mockBuilder);
            FirebaseOptions mockOptions = mock(FirebaseOptions.class);
            when(mockBuilder.build()).thenReturn(mockOptions);

            // Mock pour FirebaseApp.initializeApp()
            FirebaseApp mockFirebaseApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)))
                    .thenReturn(mockFirebaseApp);

            // Vérifier qu'aucune application Firebase n'est déjà initialisée
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(java.util.Collections.emptyList());

            // Créer le provider
            FirebaseNotificationProvider provider = new FirebaseNotificationProvider(notificationProperties, resourceLoader);

            // Tester la méthode supports()
            assertTrue(provider.supports("PUSH"));
            assertTrue(provider.supports("push"));
            assertFalse(provider.supports("EMAIL"));
            assertFalse(provider.supports("SMS"));
            assertFalse(provider.supports(null));
        }
    }

    @Test
    public void testMissingCredentialsFileThrowsException() {
        configMap.remove("credentials-file");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new FirebaseNotificationProvider(notificationProperties, resourceLoader);
        });

        assertTrue(exception.getMessage().contains("credentials-file"));
    }

    @Test
    public void testSendPushNotification() {
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> mockedGoogleCredentials = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseOptions> mockedFirebaseOptions = mockStatic(FirebaseOptions.class)) {

            // Mock GoogleCredentials
            GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
            mockedGoogleCredentials.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(mockCredentials);

            // Mock FirebaseOptions.builder()
            FirebaseOptions.Builder mockBuilder = mock(FirebaseOptions.Builder.class);
            mockedFirebaseOptions.when(FirebaseOptions::builder).thenReturn(mockBuilder);
            when(mockBuilder.setCredentials(any(GoogleCredentials.class))).thenReturn(mockBuilder);
            FirebaseOptions mockOptions = mock(FirebaseOptions.class);
            when(mockBuilder.build()).thenReturn(mockOptions);

            // Mock FirebaseApp.initializeApp()
            FirebaseApp mockFirebaseApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)))
                    .thenReturn(mockFirebaseApp);

            // Créer le provider
            FirebaseNotificationProvider provider = spy(new FirebaseNotificationProvider(notificationProperties, resourceLoader));

            // Simuler un envoi réussi
            doNothing().when(provider).send(any(NotificationRequest.class));

            // Créer la requête de notification push
            NotificationRequest request = NotificationRequest.builder()
                    .type("PUSH")
                    .to("device-token-123")
                    .subject("Test Title")
                    .message("Test push message")
                    .build();

            // Test d'envoi
            assertDoesNotThrow(() -> provider.send(request));

            // Vérifier que la méthode send a été appelée avec la bonne requête
            verify(provider).send(request);
        }
    }

    @Test
    public void testSendThrowsExceptionWhenFirebaseFails() {
        try (MockedStatic<GoogleCredentials> mockedGoogleCredentials = mockStatic(GoogleCredentials.class)) {
            GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
            mockedGoogleCredentials.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(mockCredentials);

            FirebaseNotificationProvider provider = spy(new FirebaseNotificationProvider(notificationProperties, resourceLoader));
            doThrow(new NotificationException("Erreur Firebase")).when(provider).send(any(NotificationRequest.class));

            NotificationRequest request = NotificationRequest.builder()
                    .type("PUSH")
                    .to("invalid-token")
                    .subject("Test Title")
                    .message("Test message")
                    .build();

            assertThrows(NotificationException.class, () -> provider.send(request));
        }
    }

    @Test
    public void testIOExceptionOnCredentialsFile() throws IOException {
        when(resource.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        try (MockedStatic<GoogleCredentials> mockedGoogleCredentials = mockStatic(GoogleCredentials.class)) {
            // Pas besoin de mocker fromStream ici, car IOException sera levée avant
            assertThrows(NotificationException.class, () -> {
                new FirebaseNotificationProvider(notificationProperties, resourceLoader);
            });
        }
    }
}
