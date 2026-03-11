package com.tradesim;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.model.MarketOrder;
import com.tradesim.model.Order;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.service.MarketFeedService;
import com.tradesim.service.OrderService;
import com.tradesim.singleton.Portfolio;
import com.tradesim.strategy.RandomWalkStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class OrderExecutionTest {

    private Portfolio portfolio;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
       
        portfolio = new Portfolio();
        DashboardNotifier notifier = new DashboardNotifier(new ConsoleNotificationService());
        MarketFeedService marketFeed = new MarketFeedService(new RandomWalkStrategy(new Random(42)));
        orderService = new OrderService(portfolio, notifier, marketFeed);
    }

    @Test
    void marketBuy_reducesCashAndIncreasesHoldings() {
        
        MarketOrder order = new MarketOrder("id-1", "AAPL", Order.Side.BUY, 5);
        double price = 100.0;

     
        String error = orderService.submitOrder(order, price);

      
        assertNull(error, "No error expected for valid buy");
        assertEquals(9500.0, portfolio.getCash(), 0.001);
        assertEquals(5, portfolio.getShares("AAPL"));
        assertEquals(Order.Status.EXECUTED, order.getStatus());
    }

    @Test
    void marketSell_increasesCashAndReducesHoldings() {
       
        orderService.submitOrder(new MarketOrder("id-buy", "AAPL", Order.Side.BUY, 10), 100.0);
        MarketOrder sellOrder = new MarketOrder("id-sell", "AAPL", Order.Side.SELL, 4);

      
        String error = orderService.submitOrder(sellOrder, 110.0);

       
        assertNull(error, "No error expected for valid sell");
        assertEquals(6, portfolio.getShares("AAPL"));
        assertEquals(9440.0 + 440.0, portfolio.getCash(), 0.001); 
    }

    @Test
    void marketBuy_insufficientFunds_isRejected() {
        
        MarketOrder order = new MarketOrder("id-1", "TSLA", Order.Side.BUY, 1000);

       
        String error = orderService.submitOrder(order, 200.0); 

       
        assertNotNull(error, "Error expected when funds insufficient");
        assertTrue(error.contains("Insufficient cash"));
    }

    @Test
    void marketSell_insufficientShares_isRejected() {
       
        MarketOrder order = new MarketOrder("id-1", "AAPL", Order.Side.SELL, 5);

       
        String error = orderService.submitOrder(order, 100.0);

       
        assertNotNull(error, "Error expected when shares insufficient");
        assertTrue(error.contains("Insufficient shares"));
    }
}
