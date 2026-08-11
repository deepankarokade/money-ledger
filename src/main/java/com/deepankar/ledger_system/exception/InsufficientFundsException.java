package com.deepankar.ledger_system.exception;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(){
        super("Amount must be greater than zero");
    }
}
