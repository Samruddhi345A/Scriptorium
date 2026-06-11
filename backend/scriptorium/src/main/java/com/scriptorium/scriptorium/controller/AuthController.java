package com.scriptorium.scriptorium.controller;


import com.scriptorium.scriptorium.dto.AuthResponse;
import com.scriptorium.scriptorium.dto.LoginRequest;
import com.scriptorium.scriptorium.dto.RegisterRequest;
import com.scriptorium.scriptorium.service.AuthService;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/curator")
public class AuthController {
AuthService authService;
    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registry(@RequestBody RegisterRequest requestBody){
        authService.register(requestBody);

        return ResponseEntity.ok("Curator Registered Successfully");

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest requestBody){
       AuthResponse response= authService.login(requestBody);

        return ResponseEntity.ok(response);
    }
}
