package com.tradesim;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.decorator.NotificationDecorator;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NotificationService and the Decorator chain.
 * Uses a MockNotificationService to capture messages without I/O.
 */
class NotificationServiceTest {

    /** Minimal mock that captures messages for assertion. */
    static class MockNotificationService implements NotificationService {
        final List<String> received = new ArrayList<>();
        @Override
        public void send(String message) { received.add(message); }
    }

    @Test
    void consoleNotifier_sendsMessage() {
        // Arrange
        ConsoleNotificationService svc = new ConsoleNotificationService();

        // Act & Assert: just verify it does not throw
        assertDoesNotThrow(() -> svc.send("Test notification"));
    }

    @Test
    void dashboardNotifier_storesMessage() {
        // Arrange
        MockNotificationService mock = new MockNotificationService();
        DashboardNotifier dashboard = new DashboardNotifier(mock);

        // Act
        dashboard.send("Order executed: BUY 5 AAPL at $100");

        // Assert: dashboard captured the message
        assertEquals(1, dashboard.getMessages().size());
        assertEquals("Order executed: BUY 5 AAPL at $100", dashboard.getMessages().get(0));
    }

    @Test
    void dashboardNotifier_delegatesToWrapped() {
        // Arrange
        MockNotificationService mock = new MockNotificationService();
        DashboardNotifier dashboard = new DashboardNotifier(mock);

        // Act
        dashboard.send("hello");

        // Assert: wrapped notifier also received the message
        assertEquals(1, mock.received.size());
        assertEquals("hello", mock.received.get(0));
    }

    @Test
    void decoratorChain_allWrappedNotifiersFire() {
        // Arrange: build a chain: OuterDecorator -> DashboardNotifier -> MockNotificationService
        MockNotificationService base = new MockNotificationService();
        DashboardNotifier middle = new DashboardNotifier(base);

        List<String> outerReceived = new ArrayList<>();
        NotificationDecorator outer = new NotificationDecorator(middle) {
            @Override
            public void send(String message) {
                outerReceived.add(message); // outer fires
                super.send(message);         // delegates to middle -> base
            }
        };

        // Act
        outer.send("chain test");

        // Assert: all three layers received the message
        assertEquals(1, outerReceived.size(),      "Outer decorator fired");
        assertEquals(1, middle.getMessages().size(), "Middle (dashboard) notifier fired");
        assertEquals(1, base.received.size(),       "Base (mock) notifier fired");
    }

    @Test
    void dashboardNotifier_clearMessages() {
        // Arrange
        DashboardNotifier dashboard = new DashboardNotifier(new MockNotificationService());
        dashboard.send("msg1");
        dashboard.send("msg2");

        // Act
        dashboard.clearMessages();

        // Assert
        assertTrue(dashboard.getMessages().isEmpty());
    }
}
