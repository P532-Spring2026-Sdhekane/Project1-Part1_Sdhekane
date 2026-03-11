package com.tradesim.model;

import java.time.Instant;


public class TradeRecord {

    private final String ticker;
    private final Order.Side side;
    private final int quantity;
    private final double price;
    private final double totalValue;
    private final Instant timestamp;

    public TradeRecord(String ticker, Order.Side side, int quantity, double price) {
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = price * quantity;
        this.timestamp = Instant.now();
    }

    public String getTicker()       { return ticker; }
    public Order.Side getSide()     { return side; }
    public int getQuantity()        { return quantity; }
    public double getPrice()        { return price; }
    public double getTotalValue()   { return totalValue; }
    public Instant getTimestamp()   { return timestamp; }
}
