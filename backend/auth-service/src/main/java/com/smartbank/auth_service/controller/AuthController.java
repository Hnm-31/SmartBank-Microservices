package com.smartbank.auth_service.controller;

import com.smartbank.auth_service.dto.AuthRequest;
import com.smartbank.auth_service.dto.AuthResponse;
import com.smartbank.auth_service.dto.RegisterRequest;
import com.smartbank.auth_service.exception.UserAlreadyExistsException;
import com.smartbank.auth_service.model.Role;
import com.smartbank.auth_service.model.User;
import com.smartbank.auth_service.repository.UserRepository;
import com.smartbank.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("Reached here");
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        Role role = Role.USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Hello here");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", "Invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        jwtUtil.validateToken(token);
        return ResponseEntity.ok(java.util.Map.of("message", "Token is valid"));
    }
}
