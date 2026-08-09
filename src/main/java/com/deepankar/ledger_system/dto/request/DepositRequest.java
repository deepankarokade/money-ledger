package com.deepankar.ledger_system.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    private Long id;
    private BigDecimal amount;
}
