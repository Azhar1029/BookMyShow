package com.cfs.BMS2.service;

import com.cfs.BMS2.entity.BookingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final EmailService emailService;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "notificationservice")
    public void consume(BookingNotification notification) {
        log.info("Received booking notification for bookingId={}, email={}",
                notification.bookingId(), notification.email());
        emailService.sendBookingEmail(notification);
    }
}