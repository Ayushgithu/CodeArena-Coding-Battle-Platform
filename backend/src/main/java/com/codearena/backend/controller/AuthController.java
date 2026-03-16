package com.codearena.backend.controller;

import com.codearena.backend.dto.*;
import com.codearena.backend.exception.EmailSendingException;
import com.codearena.backend.service.AuthService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.service.ValidationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            AuthResponse authResponse = userService.login(request);
            return ResponseEntity.ok(
                    StandardResponse.success("Login successful", authResponse)
            );
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestSignupOtp(@RequestParam @Valid String email) {
        try {
            validationService.validateEmail(email, "Email");
            authService.requestSignupOtp(email);
            return ResponseEntity.ok(
                    StandardResponse.success("OTP sent successfully", null)
            );
        } catch (Exception e) {
            logger.error("Signup OTP request failed for {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifySignupOtp(@RequestBody @Valid SignupVerifyRequestDTO request) {
        try {
            AuthResponse authResponse = authService.verifySignupOtp(
                    request.getEmail(),
                    request.getOtp(),
                    request.getSignupRequest()
            );
            return ResponseEntity.ok(
                    StandardResponse.success("Signup successful", authResponse)
            );
        } catch (Exception e) {
            logger.error("Signup verification failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<?> requestForgotPasswordOtp(@RequestParam @Valid String email) {
        try {
            authService.requestForgotPasswordOtp(email);
            return ResponseEntity.ok(
                    StandardResponse.success("Reset password OTP sent successfully", null)
            );
        } catch (Exception e) {
            logger.error("Forgot password OTP request failed for {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        try {
            authService.resetPassword(resetPasswordDTO);
            return ResponseEntity.ok(
                    StandardResponse.success("Password reset successfully", null)
            );
        } catch (Exception e) {
            logger.error("Password reset failed for {}: {}", resetPasswordDTO.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        try {
            userService.changePassword(changePasswordDTO);
            return ResponseEntity.ok(
                    StandardResponse.success("Password changed successfully", null)
            );
        } catch (Exception e) {
            logger.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            logger.info("User logout requested");
            // Since we're using JWT, logout is handled client-side by removing the token
            // This endpoint can be used for logging/analytics or token blacklisting if needed
            return ResponseEntity.ok(
                    StandardResponse.success("Logout successful", null)
            );
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error(e.getMessage()));
        }
    }
}