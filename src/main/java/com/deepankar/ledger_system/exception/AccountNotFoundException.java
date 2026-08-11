package com.deepankar.ledger_system.exception;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(Long id){
        super("Account not found with id: " + id);
    }
}
