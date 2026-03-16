package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.AuthResponse;
import com.codearena.backend.dto.ResetPasswordDTO;
import com.codearena.backend.dto.SignupRequestDTO;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.EmailSendingException;
import com.codearena.backend.exception.InvalidCredentialsException;
import com.codearena.backend.exception.ResourceAlreadyExistsException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.AuthService;
import com.codearena.backend.service.EmailService;
import com.codearena.backend.service.OtpService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.OtpUtil;
import com.codearena.backend.utils.constant.AppConstant;
import com.codearena.backend.utils.security.JWTHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTHelper jwtHelper;
    private final UserDetailsService userDetailsService;

    public AuthServiceImpl(UserRepository userRepository,
                          OtpService otpService,
                          EmailService emailService,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          JWTHelper jwtHelper,
                          UserDetailsService userDetailsService) {

        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
        this.userDetailsService = userDetailsService;
    }


    // Better email regex pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^.{6,}$");

    @Override
    public void requestSignupOtp(String email) {
        // Validation
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email format");
        }

        if (userRepository.findByUsername(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        try {
            String otp = OtpUtil.generateOtp();
            otpService.saveOtp("SIGNUP_OTP:" + email, otp, 5);

            emailService.sendSimpleMail(
                    email,
                    "CodeArena Signup OTP",
                    "Your OTP for CodeArena signup is: " + otp + "\nThis OTP will expire in 5 minutes."
            );

            logger.info("Signup OTP sent successfully to: {}", email);

        } catch (EmailSendingException e) {
            logger.error("Failed to send signup OTP to {}: {}", email, e.getMessage());
            // Delete OTP if email sending failed
            otpService.deleteOtp("SIGNUP_OTP:" + email);
            throw new BadRequestException("Failed to send OTP. Please try again later.");
        }
    }

    @Override
    public AuthResponse verifySignupOtp(String email, String otp, SignupRequestDTO request) {
        // Validate email matches
        if (!email.equals(request.getEmail())) {
            throw new BadRequestException("Email mismatch");
        }

        validateSignupRequest(request);

        // Set default role if not provided
        if (request.getRole() == null || request.getRole().isBlank()) {
            request.setRole(AppConstant.PLAYER);
        }

        String key = "SIGNUP_OTP:" + email;

        if (!otpService.verifyOtp(key, otp)) {
            throw new InvalidCredentialsException("Invalid or expired OTP");
        }

        // Use synchronized block to prevent race condition
        synchronized (this) {
            // Check if user already exists BEFORE deleting OTP
            if (userRepository.findByUsername(email).isPresent()) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }

            User user = userService.createUser(request);
            otpService.deleteOtp(key);

            // Generate JWT token
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtHelper.generateToken(userDetails);

            return AuthResponse.builder()
                    .jwtToken(token)
                    .userId(user.getId())
                    .email(user.getUsername())
                    .name(request.getName())
                    .role(user.getRole() != null ? user.getRole().getName() : "PLAYER") // Add role to response
                    .build();
        }
    }

    @Override
    public void requestForgotPasswordOtp(String email) {
        // Validation
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email format");
        }

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        // Rate limiting check (basic)
        String rateLimitKey = "FORGOT_RATE:" + email;
        String lastRequest = otpService.getOtp(rateLimitKey);
        if (lastRequest != null) {
            throw new BadRequestException("Please wait 2 minutes before requesting another OTP");
        }

        try {
            String otp = OtpUtil.generateOtp();
            otpService.saveOtp("FORGOT_OTP:" + email, otp, 5);
            otpService.saveOtp(rateLimitKey, "requested", 2); // 2 minute rate limit

            emailService.sendSimpleMail(
                    email,
                    "Reset Password OTP",
                    "Your password reset OTP is: " + otp + "\nThis OTP will expire in 5 minutes."
            );

            logger.info("Forgot password OTP sent successfully to: {}", email);

        } catch (EmailSendingException e) {
            logger.error("Failed to send forgot password OTP to {}: {}", email, e.getMessage());
            // Clean up OTP if email sending failed
            otpService.deleteOtp("FORGOT_OTP:" + email);
            otpService.deleteOtp(rateLimitKey);
            throw new BadRequestException("Failed to send OTP. Please try again later.");
        }
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        // Validation
        validateResetPasswordDTO(dto);

        String key = "FORGOT_OTP:" + dto.getEmail();

        if (!otpService.verifyOtp(key, dto.getOtp())) {
            throw new InvalidCredentialsException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if new password is same as old
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        otpService.deleteOtp(key);
        // Also delete rate limit key
        otpService.deleteOtp("FORGOT_RATE:" + dto.getEmail());
    }

    // ========== HELPER METHODS ==========

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    private void validateSignupRequest(SignupRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new BadRequestException("Invalid email format");
        }
        if (!isValidPassword(request.getPassword())) {
            throw new BadRequestException("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }
    }

    private void validateResetPasswordDTO(ResetPasswordDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (dto.getOtp() == null || dto.getOtp().isBlank()) {
            throw new BadRequestException("OTP is required");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (!isValidEmail(dto.getEmail())) {
            throw new BadRequestException("Invalid email format");
        }
        if (!isValidPassword(dto.getNewPassword())) {
            throw new BadRequestException("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }
    }
}