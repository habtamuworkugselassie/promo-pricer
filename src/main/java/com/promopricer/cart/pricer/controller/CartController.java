package com.promopricer.cart.pricer.controller;

import com.promopricer.cart.pricer.dto.CartConfirmResponse;
import com.promopricer.cart.pricer.dto.CartQuoteRequest;
import com.promopricer.cart.pricer.dto.CartQuoteResponse;
import com.promopricer.cart.pricer.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/cart")
class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/quote")
    public CartQuoteResponse getQuote(@Valid @RequestBody CartQuoteRequest request) {
        return cartService.quoteCart(request);
    }

    @PostMapping("/confirm")
    public ResponseEntity<CartConfirmResponse> confirm(
            @Valid @RequestBody CartQuoteRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        CartConfirmResponse response = cartService.confirmCart(request, idempotencyKey);

        if (response.message().contains("already confirmed")) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .created(URI.create("/orders/" + response.orderId()))
                .body(response);
    }
}

