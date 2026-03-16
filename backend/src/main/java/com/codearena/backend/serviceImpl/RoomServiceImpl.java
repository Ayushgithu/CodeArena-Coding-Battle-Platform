package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.*;
import com.codearena.backend.exception.ResourceNotFoundException;
// ✅ FIX: Import BadRequestException
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.repository.*;
import com.codearena.backend.service.CodingQuestionService;
import com.codearena.backend.service.McqQuestionService;
import com.codearena.backend.service.RoomService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final DropListRepository dropListRepository;
    private final UserProfileRepository userProfileRepository;
    private final CodingQuestionService codingQuestionService;
    private final McqQuestionService mcqQuestionService;
    private final MatchResultRepository matchResultRepository;
    private final SubmissionRepository submissionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    @Override
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO roomRequestDTO) {
        log.info("Creating room with request: {}", roomRequestDTO);

        // ✅ Input validation
        validateRoomRequest(roomRequestDTO);

        User currentUser = userService.getCurrentUser();

        Room room = new Room();
        room.setJoinBy(null);
        room.setMadeBy(currentUser);
        room.setExpiryDuration(roomRequestDTO.getDuration());
        room.setRoomCode(generateUniqueRoomCode());
        room.setStatus(Status.WAITING);

        room.setQuestionType(dropListRepository.findById(roomRequestDTO.getQuestionType())
                .orElseThrow(() -> new BadRequestException("Invalid question type")));

        room.setNoOfQuestion(roomRequestDTO.getNoOfQuestions());

        if (roomRequestDTO.getDifficulty() != null && !roomRequestDTO.getDifficulty().isBlank()) {
            try {
                room.setDifficulty(Difficulty.valueOf(roomRequestDTO.getDifficulty().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid difficulty value: " + roomRequestDTO.getDifficulty());
            }
        }

        Room savedRoom = roomRepository.save(room);
        log.info("Room created successfully with code: {}", savedRoom.getRoomCode());

        return mapToDTO(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDTO joinRoom(int roomCode) {
        log.info("User attempting to join room: {}", roomCode);

        User joiningUser = userService.getCurrentUser();

        // ✅ RACE CONDITION FIX: Use pessimistic write lock to prevent concurrent joins
        // This ensures only ONE transaction can modify this room at a time
        Room room = roomRepository.findByRoomCodeWithLock(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + roomCode));

        // ✅ Security: Validate room status
        if (!Status.WAITING.equals(room.getStatus()) && !Status.IN_PROGRESS.equals(room.getStatus())) {
            throw new BadRequestException("Room is no longer accepting players. Status: " + room.getStatus());
        }

        // ✅ CRITICAL: Check if room is already full (race condition prevented by lock)
        if (room.getJoinBy() != null) {
            throw new BadRequestException("Room is already full");
        }

        // ✅ Security: Prevent creator from joining their own room
        if (room.getMadeBy().getId().equals(joiningUser.getId())) {
            throw new BadRequestException("You cannot join your own room. Please wait for another player.");
        }

        // ✅ Security: Check if room has expired
        if (room.getExpiryTime() != null && LocalDateTime.now().isAfter(room.getExpiryTime())) {
            room.setStatus(Status.EXPIRED);
            roomRepository.save(room);
            throw new BadRequestException("Room has expired");
        }

        // ✅ ATOMIC UPDATE: Set joining user and update status in single transaction
        room.setJoinBy(joiningUser);
        room.setStatus(Status.IN_PROGRESS);

        Room updatedRoom = roomRepository.save(room);
        log.info("User {} successfully joined room {} (locked transaction)",
                joiningUser.getUsername(), roomCode);

        return mapToDTO(updatedRoom);
    }

    @Override
    @jakarta.transaction.Transactional
    public void startRoom(int roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomCode));

        if (room.getJoinBy() == null) {
            throw new BadRequestException("Cannot start room — waiting for another player to join.");
        }

        if (room.getStatus() == Status.ACTIVE) {
            throw new BadRequestException("Match already started.");
        }

        // 1. Fetch Question (Logic based on questionType and Difficulty)
        QuestionFetchDTO fetchedQuestion = assignQuestionToRoom(room);

        // 2. Update Room Status with match metadata (Phase 1)
        room.setStatus(Status.ACTIVE);
        room.setStartedAt(LocalDateTime.now());
        room.setExpiryTime(LocalDateTime.now().plusMinutes(room.getExpiryDuration()));
        roomRepository.save(room);

    }

    private QuestionFetchDTO assignQuestionToRoom(Room room) {
        String questionType = room.getQuestionType().getOptionValue();
        Difficulty difficulty = room.getDifficulty();
        System.out.println(" question type "+questionType);
        System.out.println(" difficulty "+difficulty);
        if ("Coding Question".equalsIgnoreCase(questionType)) {
            // Fetch one coding question based on difficulty
            List<CodingQuestionDTO> questions = codingQuestionService.getByDifficultyAndCount(
                    difficulty != null ? difficulty.name() : Difficulty.EASY.name(), 1
            );
            if (questions.isEmpty()) {
                throw new ResourceNotFoundException("No coding questions found for difficulty: " + difficulty);
            }
            CodingQuestionDTO qDto = questions.get(0);
            return mapCodingToQuestionFetchDTO(qDto);

        } else if ("MCQ Question".equalsIgnoreCase(questionType)) {
            // Fetch one MCQ question based on difficulty
            List<McqQuestionResponseDTO> questions = mcqQuestionService.getByDifficultyAndCount(
                    difficulty != null ? difficulty.name() : Difficulty.EASY.name(), 1
            );
            if (questions.isEmpty()) {
                throw new ResourceNotFoundException("No MCQ questions found for difficulty: " + difficulty);
            }
            McqQuestionResponseDTO qDto = questions.get(0);
            return mapMcqToQuestionFetchDTO(qDto);

        } else {
            throw new BadRequestException("Unsupported question type: " + questionType);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomDetails(int roomCode) {
        log.info("Fetching room details for code: {}", roomCode);

        User currentUser = userService.getCurrentUser();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + roomCode));

        // ✅ Security: Only participants can view room details
        boolean isParticipant = room.getMadeBy().getId().equals(currentUser.getId()) ||
                (room.getJoinBy() != null && room.getJoinBy().getId().equals(currentUser.getId()));

        if (!isParticipant) {
            throw new BadRequestException("You don't have permission to view this room");
        }

        return mapToDTO(room);
    }

    @Override
    public void endTest(int roomCode) {
        User user = userService.getCurrentUser();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow();

        // Prevent double end
        MatchResult result =
                matchResultRepository
                        .findByRoomIdAndUserId(room.getId(), user.getId())
                        .orElseGet(() -> {

                            MatchResult r = new MatchResult();
                            r.setRoom(room);
                            r.setUser(user);
                            return r;
                        });

        if(!result.isFinished()) {
            result.setFinished(true);
            result.setFinishedAt(LocalDateTime.now());
            matchResultRepository.save(result);
        }

        // Check if both users ended
        long finishedCount =
                matchResultRepository.countByRoomIdAndFinishedTrue(room.getId());

        log.info("Finished status for room {}: {}", room.getRoomCode(), finishedCount);

        if(finishedCount == 2) {
            finalizeRoom(room);
        }
    }
    @Transactional
    public void finalizeRoom(Room room) {

        if (room.getStatus() == Status.COMPLETED) return;

        User user1 = room.getMadeBy();
        User user2 = room.getJoinBy();

        // ---------- FETCH SCORES ----------

        List<Object[]> results =
                submissionRepository.calculateRoomScore(room.getId());

        int score1 = 0;
        int score2 = 0;

        // Map results safely
        for (Object[] row : results) {

            User u = (User) row[0];
            int score = ((Number) row[1]).intValue();

            if (u.getId().equals(user1.getId())) {
                score1 = score;
            }

            if (u.getId().equals(user2.getId())) {
                score2 = score;
            }
        }

        System.out.println("User1: " + user1.getUsername() + " Score=" + score1);
        System.out.println("User2: " + user2.getUsername() + " Score=" + score2);

        // ---------- WINNER LOGIC ----------

        boolean user1Winner;

        if (score1 == score2) {

            MatchResult r1 =
                    matchResultRepository
                            .findByRoomIdAndUserId(room.getId(), user1.getId())
                            .orElseThrow();

            MatchResult r2 =
                    matchResultRepository
                            .findByRoomIdAndUserId(room.getId(), user2.getId())
                            .orElseThrow();

            user1Winner =
                    r1.getFinishedAt().isBefore(r2.getFinishedAt());

        } else {

            user1Winner = score1 > score2;
        }

        // ---------- SAVE RESULTS ----------

        updateResult(room, user1, score1, user1Winner);
        updateResult(room, user2, score2, !user1Winner);

        room.setStatus(Status.COMPLETED);
        room.setEndedAt(LocalDateTime.now());

        roomRepository.save(room);

        // ---------- SEND NOTIFICATION ----------

        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "MATCH_COMPLETED");

        String winnerName = "TIE";
        if (score1 != score2) {
            winnerName = user1Winner ? user1.getUsername() : user2.getUsername();
        } else {
             // If scores are equal, we determined user1Winner based on time
             winnerName = user1Winner ? user1.getUsername() : user2.getUsername();
        }

        notification.put("winner", winnerName);
        notification.put("message", score1 == score2 ? "It's a Tie!" : winnerName + " wins!");
        notification.put("player1", user1.getUsername());
        notification.put("player1Score", score1);
        notification.put("player2", user2.getUsername());
        notification.put("player2Score", score2);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomCode() + "/status",
                notification
        );

        log.info("Match completed notification sent for room: {}", room.getRoomCode());
    }
    private void updateResult(Room room, User user,
                              int score, boolean winner) {

        MatchResult result =
                matchResultRepository.findByRoomIdAndUserId(room.getId(), user.getId())
                        .orElseThrow();

        result.setScore(score);
        result.setWinner(winner);

        matchResultRepository.save(result);
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId()).get();
        if (winner) {
            userProfile.setTotalWin(userProfile.getTotalWin() + 1);
            userProfile.setTotalBattle(userProfile.getTotalBattle() + 1);
        } else {
            userProfile.setTotalBattle(userProfile.getTotalBattle() + 1);
            userProfile.setTotalLoss(userProfile.getTotalLoss() + 1);
        }
        userProfileRepository.save(userProfile);
    }



    // ========== HELPER METHODS ==========

    /**
     * ✅ Validate room creation request
     */
    private void validateRoomRequest(RoomRequestDTO request) {
        if (request.getNoOfQuestions() <= 0) {
            throw new BadRequestException("Number of questions must be greater than 0");
        }

        if (request.getNoOfQuestions() > 20) {
            throw new BadRequestException("Number of questions cannot exceed 20");
        }

        if (request.getDuration() <= 0) {
            throw new BadRequestException("Duration must be greater than 0");
        }

        if (request.getDuration() > 180) {
            throw new BadRequestException("Duration cannot exceed 180 minutes");
        }

        if (request.getQuestionType() == null || request.getQuestionType().isBlank()) {
            throw new BadRequestException("Question type is required");
        }
    }

    /**
     * ✅ Generate unique room code with retry logic
     */
    private int generateUniqueRoomCode() {
        Random random = new Random();
        int maxAttempts = 10;
        int attempts = 0;

        while (attempts < maxAttempts) {
            int roomCode = 100000 + random.nextInt(900000);

            if (!roomRepository.findByRoomCode(roomCode).isPresent()) {
                return roomCode;
            }

            attempts++;
        }

        throw new RuntimeException("Failed to generate unique room code after " + maxAttempts + " attempts");
    }

    /**
     * Map Room entity to DTO
     */
    private RoomResponseDTO mapToDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomCode(room.getRoomCode());
        dto.setStatus(room.getStatus());
        dto.setNoOfQuestion(room.getNoOfQuestion());
        dto.setDifficulty(room.getDifficulty());
        dto.setQuestionType(room.getQuestionType() != null ? room.getQuestionType().getOptionValue() : null);
        dto.setCreatedAt(room.getCreationDate());
        dto.setStartedAt(room.getStartedAt());
        dto.setExpiryTime(room.getExpiryTime());
        dto.setExpiryDuration(room.getExpiryDuration());
        dto.setCreatedByName(room.getMadeBy().getUsername());
        dto.setJoinedByName(room.getJoinBy() != null ? room.getJoinBy().getUsername() : null);

        if (room.getStatus() == Status.COMPLETED) {
            matchResultRepository.findByRoomId(room.getId()).stream()
                    .filter(MatchResult::isWinner)
                    .findFirst()
                    .ifPresent(mr -> dto.setWinner(mr.getUser().getUsername()));
        }

        return dto;
    }

    /**
     * Map Coding Question to QuestionFetchDTO
     */
    private QuestionFetchDTO mapCodingToQuestionFetchDTO(CodingQuestionDTO qDto) {
        QuestionFetchDTO dto = new QuestionFetchDTO();
        dto.setId(qDto.getId());
        dto.setType("Coding Question");
        dto.setTitle(qDto.getTitle());
        dto.setDescription(qDto.getDescription());
        dto.setDifficulty(qDto.getDifficulty());
        dto.setPoints(qDto.getPoints());
        dto.setTimeLimit((int) qDto.getTimeLimit());
        dto.setInputFormat(qDto.getInputFormat());
        dto.setOutputFormat(qDto.getOutputFormat());
        dto.setConstraints(qDto.getConstraints());
        dto.setTags(qDto.getTags());
        dto.setStarterCodes(qDto.getStarterCodes());

        // ✅ Only return sample test cases to prevent cheating
        dto.setSampleTestCases(qDto.getTestCases().stream()
                .filter(TestCaseJsonDTO::isSample)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Map MCQ Question to QuestionFetchDTO
     */
    private QuestionFetchDTO mapMcqToQuestionFetchDTO(McqQuestionResponseDTO qDto) {
        QuestionFetchDTO dto = new QuestionFetchDTO();
        dto.setId(qDto.getId());
        dto.setType("MCQ Question");
        dto.setTitle(qDto.getTitle());
        dto.setDescription(qDto.getDescription());
        dto.setDifficulty(qDto.getDifficulty());
        dto.setPoints(qDto.getPoints());
        dto.setTimeLimit(qDto.getTimeLimit());
        dto.setTags(qDto.getTags());

        // ✅ Security: Sanitize options - hide correct answers from client
        List<McqOptionJsonDTO> sanitizedOptions = qDto.getOptions().stream()
                .map(o -> new McqOptionJsonDTO(
                        o.getId(),
                        o.getOptionText(),
                        false // Hide correct answer
                ))
                .collect(Collectors.toList());

        dto.setOptions(sanitizedOptions);
        return dto;
    }
}