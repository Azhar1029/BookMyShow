package com.cfs.BMS2.entity;

public record BookingNotification(
        String username,
        String email,
        Long bookingId,
        Double totalPrice,
        String razorpayOrderId,
        String razorpayPaymentId
) {
}
