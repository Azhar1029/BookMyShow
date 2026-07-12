package com.cfs.BMS2.dto;

public record PaymentVerificationRequest(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature,
        String bookingId
) {
}
