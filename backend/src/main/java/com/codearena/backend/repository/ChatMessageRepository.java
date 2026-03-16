package com.codearena.backend.repository;


import com.codearena.backend.entity.ChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * Why this method?
     * Spring Data JPA is smart. By naming the method this way, it automatically
     * understands what we want.
     * "findBy..."   -> "I want to search..."
     * "...Room..."   -> "...by the 'room' field in the ChatMessage entity..."
     * "...RoomCode" -> "...and specifically, by the 'roomCode' field inside that 'Room' entity."
     * * This will find all messages for a specific room code, which is perfect for
     * loading chat history.
     */
    List<ChatMessage> findByRoomRoomCode(int roomCode);
    List<ChatMessage> findByRoom_IdOrderByCreationDateAsc(String roomId);
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ChatMessage c
        WHERE c.creationDate < :cutoffDate
    """)
    int hardDeleteOldMessages(@Param("cutoffDate") Date cutoffDate);


}
