package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getAccountTransactions(Long accountId);
}
