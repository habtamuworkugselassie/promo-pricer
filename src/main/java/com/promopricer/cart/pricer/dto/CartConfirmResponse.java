package com.promopricer.cart.pricer.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public record CartConfirmResponse(
        UUID orderId,
        @DecimalMin("0.00") BigDecimal finalPrice,
        String message
) {
}
