package com.scriptorium.scriptorium.service;

import com.scriptorium.scriptorium.domain.Curator;
import com.scriptorium.scriptorium.dto.AuthResponse;
import com.scriptorium.scriptorium.dto.LoginRequest;
import com.scriptorium.scriptorium.dto.RegisterRequest;
import com.scriptorium.scriptorium.repository.CuratorRepository;
import com.scriptorium.scriptorium.security.JWTService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CuratorRepository curatorRepository;
    private final JWTService jwtService;
    public AuthService(CuratorRepository curatorRepository, JWTService jwtService){
        this.curatorRepository = curatorRepository;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request){
        Curator curator = new Curator();

        curator.setEmail(request.getEmail());
        curator.setRole(request.getRole());
        curator.setName(request.getName());
        curator.setInstitution(request.getInstitution());
        curator.setCreatedAt(LocalDateTime.now());
        curator.setHashedPassword(passwordEncoder.encode(request.getPassword()));

        curatorRepository.save(curator);
    }

    public AuthResponse login(LoginRequest request){

        Curator curator = curatorRepository.findByEmail(request.getEmail()).orElseThrow(()->new RuntimeException("User not found"));
         boolean matches = passwordEncoder.matches(request.getPassword(), curator.getHashedPassword());
         if(!matches){
             throw new RuntimeException("incorrect credentials");
         }
         String token =jwtService.generateToken(curator.getEmail());
        return new AuthResponse(token);
    }
}
