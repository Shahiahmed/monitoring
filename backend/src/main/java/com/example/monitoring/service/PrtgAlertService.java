package com.example.monitoring.service;

import com.example.monitoring.dto.PrtgAlertRequest;
import com.example.monitoring.dto.PrtgAlertResponse;
import com.example.monitoring.dto.SimpleStatsResponse;
import com.example.monitoring.entity.PrtgAlert;
import com.example.monitoring.entity.PrtgAlertInterval;
import com.example.monitoring.repository.PrtgAlertFileRepository;
import com.example.monitoring.repository.PrtgAlertIntervalRepository;
import com.example.monitoring.repository.PrtgAlertRepository;
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
public class PrtgAlertService {

    private final PrtgAlertRepository prtgAlertRepository;
    private final PrtgAlertIntervalRepository intervalRepository;
    private final PrtgAlertFileRepository fileRepository;

    @Transactional(readOnly = true)
    public List<PrtgAlertResponse> list(OffsetDateTime dateFrom, OffsetDateTime dateTo, String prtgStatus) {
        Specification<PrtgAlert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateFrom != null || dateTo != null) {
                var iv = root.join("intervals", JoinType.LEFT);
                if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(iv.get("dateFrom"), dateFrom));
                if (dateTo != null)   predicates.add(cb.lessThanOrEqualTo(iv.get("dateFrom"), dateTo));
                query.distinct(true);
            }
            if (prtgStatus != null && !prtgStatus.isEmpty())
                predicates.add(cb.equal(root.get("prtgStatus"), prtgStatus));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return prtgAlertRepository.findAll(spec).stream()
            .sorted((a, b) -> {
                var da = a.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(a.getCreatedAt());
                var db = b.getIntervals().stream().map(iv -> iv.getDateFrom()).filter(d -> d != null).min(java.util.Comparator.naturalOrder()).orElse(b.getCreatedAt());
                return db.compareTo(da);
            })
            .map(e -> PrtgAlertResponse.from(e, (int) fileRepository.countByPrtgAlertId(e.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> years() {
        return prtgAlertRepository.findDistinctYears().stream()
            .map(o -> ((Number) o).intValue())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrtgAlertResponse getById(Long id) {
        PrtgAlert e = prtgAlertRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PrtgAlert not found: " + id));
        return PrtgAlertResponse.from(e, (int) fileRepository.countByPrtgAlertId(e.getId()));
    }

    @Transactional
    public PrtgAlertResponse create(PrtgAlertRequest req) {
        PrtgAlert e = PrtgAlert.builder()
            .prtgStatus(req.getPrtgStatus())
            .inMessage(req.getInMessage())
            .solution(req.getSolution())
            .build();
        prtgAlertRepository.save(e);
        saveIntervals(e, req);
        PrtgAlert saved = prtgAlertRepository.findById(e.getId()).orElseThrow();
        return PrtgAlertResponse.from(saved, 0);
    }

    @Transactional
    public PrtgAlertResponse update(Long id, PrtgAlertRequest req) {
        PrtgAlert e = prtgAlertRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PrtgAlert not found: " + id));
        e.setPrtgStatus(req.getPrtgStatus());
        e.setInMessage(req.getInMessage());
        e.setSolution(req.getSolution());
        intervalRepository.deleteAll(intervalRepository.findByPrtgAlertIdOrderById(id));
        e.getIntervals().clear();
        prtgAlertRepository.save(e);
        saveIntervals(e, req);
        PrtgAlert saved = prtgAlertRepository.findById(id).orElseThrow();
        return PrtgAlertResponse.from(saved, (int) fileRepository.countByPrtgAlertId(id));
    }

    @Transactional
    public void delete(Long id) {
        fileRepository.deleteAll(fileRepository.findByPrtgAlertIdOrderByUploadedAtDesc(id));
        prtgAlertRepository.deleteById(id);
    }

    @Transactional
    public void deleteInterval(Long intervalId) {
        intervalRepository.deleteById(intervalId);
    }

    @Transactional(readOnly = true)
    public SimpleStatsResponse stats(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        Specification<PrtgAlert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateFrom != null || dateTo != null) {
                var iv = root.join("intervals", JoinType.LEFT);
                if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(iv.get("dateFrom"), dateFrom));
                if (dateTo != null)   predicates.add(cb.lessThanOrEqualTo(iv.get("dateFrom"), dateTo));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<PrtgAlert> alerts = prtgAlertRepository.findAll(spec);

        Map<String, long[]> byTypeMap = new LinkedHashMap<>();
        for (PrtgAlert a : alerts) {
            String name = a.getPrtgStatus() != null ? a.getPrtgStatus() : "Не указан";
            long mins = a.getIntervals().stream()
                .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L).sum();
            byTypeMap.merge(name, new long[]{1, mins}, (x, y) -> new long[]{x[0]+y[0], x[1]+y[1]});
        }
        List<SimpleStatsResponse.TypeCount> byType = byTypeMap.entrySet().stream()
            .map(e -> SimpleStatsResponse.TypeCount.builder()
                .name(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        Map<String, long[]> byMonthMap = new LinkedHashMap<>();
        alerts.stream()
            .filter(a -> !a.getIntervals().isEmpty())
            .sorted(Comparator.comparing(a -> a.getIntervals().stream()
                .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                .min(Comparator.naturalOrder()).orElse(a.getCreatedAt())))
            .forEach(a -> {
                var dateRef = a.getIntervals().stream()
                    .map(iv -> iv.getDateFrom()).filter(d -> d != null)
                    .min(Comparator.naturalOrder()).orElse(a.getCreatedAt());
                String month = String.format("%04d-%02d", dateRef.getYear(), dateRef.getMonthValue());
                long mins = a.getIntervals().stream()
                    .mapToLong(iv -> iv.getDiffMinutes() != null ? iv.getDiffMinutes() : 0L).sum();
                byMonthMap.merge(month, new long[]{1, mins}, (x, y) -> new long[]{x[0]+y[0], x[1]+y[1]});
            });
        List<SimpleStatsResponse.MonthCount> byMonth = byMonthMap.entrySet().stream()
            .map(e -> SimpleStatsResponse.MonthCount.builder()
                .month(e.getKey()).count((int) e.getValue()[0]).totalMinutes(e.getValue()[1]).build())
            .collect(Collectors.toList());

        return SimpleStatsResponse.builder().byType(byType).byMonth(byMonth).build();
    }

    private void saveIntervals(PrtgAlert e, PrtgAlertRequest req) {
        if (req.getIntervals() == null) return;
        req.getIntervals().forEach(ir -> {
            PrtgAlertInterval iv = new PrtgAlertInterval();
            iv.setPrtgAlert(e);
            iv.setDateFrom(ir.getDateFrom());
            iv.setDateTo(ir.getDateTo());
            iv.setDiffMinutes(ir.getDateFrom() != null && ir.getDateTo() != null
                ? (int) java.time.Duration.between(ir.getDateFrom(), ir.getDateTo()).toMinutes()
                : null);
            intervalRepository.save(iv);
        });
    }
}
