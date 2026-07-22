package com.practice.todoApp.controller;

import com.practice.todoApp.dto.ApiResponse;
import com.practice.todoApp.dto.auth.AuthResponse;
import com.practice.todoApp.dto.auth.ForgotPasswordRequest;
import com.practice.todoApp.dto.auth.LoginRequest;
import com.practice.todoApp.dto.auth.RegisterRequest;
import com.practice.todoApp.dto.auth.ResetPasswordRequest;
import com.practice.todoApp.dto.auth.VerifyOtpRequest;
import com.practice.todoApp.model.User;
import com.practice.todoApp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and password management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            ApiResponse<User> response = ApiResponse.created("User registered successfully.", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = ApiResponse.error(400, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            ApiResponse<AuthResponse> response = ApiResponse.success("Login successful.", authResponse);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<AuthResponse> response = ApiResponse.error(401, "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends an OTP to the user's email for password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            ApiResponse<Void> response = ApiResponse.success("OTP sent to your email.", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.error(400, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the user's email")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            String email = authService.verifyOtp(request);
            ApiResponse<String> response = ApiResponse.success("OTP verified successfully.", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = ApiResponse.error(400, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using the reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Reset token received after OTP verification", required = true)
            @RequestHeader("X-Reset-Token") String resetToken,
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(resetToken, request);
            ApiResponse<Void> response = ApiResponse.success("Password reset successfully.", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.error(400, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
