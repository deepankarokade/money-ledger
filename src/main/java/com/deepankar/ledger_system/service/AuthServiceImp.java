package com.deepankar.ledger_system.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.deepankar.ledger_system.dto.request.LoginRequest;
import com.deepankar.ledger_system.dto.request.RegisterRequest;
import com.deepankar.ledger_system.dto.response.LoginResponse;
import com.deepankar.ledger_system.entity.User;
import com.deepankar.ledger_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtService.generateToken(request.getUsername());

        return new LoginResponse(token);
    }
}
