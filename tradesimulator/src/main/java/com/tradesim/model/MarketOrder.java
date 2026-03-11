package com.tradesim.model;


public class MarketOrder extends Order {

    public MarketOrder(String id, String ticker, Side side, int quantity) {
        super(id, ticker, side, quantity);
    }

    @Override
    public boolean isReadyToExecute(double currentPrice) {
       
        return true;
    }
}
