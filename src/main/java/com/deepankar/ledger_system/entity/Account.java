package com.deepankar.ledger_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber; //String since we are not performing arithmetic operations on accountNumbers

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version // This is optimistic write (different locking strategy than Pessimistic lock). This technique doesn't lock the row upfront
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
