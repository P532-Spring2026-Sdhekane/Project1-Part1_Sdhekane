package com.tradesim;

import com.tradesim.model.MarketOrder;
import com.tradesim.model.Order;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.service.MarketFeedService;
import com.tradesim.service.OrderService;
import com.tradesim.singleton.UserRegistry;
import com.tradesim.strategy.PriceUpdateStrategy;
import com.tradesim.strategy.RandomWalkStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class OrderExecutionTest {

    private static final String USER = "alice";

    private UserRegistry userRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Arrange: build UserRegistry with a fresh console notifier
        ConsoleNotificationService console = new ConsoleNotificationService();
        userRegistry = new UserRegistry(console);

        // Strategy holder array (Week 2 requirement)
        PriceUpdateStrategy[] holder = new PriceUpdateStrategy[]{
                new RandomWalkStrategy(new Random(42))
        };
        MarketFeedService marketFeed = new MarketFeedService(holder);
        orderService = new OrderService(userRegistry, marketFeed);

        // Reset alice's portfolio before each test
        userRegistry.getPortfolio(USER).reset();
    }

    @Test
    void marketBuy_reducesCashAndIncreasesHoldings() {
        // Arrange
        MarketOrder order = new MarketOrder("id-1", "AAPL", Order.Side.BUY, 5);
        double price = 100.0;

        // Act
        String error = orderService.submitOrder(USER, order, price);

        // Assert
        assertNull(error, "No error expected for valid buy");
        assertEquals(9500.0, userRegistry.getPortfolio(USER).getCash(), 0.001);
        assertEquals(5, userRegistry.getPortfolio(USER).getShares("AAPL"));
        assertEquals(Order.Status.EXECUTED, order.getStatus());
    }

    @Test
    void marketSell_increasesCashAndReducesHoldings() {
        // Arrange: buy first so we have shares
        orderService.submitOrder(USER, new MarketOrder("id-buy", "AAPL", Order.Side.BUY, 10), 100.0);
        MarketOrder sellOrder = new MarketOrder("id-sell", "AAPL", Order.Side.SELL, 4);

        // Act
        String error = orderService.submitOrder(USER, sellOrder, 110.0);

        // Assert
        assertNull(error, "No error expected for valid sell");
        assertEquals(6, userRegistry.getPortfolio(USER).getShares("AAPL"));
        assertEquals(9440.0 + 440.0, userRegistry.getPortfolio(USER).getCash(), 0.001);
    }

    @Test
    void marketBuy_insufficientFunds_isRejected() {
        // Arrange: try to buy more than we can afford
        MarketOrder order = new MarketOrder("id-1", "TSLA", Order.Side.BUY, 1000);

        // Act
        String error = orderService.submitOrder(USER, order, 200.0);

        // Assert
        assertNotNull(error, "Error expected when funds insufficient");
        assertTrue(error.contains("Insufficient cash"));
    }

    @Test
    void marketSell_insufficientShares_isRejected() {
        // Arrange: no shares owned
        MarketOrder order = new MarketOrder("id-1", "AAPL", Order.Side.SELL, 5);

        // Act
        String error = orderService.submitOrder(USER, order, 100.0);

        // Assert
        assertNotNull(error, "Error expected when shares insufficient");
        assertTrue(error.contains("Insufficient shares"));
    }

    @Test
    void eachUserHasIndependentPortfolio() {
        // Arrange: buy shares for alice
        orderService.submitOrder("alice", new MarketOrder("id-1", "AAPL", Order.Side.BUY, 5), 100.0);

        // Act + Assert: bob should still have full cash and no shares
        assertEquals(10000.0, userRegistry.getPortfolio("bob").getCash(), 0.001);
        assertEquals(0, userRegistry.getPortfolio("bob").getShares("AAPL"));
    }
}