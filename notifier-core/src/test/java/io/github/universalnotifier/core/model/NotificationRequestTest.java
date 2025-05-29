package io.github.universalnotifier.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe NotificationRequest.
 */
public class NotificationRequestTest {

    @Test
    public void testNotificationRequestBuilder() {
        // Test de la construction avec builder pattern
        NotificationRequest request = NotificationRequest.builder()
                .type("EMAIL")
                .to("test@example.com")
                .subject("Test Subject")
                .message("Test Message")
                .additionalData(new TestAdditionalData("test-value"))
                .build();

        // Vérifications
        assertEquals("EMAIL", request.getType());
        assertEquals("test@example.com", request.getTo());
        assertEquals("Test Subject", request.getSubject());
        assertEquals("Test Message", request.getMessage());
        assertNotNull(request.getAdditionalData());
        assertTrue(request.getAdditionalData() instanceof TestAdditionalData);
        assertEquals("test-value", ((TestAdditionalData) request.getAdditionalData()).getValue());
    }

    @Test
    public void testNotificationRequestConstructor() {
        // Test du constructeur
        TestAdditionalData additionalData = new TestAdditionalData("test-value");
        NotificationRequest request = new NotificationRequest("SMS", "+33612345678", null, "Test SMS", additionalData);

        // Vérifications
        assertEquals("SMS", request.getType());
        assertEquals("+33612345678", request.getTo());
        assertNull(request.getSubject());
        assertEquals("Test SMS", request.getMessage());
        assertEquals(additionalData, request.getAdditionalData());
    }

    @Test
    public void testEquals() {
        NotificationRequest request1 = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);
        NotificationRequest request2 = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);
        NotificationRequest request3 = new NotificationRequest("SMS", "test@example.com", "Subject", "Message", null);

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    public void testHashCode() {
        NotificationRequest request1 = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);
        NotificationRequest request2 = new NotificationRequest("EMAIL", "test@example.com", "Subject", "Message", null);

        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    public void testToString() {
        NotificationRequest request = new NotificationRequest("PUSH", "token123", "Title", "Body", null);

        String toString = request.toString();
        assertTrue(toString.contains("PUSH"));
        assertTrue(toString.contains("token123"));
        assertTrue(toString.contains("Title"));
        assertTrue(toString.contains("Body"));
    }

    /**
     * Classe utilitaire pour tester les données supplémentaires.
     */
    private static class TestAdditionalData {
        private final String value;

        public TestAdditionalData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
