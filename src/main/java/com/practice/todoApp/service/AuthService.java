package com.practice.todoApp.service;

import com.practice.todoApp.dto.auth.AuthResponse;
import com.practice.todoApp.dto.auth.ForgotPasswordRequest;
import com.practice.todoApp.dto.auth.LoginRequest;
import com.practice.todoApp.dto.auth.RegisterRequest;
import com.practice.todoApp.dto.auth.ResetPasswordRequest;
import com.practice.todoApp.dto.auth.VerifyOtpRequest;
import com.practice.todoApp.exception.AuthenticationException;
import com.practice.todoApp.exception.DuplicateResourceException;
import com.practice.todoApp.exception.ResourceNotFoundException;
import com.practice.todoApp.model.Otp;
import com.practice.todoApp.model.Role;
import com.practice.todoApp.model.User;
import com.practice.todoApp.repository.OtpRepository;
import com.practice.todoApp.repository.UserRepository;
import com.practice.todoApp.security.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       OtpRepository otpRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    public User register(RegisterRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException("User", "username", request.getUsername());
            }
        
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setRole(Role.USER);
        
            return userRepository.save(user);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new AuthenticationException("User not found after authentication"));

            return new AuthResponse(token, user.getUsername(), user.getRole(), user.getEmail());
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Invalid credentials", e);
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        otpRepository.deleteByEmail(request.getEmail());

        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);

        Otp otpEntity = new Otp(request.getEmail(), otp, now, expiresAt);
        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(request.getEmail(), otp);
    }

    public String verifyOtp(VerifyOtpRequest request) {
        Otp otp = otpRepository.findByOtpCodeAndExpiresAtAfter(
                request.getOtp(), LocalDateTime.now())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired OTP"));

        if (otp.isVerified()) {
            throw new AuthenticationException("OTP already used");
        }

        otp.setVerified(true);
        otpRepository.save(otp);

        return jwtUtil.generateResetToken(otp.getEmail());
    }

    public void resetPassword(String resetToken, ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Passwords do not match");
        }

        String email;
        try {
            email = jwtUtil.extractUsername(resetToken);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid reset token");
        }

        if (jwtUtil.extractExpiration(resetToken).before(new Date())) {
            throw new AuthenticationException("Reset token expired");
        }

        Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new AuthenticationException("No OTP found for this email"));

        if (!otp.isVerified()) {
            throw new AuthenticationException("OTP not verified");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.deleteByEmail(email);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
