package com.deepankar.ledger_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;
}
