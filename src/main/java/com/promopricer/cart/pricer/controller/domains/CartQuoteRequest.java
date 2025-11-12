package com.promopricer.cart.pricer.controller.domains;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartQuoteRequest(
        @NotEmpty List<CartItemDto> items,
        @NotBlank String customerSegment
) {
}
