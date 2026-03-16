package com.codearena.backend.serviceImpl;

import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import com.codearena.backend.repository.UserProfileRepository;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileServiceImpl(UserRepository userRepository,
                                 UserProfileRepository userProfileRepository) {

        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }


    @Override
    @Transactional
    public void updateUserStats(User user, boolean isWinner, int matchScore) {
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        profile.setTotalBattle(profile.getTotalBattle() + 1);
        if (isWinner) {
            profile.setTotalWin(profile.getTotalWin() + 1);
        } else {
            profile.setTotalLoss(profile.getTotalLoss() + 1);
        }

        // We still keep the score in DB for record, but ranking won't use it
        profile.setScore(profile.getScore() + matchScore);

        userProfileRepository.save(profile);
    }

    @Override
    public List<UserProfile> getLeaderboard() {
        // ✅ Uses the Efficiency formula: Wins, Losses, Battles
        List<UserProfile> leaderboard = userProfileRepository.findTop10ByEfficiencyRanking();

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setUserRank(i + 1);
        }

        return leaderboard;
    }

    @Override
    public UserProfile getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        // ✅ Step 1: Calculate Current Rating (Efficiency Logic)
        int currentRating = (profile.getTotalWin() * 100) - (profile.getTotalLoss() * 20) + (profile.getTotalBattle() * 1);

        // ✅ Step 2: Use the same Advanced Ranking logic as the leaderboard
        List<UserProfile> top10 = userProfileRepository.findTop10ByEfficiencyRanking();

        int rank = -1;
        for (int i = 0; i < top10.size(); i++) {
            if (top10.get(i).getId().equals(profile.getId())) {
                rank = i + 1;
                break;
            }
        }

        // ✅ Step 3: If not in Top 10, calculate global position dynamically
        if (rank == -1) {
            long betterPlayers = userProfileRepository.countByEfficiencyRatingGreaterThan(currentRating);
            rank = (int) betterPlayers + 1;
        }

        profile.setUserRank(rank);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateProfileInfo(String username, UserProfile updatedData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        if (updatedData.getName() != null) profile.setName(updatedData.getName());

        return userProfileRepository.save(profile);
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile newProfile = new UserProfile();
        newProfile.setUser(user);
        newProfile.setName(user.getUsername());
        newProfile.setEmail(user.getUsername());
        newProfile.setTotalBattle(0);
        newProfile.setTotalWin(0);
        newProfile.setTotalLoss(0);
        newProfile.setScore(0);
        newProfile.setUserRank(0);
        return userProfileRepository.save(newProfile);
    }
}