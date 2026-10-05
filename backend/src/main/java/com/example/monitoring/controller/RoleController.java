package com.example.monitoring.controller;

import com.example.monitoring.dto.RoleResponse;
import com.example.monitoring.entity.Role;
import com.example.monitoring.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getCode(),
                        role.getNameEn(),
                        role.getNameKz(),
                        role.getNameRu()
                ))
                .toList();
    }
}

