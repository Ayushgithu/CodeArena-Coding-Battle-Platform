package com.codearena.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupVerifyRequestDTO {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    @Valid
    private SignupRequestDTO signupRequest;
}