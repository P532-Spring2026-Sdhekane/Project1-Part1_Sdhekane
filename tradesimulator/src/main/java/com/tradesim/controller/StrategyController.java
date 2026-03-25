package com.tradesim.controller;

import com.tradesim.service.MarketFeedService;
import com.tradesim.strategy.PriceUpdateStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

    private final MarketFeedService marketFeed;
    private final Map<String, PriceUpdateStrategy> availableStrategies;

    public StrategyController(MarketFeedService marketFeed,
                              Map<String, PriceUpdateStrategy> availableStrategies) {
        this.marketFeed = marketFeed;
        this.availableStrategies = availableStrategies;
    }

    @GetMapping
    public Map<String, Object> getStrategies() {
        return Map.of(
            "available", availableStrategies.keySet(),
            "active", marketFeed.getActiveStrategyName(availableStrategies)
        );
    }

    @PostMapping("/{name}")
    public ResponseEntity<Map<String, String>> setStrategy(@PathVariable String name) {
        PriceUpdateStrategy strategy = availableStrategies.get(name);
        if (strategy == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Unknown strategy: " + name));
        }
        marketFeed.setStrategy(strategy);
        return ResponseEntity.ok(Map.of("status", "switched", "active", name));
    }
}