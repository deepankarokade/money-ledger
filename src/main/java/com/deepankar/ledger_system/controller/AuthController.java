package com.deepankar.ledger_system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deepankar.ledger_system.dto.request.LoginRequest;
import com.deepankar.ledger_system.dto.request.RegisterRequest;
import com.deepankar.ledger_system.dto.response.LoginResponse;
import com.deepankar.ledger_system.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(
            @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
