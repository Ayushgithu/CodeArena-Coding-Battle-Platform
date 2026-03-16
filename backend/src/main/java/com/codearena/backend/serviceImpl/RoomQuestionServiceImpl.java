package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.McqOptionJsonDTO;
import com.codearena.backend.dto.RoomQuestionResponseDTO;
import com.codearena.backend.dto.StarterCodeDTO;
import com.codearena.backend.dto.TestCaseJsonDTO;
import com.codearena.backend.dto.*;
import com.codearena.backend.entity.*;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.*;
import com.codearena.backend.service.RoomQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomQuestionServiceImpl implements RoomQuestionService {

    private final RoomRepository roomRepository;
    private final CodingQuestionRepository codingQuestionRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqQuestionOptionRepository mcqQuestionOptionRepository;
    private final RoomQuestionRepository roomQuestionRepository;
    private final TestCaseRepository testCaseRepository;

    // In-memory locks with cleanup tracking
    private final ConcurrentHashMap<String, Lock> roomLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lockCreationTimes = new ConcurrentHashMap<>();

    private final McqQuestionServiceImpl mcqQuestionService;
    private final StarterCodeRepository starterCodeRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    @Override
    public void assignQuestionsToRoom(int roomCode) {

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Prevent reassign
        if (!roomQuestionRepository.findByRoomIdOrderByQuestionOrder(room.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Questions already assigned");
        }
        System.out.println("inside assign question");
        int limit = room.getNoOfQuestion();
        System.out.println(room.getQuestionType());

        if (room.getQuestionType().getOptionValue().equalsIgnoreCase("CODING Question")) {
            System.out.println("diff : "+room.getDifficulty().name());
            List<CodingQuestion> questions = getQuestionsByDifficulty(room.getDifficulty(),limit);

            saveCodingQuestions(roomCode, questions);
        }

        else {

            List<McqQuestion> questions =
                    mcqQuestionRepository.findRandom(limit);

            saveMcqQuestions(room, questions);
        }
    }

    private void saveCodingQuestions(int roomCode, List<CodingQuestion> questions) {

        Room room = roomRepository.findByRoomCode(roomCode).get();
        int order = 1;
        List<RoomQuestion> roomQuestions = new ArrayList<>();

        for (CodingQuestion q : questions) {

            RoomQuestion rq = new RoomQuestion();

            rq.setRoom(room);                 // FK
            rq.setCodingQuestion(q);          // FK
            rq.setMcqQuestion(null);          // IMPORTANT
            rq.setQuestionOrder(order++);


            roomQuestions.add(rq);
        }

        roomQuestionRepository.saveAll(roomQuestions);
    }

    public List<CodingQuestion> getQuestionsByDifficulty(
            Difficulty difficulty,
            int totalCount) {

        if (difficulty != Difficulty.MIXED) {

            return codingQuestionRepository.findRandomByDifficulty(
                    difficulty,
                    PageRequest.of(0, totalCount)
            );
        }

        // ---------- SAFE MIXED LOGIC ----------

        int easyCount = (int) Math.floor(totalCount * 0.4);
        int mediumCount = (int) Math.floor(totalCount * 0.4);

        int hardCount = totalCount - (easyCount + mediumCount);

        List<CodingQuestion> result = new ArrayList<>();

        if (easyCount > 0) {
            result.addAll(
                    codingQuestionRepository.findRandomByDifficulty(
                            Difficulty.EASY,
                            PageRequest.of(0, easyCount)
                    )
            );
        }

        if (mediumCount > 0) {
            result.addAll(
                    codingQuestionRepository.findRandomByDifficulty(
                            Difficulty.MEDIUM,
                            PageRequest.of(0, mediumCount)
                    )
            );
        }

        if (hardCount > 0) {
            result.addAll(
                    codingQuestionRepository.findRandomByDifficulty(
                            Difficulty.HARD,
                            PageRequest.of(0, hardCount)
                    )
            );
        }

        // Fallback if less questions returned
        if (result.size() < totalCount) {

            int remaining = totalCount - result.size();

            result.addAll(
                    codingQuestionRepository.findRandomByDifficulty(
                            Difficulty.MEDIUM,
                            PageRequest.of(0, remaining)
                    )
            );
        }

        Collections.shuffle(result);

        return result;
    }


    private void saveMcqQuestions(Room room, List<McqQuestion> questions) {

        int order = 1;

        for (McqQuestion q : questions) {

            RoomQuestion rq = new RoomQuestion();
            rq.setRoom(room);
            rq.setMcqQuestion(q);
            rq.setQuestionOrder(order++);

            roomQuestionRepository.save(rq);
        }
    }


    @Override
    public List<RoomQuestionResponseDTO> getRoomQuestions(int roomCode) {
        String roomId = roomRepository.findByRoomCode(roomCode).get().getId();
        List<RoomQuestion> list =
                roomQuestionRepository.findByRoomIdOrderByQuestionOrder(roomId);

        return list.stream().map(rq -> {

            if (rq.getCodingQuestion() != null) {
                return new RoomQuestionResponseDTO(
                        rq.getCodingQuestion().getId(),
                        rq.getCodingQuestion().getTitle(),
                        rq.getQuestionOrder(),
                        "CODING",
                        null

                );
            } else {
                return new RoomQuestionResponseDTO(
                        rq.getMcqQuestion().getId(),
                        rq.getMcqQuestion().getTitle(),
                        rq.getQuestionOrder(),
                        "MCQ",
                        mapOptions(mcqQuestionOptionRepository.findByMcqQuestionId(rq.getMcqQuestion().getId()))

                );
            }

        }).toList();
    }

    @Override
    @jakarta.transaction.Transactional
    public List<CodingQuestionJsonDTO> getRoomCodingQuestions(int roomCode) {

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomQuestion> roomQuestions =
                roomQuestionRepository.findByRoomIdOrderByQuestionOrder(room.getId());

        return roomQuestions.stream()
                .filter(rq -> rq.getCodingQuestion() != null) // only CODING
                .map(rq -> {

                    CodingQuestion cq = rq.getCodingQuestion();

                    // -------- TEST CASES --------
                    List<TestCaseJsonDTO> testCases =
                            testCaseRepository.findByCodingQuestionId(cq.getId())
                                    .stream()
                                    .map(tc -> {

                                        TestCaseJsonDTO dto =
                                                new TestCaseJsonDTO();

                                        dto.setId(tc.getId());
                                        dto.setInputData(tc.getInputData());
                                        dto.setSample(tc.isSample());
                                        dto.setExplanation(tc.getExplanation());
                                        dto.setOrderIndex(tc.getOrderIndex());
                                        dto.setExpectedOutput(tc.getExpectedOutput());

                                        return dto;
                                    })
                                    .toList();

                    // -------- STARTER CODES --------
                    List<StarterCodeJsonDTO> starterCodes =
                            starterCodeRepository.findByCodingQuestionId(cq.getId())
                                    .stream()
                                    .map(sc -> {

                                        StarterCodeJsonDTO dto =
                                                new StarterCodeJsonDTO();
                                        dto.setId(sc.getId());
                                        dto.setLanguage(sc.getLanguage().name());
                                        dto.setVersion(sc.getVersion());
                                        dto.setCodeTemplate(sc.getCodeTemplate());

                                        return dto;
                                    })
                                    .toList();

                    // -------- BUILD FINAL DTO --------
                    CodingQuestionJsonDTO dto =
                            new CodingQuestionJsonDTO();
                    dto.setId(cq.getId());
                    dto.setTitle(cq.getTitle());
                    dto.setDescription(cq.getDescription());
                    dto.setInputFormat(cq.getInputFormat());
                    dto.setOutputFormat(cq.getOutputFormat());
                    dto.setConstraints(cq.getConstraints());
                    dto.setDifficulty(cq.getDifficulty());
                    dto.setPoints(cq.getPoints());
                    dto.setTimeLimit(cq.getTimeLimit());
                    dto.setMemoryLimit(cq.getMemoryLimit());
                    dto.setTestCases(testCases);
                    dto.setStarterCodes(starterCodes);

                    return dto;

                }).toList();
    }

    @Override
    public List<QuestionStatusDTO> getRoomQuestionStatus(int roomCode) {

        User user = userService.getCurrentUser();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomQuestion> questions =
                roomQuestionRepository.findByRoomId(room.getId());

        List<QuestionStatusDTO> result = new ArrayList<>();

        for (RoomQuestion rq : questions) {

            String questionId =
                    rq.getCodingQuestion() != null
                            ? rq.getCodingQuestion().getId()
                            : rq.getMcqQuestion().getId();

            boolean solved =
                    submissionRepository.existsByUserIdAndRoomIdAndQuestionIdAndStatus(
                            user.getId(),
                            room.getId(),
                            questionId,
                            SubmissionStatus.ACCEPTED
                    );

            int attempts =
                    submissionRepository.countByUserIdAndRoomIdAndQuestionId(
                            user.getId(),
                            room.getId(),
                            questionId
                    );

            result.add(
                    new QuestionStatusDTO(
                            questionId,
                            solved,
                            attempts
                    )
            );
        }

        return result;
    }

    private List<McqOptionJsonDTO> mapOptions(List<McqQuestionOption> options) {

        return options.stream()
                .map(opt -> new McqOptionJsonDTO(
                        opt.getId(),
                        opt.getOptionText(),
                        opt.isCorrect()
                ))
                .toList();
    }

//    private List<RoomQuestion> assignCodingQuestions(Room room, int limit) {
//        List<CodingQuestion> questions = codingQuestionRepository
//                .findRandomByDifficulty(room.getDifficulty().name(), limit);
//        // Prevent reassign
//        if (!roomQuestionRepository.findByRoomIdOrderByQuestionOrder(room.getId()).isEmpty()) {
//            throw new ResourceNotFoundException("Questions already assigned");
//        }
//        System.out.println("inside assign question");
//        int limit = room.getNoOfQuestion();
//        System.out.println(room.getQuestionType());
//
//        if (room.getQuestionType().getOptionValue().equalsIgnoreCase("CODING Question")) {
//            System.out.println("diff : "+room.getDifficulty().name());
//            List<CodingQuestion> questions = getQuestionsByDifficulty(room.getDifficulty(),limit);
//
//            saveCodingQuestions(roomCode, questions);
//        }
//
//        else {
//
//            List<McqQuestion> questions =
//                    mcqQuestionRepository.findRandom(limit);
//
//            saveMcqQuestions(room, questions);
//        }
//    }

//    private void saveCodingQuestions(int roomCode, List<CodingQuestion> questions) {
//
//        Room room = roomRepository.findByRoomCode(roomCode).get();
//        int order = 1;
//        List<RoomQuestion> roomQuestions = new ArrayList<>();
//
//        if (questions.isEmpty()) {
//            throw new ResourceNotFoundException(
//                    "No coding questions available for difficulty: " + room.getDifficulty()
//            );
//        }
//
//        return createCodingRoomQuestions(room, questions);
//    }



}