
package com.esd.lab2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esd.lab2.dto.UpdateProfileRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.entity.User;
import com.esd.lab2.exception.ResourceNotFoundException;
import com.esd.lab2.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
public ResponseEntity<UserResponse> updateCurrentUser(
        Authentication authentication,
        @Valid @RequestBody UpdateProfileRequest request) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

    user.setFullName(request.getFullName());

    User savedUser = userRepository.save(user);

    UserResponse response = new UserResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getRole(),
            savedUser.getCreatedAt());

    return ResponseEntity.ok(response);
}
}
