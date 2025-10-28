package com.meetingscheduler.controller;

import com.meetingscheduler.dto.LoginRequest;
import com.meetingscheduler.dto.RegisterRequest;
import com.meetingscheduler.dto.AuthResponse;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            Optional<User> user = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            
            if (user.isPresent()) {
                AuthResponse response = new AuthResponse();
                response.setUserId(user.get().getId());
                response.setEmail(user.get().getEmail());
                response.setFullName(user.get().getFullName());
                response.setRole(user.get().getRole());
                response.setMessage("Login successful");
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());
        
        try {
            // Parse role to lowercase to match enum
            User.UserRole userRole = User.UserRole.valueOf(registerRequest.getRole().toLowerCase());
            
            User savedUser = authService.register(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFullName(),
                userRole
            );
            
            AuthResponse response = new AuthResponse();
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setFullName(savedUser.getFullName());
            response.setRole(savedUser.getRole());
            response.setMessage("Registration successful");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }
}