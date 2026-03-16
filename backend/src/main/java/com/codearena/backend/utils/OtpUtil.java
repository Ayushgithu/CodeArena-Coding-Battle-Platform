package com.codearena.backend.utils;

import java.security.SecureRandom;

public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();

    public static String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
