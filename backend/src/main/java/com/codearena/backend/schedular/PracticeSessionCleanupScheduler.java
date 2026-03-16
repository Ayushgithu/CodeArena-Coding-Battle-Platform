package com.codearena.backend.schedular;

import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.repository.PracticeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ Scheduled cleanup of expired practice sessions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PracticeSessionCleanupScheduler {

    private final PracticeSessionRepository practiceSessionRepository;

    /**
     * Run cleanup on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOnStartup() {
        log.info("🚀 Running practice session cleanup on application startup...");
        cleanupExpiredSessions();
    }

    /**
     * Run cleanup every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * ?") // Every 6 hours
    public void scheduledCleanup() {
        log.info("⏰ Running scheduled practice session cleanup...");
        cleanupExpiredSessions();
    }

    /**
     * Mark expired sessions as completed and expired
     */
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        // Find sessions that expired but weren't marked
        List<PracticeSession> expiredSessions = practiceSessionRepository
                .findAll()
                .stream()
                .filter(session -> !session.isCompleted() &&
                        !session.isExpired() &&
                        session.getExpiresAt().isBefore(now))
                .toList();

        if (expiredSessions.isEmpty()) {
            log.info("✅ No expired sessions to cleanup");
            return;
        }

        // Mark as expired and completed
        expiredSessions.forEach(session -> {
            session.setExpired(true);
            session.setCompleted(true);
            log.debug("Marked session {} as expired", session.getId());
        });

        practiceSessionRepository.saveAll(expiredSessions);

        log.info("🧹 Marked {} expired practice sessions as completed", expiredSessions.size());

        // Additional cleanup: Delete very old completed sessions (> 30 days)
        cleanupOldCompletedSessions();
    }

    /**
     * Delete completed sessions older than 30 days
     */
    @Transactional
    public void cleanupOldCompletedSessions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<PracticeSession> oldSessions = practiceSessionRepository
                .findAll()
                .stream()
                .filter(session -> session.isCompleted() &&
                        session.getExpiresAt().isBefore(cutoffDate))
                .toList();

        if (!oldSessions.isEmpty()) {
            practiceSessionRepository.deleteAll(oldSessions);
            log.info("🗑️ Deleted {} old completed practice sessions (>30 days)", oldSessions.size());
        }
    }
}