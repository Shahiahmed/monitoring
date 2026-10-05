package com.example.monitoring.service;

import com.example.monitoring.dto.SimpleStatsResponse;
import com.example.monitoring.dto.WorkRequest;
import com.example.monitoring.dto.WorkResponse;
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
public class WorkService {

    private final WorkRepository workRepository;
    private final WorkIntervalRepository intervalRepository;
    private final WorkFileRepository fileRepository;
    private final DicJobRepository dicJobRepository;
    private final DicLocationRepository locationRepository;
    private final DicIsRepository isRepository;
    private final PrtgAlertRepository prtgAlertRepository;

    @Transactional(readOnly = true)
    public List<WorkResponse> list(Long dicJobId, List<Long> isIds,
                                   Boolean emptyTime,
                                   OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        Specification<Work> spec = buildSpec(dicJobId, isIds, emptyTime, dateFrom, dateTo);
        return workRepository.findAll(spec).stream()
            .sorted((a, b) -> {
                var da = a.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(a.getCreatedAt());
                var db = b.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(b.getCreatedAt());
                return db.compareTo(da);
            })
            .map(e -> WorkResponse.from(e, (int) fileRepository.countByWorkId(e.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> years() {
        return workRepository.findDistinctYears().stream()
            .map(o -> ((Number) o).intValue())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkResponse getById(Long id) {
        Work e = workRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Work not found: " + id));
        return WorkResponse.from(e, (int) fileRepository.countByWorkId(id));
    }

    @Transactional
    public WorkResponse create(WorkRequest req) {
        Work e = buildWork(new Work(), req);
        workRepository.save(e);
        saveIntervals(e, req);
        Work saved = workRepository.findById(e.getId()).orElseThrow();
        return WorkResponse.from(saved, 0);
    }

    @Transactional
    public WorkResponse update(Long id, WorkRequest req) {
        Work e = workRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Work not found: " + id));
        buildWork(e, req);
        intervalRepository.deleteAll(intervalRepository.findByWorkIdOrderById(id));
        e.getIntervals().clear();
        workRepository.save(e);
        saveIntervals(e, req);
        Work saved = workRepository.findById(id).orElseThrow();
        return WorkResponse.from(saved, (int) fileRepository.countByWorkId(id));
    }

    @Transactional
    public void delete(Long id) {
        fileRepository.deleteAll(fileRepository.findByWorkIdOrderByUploadedAtDesc(id));
        workRepository.deleteById(id);
    }

    @Transactional
    public void deleteInterval(Long intervalId) {
        intervalRepository.deleteById(intervalId);
    }

    @Transactional(readOnly = true)
    public SimpleStatsResponse stats(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        List<Work> works = workRepository.findAll(buildSpec(null, null, null, dateFrom, dateTo));

        Map<String, long[]> byTypeMap = new LinkedHashMap<>();
        for (Work w : works) {
            String name = w.getDicJob() != null ? w.getDicJob().getNameRu() : "Не указан";
            long mins = w.getIntervals().stream()
                .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L).sum();
            byTypeMap.merge(name, new long[]{1, mins}, (a, b) -> new long[]{a[0]+b[0], a[1]+b[1]});
        }
        List<SimpleStatsResponse.TypeCount> byType = byTypeMap.entrySet().stream()
            .map(e -> SimpleStatsResponse.TypeCount.builder()
                .name(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        Map<String, long[]> byMonthMap = new LinkedHashMap<>();
        works.stream()
            .filter(w -> !w.getIntervals().isEmpty())
            .sorted(Comparator.comparing(w -> w.getIntervals().stream()
                .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                .min(Comparator.naturalOrder()).orElse(w.getCreatedAt())))
            .forEach(w -> {
                var dateRef = w.getIntervals().stream()
                    .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                    .min(Comparator.naturalOrder()).orElse(w.getCreatedAt());
                String month = String.format("%04d-%02d", dateRef.getYear(), dateRef.getMonthValue());
                long mins = w.getIntervals().stream()
                    .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L).sum();
                byMonthMap.merge(month, new long[]{1, mins}, (a, b) -> new long[]{a[0]+b[0], a[1]+b[1]});
            });
        List<SimpleStatsResponse.MonthCount> byMonth = byMonthMap.entrySet().stream()
            .map(e -> SimpleStatsResponse.MonthCount.builder()
                .month(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        return SimpleStatsResponse.builder().byType(byType).byMonth(byMonth).build();
    }

    @Transactional(readOnly = true)
    public WorkRequest loadFromPrtg(Long prtgAlertId) {
        PrtgAlert alert = prtgAlertRepository.findById(prtgAlertId)
            .orElseThrow(() -> new RuntimeException("PrtgAlert not found: " + prtgAlertId));
        WorkRequest req = new WorkRequest();
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

    private Work buildWork(Work e, WorkRequest req) {
        e.setDicJob(req.getDicJobId() != null
            ? dicJobRepository.findById(req.getDicJobId()).orElse(null) : null);
        e.setLocation(req.getLocationId() != null
            ? locationRepository.findById(req.getLocationId()).orElse(null) : null);
        e.setInformationSystems(req.getIsIds() != null
            ? new HashSet<>(isRepository.findAllById(req.getIsIds())) : new HashSet<>());
        e.setEmptyTime(req.getEmptyTime());
        e.setIncludeAvailability(req.getIncludeAvailability() != null ? req.getIncludeAvailability() : true);
        e.setInMessage(req.getInMessage());
        e.setOutMessage(req.getOutMessage());
        e.setSolution(req.getSolution());
        e.setSourcePrtgId(req.getSourcePrtgId());
        return e;
    }

    private void saveIntervals(Work e, WorkRequest req) {
        if (req.getIntervals() == null) return;
        req.getIntervals().forEach(ir -> {
            WorkInterval iv = new WorkInterval();
            iv.setWork(e);
            iv.setDateFrom(ir.getDateFrom());
            iv.setDateTo(ir.getDateTo());
            iv.setDiffMinutes(ir.getDateFrom() != null && ir.getDateTo() != null
                ? (int) java.time.Duration.between(ir.getDateFrom(), ir.getDateTo()).toMinutes()
                : null);
            intervalRepository.save(iv);
        });
    }

    private Specification<Work> buildSpec(Long dicJobId, List<Long> isIds,
                                          Boolean emptyTime,
                                          OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dicJobId != null) {
                predicates.add(cb.equal(root.get("dicJob").get("id"), dicJobId));
            }
            if (isIds != null && !isIds.isEmpty()) {
                predicates.add(root.join("informationSystems").get("id").in(isIds));
                query.distinct(true);
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
