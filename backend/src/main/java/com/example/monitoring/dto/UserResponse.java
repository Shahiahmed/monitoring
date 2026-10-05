package com.example.monitoring.dto;

import com.example.monitoring.entity.Role;
import com.example.monitoring.entity.User;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String secondName,
        Boolean isActive,
        OffsetDateTime registrationDate,
        OffsetDateTime lastLoginDate,
        Set<String> roles,
        Boolean hasAvatar,
        String passwordHint
) {
    public static UserResponse fromEntity(User user) {
        byte[] avatar = user.getAvatar();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getSecondName(),
                user.getIsActive(),
                user.getRegistrationDate(),
                user.getLastLoginDate(),
                user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()),
                avatar != null && avatar.length > 0,
                user.getPasswordHint()
        );
    }
}
