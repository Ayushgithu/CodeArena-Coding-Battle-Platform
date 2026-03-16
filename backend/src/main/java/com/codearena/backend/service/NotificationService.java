package com.codearena.backend.service;

import com.codearena.backend.dto.PracticeResultDTO;
import com.codearena.backend.entity.User;

/**
 * Service for sending notifications to users
 */
public interface NotificationService {

    /**
     * Send notification when practice session starts
     */
    void sendPracticeSessionStarted(User user, String sessionId);

    /**
     * Send notification when practice session completes
     */
    void sendPracticeSessionCompleted(User user, String sessionId, PracticeResultDTO result);

    /**
     * Send notification when session is extended
     */
    void sendSessionExtended(User user, String sessionId, int additionalMinutes);

    /**
     * Send notification when session is abandoned
     */
    void sendSessionAbandoned(User user, String sessionId);
}