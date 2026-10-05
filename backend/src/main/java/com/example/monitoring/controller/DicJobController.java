package com.example.monitoring.controller;

import com.example.monitoring.dto.DicJobResponse;
import com.example.monitoring.repository.DicJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-types")
@RequiredArgsConstructor
public class DicJobController {

    private final DicJobRepository repository;

    @GetMapping
    public List<DicJobResponse> list() {
        return repository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(j -> new DicJobResponse(j.getId(), j.getNameEn(), j.getNameKz(), j.getNameRu()))
                .toList();
    }
}
