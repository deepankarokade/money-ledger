package com.deepankar.ledger_system.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deepankar.ledger_system.entity.IdempotencyRecord;
import com.deepankar.ledger_system.enums.IdempotencyStatus;
import com.deepankar.ledger_system.repository.IdempotencyRecordRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class IdempotencyServiceImp implements IdempotencyService{
    
    private final IdempotencyRecordRepository repository;

    @Override 
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    /*
    If an active transaction already exists when this method is called, 
    Spring will pause the current outer transaction, spin up a separate inner 
    transaction, complete the method, and then resume the outer transaction */
    public boolean tryClaim(
        String idempotencyKey,
        String requestHash
    ){
        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyStatus.PROCESSING);
        record.setCreatedAt(LocalDateTime.now());

        try{
            repository.saveAndFlush(record); 
            /*save() and immediately executing flush(). 
            It forces Hibernate/JPA to push all pending changes 
            to the database as SQL queries (INSERT, UPDATE, DELETE) right away, 
            instead of holding them in memory until the transaction ends*/
            return true;
        } catch(DataIntegrityViolationException e){
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String idempotencyKey) {

        IdempotencyRecord record =
                repository.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Idempotency record not found"
                                ));

        record.setStatus(IdempotencyStatus.FAILED);

        repository.save(record);
    }   
}
