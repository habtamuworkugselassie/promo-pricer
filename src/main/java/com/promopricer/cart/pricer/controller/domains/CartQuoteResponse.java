package com.promopricer.cart.pricer.controller.domains;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;

public record CartQuoteResponse(List<PriceBreakdownDto> lineItems,
                                @DecimalMin("0.00") BigDecimal totalOriginalPrice,
                                @DecimalMin("0.00") BigDecimal totalFinalPrice,
                                @DecimalMin("0.00") BigDecimal totalDiscount
) {
}
