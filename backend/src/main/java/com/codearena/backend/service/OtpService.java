package com.codearena.backend.service;

public interface OtpService {
    void saveOtp(String key, String otp, long ttlMinutes);
    boolean verifyOtp(String key, String otp);
    void deleteOtp(String key);
    String getOtp(String key); // Add this method
}