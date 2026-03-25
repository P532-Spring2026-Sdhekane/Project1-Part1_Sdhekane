package com.tradesim;

import com.tradesim.factory.OrderFactory;
import com.tradesim.model.LimitOrder;
import com.tradesim.model.MarketOrder;
import com.tradesim.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OrderFactory.
 * Verifies correct concrete types returned and exception on unknown type.
 */
class OrderFactoryTest {

    private OrderFactory factory;

    @BeforeEach
    void setUp() {
        // Arrange: fresh factory
        factory = new OrderFactory();
    }

    @Test
    void createMarketOrder_returnsMarketOrder() {
        // Arrange
        // (factory already set up)

        // Act
        Order order = factory.createMarketOrder("AAPL", Order.Side.BUY, 10);

        // Assert
        assertInstanceOf(MarketOrder.class, order);
        assertEquals("AAPL", order.getTicker());
        assertEquals(Order.Side.BUY, order.getSide());
        assertEquals(10, order.getQuantity());
        assertNotNull(order.getId(), "Order must have a generated ID");
    }

    @Test
    void createLimitOrder_returnsLimitOrder() {
        // Arrange
        double limitPrice = 150.0;

        // Act
        Order order = factory.createLimitOrder("TSLA", Order.Side.SELL, 5, limitPrice);

        // Assert
        assertInstanceOf(LimitOrder.class, order);
        assertEquals("TSLA", order.getTicker());
        assertEquals(Order.Side.SELL, order.getSide());
        assertEquals(5, order.getQuantity());
        assertEquals(limitPrice, ((LimitOrder) order).getLimitPrice(), 0.001);
    }

    @Test
    void createOrder_withMarketType_returnsMarketOrder() {
        // Arrange + Act
        Order order = factory.createOrder(OrderFactory.OrderType.MARKET, "MSFT", Order.Side.BUY, 3, 0);

        // Assert
        assertInstanceOf(MarketOrder.class, order);
    }

    @Test
    void createOrder_withLimitType_returnsLimitOrder() {
        // Arrange + Act
        Order order = factory.createOrder(OrderFactory.OrderType.LIMIT, "GOOG", Order.Side.BUY, 2, 130.0);

        // Assert
        assertInstanceOf(LimitOrder.class, order);
    }

    @Test
    void eachOrder_hasUniqueId() {
        // Arrange + Act: create two orders
        Order o1 = factory.createMarketOrder("AAPL", Order.Side.BUY, 1);
        Order o2 = factory.createMarketOrder("AAPL", Order.Side.BUY, 1);

        // Assert: IDs must be different
        assertNotEquals(o1.getId(), o2.getId());
    }
}
