package com.tradesim;

import com.tradesim.strategy.MeanReversionStrategy;
import com.tradesim.strategy.RandomWalkStrategy;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class PricingStrategyTest {

    private static final Map<String, Double> BASE_PRICES = Map.of(
            "AAPL", 100.0,
            "TSLA", 200.0
    );

    @Test
    void randomWalk_staysWithinTwoPercentBand() {

        RandomWalkStrategy strategy = new RandomWalkStrategy(new Random(42));


        Map<String, Double> prices = BASE_PRICES;
        for (int i = 0; i < 1000; i++) {
            Map<String, Double> updated = strategy.updatePrices(prices);
            for (Map.Entry<String, Double> entry : updated.entrySet()) {
                double prev = prices.get(entry.getKey());
                double ratio = entry.getValue() / prev;
         
                assertTrue(ratio >= 0.98 - 1e-9, "Price dropped more than 2%: ratio=" + ratio);
                assertTrue(ratio <= 1.02 + 1e-9, "Price rose more than 2%: ratio=" + ratio);
            }
            prices = updated;
        }
    }

    @Test
    void randomWalk_returnsAllTickers() {

        RandomWalkStrategy strategy = new RandomWalkStrategy(new Random(1));

      
        Map<String, Double> updated = strategy.updatePrices(BASE_PRICES);

     
        assertEquals(BASE_PRICES.keySet(), updated.keySet());
    }

    @Test
    void meanReversion_pullsPriceTowardMean() {

        Map<String, Double> means = Map.of("AAPL", 100.0);
        MeanReversionStrategy strategy = new MeanReversionStrategy(means, new Random(0));
        Map<String, Double> current = Map.of("AAPL", 200.0);

    
        Map<String, Double> prices = current;
        for (int i = 0; i < 100; i++) {
            prices = strategy.updatePrices(prices);
        }

        double finalPrice = prices.get("AAPL");
        assertTrue(finalPrice < 150.0, "Mean reversion should pull price toward mean; got " + finalPrice);
    }

    @Test
    void randomWalk_priceNeverGoesNegative() {
        // Arrange
        RandomWalkStrategy strategy = new RandomWalkStrategy(new Random(99));
        Map<String, Double> prices = Map.of("PENNY", 0.05);

  
        for (int i = 0; i < 500; i++) {
            prices = strategy.updatePrices(prices);
       
            assertTrue(prices.get("PENNY") >= 0.01, "Price must never go below $0.01");
        }
    }
}
