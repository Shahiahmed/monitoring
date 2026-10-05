package com.example.monitoring.controller;

import com.example.monitoring.dto.*;
import com.example.monitoring.service.ApplicationNewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationNewController {

    private final ApplicationNewService service;

    // ── Реестр сервисов ──

    @GetMapping
    public List<ApplicationNewResponse> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ApplicationNewResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationNewResponse> create(@RequestBody ApplicationNewRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationNewResponse> update(@PathVariable Long id, @RequestBody ApplicationNewRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Развёртывания (deployments) ──

    @GetMapping("/{appId}/deployments")
    public List<ApplicationInfoNewResponse> listDeployments(@PathVariable Long appId) {
        return service.listDeployments(appId);
    }

    @PostMapping("/deployments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> createDeployment(@RequestBody ApplicationInfoNewRequest req) {
        return ResponseEntity.ok(service.createDeployment(req));
    }

    @PutMapping("/deployments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> updateDeployment(
            @PathVariable Long id, @RequestBody ApplicationInfoNewRequest req) {
        return ResponseEntity.ok(service.updateDeployment(id, req));
    }

    @DeleteMapping("/deployments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDeployment(@PathVariable Long id) {
        service.deleteDeployment(id);
        return ResponseEntity.noContent().build();
    }

    // ── SSH действия ──

    @PostMapping("/deployments/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(service.sshAction(id, "start"));
    }

    @PostMapping("/deployments/{id}/stop")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> stop(@PathVariable Long id) {
        return ResponseEntity.ok(service.sshAction(id, "stop"));
    }

    @PostMapping("/deployments/{id}/restart")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> restart(@PathVariable Long id) {
        return ResponseEntity.ok(service.sshAction(id, "restart"));
    }

    @PostMapping("/deployments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApplicationInfoNewResponse> refreshStatus(@PathVariable Long id) {
        return ResponseEntity.ok(service.refreshStatus(id));
    }

    // ── Логи (SSE стриминг) ──

    @GetMapping(value = "/deployments/{id}/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SseEmitter streamLogs(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String grep) {
        return service.streamLogs(id, grep);
    }
}
