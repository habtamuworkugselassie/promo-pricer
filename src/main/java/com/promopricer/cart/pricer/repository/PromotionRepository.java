package com.promopricer.cart.pricer.repository;

import com.promopricer.cart.pricer.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findAll();
    List<Promotion> findAllByTargetSegments(String targetSegements);
}
