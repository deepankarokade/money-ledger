package com.deepankar.ledger_system.mapper;

import com.deepankar.ledger_system.dto.response.AccountResponse;
import com.deepankar.ledger_system.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponse toResponse(Account account){

        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setBalance(account.getBalance());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }
}




// Used to Map the entity to DTO Response