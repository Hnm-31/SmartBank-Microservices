package com.smartbank.user_service.controller;

import com.smartbank.user_service.dto.UserProfileDto;
import com.smartbank.user_service.model.UserProfile;
import com.smartbank.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    @PostMapping
    public ResponseEntity<?> createProfile(
            @RequestHeader("X-User-Name") String username, 
            @Valid @RequestBody UserProfileDto dto) {

        if (userProfileRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "Profile already exists"));
        }
        if (userProfileRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "Email already in use"));
        }
        if (userProfileRepository.existsByPhone(dto.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "Phone already in use"));
        }

        UserProfile profile = UserProfile.builder()
                .username(username)
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();

        userProfileRepository.save(profile);
        
        // TODO: In Phase 5, we will publish a Kafka event here (PROFILE_COMPLETED) so the Account Service creates a bank account!

        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("message", "Profile created successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("X-User-Name") String username) {
        return userProfileRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
