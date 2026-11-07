package com.luminacare.authservice.controller;

import com.luminacare.authservice.entity.User;
import com.luminacare.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.login(request.get("username"), request.get("password")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        // Simple refresh placeholder. In reality, you validate the old token or refresh token.
        return ResponseEntity.ok("Refresh token functionality not fully implemented.");
    }
}
