package com.codearena.backend.controller;

import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.dto.TestCaseJsonDTO;
import com.codearena.backend.service.TestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions/coding/{questionId}/testcases")
public class TestCaseController {

    @Autowired
    private TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<?> createTestCase(@RequestHeader("Authorization") String token,
                                            @PathVariable String questionId,
                                            @RequestBody TestCaseJsonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Test case created successfully", testCaseService.create(questionId, dto)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTestCase(@PathVariable String questionId,
                                            @PathVariable String id,
                                            @RequestBody TestCaseJsonDTO dto) {
        return ResponseEntity.ok(StandardResponse.success("Test case updated successfully", testCaseService.update(id, dto)));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTestCases(@PathVariable String questionId) {
        List<TestCaseJsonDTO> list = testCaseService.getByQuestionId(questionId);
        return ResponseEntity.ok(StandardResponse.success("All test cases fetched successfully", list));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTestCase(@PathVariable String questionId, @PathVariable String id) {
        testCaseService.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Test case deleted successfully", null));
    }
}
