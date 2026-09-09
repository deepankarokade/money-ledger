package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.request.CreateAccountRequest;
import com.deepankar.ledger_system.dto.request.DepositRequest;
import com.deepankar.ledger_system.dto.request.TransferRequest;
import com.deepankar.ledger_system.dto.request.WithdrawRequest;
import com.deepankar.ledger_system.dto.response.AccountResponse;
import com.deepankar.ledger_system.entity.Account;
import com.deepankar.ledger_system.entity.IdempotencyRecord;
import com.deepankar.ledger_system.entity.Transaction;
import com.deepankar.ledger_system.enums.TransactionType;
import com.deepankar.ledger_system.exception.AccountNotFoundException;
import com.deepankar.ledger_system.exception.IdempotencyKeyConflictException;
import com.deepankar.ledger_system.exception.InsufficientFundsException;
import com.deepankar.ledger_system.exception.InvalidAmountException;
import com.deepankar.ledger_system.exception.SameAccountTransferException;
import com.deepankar.ledger_system.mapper.AccountMapper;
import com.deepankar.ledger_system.repository.AccountRepository;
import com.deepankar.ledger_system.repository.IdempotencyRecordRepository;
import com.deepankar.ledger_system.repository.TransactionRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountServiceImp implements AccountService{

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public AccountServiceImp(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            TransactionRepository transactionRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            ObjectMapper objectMapper
    ){
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.transactionRepository = transactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AccountResponse createAccount(CreateAccountRequest createAccountRequest) {

        Account account = new Account();

        account.setAccountHolderName(createAccountRequest.getAccountHolderName());

        String accountNumber;

        do{
            accountNumber = String.format("%012d", 
                secureRandom.nextLong(1_000_000_000_000L)
            );
        }while(accountRepository.existsByAccountNumber(accountNumber));
            account.setAccountNumber(accountNumber);
            account.setBalance(BigDecimal.ZERO);

        accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse deposit(
        DepositRequest depositRequest,
        String idempotencyKey
) {

        String requestJson;

        try {
            requestJson = objectMapper.writeValueAsString(depositRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String requestHash;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(requestJson.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }

            requestHash = hash.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }


        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {

            IdempotencyRecord record = existingRecord.get();

            // Same key but different request
            if (!record.getRequestHash().equals(requestHash)) {
               throw new IdempotencyKeyConflictException(
            "Idempotency key already used for a different request"
                );
            }

            // Same key + same request
            try {
                return objectMapper.readValue(
                        record.getResponseBody(),
                        AccountResponse.class
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(
                        "Failed to read idempotent response", e
                );
            }
        }

            Long id = depositRequest.getId();
            BigDecimal amount = depositRequest.getAmount();

            // Validate amount
            if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
                throw new InvalidAmountException();
            }

            // Locked read
            Account account = accountRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new AccountNotFoundException(id));

            // Add amount
            account.setBalance(
                    account.getBalance().add(amount)
            );

            // Create Transaction Record
            Transaction transaction = new Transaction();

            transaction.setFromAccount(null);
            transaction.setToAccount(account);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.DEPOSIT);

            // Save Transaction
            transactionRepository.save(transaction);

            accountRepository.save(account);


            // 2. Create response
            AccountResponse response = accountMapper.toResponse(account);


            // 3. Save idempotency record
            IdempotencyRecord record = new IdempotencyRecord();

            record.setIdempotencyKey(idempotencyKey);

        try {

            record.setRequestHash(requestHash);

            record.setResponseBody(
                    objectMapper.writeValueAsString(response)
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to serialize idempotency data", e
            );
        }

        record.setResponseStatus(201);
        record.setCreatedAt(LocalDateTime.now());

        idempotencyRecordRepository.save(record);


            return response;
        }

    @Override
    @Transactional
    public AccountResponse withdraw(
        WithdrawRequest withdrawRequest,
        String idempotencyKey
) {

        String requestJson;

        try {
            requestJson = objectMapper.writeValueAsString(withdrawRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String requestHash;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(requestJson.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }

            requestHash = hash.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }

        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {

            IdempotencyRecord record = existingRecord.get();

            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyKeyConflictException(
    "Idempotency key already used for a different request"
);
            }

            try {
                return objectMapper.readValue(
                        record.getResponseBody(),
                        AccountResponse.class
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(
                        "Failed to read idempotent response", e
                );
            }
        }

        Long id = withdrawRequest.getId();
        BigDecimal amount = withdrawRequest.getAmount();

        // Validate amount
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }

        // Locked Read
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        // Check sufficient balance
        if(amount.compareTo(account.getBalance()) > 0){
            throw new InsufficientFundsException();
        }

        // Subtract amount
        account.setBalance(
                account.getBalance().subtract(amount)
        );

        // Create Transaction
        Transaction transaction = new Transaction();

        transaction.setFromAccount(account);
        transaction.setToAccount(null);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAW);

        // Save transaction
        transactionRepository.save(transaction);

        accountRepository.save(account);

        // Create response
        AccountResponse response = accountMapper.toResponse(account);

        // Save idempotency record
        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey(idempotencyKey);

        try {
            record.setRequestHash(requestHash);

            record.setResponseBody(
                    objectMapper.writeValueAsString(response)
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize idempotency data", e
            );
        }

        record.setResponseStatus(201);
        record.setCreatedAt(LocalDateTime.now());

        idempotencyRecordRepository.save(record);

        return response;
    }

    @Override
    @Transactional
    public void transfer(
        TransferRequest transferRequest,
        String idempotencyKey
) {

        String requestJson;

        try {
            requestJson = objectMapper.writeValueAsString(transferRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String requestHash;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(requestJson.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }

            requestHash = hash.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }

        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {

            IdempotencyRecord record = existingRecord.get();

            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyKeyConflictException(
    "Idempotency key already used for a different request"
);
            }

            return;
        }

    Long fromId = transferRequest.getFromId();
    Long toId = transferRequest.getToId();
    BigDecimal amount = transferRequest.getAmount();

    // Validate amount
    if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
        throw new InvalidAmountException();
    }

    // No transfer from same account
    if(fromId.equals(toId)){
        throw new SameAccountTransferException();
    }

    // Deadlock prevention
    Long firstId = Math.min(fromId, toId);
    Long secondId = Math.max(fromId, toId);

    // Lock first account
    Account firstAccount = accountRepository.findByIdForUpdate(firstId)
            .orElseThrow(() -> new AccountNotFoundException(firstId));

    // Lock second account
    Account secondAccount = accountRepository.findByIdForUpdate(secondId)
            .orElseThrow(() -> new AccountNotFoundException(secondId));

    Account fromAccount;
    Account toAccount;

    if(fromId.equals(firstId)){
        fromAccount = firstAccount;
        toAccount = secondAccount;
    } else {
        fromAccount = secondAccount;
        toAccount = firstAccount;
    }

    // Check sufficient funds
    if(amount.compareTo(fromAccount.getBalance()) > 0){
        throw new InsufficientFundsException();
    }

    // Remove money from sender
    fromAccount.setBalance(
            fromAccount.getBalance().subtract(amount)
    );

    // Add money to receiver
    toAccount.setBalance(
            toAccount.getBalance().add(amount)
    );

    // Create Transaction
    Transaction transaction = new Transaction();

    transaction.setFromAccount(fromAccount);
    transaction.setToAccount(toAccount);
    transaction.setAmount(amount);
    transaction.setType(TransactionType.TRANSFER);

    transactionRepository.save(transaction);

    // Save both accounts
    accountRepository.save(fromAccount);
    accountRepository.save(toAccount);


    // Save idempotency record
    IdempotencyRecord record = new IdempotencyRecord();

    record.setIdempotencyKey(idempotencyKey);

    record.setRequestHash(requestHash);
    record.setResponseBody("{}");

    record.setResponseStatus(200);
    record.setCreatedAt(LocalDateTime.now());

    idempotencyRecordRepository.save(record);
}

    @Override
    public BigDecimal getBalance(Long id) {

    Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));

    return account.getBalance();
}
}
