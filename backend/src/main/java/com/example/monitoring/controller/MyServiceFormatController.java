package com.example.monitoring.controller;

import com.example.monitoring.entity.MyServiceField;
import com.example.monitoring.entity.MyServiceFormat;
import com.example.monitoring.repository.MyServiceFieldRepository;
import com.example.monitoring.repository.MyServiceFormatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-service-formats")
@RequiredArgsConstructor
public class MyServiceFormatController {

    private final MyServiceFormatRepository repo;
    private final MyServiceFieldRepository fieldRepo;

    @GetMapping("/service/{serviceId}")
    public List<MyServiceFormat> listByService(@PathVariable Long serviceId) {
        return repo.findByServiceIdOrderBySortOrderAscIdAsc(serviceId);
    }

    @PostMapping
    public ResponseEntity<MyServiceFormat> create(@RequestBody MyServiceFormat body) {
        body.setId(null);
        boolean isFirst = repo.findByServiceIdOrderBySortOrderAscIdAsc(body.getServiceId()).isEmpty();
        MyServiceFormat saved = repo.save(body);
        // Если это первый формат — привязать все поля с format_id=NULL к нему
        if (isFirst) {
            List<MyServiceField> nullFields = fieldRepo.findByServiceIdAndFormatIdIsNull(body.getServiceId());
            nullFields.forEach(f -> f.setFormatId(saved.getId()));
            fieldRepo.saveAll(nullFields);
        }
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyServiceFormat> update(@PathVariable Long id, @RequestBody MyServiceFormat body) {
        return repo.findById(id).map(f -> {
            f.setName(body.getName());
            f.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);
            return ResponseEntity.ok(repo.save(f));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().<Void>build();
    }
}
