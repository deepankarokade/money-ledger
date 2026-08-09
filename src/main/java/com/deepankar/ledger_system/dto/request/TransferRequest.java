package com.deepankar.ledger_system.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Long fromId;
    private Long toId;
    private BigDecimal amount;
}
