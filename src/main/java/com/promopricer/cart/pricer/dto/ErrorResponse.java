package com.promopricer.cart.pricer.dto;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
