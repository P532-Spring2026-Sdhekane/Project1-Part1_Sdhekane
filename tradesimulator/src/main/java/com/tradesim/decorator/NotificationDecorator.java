package com.tradesim.decorator;

import com.tradesim.service.NotificationService;

public abstract class NotificationDecorator implements NotificationService {

    // chain can be rebuilt at runtime : dashboard notifier holds a reference to the current bottom of the chain... 
    protected NotificationService wrapped;

    protected NotificationDecorator(NotificationService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message) {
        wrapped.send(message);
    }
}