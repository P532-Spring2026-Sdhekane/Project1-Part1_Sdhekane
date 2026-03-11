package com.tradesim.model;


public class LimitOrder extends Order {

    private final double limitPrice;

    public LimitOrder(String id, String ticker, Side side, int quantity, double limitPrice) {
        super(id, ticker, side, quantity);
        this.limitPrice = limitPrice;
    }

    @Override
    public boolean isReadyToExecute(double currentPrice) {
        if (getSide() == Side.BUY) {
           
            return currentPrice <= limitPrice;
        } else {
        
            return currentPrice >= limitPrice;
        }
    }

    public double getLimitPrice() { return limitPrice; }
}
