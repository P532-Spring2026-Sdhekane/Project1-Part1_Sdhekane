package com.tradesim;

import com.tradesim.factory.OrderFactory;
import com.tradesim.model.LimitOrder;
import com.tradesim.model.MarketOrder;
import com.tradesim.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class OrderFactoryTest {

    private OrderFactory factory;

    @BeforeEach
    void setUp() {
      
        factory = new OrderFactory();
    }

    @Test
    void createMarketOrder_returnsMarketOrder() {
      
        Order order = factory.createMarketOrder("AAPL", Order.Side.BUY, 10);

   
        assertInstanceOf(MarketOrder.class, order);
        assertEquals("AAPL", order.getTicker());
        assertEquals(Order.Side.BUY, order.getSide());
        assertEquals(10, order.getQuantity());
        assertNotNull(order.getId(), "Order must have a generated ID");
    }

    @Test
    void createLimitOrder_returnsLimitOrder() {
   
        double limitPrice = 150.0;

     
        Order order = factory.createLimitOrder("TSLA", Order.Side.SELL, 5, limitPrice);

       
        assertInstanceOf(LimitOrder.class, order);
        assertEquals("TSLA", order.getTicker());
        assertEquals(Order.Side.SELL, order.getSide());
        assertEquals(5, order.getQuantity());
        assertEquals(limitPrice, ((LimitOrder) order).getLimitPrice(), 0.001);
    }

    @Test
    void createOrder_withMarketType_returnsMarketOrder() {
        
        Order order = factory.createOrder(OrderFactory.OrderType.MARKET, "MSFT", Order.Side.BUY, 3, 0);

        assertInstanceOf(MarketOrder.class, order);
    }

    @Test
    void createOrder_withLimitType_returnsLimitOrder() {
       
        Order order = factory.createOrder(OrderFactory.OrderType.LIMIT, "GOOG", Order.Side.BUY, 2, 130.0);

     
        assertInstanceOf(LimitOrder.class, order);
    }

    @Test
    void eachOrder_hasUniqueId() {
        
        Order o1 = factory.createMarketOrder("AAPL", Order.Side.BUY, 1);
        Order o2 = factory.createMarketOrder("AAPL", Order.Side.BUY, 1);

       
        assertNotEquals(o1.getId(), o2.getId());
    }
}
