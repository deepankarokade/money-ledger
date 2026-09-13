package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.entity.IdempotencyRecord;

public interface IdempotencyService {
    public IdempotencyRecord tryClaim(
            String idempotencyKey,
            String requestHash);

    public void markFailed(
            String idempotencyKey);
}
