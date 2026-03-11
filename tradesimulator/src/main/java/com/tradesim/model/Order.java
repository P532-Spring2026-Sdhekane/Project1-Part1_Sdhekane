package com.tradesim.model;

import java.time.Instant;

public abstract class Order {

    public enum Side { BUY, SELL }
    public enum Status { PENDING, EXECUTED, REJECTED }

    private final String id;
    private final String ticker;
    private final Side side;
    private final int quantity;
    private Status status;
    private Instant executedAt;
    private double executedPrice;

    protected Order(String id, String ticker, Side side, int quantity) {
        this.id = id;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.status = Status.PENDING;
    }

    public abstract boolean isReadyToExecute(double currentPrice);

    public String getId()            { return id; }
    public String getTicker()        { return ticker; }
    public Side getSide()            { return side; }
    public int getQuantity()         { return quantity; }
    public Status getStatus()        { return status; }
    public Instant getExecutedAt()   { return executedAt; }
    public double getExecutedPrice() { return executedPrice; }

    public void markExecuted(double price) {
        this.status = Status.EXECUTED;
        this.executedPrice = price;
        this.executedAt = Instant.now();
    }

    public void markRejected() {
        this.status = Status.REJECTED;
    }
}
