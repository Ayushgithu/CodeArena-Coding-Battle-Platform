package com.codearena.backend.controller;

import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.dto.UserDetailsDTO;
import com.codearena.backend.service.UserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-detail")
public class UserDetailController {

    @Autowired
    private UserDetail userDetail;

    // ✔ Update current logged-in user's details
    @PutMapping
    public ResponseEntity<?> updateCurrentUser(@RequestHeader("Authorization") String token,
            @RequestBody UserDetailsDTO dto
    ) {
        return ResponseEntity.ok(
                StandardResponse.success(
                        "User details updated successfully",
                        userDetail.updateCurrentUser(dto)
                )
        );
    }

    // ✔ Get details of current logged-in user
    @GetMapping
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                StandardResponse.success(
                        "Current user details fetched successfully",
                        userDetail.getCurrentUserDetails()
                )
        );
    }

    // ✔ Get details of any user using username
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@RequestHeader("Authorization") String token,@PathVariable String username) {
        return ResponseEntity.ok(
                StandardResponse.success(
                        "User details fetched successfully",
                        userDetail.getUserDetailsByUsername(username)
                )
        );
    }
}
