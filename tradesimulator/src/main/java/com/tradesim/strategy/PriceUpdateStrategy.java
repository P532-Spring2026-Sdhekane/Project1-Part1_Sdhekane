package com.tradesim.strategy;

import java.util.Map;


public interface PriceUpdateStrategy {

    /**
     *
     * @param currentPrices unmodifiable view of the current price map
     * @return new price map after applying the strategy
     */
    Map<String, Double> updatePrices(Map<String, Double> currentPrices);
}
