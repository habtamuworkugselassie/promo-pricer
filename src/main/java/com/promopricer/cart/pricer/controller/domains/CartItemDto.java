package com.promopricer.cart.pricer.controller.domains;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemDto(
        @NotNull UUID productId,
        @Min(1) @NotNull Integer qty
) {
}
