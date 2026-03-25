package com.tradesim.strategy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class TrendFollowingStrategy implements PriceUpdateStrategy {

    private static final int WINDOW = 5;
    private static final double MOMENTUM_FACTOR = 0.6;
    private static final double NOISE_PCT = 0.005;

    private final Map<String, Queue<Double>> priceHistory = new HashMap<>();
    private final Random random;

    public TrendFollowingStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            String ticker = entry.getKey();
            double price = entry.getValue();

            Queue<Double> history = priceHistory.computeIfAbsent(ticker, k -> new LinkedList<>());
            history.add(price);
            if (history.size() > WINDOW) history.poll();

            double momentum = 0.0;
            if (history.size() >= 2) {
                Double[] arr = history.toArray(new Double[0]);
                momentum = (arr[arr.length - 1] - arr[0]) / arr[0];
            }

            double noise = (random.nextDouble() * 2 * NOISE_PCT) - NOISE_PCT;
            double newPrice = price * (1.0 + MOMENTUM_FACTOR * momentum + noise);
            updated.put(ticker, Math.max(0.01, newPrice));
        }
        return updated;
    }
}