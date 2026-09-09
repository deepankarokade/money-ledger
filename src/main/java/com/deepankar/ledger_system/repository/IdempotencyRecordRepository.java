package com.deepankar.ledger_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepankar.ledger_system.entity.IdempotencyRecord;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long>{
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
