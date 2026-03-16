package com.codearena.backend.controller;

import com.codearena.backend.dto.McqQuestionJsonDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;
import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.service.McqQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions/mcq")
public class McqQuestionController {

    @Autowired
    private McqQuestionService mcqQuestionService;

    // ✅ Create MCQ (Authorization Required)
    @PostMapping
    public ResponseEntity<?> createMcq(@RequestHeader("Authorization") String token,
                                       @RequestBody McqQuestionJsonDTO dto) {
        McqQuestionResponseDTO created = mcqQuestionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("MCQ created successfully", created));
    }

    // ✅ Update MCQ (Authorization Required)
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMcq(@RequestHeader("Authorization") String token,
                                       @PathVariable String id,
                                       @RequestBody McqQuestionJsonDTO dto) {
        McqQuestionResponseDTO updated = mcqQuestionService.update(id, dto);
        return ResponseEntity.ok(StandardResponse.success("MCQ updated successfully", updated));
    }

    // ✅ Get All MCQs (Authorization Required)
    @GetMapping("/all")
    public ResponseEntity<?> getAllMcq(@RequestHeader("Authorization") String token) {
        List<McqQuestionResponseDTO> all = mcqQuestionService.getAll();
        return ResponseEntity.ok(StandardResponse.success("All MCQ questions fetched successfully", all));
    }

    // ✅ Get MCQ by ID (Authorization Required)
    @GetMapping("/{id}")
    public ResponseEntity<?> getMcqById(@RequestHeader("Authorization") String token,
                                        @PathVariable String id) {
        McqQuestionResponseDTO dto = mcqQuestionService.getById(id);
        return ResponseEntity.ok(StandardResponse.success("MCQ fetched successfully", dto));
    }

    // ✅ Delete MCQ (Authorization Required)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMcq(@RequestHeader("Authorization") String token,
                                       @PathVariable String id) {
        mcqQuestionService.delete(id);
        return ResponseEntity.ok(StandardResponse.success("MCQ deleted successfully", null));
    }

    // ✅ Get MCQs by difficulty and number of questions (Authorization Required)
    @GetMapping("/by-difficulty")
    public ResponseEntity<?> getMcqByDifficulty(
            @RequestHeader("Authorization") String token,
            @RequestParam String difficulty,
            @RequestParam int count) {

        try {
            List<McqQuestionResponseDTO> questions =
                    mcqQuestionService.getByDifficultyAndCount(difficulty.toUpperCase(), count);

            return ResponseEntity.ok(
                    StandardResponse.success("MCQs fetched successfully by difficulty", questions)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(StandardResponse.error("Invalid difficulty value: " + difficulty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error fetching MCQs: " + e.getMessage()));
        }
    }

    @GetMapping("/mixed")
    public ResponseEntity<?> getMixedMcqQuestions(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "count") int count) {

        try {
            List<McqQuestionResponseDTO> questions = mcqQuestionService.getMixedDifficultyQuestions(count);
            return ResponseEntity.ok(StandardResponse.success(
                    "Mixed difficulty MCQs fetched successfully", questions
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(StandardResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error fetching mixed questions: " + e.getMessage()));
        }
    }



}

