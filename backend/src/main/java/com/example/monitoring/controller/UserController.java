package com.example.monitoring.controller;

import com.example.monitoring.dto.AdminSetPasswordRequest;
import com.example.monitoring.dto.DeleteUserRequest;
import com.example.monitoring.dto.UserActiveRequest;
import com.example.monitoring.dto.ChangeOwnPasswordRequest;
import com.example.monitoring.dto.UserRegisterRequest;
import com.example.monitoring.dto.UserResponse;
import com.example.monitoring.entity.Role;
import com.example.monitoring.entity.User;
import com.example.monitoring.repository.CertificateAssetRepository;
import com.example.monitoring.repository.RoleRepository;
import com.example.monitoring.repository.UserRepository;
import com.example.monitoring.security.MonitoringUserPrincipal;
import com.example.monitoring.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CertificateAssetRepository certificateAssetRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        var users = userRepository.findAll();

        var response = users.stream()
                .map(UserResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal MonitoringUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var optionalUser = userRepository.findById(principal.getId());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        return ResponseEntity.ok(UserResponse.fromEntity(optionalUser.get()));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> register(
            @RequestBody UserRegisterRequest request,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Простая серверная валидация
        if (request.email() == null || !request.email().endsWith("@enbek.kz")) {
            return ResponseEntity.badRequest().body("Email должен быть корпоративным (@enbek.kz)");
        }
        if (request.password() == null || request.password().length() < 6) {
            return ResponseEntity.badRequest().body("Пароль должен содержать минимум 6 символов");
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Пользователь с таким email уже существует");
        }

        // SUPER_ADMIN — любая роль; обычный ADMIN — только USER или ADMIN (не SUPER_ADMIN)
        String code;
        if (principal.isSuperAdmin()) {
            code = request.roleCode() != null && !request.roleCode().isBlank()
                    ? request.roleCode().trim()
                    : "USER";
        } else {
            String requested = request.roleCode() != null ? request.roleCode().trim() : "";
            if (requested.isEmpty()) {
                code = "USER";
            } else if ("USER".equals(requested) || "ADMIN".equals(requested)) {
                code = requested;
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Недостаточно прав: можно назначить только роли «Пользователь» или «Админ»");
            }
        }

        Role role = roleRepository.findByCode(code).orElseGet(() ->
                roleRepository.findByCode("USER")
                        .orElseThrow(() -> new IllegalStateException("Базовая роль USER не найдена в БД"))
        );

        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setSecondName(request.secondName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordHint(request.passwordHint());
        user.setIsActive(request.active() != null ? request.active() : Boolean.TRUE);
        user.setRegistrationDate(OffsetDateTime.now());
        user.getRoles().add(role);

        User saved = userRepository.save(user);

        activityLogService.log(principal.getId(), principal.getEmail(), "REGISTER",
                "USER", saved.getId(), "Зарегистрирован пользователь: " + saved.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(saved));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changeOwnPassword(
            @RequestBody ChangeOwnPasswordRequest body,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (body.newPassword() == null || body.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Новый пароль должен содержать минимум 6 символов");
        }
        if (body.currentPassword() == null || body.currentPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Укажите текущий пароль");
        }

        User user = userRepository.findById(principal.getId())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        if (!passwordEncoder.matches(body.currentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный текущий пароль");
        }

        user.setPasswordHash(passwordEncoder.encode(body.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> adminSetUserPassword(
            @PathVariable Long id,
            @RequestBody AdminSetPasswordRequest body
    ) {
        if (body.newPassword() == null || body.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Новый пароль должен содержать минимум 6 символов");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        user.setPasswordHash(passwordEncoder.encode(body.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestBody DeleteUserRequest body,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (body.currentPassword() == null || body.currentPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Укажите ваш пароль для подтверждения");
        }
        if (principal.getId().equals(id)) {
            return ResponseEntity.badRequest().body("Нельзя удалить собственную учётную запись");
        }

        User actor = userRepository.findById(principal.getId()).orElse(null);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        if (!passwordEncoder.matches(body.currentPassword(), actor.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный пароль");
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        boolean targetIsSuperAdmin = target.getRoles().stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getCode()));
        if (targetIsSuperAdmin) {
            return ResponseEntity.badRequest().body("Нельзя удалить супер-администратора");
        }

        String targetEmail = target.getEmail();
        certificateAssetRepository.deleteByUserId(id);
        userRepository.delete(target);

        activityLogService.log(principal.getId(), principal.getEmail(), "DELETE_USER",
                "USER", id, "Удалён пользователь: " + targetEmail);

        return ResponseEntity.noContent().build();
    }

    public record ChangeRoleRequest(String roleCode) {}

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestBody ChangeRoleRequest body,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (principal.getId().equals(id)) return ResponseEntity.badRequest().body("Нельзя менять собственную роль");

        String code = body.roleCode() != null ? body.roleCode().trim() : "USER";
        if (!java.util.Set.of("USER", "ADMIN", "SUPER_ADMIN").contains(code))
            return ResponseEntity.badRequest().body("Неизвестная роль: " + code);

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");

        Role role = roleRepository.findByCode(code).orElseThrow(() ->
                new IllegalStateException("Роль " + code + " не найдена в БД"));

        user.getRoles().clear();
        user.getRoles().add(role);
        User saved = userRepository.save(user);

        activityLogService.log(principal.getId(), principal.getEmail(), "UPDATE",
                "USER", id, "Изменена роль пользователя " + saved.getEmail() + " → " + code);

        return ResponseEntity.ok(UserResponse.fromEntity(saved));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> setUserActive(
            @PathVariable Long id,
            @RequestBody UserActiveRequest body,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (body.active() == null) {
            return ResponseEntity.badRequest().body("Укажите active: true или false");
        }
        if (principal.getId().equals(id) && !Boolean.TRUE.equals(body.active())) {
            return ResponseEntity.badRequest().body("Нельзя деактивировать собственную учётную запись");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        if (!Boolean.TRUE.equals(body.active())) {
            boolean targetIsSuperAdmin = user.getRoles().stream()
                    .anyMatch(r -> "SUPER_ADMIN".equals(r.getCode()));
            if (targetIsSuperAdmin) {
                return ResponseEntity.badRequest()
                        .body("Нельзя деактивировать супер-администратора");
            }
        }

        user.setIsActive(body.active());
        User saved = userRepository.save(user);

        String action = Boolean.TRUE.equals(body.active()) ? "ACTIVATE" : "DEACTIVATE";
        String desc = (Boolean.TRUE.equals(body.active()) ? "Активирован" : "Деактивирован")
                + " пользователь: " + saved.getEmail();
        activityLogService.log(principal.getId(), principal.getEmail(), action,
                "USER", id, desc);

        return ResponseEntity.ok(UserResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody UserResponse request,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!principal.getId().equals(id) && !principal.isAdminOrSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав");
        }

        var optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        User user = optionalUser.get();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setSecondName(request.secondName());
        user.setPasswordHint(request.passwordHint());

        User saved = userRepository.save(user);

        activityLogService.log(principal.getId(), principal.getEmail(), "UPDATE_PROFILE",
                "USER", id, "Обновлён профиль: " + saved.getEmail());

        return ResponseEntity.ok(UserResponse.fromEntity(saved));
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!principal.getId().equals(id) && !principal.isAdminOrSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав");
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не выбран");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("Размер файла не должен превышать 5MB");
        }

        var optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        try {
            User user = optionalUser.get();
            user.setAvatar(file.getBytes());
            userRepository.save(user);

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + file.getContentType() + ";base64," + base64;

            return ResponseEntity.ok(java.util.Map.of("avatarUrl", dataUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при сохранении аватара");
        }
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<?> getAvatar(
            @PathVariable Long id,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!principal.getId().equals(id) && !principal.isAdminOrSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty() || optionalUser.get().getAvatar() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] avatar = optionalUser.get().getAvatar();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(avatar);
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<?> deleteAvatar(
            @PathVariable Long id,
            @AuthenticationPrincipal MonitoringUserPrincipal principal
    ) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!principal.getId().equals(id) && !principal.isAdminOrSuperAdmin())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setAvatar(null);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}

