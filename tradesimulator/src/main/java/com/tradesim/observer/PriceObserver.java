package com.tradesim.observer;

import java.util.Map;


public interface PriceObserver {

    /**
     *
     * @param updatedPrices immutable snapshot of the new prices
     */
    void onPriceUpdate(Map<String, Double> updatedPrices);
}
