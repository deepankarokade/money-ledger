package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.entity.IdempotencyRecord;

public interface IdempotencyRecordInsertService {
    public IdempotencyRecord insert(IdempotencyRecord record);
}
