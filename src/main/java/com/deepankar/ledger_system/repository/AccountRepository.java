package com.deepankar.ledger_system.repository;

import com.deepankar.ledger_system.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    //Find by account number
    Optional<Account> findByAccountNumber(String accountNumber);

    //Find by ID
    Optional<Account> findById(Long id);

    //PESSIMISTIC_WRITE means lock the row already, assuming a conflict could happen
    /*
    If Account A is sending money to account B then both those rows are
    locked until the transaction is completed so there is no double entry and
    if C is trying to send money to A or while A is locked another transaction is
    initiated to transfer money to C the transaction holds until initial
    transaction is done then A and B are unlocked and A and C are locked
     */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param(("id")) Long id);

    boolean existsByAccountNumber(String accountNumber);
}
