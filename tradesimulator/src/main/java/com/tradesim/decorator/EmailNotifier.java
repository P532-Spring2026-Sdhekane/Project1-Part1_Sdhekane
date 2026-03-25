package com.tradesim.decorator;

import com.tradesim.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotifier extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    public EmailNotifier(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void send(String message) {
        log.info("[EMAIL] To: analyst@tradesim.com | Subject: Trade Alert | Body: {}", message);
        wrapped.send(message);
    }
}