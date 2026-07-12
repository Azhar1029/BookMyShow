package com.cfs.BMS2.service;

import com.cfs.BMS2.dto.CreateOrderRequest;
import com.cfs.BMS2.dto.CreateOrderResponse;
import com.cfs.BMS2.dto.PaymentVerificationRequest;
import com.cfs.BMS2.entity.Booking;
import com.cfs.BMS2.entity.BookingNotification;
import com.cfs.BMS2.enums.BookingStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final BookingService bookingService;
    private final KafkaTemplate<String, BookingNotification> kafkaTemplate;
    private final String key;
    private final String secret;
    private final String kafkaTopic;

    public PaymentService(
            RazorpayClient razorpayClient,
            BookingService bookingService,
            KafkaTemplate<String, BookingNotification> kafkaTemplate,
            @Value("${app.razorpay.api.key-id}") String key,
            @Value("${app.razorpay.api.key-secret}") String secret,
            @Value("${app.kafka.topic}") String kafkaTopic
    ) {
        this.razorpayClient = razorpayClient;
        this.bookingService = bookingService;
        this.kafkaTemplate = kafkaTemplate;
        this.key = key;
        this.secret = secret;
        this.kafkaTopic = kafkaTopic;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {
        Booking booking = bookingService.getBookingById(Long.valueOf(request.bookingId()));

        String username = booking.getUser().getName(); // or getName()
        String email = booking.getUser().getEmail();

        JSONObject notes = new JSONObject();
        notes.put("bookingId", booking.getId()) ;
        notes.put("email", request.email());

        JSONObject orderRequest = new JSONObject();
        // Razorpay expects amount in the smallest currency unit (paise for INR), not rupees.
        // booking.getTotalPrice() is in rupees (e.g. 800.0), so this must be converted to
        // paise (80000) before being sent - otherwise Razorpay silently treats it as paise
        // directly, undercharging by 100x (₹800 -> shown as ₹8.00 at checkout).
        long amountInPaise = Math.round(booking.getTotalPrice() * 100);
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "booking_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1);
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);


        return new CreateOrderResponse(
                key,
                order.get("id"),
                booking.getTotalPrice(),
                "INR",
                booking.getId(),
                booking.getUser().getName(),
                booking.getUser().getEmail()
        );
    }

    public void verifyAndNotify(PaymentVerificationRequest request) {
        if (!isValidSignature(request)) {
            throw new IllegalArgumentException("payment signature verification failed");
        }

        // isValidSignature(request) == true

        Booking booking = bookingService.getBookingById(Long.valueOf(request.bookingId()));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            // Already processed on an earlier call (e.g. the frontend retried /verify) -
            // don't re-confirm or send a second notification/email for the same payment.
            return;
        }

        booking = bookingService.confirmBooking(booking.getId());

        BookingNotification notification = new BookingNotification(
                booking.getUser().getName(),
                booking.getUser().getEmail(),
                booking.getId(),
                booking.getTotalPrice(),
                request.razorpayOrderId(),
                request.razorpayPaymentId()
        );

        kafkaTemplate.send(kafkaTopic, booking.getUser().getEmail(), notification);
    }

    public boolean isValidSignature(PaymentVerificationRequest request) {
        String payload = request.razorpayOrderId() + "|" + request.razorpayPaymentId();
        String expectedSignature = hmacSha256(payload, secret);
        return expectedSignature.equals(request.razorpaySignature());
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Unable to verify Razorpay signature", ex);
        }
    }
}