package com.codearena.backend.controller;

import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.dto.StarterCodeDTO;
import com.codearena.backend.repository.StarterCodeRepository;
import com.codearena.backend.service.StarterCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/starter-code")
public class StarterCodeController {

    @Autowired
    private StarterCodeService starterCodeService;

    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getStarterCodesByQuestionId(@RequestHeader("Authorization") String token,@PathVariable String questionId) {
        List<StarterCodeDTO> starterCodes = starterCodeService.getStarterCodesByQuestionId(questionId);
        return ResponseEntity.ok(StandardResponse.success("All stater code ",starterCodes));
    }

    @GetMapping("/{questionId}/{language}")
    public ResponseEntity<?> getStarterCodeByQuestionAndLanguage(@RequestHeader("Authorization") String token,
            @PathVariable String questionId,
            @PathVariable String language) {
        StarterCodeDTO starterCode = starterCodeService.getStarterCodeByQuestionAndLanguage(questionId, language);
        return ResponseEntity.ok(StandardResponse.success("template for "+language+" is ",starterCode));
    }

    /**
     * Delete a starter code by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStarterCode(@RequestHeader("Authorization") String token,@PathVariable String id) {
        starterCodeService.deleteStarterCode(id);
        return ResponseEntity.ok(StandardResponse.success("Starter code deleted successfully",null));
    }
}
