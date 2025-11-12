package com.promopricer.cart.pricer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Entity
@Data
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    // Stores JSON string configuration for the rule (e.g., {"category":"STATIONERY", "percentage":10})
    @Column(columnDefinition = "TEXT")
    private String config;

    @Column(columnDefinition = "TEXT")
    private String targetSegments; // NEW: e.g., "REGULAR,PREMIUM"

}

