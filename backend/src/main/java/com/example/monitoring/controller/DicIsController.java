package com.example.monitoring.controller;

import com.example.monitoring.dto.DicIsResponse;
import com.example.monitoring.entity.DicGo;
import com.example.monitoring.entity.DicIs;
import com.example.monitoring.repository.DicGoRepository;
import com.example.monitoring.repository.DicIsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/information-systems")
@RequiredArgsConstructor
public class DicIsController {

    private final DicIsRepository repository;
    private final DicGoRepository goRepository;

    @GetMapping
    public List<DicIsResponse> getAll() {
        return repository.findAll().stream()
                .sorted(java.util.Comparator.comparingInt(e -> (e.getSortOrder() != null ? e.getSortOrder() : 0)))
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicIsResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(toResponse(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DicIsResponse request) {
        var go = goRepository.findById(request.goId());
        if (go.isEmpty()) {
            return ResponseEntity.badRequest().body("Гос орган с ID " + request.goId() + " не найден");
        }

        DicIs entity = DicIs.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .includeInAvailability(request.includeInAvailability() == null || request.includeInAvailability())
                .go(go.get())
                .build();

        DicIs saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DicIsResponse request) {
        var optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DicIs entity = optional.get();
        entity.setNameEn(request.nameEn());
        entity.setNameKz(request.nameKz());
        entity.setNameRu(request.nameRu());
        entity.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        entity.setIncludeInAvailability(request.includeInAvailability() == null || request.includeInAvailability());

        if (request.goId() != null && !request.goId().equals(entity.getGo().getId())) {
            var go = goRepository.findById(request.goId());
            if (go.isEmpty()) {
                return ResponseEntity.badRequest().body("Гос орган с ID " + request.goId() + " не найден");
            }
            entity.setGo(go.get());
        }

        DicIs saved = repository.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DicIsResponse toResponse(DicIs e) {
        return new DicIsResponse(
                e.getId(),
                e.getNameEn(),
                e.getNameKz(),
                e.getNameRu(),
                e.getGo().getId(),
                e.getGo().getNameRu(),
                e.getSortOrder(),
                e.getIncludeInAvailability() == null || e.getIncludeInAvailability()
        );
    }
}
