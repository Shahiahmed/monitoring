package com.example.monitoring.controller;

import com.example.monitoring.entity.MyService;
import com.example.monitoring.repository.MyServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/my-services")
@RequiredArgsConstructor
public class MyServiceController {

    private final MyServiceRepository repo;

    @GetMapping
    public List<MyService> list() {
        return repo.findAllByOrderBySortOrderAscServiceNameAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyService> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MyService> create(@RequestBody MyService body) {
        body.setId(null);
        body.setContractFile(null);
        body.setContractFilename(null);
        body.setContractContentType(null);
        return ResponseEntity.ok(repo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyService> update(@PathVariable Long id, @RequestBody MyService body) {
        return repo.findById(id).map(existing -> {
            existing.setPublishedAt(body.getPublishedAt());
            existing.setServiceKey(body.getServiceKey());
            existing.setInformationSystem(body.getInformationSystem());
            existing.setServiceName(body.getServiceName());
            existing.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);
            existing.setIsPaid(body.getIsPaid());
            existing.setSmartBridgeTicket(body.getSmartBridgeTicket());
            existing.setContractExpiresAt(body.getContractExpiresAt());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ── Договор (контракт) ────────────────────────────────────────────────

    @PostMapping("/{id}/contract")
    public ResponseEntity<MyService> uploadContract(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return repo.findById(id).map(svc -> {
            try {
                svc.setContractFile(file.getBytes());
                svc.setContractFilename(file.getOriginalFilename());
                svc.setContractContentType(file.getContentType());
                return ResponseEntity.ok(repo.save(svc));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/contract")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long id) {
        return repo.findById(id)
                .filter(s -> s.getContractFile() != null && s.getContractFile().length > 0)
                .map(s -> ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + s.getContractFilename() + "\"")
                        .contentType(MediaType.parseMediaType(
                                s.getContractContentType() != null ? s.getContractContentType() : "application/octet-stream"))
                        .body(s.getContractFile()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/contract")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        return repo.findById(id).map(svc -> {
            svc.setContractFile(null);
            svc.setContractFilename(null);
            svc.setContractContentType(null);
            repo.save(svc);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
