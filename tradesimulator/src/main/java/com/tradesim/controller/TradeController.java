package com.tradesim.controller;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.factory.OrderFactory;
import com.tradesim.model.Order;
import com.tradesim.service.MarketFeedService;
import com.tradesim.service.OrderService;
import com.tradesim.singleton.Portfolio;
import com.tradesim.singleton.UserRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final MarketFeedService marketFeed;
    private final OrderService orderService;
    private final OrderFactory orderFactory;
    private final UserRegistry userRegistry;

    public TradeController(MarketFeedService marketFeed, OrderService orderService,
                           OrderFactory orderFactory, UserRegistry userRegistry) {
        this.marketFeed   = marketFeed;
        this.orderService = orderService;
        this.orderFactory = orderFactory;
        this.userRegistry = userRegistry;
    }

    @GetMapping("/users")
    public List<String> getUsers() {
        return new ArrayList<>(userRegistry.getUsers());
    }

    @GetMapping("/prices")
    public Map<String, Double> getPrices() {
        return marketFeed.getPrices();
    }

    @GetMapping("/portfolio")
    public Map<String, Object> getPortfolio(
            @RequestParam(defaultValue = "alice") String user) {
        Portfolio portfolio = userRegistry.getPortfolio(user);
        Map<String, Object> result = new HashMap<>();
        result.put("cash", portfolio.getCash());
        result.put("holdings", portfolio.getHoldings());
        result.put("totalValue", portfolio.getTotalValue(marketFeed.getPrices()));
        return result;
    }

    @PostMapping("/orders/market")
    public ResponseEntity<Map<String, String>> placeMarketOrder(
            @RequestParam(defaultValue = "alice") String user,
            @RequestBody MarketOrderRequest req) {
        Order.Side side;
        try { side = Order.Side.valueOf(req.side.toUpperCase()); }
        catch (Exception e) { return badRequest("Invalid side: " + req.side); }
        if (req.quantity <= 0) return badRequest("Quantity must be positive");
        Order order = orderFactory.createMarketOrder(req.ticker, side, req.quantity);
        String error = orderService.submitOrder(user, order, marketFeed.getPrice(req.ticker));
        if (error != null) return badRequest(error);
        return ResponseEntity.ok(Map.of("status", "executed", "orderId", order.getId()));
    }

    @PostMapping("/orders/limit")
    public ResponseEntity<Map<String, String>> placeLimitOrder(
            @RequestParam(defaultValue = "alice") String user,
            @RequestBody LimitOrderRequest req) {
        Order.Side side;
        try { side = Order.Side.valueOf(req.side.toUpperCase()); }
        catch (Exception e) { return badRequest("Invalid side: " + req.side); }
        if (req.quantity <= 0)   return badRequest("Quantity must be positive");
        if (req.limitPrice <= 0) return badRequest("Limit price must be positive");
        Order order = orderFactory.createLimitOrder(req.ticker, side, req.quantity, req.limitPrice);
        String error = orderService.submitOrder(user, order, marketFeed.getPrice(req.ticker));
        if (error != null) return badRequest(error);
        return ResponseEntity.ok(Map.of("status", "pending", "orderId", order.getId()));
    }

    @GetMapping("/orders/pending")
    public List<Map<String, Object>> getPendingOrders(
            @RequestParam(defaultValue = "alice") String user) {
        return orderService.getPendingOrders(user).stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId()); m.put("ticker", o.getTicker());
            m.put("side", o.getSide()); m.put("quantity", o.getQuantity());
            m.put("status", o.getStatus());
            return m;
        }).toList();
    }

    @GetMapping("/trades")
    public List<Map<String, Object>> getTradeHistory(
            @RequestParam(defaultValue = "alice") String user) {
        return orderService.getTradeHistory(user).stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("ticker", t.getTicker()); m.put("side", t.getSide());
            m.put("quantity", t.getQuantity()); m.put("price", t.getPrice());
            m.put("totalValue", t.getTotalValue());
            m.put("timestamp", t.getTimestamp().toString());
            return m;
        }).toList();
    }

    @GetMapping("/notifications")
    public Map<String, Object> getNotifications(
            @RequestParam(defaultValue = "alice") String user) {
        DashboardNotifier notifier = userRegistry.getNotifier(user);
        List<String> msgs = List.copyOf(notifier.getMessages());
        notifier.clearMessages();
        return Map.of("messages", msgs);
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    public static class MarketOrderRequest {
        public String ticker; public String side; public int quantity;
    }
    public static class LimitOrderRequest {
        public String ticker; public String side; public int quantity; public double limitPrice;
    }
}