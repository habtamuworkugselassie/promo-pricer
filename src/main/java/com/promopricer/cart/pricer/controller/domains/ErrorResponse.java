package com.promopricer.cart.pricer.controller.domains;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
