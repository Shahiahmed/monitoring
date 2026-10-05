package com.example.monitoring.controller;

import com.example.monitoring.entity.MongoInoutStat;
import com.example.monitoring.repository.MongoInoutStatRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mongo-stat")
public class MongoStatController {

    private final MongoInoutStatRepository repo;
    private final JdbcTemplate oracleJdbc;
    private final JdbcTemplate localJdbc;

    public MongoStatController(
            MongoInoutStatRepository repo,
            @Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbc,
            @Qualifier("localJdbcTemplate") JdbcTemplate localJdbc) {
        this.repo = repo;
        this.oracleJdbc = oracleJdbc;
        this.localJdbc = localJdbc;
    }

    @GetMapping
    public Map<String, Object> list() {
        List<MongoInoutStat> rows = repo.findAllByOrderByStatYearDescStatMonthDescCntDesc();
        LocalDateTime lastSync = repo.findLastSyncedAt().orElse(null);

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          r.getId());
            m.put("SUBSYSTEM",   r.getSubsystem());
            m.put("SENDER_ID",   r.getSenderId());
            m.put("STAT_YEAR",   r.getStatYear());
            m.put("STAT_MONTH",  r.getStatMonth());
            m.put("CNT",         r.getCnt());
            m.put("INSERT_DATE", r.getInsertDate() != null ? r.getInsertDate().toString() : null);
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows",     data);
        result.put("lastSync", lastSync != null ? lastSync.toString() : null);
        return result;
    }

    @GetMapping("/service/{subsystem}")
    public List<Map<String, Object>> byService(@PathVariable String subsystem) {
        List<MongoInoutStat> rows = repo.findBySubsystemOrderByStatYearAscStatMonthAsc(subsystem);

        // Загружаем маппинг sender_id -> client_name
        Map<String, String> senderMap = new HashMap<>();
        List<Map<String, Object>> senderRows = localJdbc.queryForList("SELECT sender_id, client_name FROM sender_client_map");
        for (Map<String, Object> row : senderRows) {
            senderMap.put((String) row.get("sender_id"), (String) row.get("client_name"));
        }

        // Группируем по (yearMonth, senderId)
        Map<String, Map<String, Long>> byMonthSender = new LinkedHashMap<>();
        for (MongoInoutStat r : rows) {
            String ym = String.format("%d-%02d", r.getStatYear(), r.getStatMonth());
            byMonthSender.computeIfAbsent(ym, k -> new LinkedHashMap<>())
                .merge(r.getSenderId() != null ? r.getSenderId() : "", r.getCnt(), Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> monthEntry : byMonthSender.entrySet()) {
            String ym = monthEntry.getKey();
            for (Map.Entry<String, Long> senderEntry : monthEntry.getValue().entrySet()) {
                String senderId = senderEntry.getKey();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("yearMonth", ym);
                m.put("monthlyCount", senderEntry.getValue());
                m.put("sender", senderId.isEmpty() ? null : senderId);
                m.put("clientName", senderMap.getOrDefault(senderId, null));
                result.add(m);
            }
        }
        return result;
    }

    @PostMapping("/sync")
    @Transactional
    public Map<String, Object> sync() {
        List<MongoInoutStat> fromOracle = oracleJdbc.query(
            "SELECT SUBSYSTEM, SENDER_ID, STAT_YEAR, STAT_MONTH, CNT, INSERT_DATE " +
            "FROM ESERV.MONGO_INOUT_STAT ORDER BY STAT_YEAR DESC, STAT_MONTH DESC, CNT DESC",
            (rs, rowNum) -> {
                MongoInoutStat s = new MongoInoutStat();
                s.setSubsystem(rs.getString("SUBSYSTEM"));
                s.setSenderId(rs.getString("SENDER_ID"));
                s.setStatYear(rs.getInt("STAT_YEAR"));
                s.setStatMonth(rs.getInt("STAT_MONTH"));
                s.setCnt(rs.getLong("CNT"));
                Timestamp ts = rs.getTimestamp("INSERT_DATE");
                s.setInsertDate(ts != null ? ts.toLocalDateTime() : null);
                s.setSyncedAt(LocalDateTime.now());
                return s;
            }
        );

        // Дедупликация: в Oracle есть дубли по (subsystem, sender_id, year, month) — суммируем CNT
        Map<String, MongoInoutStat> deduped = new LinkedHashMap<>();
        for (MongoInoutStat s : fromOracle) {
            String key = s.getSubsystem() + "|" + (s.getSenderId() != null ? s.getSenderId() : "") + "|" + s.getStatYear() + "|" + s.getStatMonth();
            if (deduped.containsKey(key)) {
                deduped.get(key).setCnt(deduped.get(key).getCnt() + s.getCnt());
            } else {
                deduped.put(key, s);
            }
        }

        // UPSERT — обновляем cnt если запись есть, иначе вставляем. Локальные данные не удаляем.
        List<Object[]> batchArgs = deduped.values().stream().map(s -> new Object[]{
            s.getSubsystem(),
            s.getSenderId(),
            s.getStatYear(),
            s.getStatMonth(),
            s.getCnt(),
            s.getInsertDate() != null ? s.getInsertDate() : LocalDateTime.now(),
            LocalDateTime.now()
        }).toList();

        localJdbc.batchUpdate(
            "INSERT INTO mongo_inout_stat (subsystem, sender_id, stat_year, stat_month, cnt, insert_date, synced_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (subsystem, COALESCE(sender_id, ''), stat_year, stat_month) " +
            "DO UPDATE SET cnt = EXCLUDED.cnt, synced_at = EXCLUDED.synced_at",
            batchArgs
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("synced",   deduped.size());
        result.put("syncedAt", LocalDateTime.now().toString());
        return result;
    }
}
