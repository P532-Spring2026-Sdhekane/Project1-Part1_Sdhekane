package com.tradesim.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MeanReversionStrategy implements PriceUpdateStrategy {

    private static final double ALPHA = 0.05;     
    private static final double NOISE_PCT = 0.005;  

    private final Map<String, Double> means;
    private final Random random;

    public MeanReversionStrategy(Map<String, Double> means, Random random) {
        this.means = means;
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            String ticker = entry.getKey();
            double price = entry.getValue();
            double mean = means.getOrDefault(ticker, price);
            double noise = (random.nextDouble() * 2 * NOISE_PCT) - NOISE_PCT;
            double newPrice = price + ALPHA * (mean - price) + price * noise;
            updated.put(ticker, Math.max(0.01, newPrice));
        }
        return updated;
    }
}
