package com.example.monitoring.controller;

import com.example.monitoring.dto.DicInteractionTypeResponse;
import com.example.monitoring.entity.DicInteractionType;
import com.example.monitoring.repository.DicInteractionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interaction-types")
@RequiredArgsConstructor
public class DicInteractionTypeController {

    private final DicInteractionTypeRepository repository;

    @GetMapping
    public List<DicInteractionTypeResponse> getAll() {
        return repository.findAll().stream()
                .map(e -> new DicInteractionTypeResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicInteractionTypeResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(new DicInteractionTypeResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DicInteractionTypeResponse> create(@RequestBody DicInteractionTypeResponse request) {
        DicInteractionType saved = repository.save(DicInteractionType.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DicInteractionTypeResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DicInteractionTypeResponse> update(@PathVariable Long id, @RequestBody DicInteractionTypeResponse request) {
        return repository.findById(id).map(e -> {
            e.setNameEn(request.nameEn());
            e.setNameKz(request.nameKz());
            e.setNameRu(request.nameRu());
            DicInteractionType saved = repository.save(e);
            return ResponseEntity.ok(new DicInteractionTypeResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
