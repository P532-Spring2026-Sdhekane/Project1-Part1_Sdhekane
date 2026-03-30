package com.tradesim.singleton;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.service.ConsoleNotificationService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class UserRegistry {

    public static final String DEFAULT_USER = "alice";

    private final Map<String, Portfolio> portfolios = new LinkedHashMap<>();
    private final Map<String, DashboardNotifier> notifiers = new LinkedHashMap<>();

    public UserRegistry(ConsoleNotificationService console) {
        for (String user : new String[]{"alice", "bob", "charlie", "shreyas"}) {
            portfolios.put(user, new Portfolio());
            notifiers.put(user, new DashboardNotifier(console));
        }
    }

    public Portfolio getPortfolio(String userId) {
        String key = userId == null ? DEFAULT_USER : userId.toLowerCase().trim();
        Portfolio p = portfolios.get(key);
        if (p == null) {
            return portfolios.get(DEFAULT_USER);
        }
        return p;
    }

    public DashboardNotifier getNotifier(String userId) {
        String key = userId == null ? DEFAULT_USER : userId.toLowerCase().trim();
        DashboardNotifier n = notifiers.get(key);
        return n != null ? n : notifiers.get(DEFAULT_USER);
    }

    public void resetPortfolio(String userId) {
        String key = userId == null ? DEFAULT_USER : userId.toLowerCase().trim();
        Portfolio p = portfolios.get(key);
        if (p != null) p.reset();
    }

    public void resetAll() {
        portfolios.values().forEach(Portfolio::reset);
        notifiers.values().forEach(DashboardNotifier::clearMessages);
    }

    public Set<String> getUsers() {
        return portfolios.keySet();
    }
}