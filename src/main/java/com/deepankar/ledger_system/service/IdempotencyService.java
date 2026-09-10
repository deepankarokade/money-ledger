package com.deepankar.ledger_system.service;

public interface IdempotencyService {
    public boolean tryClaim(
        String idempotencyKey,
        String requestHash
    );

    public void markFailed(
        String idempotencyKey
    );
}
