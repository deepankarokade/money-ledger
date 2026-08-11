package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.request.CreateAccountRequest;
import com.deepankar.ledger_system.dto.request.DepositRequest;
import com.deepankar.ledger_system.dto.request.TransferRequest;
import com.deepankar.ledger_system.dto.request.WithdrawRequest;
import com.deepankar.ledger_system.dto.response.AccountResponse;
import com.deepankar.ledger_system.entity.Account;
import com.deepankar.ledger_system.entity.Transaction;
import com.deepankar.ledger_system.enums.TransactionType;
import com.deepankar.ledger_system.exception.AccountNotFoundException;
import com.deepankar.ledger_system.exception.InsufficientFundsException;
import com.deepankar.ledger_system.exception.InvalidAmountException;
import com.deepankar.ledger_system.exception.SameAccountTransferException;
import com.deepankar.ledger_system.mapper.AccountMapper;
import com.deepankar.ledger_system.repository.AccountRepository;
import com.deepankar.ledger_system.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountServiceImp implements AccountService{

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionRepository transactionRepository;

    public AccountServiceImp(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            TransactionRepository transactionRepository
    ){
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.transactionRepository = transactionRepository;
    }


    @Override
    public AccountResponse createAccount(CreateAccountRequest createAccountRequest) {

        Account account = new Account();

        account.setAccountHolderName(createAccountRequest.getAccountHolderName());

        String accountNumber = UUID.randomUUID()
                .toString()
                .replace("-","")
                .substring(0,12);

        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);

        accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse deposit(DepositRequest depositRequest) {

        Long id = depositRequest.getId();
        BigDecimal amount = depositRequest.getAmount();

        //Validate amount
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }

        //Locked read
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        //Add amount
        account.setBalance(
                account.getBalance().add(amount)
        );

        //Create Transaction Record
        Transaction transaction = new Transaction();

        transaction.setFromAccount(null);
        transaction.setToAccount(account);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);

        //Save Transaction
        transactionRepository.save(transaction);

        accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse withdraw(WithdrawRequest withdrawRequest) {

        Long id = withdrawRequest.getId();
        BigDecimal amount = withdrawRequest.getAmount();

        //Validate amount
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }

        //Locked Read
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() ->new AccountNotFoundException(id));

        //Check sufficient balance
        if(amount.compareTo(account.getBalance()) > 0){
            throw new InsufficientFundsException();
        }

        //Subtract amount
        account.setBalance(
                account.getBalance().subtract(amount)
        );

        //Create Transaction
        Transaction transaction = new Transaction();

        transaction.setFromAccount(account);
        transaction.setToAccount(null);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAW);

        //Save transaction
        transactionRepository.save(transaction);

        accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    @Transactional
    @Override
    public void transfer(TransferRequest transferRequest) {

        Long fromId = transferRequest.getFromId();
        Long toId = transferRequest.getToId();
        BigDecimal amount = transferRequest.getAmount();

        //Validate amount
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }

        //No transfer from the same account
        if(fromId.equals(toId)){
            throw new SameAccountTransferException();
        }

        //Deadlock prevention

        Long firstId = Math.min(fromId,toId);
        Long secondId = Math.max(fromId,toId);

        //Lock the 1st account
        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));

        //Lock the 2nd account
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

        //Check sufficient funds
        /*
        Here suppose amount = 800 and balance = 500
        compareTo returns a positive value and since
        positive value is greater than 0 it throws Exception
         */
        if(amount.compareTo(fromAccount.getBalance()) > 0){
            throw new InsufficientFundsException();
        }

        //Remove money from sendder's account
        fromAccount.setBalance(
                fromAccount.getBalance().subtract(amount)
        );

        //Add money to receiver's account
        toAccount.setBalance(
                toAccount.getBalance().add(amount)
        );

        //Create Transaction
        Transaction transaction = new Transaction();

        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);

        transactionRepository.save(transaction);

        //Save both
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Override
    public BigDecimal getBalance(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        return account.getBalance();
    }
}
