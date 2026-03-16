package com.codearena.backend.repository;

import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String id);
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByUser_Id(String id);
    Optional<UserProfile> findByEmail(String email);

    // ✅ Efficiency Ranking (Removing Score)
    // Formula: (Wins * 100) - (Losses * 20) + (TotalBattles * 1)
    @Query("SELECT u FROM UserProfile u ORDER BY " +
            "((u.totalWin * 100) - (u.totalLoss * 20) + (u.totalBattle * 1)) DESC, " +
            "u.totalLoss ASC")
    List<UserProfile> findTop10ByEfficiencyRanking();

    // ✅ Dynamic Count Query for Dashboard Rank
    @Query("SELECT COUNT(u) FROM UserProfile u WHERE " +
            "((u.totalWin * 100) - (u.totalLoss * 20) + (u.totalBattle * 1)) > :rating")
    long countByEfficiencyRatingGreaterThan(@Param("rating") int rating);

    List<UserProfile> findTop10ByOrderByScoreDesc();
}