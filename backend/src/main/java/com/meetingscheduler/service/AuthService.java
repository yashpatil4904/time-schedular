package com.meetingscheduler.service;

import com.meetingscheduler.entity.User;
import com.meetingscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Optional<User> authenticate(String email, String password) {
        log.info("Authenticating user with email: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        log.warn("Authentication failed for email: {}", email);
        return Optional.empty();
    }
    
    public User register(String email, String password, String fullName, User.UserRole role) {
        log.info("Registering new user: {}", email);
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // Get all executives (for secretaries and admins)
    public List<User> getAllExecutives() {
        return userRepository.findByRole(User.UserRole.executive);
    }

    // Get all users (for meeting participants)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get executives assigned to a specific secretary
    public List<User> getExecutivesBySecretary(UUID secretaryId) {
        // For now, return all executives. In a real system, you'd have a mapping table
        return userRepository.findByRole(User.UserRole.executive);
    }

    // Assign secretary to executive (for admins)
    public void assignSecretaryToExecutive(UUID executiveId, UUID secretaryId) {
        // In a real system, you'd update a mapping table
        // For now, just validate that both users exist
        User executive = userRepository.findById(executiveId).orElse(null);
        User secretary = userRepository.findById(secretaryId).orElse(null);
        
        if (executive == null) {
            throw new RuntimeException("Executive not found");
        }
        if (secretary == null) {
            throw new RuntimeException("Secretary not found");
        }
        if (secretary.getRole() != User.UserRole.secretary) {
            throw new RuntimeException("User is not a secretary");
        }
    }
}