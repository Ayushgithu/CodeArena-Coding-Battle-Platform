package com.codearena.backend.controller;
import com.codearena.backend.dto.CodeExecutionDTO;
import com.codearena.backend.dto.CodeExecutionResultDTO; // <-- ADDED IMPORT
import com.codearena.backend.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

//    @PostMapping("/execute")
//    // CHANGED: Return type updated from String to CodeExecutionResultDTO
//    public CodeExecutionResultDTO executeCode(@RequestBody CodeExecutionDTO request) {
//        System.out.println(request);
//        return codeExecutionService.executeCode1(request);
//    }
    @PostMapping("/run")
    public ResponseEntity<CodeExecutionResultDTO> runCode(
            @RequestBody CodeExecutionDTO request) {

        CodeExecutionResultDTO result =
                codeExecutionService.runCode(request);

        return ResponseEntity.ok(result);
    }
    @PostMapping("/submit/{roomCode}")
    public ResponseEntity<CodeExecutionResultDTO> submitCode(
            @RequestBody CodeExecutionDTO request,
            @PathVariable int roomCode) {

        CodeExecutionResultDTO result =
                codeExecutionService.submitCode(request,roomCode);

        return ResponseEntity.ok(result);
    }

}