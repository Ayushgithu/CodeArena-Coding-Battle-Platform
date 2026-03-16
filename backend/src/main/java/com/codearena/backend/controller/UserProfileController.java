package com.codearena.backend.controller;

import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.entity.UserProfile;
import com.codearena.backend.service.UserProfileService;
import com.codearena.backend.utils.security.JWTHelper; // ✅ Import JWTHelper
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final JWTHelper jwtHelper; // ✅ Inject Helper to extract username

    public UserProfileController(UserProfileService userProfileService, JWTHelper jwtHelper) {
        this.userProfileService = userProfileService;
        this.jwtHelper = jwtHelper;
    }

    // 1. Get Leaderboard (Anyone can access - No change needed)
    @GetMapping("/leaderboard")
    public ResponseEntity<StandardResponse<List<UserProfile>>> getLeaderboard() {
        List<UserProfile> leaderboard = userProfileService.getLeaderboard();
        return ResponseEntity.ok(StandardResponse.success("Leaderboard fetched", leaderboard));
    }

    // 2. Get My Profile (Manually handling Token)
    @GetMapping("/profile")
    public ResponseEntity<StandardResponse<UserProfile>> getMyProfile(
            @RequestHeader("Authorization") String token) { // ✅ Use RequestHeader

        // Manual extraction: Remove "Bearer " and get username
        String username = jwtHelper.getUsernameFromToken(token.substring(7));

        // Use the extracted username
        UserProfile profile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(StandardResponse.success("Profile fetched", profile));
    }

    // 3. Update My Profile (Manually handling Token)
    @PutMapping("/profile")
    public ResponseEntity<StandardResponse<UserProfile>> updateProfile(
            @RequestHeader("Authorization") String token, // ✅ Use RequestHeader
            @RequestBody UserProfile updatedData) {

        // Manual extraction
        String username = jwtHelper.getUsernameFromToken(token.substring(7));

        UserProfile result = userProfileService.updateProfileInfo(username, updatedData);
        return ResponseEntity.ok(StandardResponse.success("Profile updated", result));
    }
}