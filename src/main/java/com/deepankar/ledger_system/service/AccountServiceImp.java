package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.request.CreateAccountRequest;
import com.deepankar.ledger_system.dto.request.DepositRequest;
import com.deepankar.ledger_system.dto.request.TransferRequest;
import com.deepankar.ledger_system.dto.request.WithdrawRequest;
import com.deepankar.ledger_system.dto.response.AccountResponse;
import com.deepankar.ledger_system.entity.Account;
import com.deepankar.ledger_system.mapper.AccountMapper;
import com.deepankar.ledger_system.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.*;

@Service
public class AccountServiceImp implements AccountService{

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImp(
            AccountRepository accountRepository,
            AccountMapper accountMapper
    ){
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
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
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero"
            );
        }

        //Locked read
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found: " + id
                ));

        //Add amount
        account.setBalance(
                account.getBalance().add(amount)
        );

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
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero"
            );
        }

        //Locked Read
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found: " + id
                ));

        //Check sufficient balance
        if(amount.compareTo(account.getBalance()) > 0){
            throw new RuntimeException(
                    "Insufficient funds"
            );
        }

        //Subtract amount
        account.setBalance(
                account.getBalance().subtract(amount)
        );

        accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    public void transfer(TransferRequest transferRequest) {
        System.out.println();
    }

    @Override
    public BigDecimal getBalance(Long id) {
        return null;
    }
}
