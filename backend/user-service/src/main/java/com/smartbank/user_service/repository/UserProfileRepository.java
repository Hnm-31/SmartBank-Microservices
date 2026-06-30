package com.smartbank.user_service.repository;

import com.smartbank.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
