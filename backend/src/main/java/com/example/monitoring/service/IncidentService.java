package com.example.monitoring.service;

import com.example.monitoring.dto.IncidentRequest;
import com.example.monitoring.dto.IncidentResponse;
import com.example.monitoring.dto.IncidentStatsResponse;
import com.example.monitoring.entity.*;
import com.example.monitoring.repository.*;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentIntervalRepository intervalRepository;
    private final IncidentFileRepository fileRepository;
    private final DicFailureTypeRepository failureTypeRepository;
    private final DicLocationRepository locationRepository;
    private final DicIsRepository isRepository;
    private final PrtgAlertRepository prtgAlertRepository;

    @Transactional(readOnly = true)
    public List<IncidentResponse> list(Long failureTypeId, List<Long> isIds,
                                       Boolean fixed, Boolean emptyTime,
                                       OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        Specification<Incident> spec = buildSpec(failureTypeId, isIds, fixed, emptyTime, dateFrom, dateTo);
        return incidentRepository.findAll(spec).stream()
            .sorted((a, b) -> {
                var da = a.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(a.getCreatedAt());
                var db = b.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(b.getCreatedAt());
                return db.compareTo(da);
            })
            .map(e -> IncidentResponse.from(e, (int) fileRepository.countByIncidentId(e.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> years() {
        return incidentRepository.findDistinctYears().stream()
            .map(o -> ((Number) o).intValue())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        Incident e = incidentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Incident not found: " + id));
        return IncidentResponse.from(e, (int) fileRepository.countByIncidentId(id));
    }

    @Transactional
    public IncidentResponse create(IncidentRequest req) {
        Incident e = buildIncident(new Incident(), req);
        incidentRepository.save(e);
        saveIntervals(e, req);
        Incident saved = incidentRepository.findById(e.getId()).orElseThrow();
        return IncidentResponse.from(saved, 0);
    }

    @Transactional
    public IncidentResponse update(Long id, IncidentRequest req) {
        Incident e = incidentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Incident not found: " + id));
        buildIncident(e, req);
        intervalRepository.deleteAll(intervalRepository.findByIncidentIdOrderById(id));
        e.getIntervals().clear();
        incidentRepository.save(e);
        saveIntervals(e, req);
        Incident saved = incidentRepository.findById(id).orElseThrow();
        return IncidentResponse.from(saved, (int) fileRepository.countByIncidentId(id));
    }

    @Transactional
    public void delete(Long id) {
        fileRepository.deleteAll(fileRepository.findByIncidentIdOrderByUploadedAtDesc(id));
        incidentRepository.deleteById(id);
    }

    @Transactional
    public void deleteInterval(Long intervalId) {
        intervalRepository.deleteById(intervalId);
    }

    @Transactional(readOnly = true)
    public IncidentStatsResponse stats(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        Specification<Incident> spec = buildSpec(null, null, null, null, dateFrom, dateTo);
        List<Incident> incidents = incidentRepository.findAll(spec);

        // byType
        Map<String, long[]> byTypeMap = new LinkedHashMap<>();
        for (Incident inc : incidents) {
            String typeName = inc.getFailureType() != null ? inc.getFailureType().getNameRu() : "Не указан";
            long mins = Boolean.TRUE.equals(inc.getEmptyTime()) ? 0L :
                inc.getIntervals().stream()
                    .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L).sum();
            byTypeMap.merge(typeName, new long[]{1, mins}, (a, b) -> new long[]{a[0] + b[0], a[1] + b[1]});
        }
        List<IncidentStatsResponse.TypeCount> byType = byTypeMap.entrySet().stream()
            .map(e -> IncidentStatsResponse.TypeCount.builder()
                .name(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        // includedIsIds — ИС с includeInAvailability=true (вычисляем заранее для byMonth и totalDowntimeMinutes)
        List<DicIs> allIs = isRepository.findAll().stream()
            .filter(is -> !Boolean.FALSE.equals(is.getIncludeInAvailability()))
            .sorted(Comparator.comparingInt(is -> (is.getSortOrder() != null ? is.getSortOrder() : 0)))
            .collect(Collectors.toList());
        Set<Long> includedIsIds = allIs.stream().map(DicIs::getId).collect(Collectors.toSet());

        // byMonth — группируем по dateFrom первого интервала
        // totalMinutes считается только для инцидентов с includeAvailability=true и хотя бы одной ИС с includeInAvailability=true
        Map<String, long[]> byMonthMap = new LinkedHashMap<>();
        incidents.stream()
            .filter(inc -> !inc.getIntervals().isEmpty())
            .sorted(Comparator.comparing(inc -> inc.getIntervals().stream()
                .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                .min(Comparator.naturalOrder()).orElse(inc.getCreatedAt())))
            .forEach(inc -> {
                var dateRef = inc.getIntervals().stream()
                    .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                    .min(Comparator.naturalOrder()).orElse(inc.getCreatedAt());
                String month = String.format("%04d-%02d", dateRef.getYear(), dateRef.getMonthValue());
                boolean counted = Boolean.TRUE.equals(inc.getIncludeAvailability())
                    && !Boolean.TRUE.equals(inc.getEmptyTime())
                    && inc.getInformationSystems().stream().anyMatch(s -> includedIsIds.contains(s.getId()));
                long mins = counted
                    ? inc.getIntervals().stream()
                        .mapToLong(iv -> Math.max(0, iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L)).sum()
                    : 0L;
                byMonthMap.merge(month, new long[]{1, mins}, (a, b) -> new long[]{a[0] + b[0], a[1] + b[1]});
            });
        List<IncidentStatsResponse.MonthCount> byMonth = byMonthMap.entrySet().stream()
            .map(e -> IncidentStatsResponse.MonthCount.builder()
                .month(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        // byIsAvailability
        int year = dateFrom != null ? dateFrom.getYear() : java.time.Year.now().getValue();
        long minutesPerYear = (java.time.Year.of(year).isLeap() ? 366L : 365L) * 1440L;
        List<IncidentStatsResponse.IsAvailability> byIsAvailability = allIs.stream()
            .map(is -> {
                int isCount = (int) incidents.stream()
                    .filter(inc -> inc.getInformationSystems().stream().anyMatch(s -> s.getId().equals(is.getId())))
                    .count();
                long downtime = incidents.stream()
                    .filter(inc -> Boolean.TRUE.equals(inc.getIncludeAvailability())
                        && !Boolean.TRUE.equals(inc.getEmptyTime())
                        && inc.getInformationSystems().stream().anyMatch(s -> s.getId().equals(is.getId())))
                    .flatMap(inc -> inc.getIntervals().stream())
                    .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L)
                    .sum();
                double avail = 100.0 * (1.0 - (double) downtime / minutesPerYear);
                if (avail < 0) avail = 0;
                return IncidentStatsResponse.IsAvailability.builder()
                    .id(is.getId()).nameRu(is.getNameRu()).count(isCount)
                    .totalDowntimeMinutes(downtime).availabilityPercent(avail).build();
            })
            .collect(Collectors.toList());

        long totalDowntimeMinutes = incidents.stream()
            .filter(inc -> Boolean.TRUE.equals(inc.getIncludeAvailability())
                && !Boolean.TRUE.equals(inc.getEmptyTime())
                && inc.getInformationSystems().stream().anyMatch(s -> includedIsIds.contains(s.getId())))
            .flatMap(inc -> inc.getIntervals().stream())
            .mapToLong(iv -> Math.max(0, iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L))
            .sum();

        return IncidentStatsResponse.builder()
            .byType(byType).byMonth(byMonth).byIsAvailability(byIsAvailability)
            .totalDowntimeMinutes(totalDowntimeMinutes).build();
    }

    @Transactional(readOnly = true)
    public IncidentRequest loadFromPrtg(Long prtgAlertId) {
        PrtgAlert alert = prtgAlertRepository.findById(prtgAlertId)
            .orElseThrow(() -> new RuntimeException("PrtgAlert not found: " + prtgAlertId));
        IncidentRequest req = new IncidentRequest();
        req.setInMessage(alert.getInMessage());
        req.setSolution(alert.getSolution());
        req.setSourcePrtgId(prtgAlertId);
        req.setIntervals(alert.getIntervals().stream()
            .map(iv -> {
                com.example.monitoring.dto.IntervalRequest ir = new com.example.monitoring.dto.IntervalRequest();
                ir.setDateFrom(iv.getDateFrom());
                ir.setDateTo(iv.getDateTo());
                return ir;
            }).collect(Collectors.toList()));
        return req;
    }

    private Incident buildIncident(Incident e, IncidentRequest req) {
        e.setFailureType(req.getFailureTypeId() != null
            ? failureTypeRepository.findById(req.getFailureTypeId()).orElse(null) : null);
        e.setLocation(req.getLocationId() != null
            ? locationRepository.findById(req.getLocationId()).orElse(null) : null);
        e.setInformationSystems(req.getIsIds() != null
            ? new HashSet<>(isRepository.findAllById(req.getIsIds())) : new HashSet<>());
        e.setFixed(req.getFixed());
        e.setEmptyTime(req.getEmptyTime());
        e.setIncludeAvailability(req.getIncludeAvailability() != null ? req.getIncludeAvailability() : true);
        e.setInMessage(req.getInMessage());
        e.setOutMessage(req.getOutMessage());
        e.setAct(req.getAct());
        e.setProblem(req.getProblem());
        e.setSolution(req.getSolution());
        e.setSourcePrtgId(req.getSourcePrtgId());
        return e;
    }

    private void saveIntervals(Incident e, IncidentRequest req) {
        if (req.getIntervals() == null) return;
        req.getIntervals().forEach(ir -> {
            IncidentInterval iv = new IncidentInterval();
            iv.setIncident(e);
            iv.setDateFrom(ir.getDateFrom());
            iv.setDateTo(ir.getDateTo());
            iv.setDiffMinutes(ir.getDateFrom() != null && ir.getDateTo() != null
                ? (int) java.time.Duration.between(ir.getDateFrom(), ir.getDateTo()).toMinutes()
                : null);
            intervalRepository.save(iv);
        });
    }

    private Specification<Incident> buildSpec(Long failureTypeId, List<Long> isIds,
                                               Boolean fixed, Boolean emptyTime,
                                               OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (failureTypeId != null) {
                predicates.add(cb.equal(root.get("failureType").get("id"), failureTypeId));
            }
            if (isIds != null && !isIds.isEmpty()) {
                predicates.add(root.join("informationSystems").get("id").in(isIds));
                query.distinct(true);
            }
            if (fixed != null) {
                predicates.add(cb.equal(root.get("fixed"), fixed));
            }
            if (emptyTime != null) {
                predicates.add(cb.equal(root.get("emptyTime"), emptyTime));
            }
            if (dateFrom != null || dateTo != null) {
                var iv = root.join("intervals", JoinType.LEFT);
                if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(iv.get("dateFrom"), dateFrom));
                if (dateTo != null)   predicates.add(cb.lessThanOrEqualTo(iv.get("dateFrom"), dateTo));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
