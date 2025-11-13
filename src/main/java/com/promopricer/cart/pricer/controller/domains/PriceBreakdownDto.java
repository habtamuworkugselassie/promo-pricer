package com.promopricer.cart.pricer.controller.domains;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;

public record PriceBreakdownDto(
        String description,
        @DecimalMin("0.00") BigDecimal originalPrice,
        @DecimalMin("0.00") BigDecimal finalPrice,
        @DecimalMin("0.00") BigDecimal discountAmount,
        Integer quantity,
        List<AppliedPromotionDto> appliedPromotions
) {
}
