package com.promopricer.cart.pricer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(@NotNull UUID id,
                             @NotBlank String name,
                             @NotBlank String category,
                             @DecimalMin("0.01") @NotNull BigDecimal price,
                             @Min(0) @NotNull Integer stock
) {
}
