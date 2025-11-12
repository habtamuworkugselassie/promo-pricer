package com.promopricer.cart.pricer.repository;

import com.promopricer.cart.pricer.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}

