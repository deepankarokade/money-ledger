package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.response.TransactionResponse;
import com.deepankar.ledger_system.entity.Transaction;
import com.deepankar.ledger_system.exception.AccountNotFoundException;
import com.deepankar.ledger_system.repository.AccountRepository;
import com.deepankar.ledger_system.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImp implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImp(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<TransactionResponse> getAccountTransactions(Long accountId) {

        // Make sure account exists
        accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountId)
                );

        // Get transactions
        List<Transaction> transactions =
                transactionRepository
                        .findByFromAccountIdOrToAccountId(
                                accountId,
                                accountId
                        );

        // Convert entities to response DTOs
        return transactions.stream()
                .map(transaction -> {
                    TransactionResponse response =
                            new TransactionResponse();

                    response.setId(transaction.getId());

                    if (transaction.getFromAccount() != null) {
                        response.setFromAccountId(
                                transaction.getFromAccount().getId()
                        );
                    }

                    if (transaction.getToAccount() != null) {
                        response.setToAccountId(
                                transaction.getToAccount().getId()
                        );
                    }

                    response.setAmount(transaction.getAmount());
                    response.setType(transaction.getType());
                    response.setCreatedAt(transaction.getCreatedAt());

                    return response;
                })
                .toList();
    }
}
