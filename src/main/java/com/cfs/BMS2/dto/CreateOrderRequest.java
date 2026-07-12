package com.cfs.BMS2.dto;

public record CreateOrderRequest(
        String username,
        String email,
        String bookingId
) {
}
