package com.example.monitoring.controller;

import com.example.monitoring.entity.MyServiceField;
import com.example.monitoring.repository.MyServiceFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-service-fields")
@RequiredArgsConstructor
public class MyServiceFieldController {

    private final MyServiceFieldRepository repo;

    @GetMapping("/service/{serviceId}")
    public List<MyServiceField> listByService(@PathVariable Long serviceId) {
        return repo.findByServiceIdOrderBySortOrderAscIdAsc(serviceId);
    }

    @PostMapping
    public ResponseEntity<MyServiceField> create(@RequestBody MyServiceField body) {
        body.setId(null);
        return ResponseEntity.ok(repo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyServiceField> update(@PathVariable Long id, @RequestBody MyServiceField body) {
        return repo.findById(id).map(existing -> {
            existing.setDirection(body.getDirection());
            existing.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);
            existing.setGroupName(body.getGroupName());
            existing.setFieldNumber(body.getFieldNumber());
            existing.setNameRu(body.getNameRu());
            existing.setTagName(body.getTagName());
            existing.setFormatInfo(body.getFormatInfo());
            existing.setSizeInfo(body.getSizeInfo());
            existing.setIsRequired(body.getIsRequired());
            existing.setNotes(body.getNotes());
            existing.setFormatId(body.getFormatId());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().<Void>build();
    }
}
