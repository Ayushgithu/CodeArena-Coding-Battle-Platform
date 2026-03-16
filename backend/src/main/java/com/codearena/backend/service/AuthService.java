package com.codearena.backend.service;

import com.codearena.backend.dto.AuthResponse;
import com.codearena.backend.dto.ResetPasswordDTO;
import com.codearena.backend.dto.SignupRequestDTO;
import com.codearena.backend.entity.User;

public interface AuthService {
    void requestSignupOtp(String email);
    AuthResponse verifySignupOtp(String email, String otp, SignupRequestDTO request);
    void requestForgotPasswordOtp(String email);
    void resetPassword(ResetPasswordDTO dto);
}