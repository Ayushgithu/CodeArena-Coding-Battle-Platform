package com.codearena.backend.controller;


import com.codearena.backend.dto.CodingQuestionDTO;
import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.service.CodingQuestionService;
import com.codearena.backend.utils.constant.Difficulty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions/coding")
public class CodingQuestionController {

    @Autowired
    private CodingQuestionService codingQuestionService;

    // ✅ Create a new coding question
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestHeader("Authorization") String token, @RequestBody CodingQuestionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(codingQuestionService.createQuestion(dto));
    }

    // ✅ Update an existing coding question
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable String id, @RequestBody CodingQuestionDTO dto) {
        return ResponseEntity.ok(StandardResponse.success("Coding question updated successfully", codingQuestionService.updateQuestion(id, dto)));
    }


    // ✅ Get all coding questions
    @GetMapping("/all")
    public ResponseEntity<?> getAllQuestions() {
        List<CodingQuestionDTO> questions = codingQuestionService.getAllQuestions();
        return ResponseEntity.ok(StandardResponse.success("All coding questions fetched successfully", questions));
    }

    // ✅ Get coding question by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@RequestHeader("Authorization") String token,@PathVariable String id) {
        return ResponseEntity.ok(codingQuestionService.getQuestionById(id));
    }

    @GetMapping("/by-difficulty")
    public ResponseEntity<?> getByDifficulty(
            @RequestParam String difficulty,
            @RequestParam int count) {

        List<CodingQuestionDTO> questions = codingQuestionService.getByDifficultyAndCount(difficulty, count);

        return ResponseEntity.ok(
                StandardResponse.success("Coding questions fetched successfully", questions)
        );
    }





    // ✅ Delete a coding question
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable String id) {
        codingQuestionService.deleteQuestion(id);
        return ResponseEntity.ok(StandardResponse.success("Coding question deleted successfully", null));
    }

    @GetMapping("/mixed")
    public ResponseEntity<?> getMixedCodingQuestions(
            @RequestHeader("Authorization") String token,
            @RequestParam int number) {

        if (number <= 0) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("Number must be greater than 0")
            );
        }

        if (number > 10) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("Number cannot be greater than 10")
            );
        }

        List<CodingQuestionDTO> questions = codingQuestionService.getMixedQuestions(number);

        return ResponseEntity.ok(
                StandardResponse.success("Mixed difficulty coding questions fetched successfully", questions)
        );
    }


}
