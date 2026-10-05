package com.example.monitoring.controller;

import com.example.monitoring.dto.DicAppTypeResponse;
import com.example.monitoring.entity.DicAppType;
import com.example.monitoring.repository.DicAppTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application-types")
@RequiredArgsConstructor
public class DicAppTypeController {

    private final DicAppTypeRepository repository;

    @GetMapping
    public List<DicAppTypeResponse> getAll() {
        return repository.findAll().stream()
                .map(e -> new DicAppTypeResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicAppTypeResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(new DicAppTypeResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DicAppTypeResponse> create(@RequestBody DicAppTypeResponse request) {
        DicAppType saved = repository.save(DicAppType.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DicAppTypeResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DicAppTypeResponse> update(@PathVariable Long id, @RequestBody DicAppTypeResponse request) {
        return repository.findById(id).map(e -> {
            e.setNameEn(request.nameEn());
            e.setNameKz(request.nameKz());
            e.setNameRu(request.nameRu());
            DicAppType saved = repository.save(e);
            return ResponseEntity.ok(new DicAppTypeResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
