package com.promopricer.cart.pricer.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Data
public class Product {

    @Id
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private BigDecimal price;

    private int stock;

    @Version
    private int version;
}
