package com.tradesim;

import com.tradesim.model.LimitOrder;
import com.tradesim.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class LimitOrderMatchingTest {

    @Test
    void buyLimit_triggersWhenPriceAtOrBelowLimit() {

        LimitOrder order = new LimitOrder("id-1", "AAPL", Order.Side.BUY, 10, 150.0);

      
        assertTrue(order.isReadyToExecute(150.0), "Should trigger at exactly limit price");


        assertTrue(order.isReadyToExecute(149.99), "Should trigger below limit price");

     
        assertFalse(order.isReadyToExecute(150.01), "Should not trigger above limit price");
    }

    @Test
    void sellLimit_triggersWhenPriceAtOrAboveLimit() {

        LimitOrder order = new LimitOrder("id-2", "TSLA", Order.Side.SELL, 5, 200.0);

     
        assertTrue(order.isReadyToExecute(200.0), "Should trigger at exactly limit price");

      
        assertTrue(order.isReadyToExecute(200.01), "Should trigger above limit price");

     
        assertFalse(order.isReadyToExecute(199.99), "Should not trigger below limit price");
    }

    @Test
    void buyLimit_doesNotTriggerWhenPriceHigh() {

        LimitOrder order = new LimitOrder("id-3", "GOOG", Order.Side.BUY, 1, 100.0);

      
        assertFalse(order.isReadyToExecute(500.0));
    }
}
