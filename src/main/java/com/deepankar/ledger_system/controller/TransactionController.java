package com.deepankar.ledger_system.controller;

import com.deepankar.ledger_system.dto.response.TransactionResponse;
import com.deepankar.ledger_system.service.TransactionServiceImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionServiceImp transactionServiceImp;

    public TransactionController(
            TransactionServiceImp transactionServiceImp
    ){
        this.transactionServiceImp = transactionServiceImp;
    }
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable Long accountId
    ) {

        List<TransactionResponse> transactions =
                transactionServiceImp.getAccountTransactions(accountId);

        return ResponseEntity.ok(transactions);
    }
}
