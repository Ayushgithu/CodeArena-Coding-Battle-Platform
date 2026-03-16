package com.codearena.backend.serviceImpl;


import com.codearena.backend.dto.CodeExecutionDTO;
import com.codearena.backend.dto.CodeExecutionResultDTO; // <-- ADDED
import com.codearena.backend.entity.*;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.repository.SubmissionRepository;
import com.codearena.backend.repository.TestCaseRepository;
import com.codearena.backend.service.CodeExecutionService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.constant.Status;
import com.codearena.backend.utils.constant.SubmissionStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private static final String PISTON_API_URL = "https://emkc.org/api/v2/piston/execute";
    private final CodingQuestionRepository codingQuestionRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final RoomRepository roomRepository;

    public CodeExecutionServiceImpl(CodingQuestionRepository codingQuestionRepository,
                             TestCaseRepository testCaseRepository,
                             SubmissionRepository submissionRepository,
                             UserService userService,
                             RoomRepository roomRepository) {

        this.codingQuestionRepository = codingQuestionRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.roomRepository = roomRepository;
    }


    @Override
    public CodeExecutionResultDTO runCode(CodeExecutionDTO request) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();

        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        CodingQuestion codingQuestion =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        try {

            String language = request.getLanguage();
            String version = request.getVersion();
            String code = request.getCode();
            System.out.println(request);
            // ✅ Fetch FIRST SAMPLE test case
            TestCase sampleTest =
                    testCaseRepository.findFirstByCodingQuestionIdAndIsSampleTrue(
                            codingQuestion.getId()
                    ).orElseThrow(() ->
                            new RuntimeException("Sample test case not found"));

            String stdin = sampleTest.getInputData();

            // ---------- Build Payload ----------

            JSONObject payload = new JSONObject();
            payload.put("language", language);
            payload.put("version", version);

            JSONArray filesArray = new JSONArray();
            JSONObject fileObject = new JSONObject();
            fileObject.put("name", "Main." + getFileExtension(language));
            fileObject.put("content", code);

            filesArray.put(fileObject);

            payload.put("files", filesArray);
            payload.put("stdin", stdin);

            // ---------- Call Piston ----------
            System.out.println(payload.toString());
            HttpResponse<JsonNode> response =
                    Unirest.post(PISTON_API_URL)
                            .header("Content-Type", "application/json")
                            .body(payload.toString())
                            .asJson();

            kong.unirest.json.JSONObject body =
                    response.getBody().getObject();

            // ---------- Compile Error ----------

            if (body.has("compile")) {

                String compileError =
                        body.getJSONObject("compile").optString("stderr", "Compile Error");

                result.setStderr(compileError);
                result.setExitCode(1);

                return result;
            }

            kong.unirest.json.JSONObject run =
                    body.getJSONObject("run");

            result.setStdout(run.optString("stdout", ""));
            result.setStderr(run.optString("stderr", ""));
            result.setExitCode(run.optInt("code", 0));

            result.setTime(run.optDouble("time", 0.0));
            result.setMemory(run.optDouble("memory", 0.0));

            return result;

        } catch (Exception e) {

            e.printStackTrace();
            result.setStderr("Execution error: " + e.getMessage());

            return result;
        }
    }

    @Override
    public CodeExecutionResultDTO submitCode(
            CodeExecutionDTO request,
            int roomCode) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();
        User user = userService.getCurrentUser();
        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        // ================= FETCH ROOM =================

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getStatus().equals(Status.ACTIVE)) {
            throw new RuntimeException("Room is not active");
        }

        // ================= FETCH QUESTION =================

        CodingQuestion question =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        // ================= ATTEMPT NUMBER =================

        int attempt =
                submissionRepository.countByUserIdAndRoomIdAndQuestionId(
                        user.getId(),
                        room.getId(),
                        question.getId()
                ) + 1;

        // ================= FETCH TEST CASES =================

        List<TestCase> testCases =
                testCaseRepository.findByCodingQuestionId(question.getId());

        int passed = 0;
        int total = testCases.size();

        Submission submission = new Submission();

        submission.setUser(user);
        submission.setRoom(room);
        submission.setQuestion(question);
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(request.getCode());
        submission.setAttemptNumber(attempt);
        submission.setSubmittedAt(LocalDateTime.now());

        // ================= EXECUTION LOOP =================

        try {

            for (TestCase tc : testCases) {

                String stdin = tc.getInputData();

                JSONObject payload = new JSONObject();
                payload.put("language", request.getLanguage());
                payload.put("version", request.getVersion());

                JSONArray filesArray = new JSONArray();
                JSONObject fileObject = new JSONObject();
                fileObject.put("name", "Main." + getFileExtension(request.getLanguage()));
                fileObject.put("content", request.getCode());

                filesArray.put(fileObject);

                payload.put("files", filesArray);
                payload.put("stdin", stdin);

                HttpResponse<JsonNode> response =
                        Unirest.post(PISTON_API_URL)
                                .header("Content-Type", "application/json")
                                .body(payload.toString())
                                .asJson();

                kong.unirest.json.JSONObject body =
                        response.getBody().getObject();

                // ================= COMPILE ERROR =================

                if (body.has("compile")) {

                    String compileErr =
                            body.getJSONObject("compile").optString("stderr");

                    submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    submission.setCompilerMessage(compileErr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(compileErr);
                    result.setExitCode(1);

                    return result;
                }

                kong.unirest.json.JSONObject run =
                        body.getJSONObject("run");

                String stderr =
                        run.optString("stderr", "").trim();

                // ================= RUNTIME ERROR =================

                if (!stderr.isEmpty()) {

                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setCompilerMessage(stderr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(stderr);
                    result.setExitCode(1);

                    return result;
                }

                String actual =
                        normalize(run.optString("stdout"));

                String expected =
                        normalize(tc.getExpectedOutput());

                // ================= WRONG ANSWER =================

                if (!actual.equals(expected)) {

                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    submission.setScore(0);

                    // Optional: store judge feedback in DB
                    submission.setCompilerMessage(
                            "TestCase " + tc.getOrderIndex()
                                    + " Failed\nExpected: " + expected
                                    + "\nFound: " + actual
                    );

                    submissionRepository.save(submission);

                    // Send detailed result to frontend
                    result.setStdout(
                            "❌ Wrong Answer at TestCase " + tc.getOrderIndex()
                                    + "\nExpected Output:\n" + expected
                                    + "\n\nYour Output:\n" + actual
                    );

                    result.setExitCode(1);

                    return result;
                }


                // ================= PASS =================

                passed++;

                // Save max execution metrics
                submission.setExecutionTime(
                        Math.max(submission.getExecutionTime(),
                                run.optDouble("time", 0.0)));

                submission.setMemoryUsed(
                        Math.max(submission.getMemoryUsed(),
                                run.optDouble("memory", 0.0)));
            }

            // ================= ACCEPTED =================

            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(question.getPoints());

            submission.setPassedTestCases(passed);
            submission.setTotalTestCases(total);

            submissionRepository.save(submission);

            result.setPassedTestCases(passed);
            result.setTotalTestCases(total);

            result.setStdout("✅ Accepted");
            result.setExitCode(0);

            return result;

        } catch (Exception e) {

            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompilerMessage(e.getMessage());
            submission.setScore(0);

            submissionRepository.save(submission);

            result.setStderr("Execution failed");
            return result;
        }
    }


    @Override
    public CodeExecutionResultDTO submitCode(
            CodeExecutionDTO request) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();
        User user = userService.getCurrentUser();
        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        // ================= FETCH ROOM =================



        // ================= FETCH QUESTION =================

        CodingQuestion question =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        // ================= ATTEMPT NUMBER =================

//        int attempt =
//                submissionRepository.countByUserIdAndRoomIdAndQuestionId(
//                        user.getId(),
//                        room.getId(),
//                        question.getId()
//                ) + 1;

        // ================= FETCH TEST CASES =================

        List<TestCase> testCases =
                testCaseRepository.findByCodingQuestionId(question.getId());

        int passed = 0;
        int total = testCases.size();

        Submission submission = new Submission();

        submission.setUser(user);
//        submission.setRoom(room);
        submission.setQuestion(question);
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(request.getCode());
//        submission.setAttemptNumber(attempt);
        submission.setSubmittedAt(LocalDateTime.now());

        // ================= EXECUTION LOOP =================

        try {

            for (TestCase tc : testCases) {

                String stdin = tc.getInputData();

                JSONObject payload = new JSONObject();
                payload.put("language", request.getLanguage());
                payload.put("version", request.getVersion());

                JSONArray filesArray = new JSONArray();
                JSONObject fileObject = new JSONObject();
                fileObject.put("name", "Main." + getFileExtension(request.getLanguage()));
                fileObject.put("content", request.getCode());

                filesArray.put(fileObject);

                payload.put("files", filesArray);
                payload.put("stdin", stdin);

                HttpResponse<JsonNode> response =
                        Unirest.post(PISTON_API_URL)
                                .header("Content-Type", "application/json")
                                .body(payload.toString())
                                .asJson();

                kong.unirest.json.JSONObject body =
                        response.getBody().getObject();

                // ================= COMPILE ERROR =================

                if (body.has("compile")) {

                    String compileErr =
                            body.getJSONObject("compile").optString("stderr");

                    submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    submission.setCompilerMessage(compileErr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(compileErr);
                    result.setExitCode(1);

                    return result;
                }

                kong.unirest.json.JSONObject run =
                        body.getJSONObject("run");

                String stderr =
                        run.optString("stderr", "").trim();

                // ================= RUNTIME ERROR =================

                if (!stderr.isEmpty()) {

                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setCompilerMessage(stderr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(stderr);
                    result.setExitCode(1);

                    return result;
                }

                String actual =
                        normalize(run.optString("stdout"));

                String expected =
                        normalize(tc.getExpectedOutput());

                // ================= WRONG ANSWER =================

                if (!actual.equals(expected)) {

                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    submission.setScore(0);

                    // Optional: store judge feedback in DB
                    submission.setCompilerMessage(
                            "TestCase " + tc.getOrderIndex()
                                    + " Failed\nExpected: " + expected
                                    + "\nFound: " + actual
                    );

                    submissionRepository.save(submission);

                    // Send detailed result to frontend
                    result.setStdout(
                            "❌ Wrong Answer at TestCase " + tc.getOrderIndex()
                                    + "\nExpected Output:\n" + expected
                                    + "\n\nYour Output:\n" + actual
                    );

                    result.setExitCode(1);

                    return result;
                }


                // ================= PASS =================

                passed++;

                // Save max execution metrics
                submission.setExecutionTime(
                        Math.max(submission.getExecutionTime(),
                                run.optDouble("time", 0.0)));

                submission.setMemoryUsed(
                        Math.max(submission.getMemoryUsed(),
                                run.optDouble("memory", 0.0)));
            }

            // ================= ACCEPTED =================

            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(question.getPoints());

            submission.setPassedTestCases(passed);
            submission.setTotalTestCases(total);

            submissionRepository.save(submission);

            result.setPassedTestCases(passed);
            result.setTotalTestCases(total);

            result.setStdout("✅ Accepted");
            result.setExitCode(0);

            return result;

        } catch (Exception e) {

            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompilerMessage(e.getMessage());
            submission.setScore(0);

            submissionRepository.save(submission);

            result.setStderr("Execution failed");
            return result;
        }
    }


    private String normalize(String output) {

        return output
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Utility function for file extension
    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "py";
            case "java" -> "java";
            case "c" -> "c";
            case "cpp" -> "cpp";
            case "javascript" -> "js";
            case "go" -> "go";
            case "ruby" -> "rb";
            case "php" -> "php";
            default -> "txt";
        };
    }
}