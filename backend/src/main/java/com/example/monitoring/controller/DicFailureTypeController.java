package com.example.monitoring.controller;

import com.example.monitoring.dto.DicFailureTypeResponse;
import com.example.monitoring.entity.DicFailureType;
import com.example.monitoring.repository.DicFailureTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dic-failure-types")
@RequiredArgsConstructor
public class DicFailureTypeController {

    private final DicFailureTypeRepository repository;

    @GetMapping
    public List<DicFailureTypeResponse> getAll() {
        return repository.findAll().stream()
                .map(DicFailureTypeResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<DicFailureTypeResponse> create(@RequestBody DicFailureTypeResponse request) {
        DicFailureType saved = repository.save(DicFailureType.builder()
                .nameRu(request.nameRu())
                .nameKz(request.nameKz())
                .nameEn(request.nameEn())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(DicFailureTypeResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DicFailureTypeResponse> update(@PathVariable Long id, @RequestBody DicFailureTypeResponse request) {
        return repository.findById(id).map(e -> {
            e.setNameRu(request.nameRu());
            e.setNameKz(request.nameKz());
            e.setNameEn(request.nameEn());
            return ResponseEntity.ok(DicFailureTypeResponse.from(repository.save(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
