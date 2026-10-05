package com.example.monitoring.controller;

import com.example.monitoring.dto.IncidentRequest;
import com.example.monitoring.dto.IncidentResponse;
import com.example.monitoring.dto.IncidentStatsResponse;
import com.example.monitoring.service.ActivityLogService;
import com.example.monitoring.service.IncidentService;
import com.example.monitoring.security.MonitoringUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService service;
    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<IncidentResponse> list(
            @RequestParam(required = false) Long failureTypeId,
            @RequestParam(required = false) List<Long> isIds,
            @RequestParam(required = false) Boolean fixed,
            @RequestParam(required = false) Boolean emptyTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return service.list(failureTypeId, isIds, fixed, emptyTime, dateFrom, dateTo);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public IncidentStatsResponse stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return service.stats(dateFrom, dateTo);
    }

    @GetMapping("/years")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<Integer> years() {
        return service.years();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<IncidentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<IncidentResponse> create(@RequestBody IncidentRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<IncidentResponse> update(@PathVariable Long id, @RequestBody IncidentRequest req,
            @AuthenticationPrincipal MonitoringUserPrincipal principal) {
        IncidentResponse result = service.update(id, req);
        if (principal != null) {
            activityLogService.log(principal.getId(), principal.getEmail(), "UPDATE",
                    "INCIDENT", id, "Изменён инцидент №" + id);
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
                    "INCIDENT", id, "Удалён инцидент №" + id);
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
    public ResponseEntity<IncidentRequest> loadFromPrtg(@PathVariable Long prtgAlertId) {
        return ResponseEntity.ok(service.loadFromPrtg(prtgAlertId));
    }
}
