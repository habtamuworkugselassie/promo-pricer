package com.promopricer.cart.pricer.controller.domains;

public record AppliedPromotionDto(
        String promotionName,
        String ruleType,
        String description
) {
}
