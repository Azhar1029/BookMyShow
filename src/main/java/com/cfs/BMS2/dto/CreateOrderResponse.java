package com.cfs.BMS2.dto;

public record CreateOrderResponse(
        String keyId,
        String orderId,
        Double totalPrice,
        String currency,
        Long bookingId,
        String username,
        String email
) {
}
