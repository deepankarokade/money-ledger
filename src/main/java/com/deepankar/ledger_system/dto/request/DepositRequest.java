package com.deepankar.ledger_system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull(message = "Account id is required")
    private Long id;

    @NotNull(message = "Amount is required")
    @Positive(message = "Enter amount greater than zero")
    private BigDecimal amount;
}
