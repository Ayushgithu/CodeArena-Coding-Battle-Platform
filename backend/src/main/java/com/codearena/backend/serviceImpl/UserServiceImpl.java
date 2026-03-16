package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.AuthResponse;
import com.codearena.backend.dto.ChangePasswordDTO;
import com.codearena.backend.dto.LoginRequestDTO;
import com.codearena.backend.dto.SignupRequestDTO;
import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.InvalidCredentialsException;
import com.codearena.backend.exception.ResourceAlreadyExistsException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.RoleRepository;
import com.codearena.backend.repository.UserProfileRepository;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.constant.AppConstant;
import com.codearena.backend.utils.security.JWTHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsService userDetailsService;
    private final UserProfileRepository userProfileRepository;
    private final JWTHelper helper;
    private final PasswordEncoder passwordEncoder;



    @Override
    public AuthResponse login(LoginRequestDTO request) {
        logger.info("🔑 Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("❌ User not found: {}", request.getUsername());
                    return new InvalidCredentialsException("Invalid username or password");
                });

        logger.info("✅ User found: {} with ID: {}", user.getUsername(), user.getId());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        logger.info("🔑 Password match result: {}", passwordMatches);

        if (!passwordMatches) {
            logger.warn("❌ Password mismatch for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Load UserDetails
        org.springframework.security.core.userdetails.UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            logger.info("✅ UserDetails loaded successfully for: {}", user.getUsername());
            logger.info("✅ UserDetails class: {}", userDetails.getClass().getName());
            logger.info("✅ Authorities: {}", userDetails.getAuthorities());
        } catch (Exception e) {
            logger.error("❌ Failed to load UserDetails: {}", e.getMessage(), e);
            throw new InvalidCredentialsException("Authentication failed");
        }

        // Generate JWT token
        String jwtToken;
        try {
            logger.info("🔄 Attempting to generate JWT token...");
            jwtToken = helper.generateToken(userDetails);
            logger.info("✅ JWT token generated for user: {}", user.getUsername());

            // Debug: Show token structure
            if (jwtToken != null && jwtToken.length() > 50) {
                logger.info("🔍 Generated token (first 50 chars): {}...",
                        jwtToken.substring(0, 50));
            } else {
                logger.info("🔍 Generated token: {}", jwtToken);
            }

            String[] parts = jwtToken != null ? jwtToken.split("\\.") : new String[0];
            logger.info("🔍 Token has {} parts (should be 3)", parts.length);

            // Try to decode it back to verify
            try {
                String decodedUsername = helper.getUsernameFromToken(jwtToken);
                logger.info("✅ Token can be decoded, username: {}", decodedUsername);
            } catch (Exception e) {
                logger.error("❌ Generated token cannot be decoded: {}", e.getMessage());
                // Don't throw here, just log the error
            }

        } catch (Exception e) {
            logger.error("❌ Failed to generate JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate authentication token: " + e.getMessage());
        }

        // Get user details for response
        UserProfile userProfileEntity = userProfileRepository.findByUser_Id(user.getId())
                .orElse(new UserProfile());

        AuthResponse response = AuthResponse.builder()
                .jwtToken(jwtToken)
                .userId(user.getId())
                .email(user.getUsername())
                .name(userProfileEntity.getName() != null ? userProfileEntity.getName() : user.getUsername())
                .role(user.getRole() != null ? user.getRole().getName() : "PLAYER")
                .build();

        logger.info("✅ Login successful for: {}", user.getUsername());
        logger.info("✅ Response JWT token length: {}", jwtToken != null ? jwtToken.length() : 0);

        return response;
    }

    @Override
    public User createUser(@Valid SignupRequestDTO request) {
        logger.info("👤 Creating user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByUsername(request.getEmail()).isPresent()) {
            logger.warn("❌ User already exists: {}", request.getEmail());
            throw new ResourceAlreadyExistsException("Email already exists, please login");
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String role = request.getRole();
        if (role == null || role.isBlank()) {
            role = AppConstant.PLAYER;
        }

        if (AppConstant.PLAYER.equals(role)) {
            user.setRole(roleRepository.findByName(AppConstant.PLAYER)
                    .orElseThrow(() -> new ResourceNotFoundException("PLAYER role not found")));
        } else if (AppConstant.INTERVIEWER.equals(role)) {
            user.setRole(roleRepository.findByName(AppConstant.INTERVIEWER)
                    .orElseThrow(() -> new ResourceNotFoundException("INTERVIEWER role not found")));
        } else {
            throw new BadRequestException("Invalid role specified");
        }

        user = userRepository.save(user);
        logger.info("✅ User created with ID: {}", user.getId());

        // Save user details
        UserProfile details = new UserProfile();
        details.setUser(user);
        details.setEmail(request.getEmail());
        details.setName(request.getName());
        details.setTotalBattle(0);
        details.setTotalWin(0);
        details.setTotalLoss(0);
        details.setUserRank(0);
        userProfileRepository.save(details);

        logger.info("✅ User profile created for: {}", request.getEmail());

        return user;
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("🔍 Getting current user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in session"));
    }

    @Override
    public void changePassword(@Valid ChangePasswordDTO changePasswordDTO) {
        logger.info("🔐 Changing password for current user");

        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), currentUser.getPassword())) {
            logger.warn("❌ Current password incorrect for user: {}", currentUser.getUsername());
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), currentUser.getPassword())) {
            logger.warn("❌ New password same as old for user: {}", currentUser.getUsername());
            throw new BadRequestException("New password must be different from current password");
        }

        currentUser.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(currentUser);

        logger.info("✅ Password changed successfully for user: {}", currentUser.getUsername());
    }
}