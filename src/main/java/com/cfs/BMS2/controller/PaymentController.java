package com.cfs.BMS2.controller;

import com.cfs.BMS2.dto.CreateOrderRequest;
import com.cfs.BMS2.dto.CreateOrderResponse;
import com.cfs.BMS2.dto.PaymentVerificationRequest;
import com.cfs.BMS2.dto.PaymentVerificationResponse;
import com.cfs.BMS2.service.BookingService;
import com.cfs.BMS2.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final String razorpayKeyId;

    public PaymentController(
            BookingService bookingService,
            PaymentService paymentService,
            @Value("${app.razorpay.api.key-id}") String razorpayKeyId
    ) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.razorpayKeyId = razorpayKeyId;
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("razorpayKeyId", razorpayKeyId);
    }

    @PostMapping("/orders")
    public CreateOrderResponse creteOrder(@RequestBody CreateOrderRequest request) throws RazorpayException {
        return paymentService.createOrder(request);
    }

    @PostMapping("/verify")
    public PaymentVerificationResponse verify(@RequestBody PaymentVerificationRequest request) {
        paymentService.verifyAndNotify(request);
        return new PaymentVerificationResponse(true, "Payment verified, Notification sent");
    }

    @ExceptionHandler({IllegalArgumentException.class, RazorpayException.class})
    public ResponseEntity<Map<String, String>> handleBadReq(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}