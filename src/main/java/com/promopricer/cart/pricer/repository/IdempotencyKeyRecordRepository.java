package com.promopricer.cart.pricer.repository;

import com.promopricer.cart.pricer.models.IdempotencyKeyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface IdempotencyKeyRecordRepository extends JpaRepository<IdempotencyKeyRecord, String> {
    Optional<IdempotencyKeyRecord> findByKeyId(String keyId);
}
