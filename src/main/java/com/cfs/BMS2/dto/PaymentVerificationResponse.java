package com.cfs.BMS2.dto;

public record PaymentVerificationResponse(
        boolean verified,
        String message
) {
}
