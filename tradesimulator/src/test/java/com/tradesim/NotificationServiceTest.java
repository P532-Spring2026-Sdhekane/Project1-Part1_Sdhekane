package com.tradesim;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.decorator.NotificationDecorator;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class NotificationServiceTest {


    static class MockNotificationService implements NotificationService {
        final List<String> received = new ArrayList<>();
        @Override
        public void send(String message) { received.add(message); }
    }

    @Test
    void consoleNotifier_sendsMessage() {

        ConsoleNotificationService svc = new ConsoleNotificationService();

       
        assertDoesNotThrow(() -> svc.send("Test notification"));
    }

    @Test
    void dashboardNotifier_storesMessage() {
 
        MockNotificationService mock = new MockNotificationService();
        DashboardNotifier dashboard = new DashboardNotifier(mock);


        dashboard.send("Order executed: BUY 5 AAPL at $100");


        assertEquals(1, dashboard.getMessages().size());
        assertEquals("Order executed: BUY 5 AAPL at $100", dashboard.getMessages().get(0));
    }

    @Test
    void dashboardNotifier_delegatesToWrapped() {

        MockNotificationService mock = new MockNotificationService();
        DashboardNotifier dashboard = new DashboardNotifier(mock);


        dashboard.send("hello");


        assertEquals(1, mock.received.size());
        assertEquals("hello", mock.received.get(0));
    }

    @Test
    void decoratorChain_allWrappedNotifiersFire() {

        MockNotificationService base = new MockNotificationService();
        DashboardNotifier middle = new DashboardNotifier(base);

        List<String> outerReceived = new ArrayList<>();
        NotificationDecorator outer = new NotificationDecorator(middle) {
            @Override
            public void send(String message) {
                outerReceived.add(message); 
                super.send(message);         
            }
        };


        outer.send("chain test");

        assertEquals(1, outerReceived.size(),      "Outer decorator fired");
        assertEquals(1, middle.getMessages().size(), "Middle (dashboard) notifier fired");
        assertEquals(1, base.received.size(),       "Base (mock) notifier fired");
    }

    @Test
    void dashboardNotifier_clearMessages() {

        DashboardNotifier dashboard = new DashboardNotifier(new MockNotificationService());
        dashboard.send("msg1");
        dashboard.send("msg2");


        dashboard.clearMessages();


        assertTrue(dashboard.getMessages().isEmpty());
    }
}
