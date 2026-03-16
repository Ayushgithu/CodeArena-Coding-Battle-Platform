package com.codearena.backend.repository;

import com.codearena.backend.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    /**
     * Find room by room code (no lock)
     */
    Optional<Room> findByRoomCode(int roomCode);

    /**
     * ✅ RACE CONDITION FIX: Find room with pessimistic write lock
     * This prevents multiple users from joining the same room simultaneously
     *
     * How it works:
     * - Thread 1 calls this method → Gets lock on Room row in DB
     * - Thread 2 calls this method → WAITS for Thread 1 to finish
     * - Thread 1 checks joinBy == null, sets it, commits → Releases lock
     * - Thread 2 now gets lock → Sees joinBy != null → Throws exception
     *
     * @param roomCode - The room code to find and lock
     * @return Optional<Room> with database-level pessimistic write lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.roomCode = :roomCode")
    Optional<Room> findByRoomCodeWithLock(@Param("roomCode") int roomCode);
}