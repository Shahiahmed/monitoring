package com.example.monitoring.controller;

import com.example.monitoring.dto.UserResponse;
import com.example.monitoring.entity.User;
import com.example.monitoring.repository.UserRepository;
import com.example.monitoring.security.JwtService;
import com.example.monitoring.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ActivityLogService activityLogService;

    public record LoginRequest(String email, String password) {}
    public record HintRequest(String email) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный email или пользователь не активен");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный email или пароль");
        }

        user.setLastLoginDate(OffsetDateTime.now());
        userRepository.save(user);

        UserResponse userResponse = UserResponse.fromEntity(user);

        String token = jwtService.generateToken(user);

        activityLogService.log(user.getId(), user.getEmail(), "LOGIN",
                "USER", user.getId(), "Вход в систему");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userResponse
        ));
    }

    @PostMapping("/hint")
    public ResponseEntity<?> hint(@RequestBody HintRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь с таким email не найден");
        }
        String hint = user.getPasswordHint();
        if (hint == null || hint.isBlank()) {
            return ResponseEntity.ok(Map.of("hint", ""));
        }
        return ResponseEntity.ok(Map.of("hint", hint));
    }
}

