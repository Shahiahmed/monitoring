package com.example.monitoring.controller;

import com.example.monitoring.dto.DicServerRequest;
import com.example.monitoring.dto.DicServerResponse;
import com.example.monitoring.dto.ServerMetricsHistoryResponse;
import com.example.monitoring.dto.ServerMetricsResponse;
import com.example.monitoring.entity.DicEnv;
import com.example.monitoring.entity.DicServer;
import com.example.monitoring.entity.ServerMetricsHistory;
import com.example.monitoring.repository.DicEnvRepository;
import com.example.monitoring.repository.DicServerRepository;
import com.example.monitoring.repository.ServerMetricsHistoryRepository;
import com.example.monitoring.service.SshMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class DicServerController {

    private static final long PROD_ENV_ID = 2L;

    private final DicServerRepository serverRepository;
    private final DicEnvRepository envRepository;
    private final SshMetricsService sshMetricsService;
    private final ServerMetricsHistoryRepository historyRepository;

    @GetMapping
    public List<DicServerResponse> getAll() {
        return serverRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/metrics")
    public List<ServerMetricsResponse> getProdMetrics(@RequestParam(required = false) Boolean refresh) {
        List<DicServer> prodServers = serverRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .filter(s -> s.getEnv() != null && PROD_ENV_ID == s.getEnv().getId())
                .toList();
        return sshMetricsService.fetchMetrics(prodServers, Boolean.TRUE.equals(refresh));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DicServerResponse> getById(@PathVariable Long id) {
        return serverRepository.findById(id)
                .map(e -> ResponseEntity.ok(toResponse(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ServerMetricsHistoryResponse>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours
    ) {
        if (!serverRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        LocalDateTime after = LocalDateTime.now().minusHours(hours);
        List<ServerMetricsHistoryResponse> result = historyRepository
                .findByServerIdAndCollectedAtAfterOrderByCollectedAtAsc(id, after)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/history/all")
    public ResponseEntity<?> clearAllHistory() {
        historyRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<DicServerResponse> create(@RequestBody DicServerRequest request) {
        DicEnv env = request.envId() != null ? envRepository.findById(request.envId()).orElse(null) : null;
        DicServer entity = DicServer.builder()
                .id(request.id())
                .active(request.active())
                .description(request.description())
                .ip(request.ip())
                .env(env)
                .warnRam(request.warnRam())
                .warnDisk(request.warnDisk())
                .critRam(request.critRam())
                .critDisk(request.critDisk())
                .build();
        DicServer saved = serverRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DicServerRequest request) {
        var optional = serverRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DicEnv env = request.envId() != null ? envRepository.findById(request.envId()).orElse(null) : null;
        DicServer entity = optional.get();
        entity.setActive(request.active());
        entity.setDescription(request.description());
        entity.setIp(request.ip());
        entity.setEnv(env);
        entity.setWarnRam(request.warnRam());
        entity.setWarnDisk(request.warnDisk());
        entity.setCritRam(request.critRam());
        entity.setCritDisk(request.critDisk());
        DicServer saved = serverRepository.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!serverRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serverRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ServerMetricsHistoryResponse toHistoryResponse(ServerMetricsHistory h) {
        return new ServerMetricsHistoryResponse(
                h.getCollectedAt(),
                h.getCpuPercent(),
                h.getMemoryPercent(),
                h.getDiskPercent(),
                h.getMemoryUsedMb(),
                h.getMemoryTotalMb(),
                h.getDiskUsedGb(),
                h.getDiskTotalGb()
        );
    }

    private DicServerResponse toResponse(DicServer e) {
        Long envId = e.getEnv() != null ? e.getEnv().getId() : null;
        String envNameRu = e.getEnv() != null ? e.getEnv().getNameRu() : null;
        return new DicServerResponse(e.getId(), e.getActive(), e.getDescription(), e.getIp(), envId, envNameRu,
                e.getWarnRam(), e.getWarnDisk(), e.getCritRam(), e.getCritDisk());
    }
}
