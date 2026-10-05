package com.example.monitoring.dto;

public record UserRegisterRequest(
        String email,
        String firstName,
        String lastName,
        String secondName,
        String password,
        String passwordHint,
        String roleCode,
        Boolean active
) {
}

