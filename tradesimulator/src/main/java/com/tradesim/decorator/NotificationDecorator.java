package com.tradesim.decorator;

import com.tradesim.service.NotificationService;


public abstract class NotificationDecorator implements NotificationService {

    protected final NotificationService wrapped;

    protected NotificationDecorator(NotificationService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message) {
        wrapped.send(message);
    }
}
