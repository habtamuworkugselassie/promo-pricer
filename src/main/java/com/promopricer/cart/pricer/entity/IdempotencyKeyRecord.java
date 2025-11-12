package com.promopricer.cart.pricer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyRecord {
    @Id
    private String keyId;

    private UUID orderId;

    private Instant createdAt = Instant.now();

}

