package com.deepankar.ledger_system.service;

import com.deepankar.ledger_system.dto.request.LoginRequest;
import com.deepankar.ledger_system.dto.request.RegisterRequest;
import com.deepankar.ledger_system.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
