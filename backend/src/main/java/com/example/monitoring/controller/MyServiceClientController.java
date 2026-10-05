package com.example.monitoring.controller;

import com.example.monitoring.entity.MyServiceClient;
import com.example.monitoring.repository.MyServiceClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/my-service-clients")
@RequiredArgsConstructor
public class MyServiceClientController {

    private final MyServiceClientRepository repo;

    @GetMapping("/service/{serviceId}")
    public List<MyServiceClient> listByService(@PathVariable Long serviceId) {
        return repo.findByServiceIdOrderByOrganizationNameAsc(serviceId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyServiceClient> getOne(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MyServiceClient> create(@RequestBody MyServiceClient body) {
        body.setId(null);
        return ResponseEntity.ok(repo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyServiceClient> update(@PathVariable Long id, @RequestBody MyServiceClient body) {
        return repo.findById(id).map(c -> {
            c.setOrganizationName(body.getOrganizationName());
            c.setInformationSystem(body.getInformationSystem());
            c.setPaid(body.isPaid());
            c.setNotes(body.getNotes());
            c.setSmartBridgeTicket(body.getSmartBridgeTicket());
            c.setConnectionBasis(body.getConnectionBasis());
            c.setConnectionDate(body.getConnectionDate());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().<Void>build();
    }

    @PostMapping("/{id}/contract")
    public ResponseEntity<MyServiceClient> uploadContract(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return repo.findById(id).map(c -> {
            try {
                c.setContractFileName(file.getOriginalFilename());
                c.setContractFileData(file.getBytes());
                return ResponseEntity.ok(repo.save(c));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/contract")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long id) {
        return repo.findById(id).map(c -> {
            if (c.getContractFileData() == null) return ResponseEntity.notFound().<byte[]>build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + c.getContractFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(c.getContractFileData());
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/contract")
    public ResponseEntity<MyServiceClient> deleteContract(@PathVariable Long id) {
        return repo.findById(id).map(c -> {
            c.setContractFileName(null);
            c.setContractFileData(null);
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }
}
