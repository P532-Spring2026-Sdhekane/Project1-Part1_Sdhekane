package com.tradesim.service;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.model.Order;
import com.tradesim.model.TradeRecord;
import com.tradesim.observer.PriceObserver;
import com.tradesim.singleton.Portfolio;
import com.tradesim.singleton.UserRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderService implements PriceObserver {

    private final UserRegistry userRegistry;


    private final Map<String, List<Order>> pendingOrders = new ConcurrentHashMap<>();
    private final Map<String, List<TradeRecord>> tradeHistory = new ConcurrentHashMap<>();

    public OrderService(UserRegistry userRegistry, MarketFeedService marketFeedService) {
        this.userRegistry = userRegistry;
        marketFeedService.addObserver(this);
    }


    @Override
    public synchronized void onPriceUpdate(Map<String, Double> updatedPrices) {
        for (String userId : userRegistry.getUsers()) {
            List<Order> pending = getPending(userId);
            List<Order> toRemove = new ArrayList<>();
            for (Order order : pending) {
                double price = updatedPrices.getOrDefault(order.getTicker(), 0.0);
                if (order.isReadyToExecute(price)) {
                    executeOrder(userId, order, price);
                    toRemove.add(order);
                }
            }
            pending.removeAll(toRemove);
        }
    }



    public synchronized String submitOrder(String userId, Order order, double currentPrice) {
        String uid = userId == null ? UserRegistry.DEFAULT_USER : userId.toLowerCase().trim();
        if (order.isReadyToExecute(currentPrice)) {
            String error = validate(uid, order, currentPrice);
            if (error != null) { order.markRejected(); return error; }
            executeOrder(uid, order, currentPrice);
        } else {
            getPending(uid).add(order);
        }
        return null;
    }


    private String validate(String userId, Order order, double price) {
        Portfolio portfolio = userRegistry.getPortfolio(userId);
        double total = price * order.getQuantity();
        if (order.getSide() == Order.Side.BUY) {
            if (total > portfolio.getCash())
                return "Insufficient cash: need $" + String.format("%.2f", total)
                        + " but have $" + String.format("%.2f", portfolio.getCash());
        } else {
            int owned = portfolio.getShares(order.getTicker());
            if (order.getQuantity() > owned)
                return "Insufficient shares: need " + order.getQuantity()
                        + " of " + order.getTicker() + " but own " + owned;
        }
        return null;
    }



    private void executeOrder(String userId, Order order, double price) {
        String err = validate(userId, order, price);
        Portfolio portfolio = userRegistry.getPortfolio(userId);
        DashboardNotifier notifier = userRegistry.getNotifier(userId);
        if (err != null) {
            order.markRejected();
            notifier.send("Order REJECTED for " + order.getTicker() + ": " + err);
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
        getHistory(userId).add(new TradeRecord(
                order.getTicker(), order.getSide(), order.getQuantity(), price));
        notifier.send(String.format("Order EXECUTED: %s %d shares of %s at $%.2f (total $%.2f)",
                order.getSide(), order.getQuantity(), order.getTicker(), price, total));
    }


    public synchronized void clearUserData(String userId) {
        String uid = userId == null ? UserRegistry.DEFAULT_USER : userId.toLowerCase().trim();
        pendingOrders.remove(uid);
        tradeHistory.remove(uid);
    }


    public synchronized void clearAllData() {
        pendingOrders.clear();
        tradeHistory.clear();
    }


    private List<Order> getPending(String userId) {
        return pendingOrders.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
    }

    private List<TradeRecord> getHistory(String userId) {
        return tradeHistory.computeIfAbsent(userId, k -> new ArrayList<>());
    }


    public List<Order> getPendingOrders(String userId) {
        String uid = userId == null ? UserRegistry.DEFAULT_USER : userId.toLowerCase().trim();
        return Collections.unmodifiableList(getPending(uid));
    }

    public List<TradeRecord> getTradeHistory(String userId) {
        String uid = userId == null ? UserRegistry.DEFAULT_USER : userId.toLowerCase().trim();
        return Collections.unmodifiableList(getHistory(uid));
    }
}