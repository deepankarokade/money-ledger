package com.deepankar.ledger_system.exception;

public class IdempotencyRequestInProgressException extends RuntimeException{

    public IdempotencyRequestInProgressException(String message) {
        super(message);
    }    
}
