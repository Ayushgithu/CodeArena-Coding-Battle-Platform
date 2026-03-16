package com.codearena.backend.dto;


public class LoginResponseDTO {
    private String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    // getter
    public String getToken() { return token; }
}

