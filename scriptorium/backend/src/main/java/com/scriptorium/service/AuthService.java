package com.scriptorium.service;

import com.scriptorium.domain.CuratorEntity;
import com.scriptorium.dto.LoginRequest;
import com.scriptorium.dto.LoginResponse;
import com.scriptorium.dto.RegisterRequest;
import com.scriptorium.repository.CuratorRepository;
import com.scriptorium.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuratorRepository curatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (curatorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A curator with that email already exists.");
        }

        CuratorEntity curator = new CuratorEntity();
        curator.setEmail(request.getEmail());
        curator.setName(request.getName());
        curator.setInstitution(request.getInstitution());
        curator.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        curator.setRole("curator");

        curatorRepository.save(curator);

        String token = jwtUtil.generateToken(curator.getEmail(), curator.getRole());
        return new LoginResponse(token, curator.getEmail(), curator.getName(), curator.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        CuratorEntity curator = curatorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), curator.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(curator.getEmail(), curator.getRole());
        return new LoginResponse(token, curator.getEmail(), curator.getName(), curator.getRole());
    }
}
