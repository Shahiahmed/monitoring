package com.example.monitoring.controller;

import com.example.monitoring.dto.DicGoResponse;
import com.example.monitoring.entity.DicGo;
import com.example.monitoring.repository.DicGoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/government-bodies")
@RequiredArgsConstructor
public class DicGoController {

    private final DicGoRepository repository;

    @GetMapping
    public List<DicGoResponse> getAll() {
        return repository.findAll().stream()
                .map(e -> new DicGoResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicGoResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(new DicGoResponse(e.getId(), e.getNameEn(), e.getNameKz(), e.getNameRu())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DicGoResponse> create(@RequestBody DicGoResponse request) {
        DicGo entity = DicGo.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .build();
        DicGo saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DicGoResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DicGoResponse request) {
        var optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DicGo entity = optional.get();
        entity.setNameEn(request.nameEn());
        entity.setNameKz(request.nameKz());
        entity.setNameRu(request.nameRu());
        DicGo saved = repository.save(entity);
        return ResponseEntity.ok(new DicGoResponse(saved.getId(), saved.getNameEn(), saved.getNameKz(), saved.getNameRu()));
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
