package com.codearena.backend.controller;


import com.codearena.backend.dto.DropListDTO;
import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.entity.DropList;
import com.codearena.backend.service.DropListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drop-list")
public class DropListController {
    @Autowired
    private DropListService dropListService;

    @PostMapping("/create")
    public ResponseEntity<?> createDropList(@RequestHeader("Authorization") String token,
                                            @RequestBody DropListDTO dropListDTO){
        return ResponseEntity.ok(dropListService.createDropList(dropListDTO));
    }
    @GetMapping
    public ResponseEntity<?> findByLabelKey(@RequestHeader("Authorization") String token,
                                            @RequestParam String labelKey){
        return ResponseEntity.ok(StandardResponse.success("data for "+labelKey, dropListService.findByLabelKey(labelKey)));
    }
}
