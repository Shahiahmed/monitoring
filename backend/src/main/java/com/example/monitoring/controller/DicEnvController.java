package com.example.monitoring.controller;

import com.example.monitoring.dto.DicEnvResponse;
import com.example.monitoring.entity.DicEnv;
import com.example.monitoring.repository.DicEnvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class DicEnvController {

    private final DicEnvRepository repository;

    @GetMapping
    public List<DicEnvResponse> getAll() {
        return repository.findAll().stream()
                .map(e -> new DicEnvResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicEnvResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(new DicEnvResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DicEnvResponse> create(@RequestBody DicEnvResponse request) {
        DicEnv entity = DicEnv.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .build();
        DicEnv saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DicEnvResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DicEnvResponse request) {
        var optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DicEnv entity = optional.get();
        entity.setNameEn(request.nameEn());
        entity.setNameKz(request.nameKz());
        entity.setNameRu(request.nameRu());
        DicEnv saved = repository.save(entity);
        return ResponseEntity.ok(new DicEnvResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
