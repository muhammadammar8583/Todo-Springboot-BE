package com.practice.todoApp.repository;

import com.practice.todoApp.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByOtpCodeAndExpiresAtAfter(String otpCode, LocalDateTime now);

    Optional<Otp> findByEmailAndOtpCodeAndExpiresAtAfter(String email, String otpCode, LocalDateTime now);

    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
