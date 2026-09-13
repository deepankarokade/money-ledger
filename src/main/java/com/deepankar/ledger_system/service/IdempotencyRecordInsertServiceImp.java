package com.deepankar.ledger_system.service;

import org.springframework.stereotype.Service;

import com.deepankar.ledger_system.entity.IdempotencyRecord;
import com.deepankar.ledger_system.repository.IdempotencyRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyRecordInsertServiceImp implements IdempotencyRecordInsertService {

    private final IdempotencyRecordRepository repository;

    @Override
    public IdempotencyRecord insert(IdempotencyRecord record) {
        return repository.saveAndFlush(record);
    }
}
