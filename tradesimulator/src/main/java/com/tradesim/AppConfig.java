package com.tradesim;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.strategy.MeanReversionStrategy;
import com.tradesim.strategy.PriceUpdateStrategy;
import com.tradesim.strategy.RandomWalkStrategy;
import com.tradesim.strategy.TrendFollowingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public Map<String, PriceUpdateStrategy> availableStrategies(Random random) {
        Map<String, PriceUpdateStrategy> strategies = new ConcurrentHashMap<>();
        strategies.put("random-walk", new RandomWalkStrategy(random));
        strategies.put("mean-reversion", new MeanReversionStrategy(
                Map.of("AAPL", 182.5, "GOOG", 140.25, "TSLA", 248.75,
                       "AMZN", 178.9, "MSFT", 374.3, "NVDA", 495.0, "META", 353.4), random));
        strategies.put("trend-following", new TrendFollowingStrategy(random));
        return strategies;
    }

    @Bean
    public PriceUpdateStrategy[] activeStrategy(Map<String, PriceUpdateStrategy> strategies) {
        return new PriceUpdateStrategy[]{ strategies.get("random-walk") };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }

    @Bean
    @Primary
    public DashboardNotifier dashboardNotifier(ConsoleNotificationService console) {
        return new DashboardNotifier(console);
    }
}