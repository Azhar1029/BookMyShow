package com.cfs.BMS2.service;

import com.cfs.BMS2.entity.BookingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String mailFrom;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled}") boolean mailEnabled,
            @Value("${app.mail.from}") String mailFrom
    ) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
    }

    public void sendBookingEmail(BookingNotification notification) {
        if (!mailEnabled) {
            log.info("Mail disabled. Email skipped for {}", notification.email());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(notification.email());
        message.setSubject("Booking Confirmation");
        message.setText("""
                Hello %s,
                Your movie ticket has been booked successfully.
                
                Booking ID: %s
                Payment ID: %s
                Order ID: %s

                Enjoy your movie!
                """.formatted(
                notification.username(),
                notification.bookingId(),
                notification.razorpayPaymentId(),
                notification.razorpayOrderId()
        ));

        mailSender.send(message);
        log.info("Enrollment email sent to {}", notification.email());
    }
}
