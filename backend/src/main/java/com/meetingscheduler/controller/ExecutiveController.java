package com.meetingscheduler.controller;

import com.meetingscheduler.entity.User;
import com.meetingscheduler.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/executives")
@CrossOrigin(origins = "http://localhost:3000")
public class ExecutiveController {

    @Autowired
    private AuthService authService;

    // Get all executives (for secretaries and admins)
    @GetMapping
    public ResponseEntity<List<User>> getAllExecutives() {
        try {
            List<User> executives = authService.getAllExecutives();
            return ResponseEntity.ok(executives);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all users (for meeting participants)
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get executives by secretary (for secretaries to see their assigned executives)
    @GetMapping("/secretary/{secretaryId}")
    public ResponseEntity<List<User>> getExecutivesBySecretary(@PathVariable UUID secretaryId) {
        try {
            List<User> executives = authService.getExecutivesBySecretary(secretaryId);
            return ResponseEntity.ok(executives);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Assign executive to secretary (for admins)
    @PostMapping("/{executiveId}/assign-secretary/{secretaryId}")
    public ResponseEntity<String> assignSecretaryToExecutive(
            @PathVariable UUID executiveId, 
            @PathVariable UUID secretaryId) {
        try {
            authService.assignSecretaryToExecutive(executiveId, secretaryId);
            return ResponseEntity.ok("Secretary assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to assign secretary: " + e.getMessage());
        }
    }
}
