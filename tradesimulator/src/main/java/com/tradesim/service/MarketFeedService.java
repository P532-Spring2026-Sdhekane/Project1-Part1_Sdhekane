package com.tradesim.service;

import com.tradesim.observer.PriceObserver;
import com.tradesim.strategy.PriceUpdateStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MarketFeedService {

    private static final Map<String, Double> INITIAL_PRICES = Map.of(
            "AAPL", 182.50, "GOOG", 140.25, "TSLA", 248.75,
            "AMZN", 178.90, "MSFT", 374.30, "NVDA", 495.00, "META", 353.40);

    private final Map<String, Double> prices;
    // array holder so strategy can be swapped at runtime
    private final PriceUpdateStrategy[] strategyHolder;
    private final List<PriceObserver> observers;

    public MarketFeedService(PriceUpdateStrategy[] strategyHolder) {
        this.strategyHolder = strategyHolder;
        this.prices = new HashMap<>(INITIAL_PRICES);
        this.observers = new ArrayList<>();
    }

    public void addObserver(PriceObserver observer)    { observers.add(observer); }
    public void removeObserver(PriceObserver observer) { observers.remove(observer); }

    // called by Strategy Controller to switch algorithm at runtime
    public void setStrategy(PriceUpdateStrategy strategy) {
        strategyHolder[0] = strategy;
    }

    public String getActiveStrategyName(Map<String, PriceUpdateStrategy> available) {
        PriceUpdateStrategy active = strategyHolder[0];
        return available.entrySet().stream()
                .filter(e -> e.getValue() == active)
                .map(Map.Entry::getKey)
                .findFirst().orElse("unknown");
    }

    @Scheduled(fixedRate = 5000)
    public void tick() {
        Map<String, Double> updated = strategyHolder[0].updatePrices(
                Collections.unmodifiableMap(prices));
        prices.putAll(updated);
        Map<String, Double> snapshot = Collections.unmodifiableMap(new HashMap<>(prices));
        for (PriceObserver observer : observers) {
            observer.onPriceUpdate(snapshot);
        }
    }

    public Map<String, Double> getPrices() {
        return Collections.unmodifiableMap(prices);
    }

    public double getPrice(String ticker) {
        Double p = prices.get(ticker);
        if (p == null) throw new IllegalArgumentException("Unknown ticker: " + ticker);
        return p;
    }
}