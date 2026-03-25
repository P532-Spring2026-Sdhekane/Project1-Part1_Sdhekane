package com.tradesim.singleton;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.service.ConsoleNotificationService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class UserRegistry {

    public static final String DEFAULT_USER = "Alice";

    private final Map<String, Portfolio> portfolios = new LinkedHashMap<>();
    private final Map<String, DashboardNotifier> notifiers = new LinkedHashMap<>();

    public UserRegistry(ConsoleNotificationService console) {
        for (String user : new String[]{"Alice" , "Bob" , "Charlie", "Shreyas"}) {
            portfolios.put(user, new Portfolio());
            notifiers.put(user, new DashboardNotifier(console));
        }
    }

    public Portfolio getPortfolio(String userId) {
        return portfolios.getOrDefault(userId, portfolios.get(DEFAULT_USER));
    }

    public DashboardNotifier getNotifier(String userId) {
        return notifiers.getOrDefault(userId, notifiers.get(DEFAULT_USER));
    }

    public Set<String> getUsers() {
        return portfolios.keySet();
    }
}