package com.example.monitoring.controller;

import com.example.monitoring.dto.DicLocationResponse;
import com.example.monitoring.entity.DicLocation;
import com.example.monitoring.repository.DicLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class DicLocationController {

    private final DicLocationRepository repository;

    @GetMapping
    public List<DicLocationResponse> getAll() {
        return repository.findAll().stream()
                .map(loc -> new DicLocationResponse(
                        loc.getId(),
                        loc.getNameEn(),
                        loc.getNameKz(),
                        loc.getNameRu()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicLocationResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(loc -> ResponseEntity.ok(new DicLocationResponse(
                        loc.getId(),
                        loc.getNameEn(),
                        loc.getNameKz(),
                        loc.getNameRu()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DicLocationResponse> create(@RequestBody DicLocationResponse request) {
        DicLocation entity = DicLocation.builder()
                .id(request.id())
                .nameEn(request.nameEn())
                .nameKz(request.nameKz())
                .nameRu(request.nameRu())
                .build();

        DicLocation saved = repository.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(new DicLocationResponse(
                saved.getId(),
                saved.getNameEn(),
                saved.getNameKz(),
                saved.getNameRu()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DicLocationResponse request) {
        var optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DicLocation entity = optional.get();
        entity.setNameEn(request.nameEn());
        entity.setNameKz(request.nameKz());
        entity.setNameRu(request.nameRu());

        DicLocation saved = repository.save(entity);

        return ResponseEntity.ok(new DicLocationResponse(
                saved.getId(),
                saved.getNameEn(),
                saved.getNameKz(),
                saved.getNameRu()
        ));
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
