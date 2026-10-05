package com.example.monitoring.controller;

import com.example.monitoring.entity.EQueryCount;
import com.example.monitoring.repository.EQueryCountRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/equery-stat")
public class EQueryCountController {

    private final EQueryCountRepository repo;
    private final JdbcTemplate localJdbc;
    private final JdbcTemplate oracleJdbc;

    public EQueryCountController(
            EQueryCountRepository repo,
            @Qualifier("localJdbcTemplate") JdbcTemplate localJdbc,
            @Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbc) {
        this.repo = repo;
        this.localJdbc = localJdbc;
        this.oracleJdbc = oracleJdbc;
    }

    @GetMapping("/service/{code}")
    public List<Map<String, Object>> byService(@PathVariable String code) {
        return repo.findByServiceKey(code).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("yearMonth",    r[0]);
            m.put("sender",       r[1]);
            m.put("monthlyCount", r[2]);
            m.put("clientName",   r[3]);
            return m;
        }).toList();
    }

    @GetMapping
    public Map<String, Object> list() {
        List<EQueryCount> rows = repo.findAllByOrderByYearMonthDescMonthlyCountDesc();
        LocalDateTime lastSync = repo.findLastSyncedAt().orElse(null);

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",              r.getId());
            m.put("YEAR_MONTH",      r.getYearMonth());
            m.put("CODE",            r.getCode());
            m.put("SHEP_SERVICE_ID", r.getShepServiceId());
            m.put("MONTHLY_COUNT",   r.getMonthlyCount());
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows",     data);
        result.put("lastSync", lastSync != null ? lastSync.toString() : null);
        return result;
    }

    @PostMapping("/sync")
    @Transactional
    public Map<String, Object> sync() {
        List<EQueryCount> fromOracle = oracleJdbc.query(
            "SELECT ID, YEAR_MONTH, CODE, SHEP_SERVICE_ID, MONTHLY_COUNT " +
            "FROM ESERV.E_QUERY_COUNTS ORDER BY YEAR_MONTH DESC, MONTHLY_COUNT DESC",
            (rs, rowNum) -> {
                EQueryCount e = new EQueryCount();
                e.setOracleId(rs.getLong("ID"));
                e.setYearMonth(rs.getString("YEAR_MONTH"));
                e.setCode(rs.getString("CODE"));
                e.setShepServiceId(rs.getString("SHEP_SERVICE_ID"));
                e.setMonthlyCount(rs.getLong("MONTHLY_COUNT"));
                e.setSyncedAt(LocalDateTime.now());
                return e;
            }
        );

        // Дедупликация: суммируем monthly_count по одинаковым ключам
        Map<String, EQueryCount> deduped = new LinkedHashMap<>();
        for (EQueryCount e : fromOracle) {
            String key = e.getYearMonth() + "|" + (e.getShepServiceId() != null ? e.getShepServiceId() : "") + "|" + (e.getCode() != null ? e.getCode() : "");
            if (deduped.containsKey(key)) {
                deduped.get(key).setMonthlyCount(deduped.get(key).getMonthlyCount() + e.getMonthlyCount());
            } else {
                deduped.put(key, e);
            }
        }

        // UPSERT — не удаляем Excel-импорт, только обновляем/добавляем Oracle данные
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> batchArgs = deduped.values().stream().map(e -> new Object[]{
            e.getYearMonth(),
            e.getShepServiceId() != null ? e.getShepServiceId() : "",
            e.getCode() != null ? e.getCode() : "",
            e.getMonthlyCount(),
            now,
            e.getOracleId()
        }).toList();

        localJdbc.batchUpdate(
            "INSERT INTO e_query_counts (year_month, shep_service_id, code, monthly_count, synced_at, oracle_id) " +
            "VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (year_month, shep_service_id, COALESCE(code, '')) " +
            "DO UPDATE SET monthly_count = EXCLUDED.monthly_count, " +
            "              synced_at = EXCLUDED.synced_at, " +
            "              oracle_id = EXCLUDED.oracle_id",
            batchArgs
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("synced",   deduped.size());
        result.put("syncedAt", now.toString());
        return result;
    }
}
