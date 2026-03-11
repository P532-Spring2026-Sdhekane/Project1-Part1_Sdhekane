package com.tradesim.controller;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.factory.OrderFactory;
import com.tradesim.model.Order;
import com.tradesim.model.TradeRecord;
import com.tradesim.service.MarketFeedService;
import com.tradesim.service.OrderService;
import com.tradesim.singleton.Portfolio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final MarketFeedService marketFeed;
    private final OrderService orderService;
    private final Portfolio portfolio;
    private final OrderFactory orderFactory;
    private final DashboardNotifier notifier;

    public TradeController(MarketFeedService marketFeed, OrderService orderService,
                           Portfolio portfolio, OrderFactory orderFactory,
                           DashboardNotifier notifier) {
        this.marketFeed = marketFeed;
        this.orderService = orderService;
        this.portfolio = portfolio;
        this.orderFactory = orderFactory;
        this.notifier = notifier;
    }


    @GetMapping("/prices")
    public Map<String, Double> getPrices() {
        return marketFeed.getPrices();
    }


    @GetMapping("/portfolio")
    public Map<String, Object> getPortfolio() {
        Map<String, Double> prices = marketFeed.getPrices();
        Map<String, Object> result = new HashMap<>();
        result.put("cash", portfolio.getCash());
        result.put("holdings", portfolio.getHoldings());
        result.put("totalValue", portfolio.getTotalValue(prices));
        return result;
    }



    @PostMapping("/orders/market")
    public ResponseEntity<Map<String, String>> placeMarketOrder(@RequestBody MarketOrderRequest req) {
        Order.Side side;
        try { side = Order.Side.valueOf(req.side.toUpperCase()); }
        catch (Exception e) { return badRequest("Invalid side: " + req.side); }

        if (req.quantity <= 0) return badRequest("Quantity must be positive");

        Order order = orderFactory.createMarketOrder(req.ticker, side, req.quantity);
        double price = marketFeed.getPrice(req.ticker);
        String error = orderService.submitOrder(order, price);
        if (error != null) return badRequest(error);
        return ResponseEntity.ok(Map.of("status", "executed", "orderId", order.getId()));
    }

    @PostMapping("/orders/limit")
    public ResponseEntity<Map<String, String>> placeLimitOrder(@RequestBody LimitOrderRequest req) {
        Order.Side side;
        try { side = Order.Side.valueOf(req.side.toUpperCase()); }
        catch (Exception e) { return badRequest("Invalid side: " + req.side); }

        if (req.quantity <= 0)    return badRequest("Quantity must be positive");
        if (req.limitPrice <= 0)  return badRequest("Limit price must be positive");

        Order order = orderFactory.createLimitOrder(req.ticker, side, req.quantity, req.limitPrice);
        double price = marketFeed.getPrice(req.ticker);
        String error = orderService.submitOrder(order, price);
        if (error != null) return badRequest(error);
        return ResponseEntity.ok(Map.of("status", "pending", "orderId", order.getId()));
    }

    @GetMapping("/orders/pending")
    public List<Map<String, Object>> getPendingOrders() {
        return orderService.getPendingOrders().stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("ticker", o.getTicker());
            m.put("side", o.getSide());
            m.put("quantity", o.getQuantity());
            m.put("status", o.getStatus());
            return m;
        }).toList();
    }



    @GetMapping("/trades")
    public List<Map<String, Object>> getTradeHistory() {
        return orderService.getTradeHistory().stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("ticker", t.getTicker());
            m.put("side", t.getSide());
            m.put("quantity", t.getQuantity());
            m.put("price", t.getPrice());
            m.put("totalValue", t.getTotalValue());
            m.put("timestamp", t.getTimestamp().toString());
            return m;
        }).toList();
    }

   

    @GetMapping("/notifications")
    public Map<String, Object> getNotifications() {
        List<String> msgs = List.copyOf(notifier.getMessages());
        notifier.clearMessages();
        return Map.of("messages", msgs);
    }

  

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

   

    public static class MarketOrderRequest {
        public String ticker;
        public String side;
        public int quantity;
    }

    public static class LimitOrderRequest {
        public String ticker;
        public String side;
        public int quantity;
        public double limitPrice;
    }
}
