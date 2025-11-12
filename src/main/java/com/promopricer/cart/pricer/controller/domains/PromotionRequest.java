package com.promopricer.cart.pricer.controller.domains;

import jakarta.validation.constraints.NotBlank;

public record PromotionRequest(@NotBlank String name,
                               @NotBlank String type,
                               @NotBlank String config,
                               String targetSegments
) {
}
