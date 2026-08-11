package com.deepankar.ledger_system.exception;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException(){
        super("Amount must be greater than zero");
    }
}
