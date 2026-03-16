package com.codearena.backend.service;

import com.codearena.backend.dto.AuthResponse;
import com.codearena.backend.dto.ChangePasswordDTO;
import com.codearena.backend.dto.LoginRequestDTO;
import com.codearena.backend.dto.SignupRequestDTO;
import com.codearena.backend.entity.User;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserService {
    AuthResponse login(@Valid LoginRequestDTO request);
    User createUser(@Valid SignupRequestDTO request);
    User getCurrentUser();
    void changePassword(@Valid ChangePasswordDTO changePasswordDTO);
}