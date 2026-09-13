package com.deepankar.ledger_system.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deepankar.ledger_system.entity.IdempotencyRecord;
import com.deepankar.ledger_system.enums.IdempotencyStatus;
import com.deepankar.ledger_system.exception.IdempotencyKeyConflictException;
import com.deepankar.ledger_system.exception.IdempotencyRequestInProgressException;
import com.deepankar.ledger_system.repository.IdempotencyRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImp implements IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final IdempotencyRecordInsertServiceImp insertService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    /*
     * If an active transaction already exists when this method is called,
     * Spring will pause the current outer transaction, spin up a separate inner
     * transaction, complete the method, and then resume the outer transaction
     */

    public IdempotencyRecord tryClaim(
            String idempotencyKey,
            String requestHash) {

        /*
         * WHY CHANGED boolean TO IdempotencyRecord
         * 
         * If this request owns the key, return the PROCESSING record.
         * If the request is already COMPLETED, return that record.
         * If it's FAILED, change it to PROCESSING and return it.
         * If it's PROCESSING, throw the in-progress exception.
         * Different hash → throw conflict.
         */

        Optional<IdempotencyRecord> existing = repository.findByIdempotencyKey(idempotencyKey);

        // Key does not exist → create a new PROCESSING record
        if (existing.isEmpty()) {
            IdempotencyRecord record = new IdempotencyRecord();

            record.setIdempotencyKey(idempotencyKey);
            record.setRequestHash(requestHash);
            record.setStatus(IdempotencyStatus.PROCESSING);
            record.setCreatedAt(LocalDateTime.now());

            try {
                return insertService.insert(record);
                /*
                 * Attempt to claim the key.
                 *
                 * The actual INSERT happens inside a
                 * separate transaction.
                 */
            } catch (DataIntegrityViolationException e) {
                existing = repository.findByIdempotencyKey(idempotencyKey);

                if (existing.isEmpty()) {
                    throw new RuntimeException(
                            "Idempotency record disappeared after conflict", e);
                }
            }

            record = existing.get();

            // Same key + different request
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyKeyConflictException(
                        "Idempotency key already used for a different request");
            }

            // Same key + request still processing
            if (record.getStatus() == IdempotencyStatus.PROCESSING) {

                throw new IdempotencyRequestInProgressException(
                        "Request with this idempotency key is already processing");
            }

            // The other request already completed
            if (record.getStatus() == IdempotencyStatus.COMPLETED) {

                return record;
            }
            // Unexpected state
            throw new RuntimeException(
                    "Unexpected idempotency status");
        }

        // Key already exists

        IdempotencyRecord record = existing.get();

        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyKeyConflictException(
                    "Idempotency key already used for a different request");
        }

        // Request is currently being processed
        if (record.getStatus() == IdempotencyStatus.PROCESSING) {
            throw new IdempotencyRequestInProgressException(
                    "Request with this idempotency key is already processing");
        }

        // Request already completed
        if (record.getStatus() == IdempotencyStatus.COMPLETED) {
            return record;
        }

        // Previous attempt failed → allow retry
        if (record.getStatus() == IdempotencyStatus.FAILED) {
            record.setStatus(IdempotencyStatus.PROCESSING);
            record.setResponseStatus(null);
            record.setResponseBody(null);

            repository.saveAndFlush(record);

            return record;
        }
        throw new RuntimeException(
                "Unknown Idempotency Status");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String idempotencyKey) {

        IdempotencyRecord record = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new RuntimeException(
                        "Idempotency record not found"));

        record.setStatus(IdempotencyStatus.FAILED);

        repository.save(record);
    }
}
