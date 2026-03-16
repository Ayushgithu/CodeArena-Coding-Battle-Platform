package com.codearena.backend.service;

import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserProfileService {
    @Transactional
    void updateUserStats(User user, boolean isWinner, int matchScore);

    UserProfile updateProfileInfo(String username, UserProfile updatedData);

    UserProfile getUserProfile(String username);

    List<UserProfile> getLeaderboard();
}
