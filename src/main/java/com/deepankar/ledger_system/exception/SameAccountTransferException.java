package com.deepankar.ledger_system.exception;

public class SameAccountTransferException extends RuntimeException{
    public SameAccountTransferException() {
        super("Cannot transfer money to the same account");
    }
}
