package com.codearena.backend.schedular;

import com.codearena.backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCleanupScheduler {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Runs when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void cleanOnStartup() {
        log.info("🚀 Running chat cleanup on application startup...");
        deleteOldChats();
    }

    /**
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanDailyAt2AM() {
        log.info("⏰ Running scheduled chat cleanup at 2 AM...");
        deleteOldChats();
    }

    private void deleteOldChats() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date cutoffDate = calendar.getTime();

        int deletedCount = chatMessageRepository.hardDeleteOldMessages(cutoffDate);

        log.info("🧹 Hard deleted {} chat messages older than 1 day", deletedCount);
    }
}
