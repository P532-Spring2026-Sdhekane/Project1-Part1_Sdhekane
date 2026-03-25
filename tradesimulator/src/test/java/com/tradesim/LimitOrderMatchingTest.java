package com.tradesim;

import com.tradesim.model.LimitOrder;
import com.tradesim.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for limit order trigger conditions.
 * BUY limit fires when price <= limitPrice.
 * SELL limit fires when price >= limitPrice.
 */
class LimitOrderMatchingTest {

    @Test
    void buyLimit_triggersWhenPriceAtOrBelowLimit() {
        // Arrange
        LimitOrder order = new LimitOrder("id-1", "AAPL", Order.Side.BUY, 10, 150.0);

        // Act & Assert: price exactly at limit -> should trigger
        assertTrue(order.isReadyToExecute(150.0), "Should trigger at exactly limit price");

        // Act & Assert: price below limit -> should trigger
        assertTrue(order.isReadyToExecute(149.99), "Should trigger below limit price");

        // Act & Assert: price above limit -> should NOT trigger
        assertFalse(order.isReadyToExecute(150.01), "Should not trigger above limit price");
    }

    @Test
    void sellLimit_triggersWhenPriceAtOrAboveLimit() {
        // Arrange
        LimitOrder order = new LimitOrder("id-2", "TSLA", Order.Side.SELL, 5, 200.0);

        // Act & Assert: price exactly at limit -> should trigger
        assertTrue(order.isReadyToExecute(200.0), "Should trigger at exactly limit price");

        // Act & Assert: price above limit -> should trigger
        assertTrue(order.isReadyToExecute(200.01), "Should trigger above limit price");

        // Act & Assert: price below limit -> should NOT trigger
        assertFalse(order.isReadyToExecute(199.99), "Should not trigger below limit price");
    }

    @Test
    void buyLimit_doesNotTriggerWhenPriceHigh() {
        // Arrange
        LimitOrder order = new LimitOrder("id-3", "GOOG", Order.Side.BUY, 1, 100.0);

        // Act & Assert: price far above limit
        assertFalse(order.isReadyToExecute(500.0));
    }
}
