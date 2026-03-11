package com.tradesim.service;

import com.tradesim.observer.PriceObserver;
import com.tradesim.strategy.PriceUpdateStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MarketFeedService {

    private static final Map<String, Double> INITIAL_PRICES = Map.of(
            "AAPL",  182.50,
            "GOOG",  140.25,
            "TSLA",  248.75,
            "AMZN",  178.90,
            "MSFT",  374.30,
            "NVDA",  495.00,
            "META",  353.40
    );

    private final Map<String, Double> prices;
    private final PriceUpdateStrategy strategy;
    private final List<PriceObserver> observers;

    public MarketFeedService(PriceUpdateStrategy strategy) {
        this.strategy = strategy;
        this.prices = new HashMap<>(INITIAL_PRICES);
        this.observers = new ArrayList<>();
    }

    public void addObserver(PriceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(PriceObserver observer) {
        observers.remove(observer);
    }


    @Scheduled(fixedRate = 5000)
    public void tick() {
        Map<String, Double> updated = strategy.updatePrices(Collections.unmodifiableMap(prices));
        prices.putAll(updated);
        notifyObservers(Collections.unmodifiableMap(prices));
    }

    private void notifyObservers(Map<String, Double> snapshot) {
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
