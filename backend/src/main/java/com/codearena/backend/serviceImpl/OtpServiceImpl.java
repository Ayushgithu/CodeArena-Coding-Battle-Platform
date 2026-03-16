package com.codearena.backend.serviceImpl;

import com.codearena.backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;

    public OtpServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void saveOtp(String key, String otp, long ttlMinutes) {
        redisTemplate.opsForValue()
                .set(key, otp, Duration.ofMinutes(ttlMinutes));
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(key);
        return otp != null && otp.equals(storedOtp);
    }

    @Override
    public void deleteOtp(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public String getOtp(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}