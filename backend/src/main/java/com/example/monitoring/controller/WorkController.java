package com.example.monitoring.controller;

import com.example.monitoring.dto.SimpleStatsResponse;
import com.example.monitoring.dto.WorkRequest;
import com.example.monitoring.dto.WorkResponse;
import com.example.monitoring.security.MonitoringUserPrincipal;
import com.example.monitoring.service.ActivityLogService;
import com.example.monitoring.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService service;
    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<WorkResponse> list(
            @RequestParam(required = false) Long dicJobId,
            @RequestParam(required = false) List<Long> isIds,
            @RequestParam(required = false) Boolean emptyTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return service.list(dicJobId, isIds, emptyTime, dateFrom, dateTo);
    }

    @GetMapping("/years")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<Integer> years() {
        return service.years();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public SimpleStatsResponse stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return service.stats(dateFrom, dateTo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<WorkResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<WorkResponse> create(@RequestBody WorkRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<WorkResponse> update(@PathVariable Long id, @RequestBody WorkRequest req,
            @AuthenticationPrincipal MonitoringUserPrincipal principal) {
        WorkResponse result = service.update(id, req);
        if (principal != null) {
            activityLogService.log(principal.getId(), principal.getEmail(), "UPDATE",
                    "WORK", id, "Изменена работа №" + id);
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
                    "WORK", id, "Удалена работа №" + id);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/intervals/{intervalId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteInterval(@PathVariable Long intervalId) {
        service.deleteInterval(intervalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/load-from-prtg/{prtgAlertId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<WorkRequest> loadFromPrtg(@PathVariable Long prtgAlertId) {
        return ResponseEntity.ok(service.loadFromPrtg(prtgAlertId));
    }
}
