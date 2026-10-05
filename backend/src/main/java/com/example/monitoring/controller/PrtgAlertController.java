package com.example.monitoring.controller;

import com.example.monitoring.dto.PrtgAlertRequest;
import com.example.monitoring.dto.PrtgAlertResponse;
import com.example.monitoring.dto.SimpleStatsResponse;
import com.example.monitoring.security.MonitoringUserPrincipal;
import com.example.monitoring.service.ActivityLogService;
import com.example.monitoring.service.PrtgAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prtg-alerts")
@RequiredArgsConstructor
public class PrtgAlertController {

    private final PrtgAlertService service;
    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<PrtgAlertResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) String prtgStatus) {
        return service.list(dateFrom, dateTo, prtgStatus);
    }

    @GetMapping("/years")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<Integer> years() {
        return service.years();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SimpleStatsResponse stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return service.stats(dateFrom, dateTo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PrtgAlertResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PrtgAlertResponse> create(@RequestBody PrtgAlertRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PrtgAlertResponse> update(@PathVariable Long id, @RequestBody PrtgAlertRequest req,
            @AuthenticationPrincipal MonitoringUserPrincipal principal) {
        PrtgAlertResponse result = service.update(id, req);
        if (principal != null) {
            activityLogService.log(principal.getId(), principal.getEmail(), "UPDATE",
                    "PRTG_ALERT", id, "Изменена тревога PRTG №" + id);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal MonitoringUserPrincipal principal) {
        service.delete(id);
        if (principal != null) {
            activityLogService.log(principal.getId(), principal.getEmail(), "DELETE",
                    "PRTG_ALERT", id, "Удалена тревога PRTG №" + id);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/intervals/{intervalId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteInterval(@PathVariable Long intervalId) {
        service.deleteInterval(intervalId);
        return ResponseEntity.noContent().build();
    }
}
