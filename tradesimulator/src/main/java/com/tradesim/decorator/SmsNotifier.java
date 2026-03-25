package com.tradesim.decorator;

import com.tradesim.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsNotifier extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(SmsNotifier.class);

    public SmsNotifier(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void send(String message) {
        String sms = message.length() > 160 ? message.substring(0, 157) + "..." : message;
        log.info("[SMS] To: +1-555-0100 | {}", sms);
        wrapped.send(message);
    }
}