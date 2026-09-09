package com.deepankar.ledger_system.controller;

import com.deepankar.ledger_system.dto.request.CreateAccountRequest;
import com.deepankar.ledger_system.dto.request.DepositRequest;
import com.deepankar.ledger_system.dto.request.TransferRequest;
import com.deepankar.ledger_system.dto.request.WithdrawRequest;
import com.deepankar.ledger_system.dto.response.AccountResponse;
import com.deepankar.ledger_system.service.AccountServiceImp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountServiceImp accountServiceImp;

    public AccountController(AccountServiceImp accountServiceImp) {
        this.accountServiceImp = accountServiceImp;
    }

    // CREATE ACCOUNT
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        AccountResponse response =
                accountServiceImp.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // DEPOSIT
    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request
    ) {

        AccountResponse response =
                accountServiceImp.deposit(
                        request,
                        idempotencyKey
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // WITHDRAW
    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequest request
    ) {

        AccountResponse response =
                accountServiceImp.withdraw(
                        request,
                        idempotencyKey
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // TRANSFER
    @PostMapping("/transfer")
    public ResponseEntity<AccountResponse> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {

        accountServiceImp.transfer(
                request,
                idempotencyKey
        );

        return ResponseEntity
                .ok()
                .build();
    }


    // GET BALANCE
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable Long id
    ) {

        BigDecimal balance =
                accountServiceImp.getBalance(id);

        return ResponseEntity.ok(balance);
    }
}