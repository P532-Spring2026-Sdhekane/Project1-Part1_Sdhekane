package com.tradesim.singleton;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class Portfolio {

    private static final double INITIAL_CASH = 10_000.00;

    private double cash;
    private final Map<String, Integer> holdings; // ticker -> shares owned

    public Portfolio() {
        this.cash = INITIAL_CASH;
        this.holdings = new HashMap<>();
    }

  

    public double getCash() { return cash; }

    public void deductCash(double amount) {
        if (amount > cash) throw new IllegalStateException("Insufficient funds");
        cash -= amount;
    }

    public void addCash(double amount) {
        cash += amount;
    }

  

    public Map<String, Integer> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public int getShares(String ticker) {
        return holdings.getOrDefault(ticker, 0);
    }

    public void addShares(String ticker, int qty) {
        holdings.merge(ticker, qty, Integer::sum);
    }

    public void removeShares(String ticker, int qty) {
        int owned = getShares(ticker);
        if (qty > owned) throw new IllegalStateException("Insufficient shares of " + ticker);
        int remaining = owned - qty;
        if (remaining == 0) holdings.remove(ticker);
        else holdings.put(ticker, remaining);
    }

  

    public double getTotalValue(Map<String, Double> prices) {
        double stockValue = holdings.entrySet().stream()
                .mapToDouble(e -> e.getValue() * prices.getOrDefault(e.getKey(), 0.0))
                .sum();
        return cash + stockValue;
    }



    public void reset() {
        cash = INITIAL_CASH;
        holdings.clear();
    }
}
