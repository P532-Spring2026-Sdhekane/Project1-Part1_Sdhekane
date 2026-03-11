package com.tradesim.service;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.model.Order;
import com.tradesim.model.TradeRecord;
import com.tradesim.observer.PriceObserver;
import com.tradesim.singleton.Portfolio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class OrderService implements PriceObserver {

    private final Portfolio portfolio;
    private final DashboardNotifier notifier;

    private final List<Order> pendingOrders = new CopyOnWriteArrayList<>();
    private final List<TradeRecord> tradeHistory = new ArrayList<>();

    public OrderService(Portfolio portfolio, DashboardNotifier notifier,
                        MarketFeedService marketFeedService) {
        this.portfolio = portfolio;
        this.notifier = notifier;
        marketFeedService.addObserver(this);
    }


    @Override
    public synchronized void onPriceUpdate(Map<String, Double> updatedPrices) {
        List<Order> toRemove = new ArrayList<>();
        for (Order order : pendingOrders) {
            double price = updatedPrices.getOrDefault(order.getTicker(), 0.0);
            if (order.isReadyToExecute(price)) {
                executeOrder(order, price);
                toRemove.add(order);
            }
        }
        pendingOrders.removeAll(toRemove);
    }

    public synchronized String submitOrder(Order order, double currentPrice) {
        if (order.isReadyToExecute(currentPrice)) {
            // Validate before executing
            String error = validate(order, currentPrice);
            if (error != null) {
                order.markRejected();
                return error;
            }
            executeOrder(order, currentPrice);
        } else {
          
            pendingOrders.add(order);
        }
        return null; 
    }


    private String validate(Order order, double price) {
        double total = price * order.getQuantity();
        if (order.getSide() == Order.Side.BUY) {
            if (total > portfolio.getCash()) {
                return "Insufficient cash: need $" + String.format("%.2f", total)
                        + " but have $" + String.format("%.2f", portfolio.getCash());
            }
        } else {
            int owned = portfolio.getShares(order.getTicker());
            if (order.getQuantity() > owned) {
                return "Insufficient shares: need " + order.getQuantity()
                        + " of " + order.getTicker() + " but own " + owned;
            }
        }
        return null;
    }

    private void executeOrder(Order order, double price) {
        String validationError = validate(order, price);
        if (validationError != null) {
            order.markRejected();
            notifier.send("Order REJECTED for " + order.getTicker() + ": " + validationError);
            return;
        }

        double total = price * order.getQuantity();
        if (order.getSide() == Order.Side.BUY) {
            portfolio.deductCash(total);
            portfolio.addShares(order.getTicker(), order.getQuantity());
        } else {
            portfolio.removeShares(order.getTicker(), order.getQuantity());
            portfolio.addCash(total);
        }

        order.markExecuted(price);

        TradeRecord record = new TradeRecord(
                order.getTicker(), order.getSide(), order.getQuantity(), price);
        tradeHistory.add(record);

        notifier.send(String.format("Order EXECUTED: %s %d shares of %s at $%.2f (total $%.2f)",
                order.getSide(), order.getQuantity(), order.getTicker(), price, total));
    }


    public List<Order> getPendingOrders() {
        return Collections.unmodifiableList(pendingOrders);
    }

    public List<TradeRecord> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }
}
