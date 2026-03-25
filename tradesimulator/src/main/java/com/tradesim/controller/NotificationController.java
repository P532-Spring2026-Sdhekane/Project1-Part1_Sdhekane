package com.tradesim.controller;

import com.tradesim.decorator.EmailNotifier;
import com.tradesim.decorator.SmsNotifier;
import com.tradesim.service.ConsoleNotificationService;
import com.tradesim.service.NotificationService;
import com.tradesim.singleton.UserRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final UserRegistry userRegistry;
    private final ConsoleNotificationService console;
    private final Map<String, Set<String>> activeChannels = new HashMap<>();

    public NotificationController(UserRegistry userRegistry,
                                  ConsoleNotificationService console) {
        this.userRegistry = userRegistry;
        this.console = console;
        for (String user : userRegistry.getUsers()) {
            activeChannels.put(user, new HashSet<>(Set.of("console")));
        }
    }

    @GetMapping("/channels")
    public Map<String, Object> getChannels(
            @RequestParam(defaultValue = "alice") String user) {
        return Map.of(
            "available", List.of("console", "email", "sms"),
            "active", activeChannels.getOrDefault(user, Set.of("console"))
        );
    }

    @PostMapping("/channels")
    public ResponseEntity<Map<String, Object>> setChannels(
            @RequestParam(defaultValue = "alice") String user,
            @RequestBody ChannelRequest req) {
        Set<String> channels = new HashSet<>(req.channels);
        channels.add("console");
        activeChannels.put(user, channels);

        NotificationService chain = console;
        if (channels.contains("email")) chain = new EmailNotifier(chain);
        if (channels.contains("sms"))   chain = new SmsNotifier(chain);

        userRegistry.getNotifier(user).updateWrapped(chain);

        return ResponseEntity.ok(Map.of("status", "updated", "active", channels));
    }

    public static class ChannelRequest {
        public List<String> channels;
    }
}