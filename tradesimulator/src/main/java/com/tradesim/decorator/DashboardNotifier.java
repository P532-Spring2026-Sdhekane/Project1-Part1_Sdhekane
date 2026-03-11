package com.tradesim.decorator;

import com.tradesim.service.NotificationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardNotifier extends NotificationDecorator {

    private final List<String> messages = new ArrayList<>();

    public DashboardNotifier(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void send(String message) {
        
        messages.add(message);
       
        wrapped.send(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clearMessages() {
        messages.clear();
    }
}
