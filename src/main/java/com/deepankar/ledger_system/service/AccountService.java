package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.request.CreateAccountRequest;
import com.deepankar.ledger_system.dto.request.DepositRequest;
import com.deepankar.ledger_system.dto.request.TransferRequest;
import com.deepankar.ledger_system.dto.request.WithdrawRequest;
import com.deepankar.ledger_system.dto.response.AccountResponse;

import java.math.BigDecimal;

public interface AccountService {

     AccountResponse createAccount(CreateAccountRequest createAccountRequest);
     AccountResponse deposit(DepositRequest depositRequest);
     AccountResponse withdraw(WithdrawRequest withdrawRequest);
     void transfer(TransferRequest transferRequest);
     BigDecimal getBalance(Long id);
}
