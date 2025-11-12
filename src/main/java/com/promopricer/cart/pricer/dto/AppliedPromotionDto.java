package com.promopricer.cart.pricer.dto;

public record AppliedPromotionDto(
        String promotionName,
        String ruleType,
        String description
) {
}
