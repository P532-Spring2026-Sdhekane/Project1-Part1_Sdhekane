package com.tradesim.factory;

import com.tradesim.model.LimitOrder;
import com.tradesim.model.MarketOrder;
import com.tradesim.model.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderFactory {

    public enum OrderType { MARKET, LIMIT }


    public MarketOrder createMarketOrder(String ticker, Order.Side side, int quantity) {
        return new MarketOrder(UUID.randomUUID().toString(), ticker, side, quantity);
    }


    public LimitOrder createLimitOrder(String ticker, Order.Side side, int quantity, double limitPrice) {
        return new LimitOrder(UUID.randomUUID().toString(), ticker, side, quantity, limitPrice);
    }

    /**
     * @throws IllegalArgumentException for unknown order types
     */
    public Order createOrder(OrderType type, String ticker, Order.Side side, int quantity, double limitPrice) {
        return switch (type) {
            case MARKET -> createMarketOrder(ticker, side, quantity);
            case LIMIT  -> createLimitOrder(ticker, side, quantity, limitPrice);
            default     -> throw new IllegalArgumentException("Unknown order type: " + type);
        };
    }
}
