package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.UserDetailsDTO;
import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.UserProfileRepository;
import com.codearena.backend.service.UserDetail;
import com.codearena.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailImpl implements UserDetail {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;

    public UserDetailImpl(UserProfileRepository userProfileRepository,
                              UserService userService) {

        this.userProfileRepository = userProfileRepository;
        this.userService = userService;
    }



    // ---------------------------------------------------------
    // UPDATE CURRENT LOGGED-IN USER
    // ---------------------------------------------------------
    @Override
    public UserDetailsDTO updateCurrentUser(UserDetailsDTO dto) {

        User user = userService.getCurrentUser();

        UserProfile userProfile = userProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User details not found for current user")
                );

        // Update fields ONLY if provided
        if (dto.getName() != null) userProfile.setName(dto.getName());
        if (dto.getUserRank() > 0) userProfile.setUserRank(dto.getUserRank());

        userProfile.setTotalWin(dto.getTotalWin());
        userProfile.setTotalLoss(dto.getTotalLoss());
        userProfile.setTotalBattle(dto.getTotalBattle());


        // Save updated details
        userProfile = userProfileRepository.save(userProfile);

        return toDTO(userProfile);
    }


    // ---------------------------------------------------------
    // GET CURRENT USER DETAILS
    // ---------------------------------------------------------
    @Override
    public UserDetailsDTO getCurrentUserDetails() {

        User user = userService.getCurrentUser();

        UserProfile details = userProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User details not found")
                );

        return toDTO(details);
    }


    // ---------------------------------------------------------
    // GET USER DETAILS BY USERNAME
    // ---------------------------------------------------------
    @Override
    public UserDetailsDTO getUserDetailsByUsername(String email) {

        UserProfile details = userProfileRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User details not found for username: " + email)
                );

        return toDTO(details);
    }


    // ---------------------------------------------------------
    // DTO MAPPER
    // ---------------------------------------------------------
    private UserDetailsDTO toDTO(UserProfile userProfile) {

        UserDetailsDTO dto = new UserDetailsDTO();

        dto.setId(userProfile.getId());
        dto.setUserId(userProfile.getUser().getId());
        dto.setUsername(userProfile.getUser().getUsername());
        dto.setName(userProfile.getName());
        dto.setEmail(userProfile.getEmail());
        dto.setUserRank(userProfile.getUserRank());
        dto.setTotalWin(userProfile.getTotalWin());
        dto.setTotalLoss(userProfile.getTotalLoss());
        dto.setTotalBattle(userProfile.getTotalBattle());

        return dto;
    }
}
