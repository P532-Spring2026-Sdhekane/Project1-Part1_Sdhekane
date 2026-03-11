package com.tradesim.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Component
public class RandomWalkStrategy implements PriceUpdateStrategy {

    private static final double MAX_CHANGE = 0.02;

    private final Random random;

    public RandomWalkStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
          
            double changePct = (random.nextDouble() * 2 * MAX_CHANGE) - MAX_CHANGE;
            double newPrice = entry.getValue() * (1.0 + changePct);
            
            updated.put(entry.getKey(), Math.max(0.01, newPrice));
        }
        return updated;
    }
}
